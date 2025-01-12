import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import java.time.Instant;

public class Master {
    private int port;
    Boolean connectionOpen;
    private ServerSocket server;

    private Map<Integer,Node> connectionMap;
    private List<RequestHandler> requestHandlerList;

    public Master (int port) {
        this.port = port;
        connectionOpen = false;
//        FIXME: ConcurrentHashMap scheint hierfür besser geeignet zu sein
        connectionMap = Collections.synchronizedMap(new HashMap<Integer,Node>());
        requestHandlerList = new ArrayList<RequestHandler>();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Master master = new Master(9876);
        master.start();
        master.delegateConnections();
    }

    public void start() throws IOException {
        // Create the socket server object
        this.server = new ServerSocket(port);
        // Open the connection
        connectionOpen = true;
    }

    public void delegateConnections() throws IOException {
        while (connectionOpen) {
            Socket socket = null;

            try {
                socket = server.accept();
                // Confirm client connection
                System.out.println("New Slave connected: " + socket);

                // Create new object streams for the created socket
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                // Create ClientHandler thread and start it
                Thread thread = new RequestHandler(this, socket, objectInputStream, objectOutputStream, connectionMap);
                thread.start();
                requestHandlerList.add((RequestHandler) thread);
            } catch (Exception e){
                socket.close();
                e.printStackTrace();
            }
        }
    }
}
