import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;

public class Slave {

    private int slavePort;
    private ServerSocket server;
    private int masterPort;
    private String masterHost;
    private Socket socket;
    Boolean clientConnectionOpen;

    ObjectOutputStream masterObjectOutputStream;
    ObjectInputStream masterObjectInputStream;

    public Slave(int slavePort, String masterHost, int masterPort) {
        this.slavePort = slavePort;
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        clientConnectionOpen = false;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Slave slave = new Slave(9999, "localhost", 9876);
        //Slave slave2 = new Slave(9998, "localhost", 9877);
        slave.start();
        //slave2.start();
        slave.connectToMaster();
        //slave2.connectToMaster();
        slave.delegateConnections();
        //slave2.delegateConnections();
    }

    public void start() throws IOException {
        // Create the socket server object
        this.server = new ServerSocket(slavePort);
        // Open the client connection
        clientConnectionOpen = true;
    }

    public void connectToMaster() throws IOException {
        // Connect to server
        this.socket = new Socket(masterHost, masterPort);
        //write to socket using ObjectOutputStream
        masterObjectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        //read the server response message
        masterObjectInputStream = new ObjectInputStream(socket.getInputStream());
//        this.messageSender = "Client, " + socket.getLocalPort();
    }

    public void delegateConnections() throws IOException {
        while (clientConnectionOpen) {
            Socket socket = null;

            try {
                socket = server.accept();

                // Confirm client connection
                System.out.println("New Client connected: " + socket);

                // Create new object streams for the created socket
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                // Create ClientHandler thread and start it
                Thread thread = new ClientHandler(socket, objectInputStream, objectOutputStream, masterObjectInputStream, masterObjectOutputStream);
                thread.start();

//                try {
//                    Message clientMessage = (Message) objectInputStream.readObject();
//                    masterObjectOutputStream.writeObject(clientMessage);
//                    masterObjectOutputStream.flush();
//                } catch (IOException | ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//                // ToDo: Die Streaminhalte können können nicht hin- und hergeschickt werden,
//                //  da pro Schleifendurchlauf immer ein neuer Thread erstellt werden kann
//                try {
//                    Message masterMessage = (Message) objectInputStream.readObject();
//                    masterObjectOutputStream.writeObject(masterMessage);
//                    masterObjectOutputStream.flush();
//                } catch (IOException | ClassNotFoundException e) {
//                    e.printStackTrace();
//                }

            } catch (Exception e){
                socket.close();
                e.printStackTrace();
            }
        }
    }
}