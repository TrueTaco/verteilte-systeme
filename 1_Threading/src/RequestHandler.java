import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;

public class RequestHandler extends Thread {
    final private Socket socket;
    final private ObjectInputStream objectInputStream;
    final private ObjectOutputStream objectOutputStream;
    private boolean connectionOpen;
    private String messageSender;

    public RequestHandler(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
        this.socket = socket;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        connectionOpen = true;
        messageSender = "Master, " + socket.getLocalPort();
    }

    @Override
    public void run() {

        Message incomingMessage = null;

        while (connectionOpen) {
            System.out.println(messageSender + ": Waiting for client request");

            //convert ObjectInputStream object to Message
            try {
                incomingMessage = (Message) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            String incomingMessageType = (String) incomingMessage.getType();

//            switch (incomingMessageType) {
//                case "connect":
//                    // ToDo: Wie kriegen alle anderen Master Threads von der Node Liste mit?
//                default:
//            }

            String incomingMessagePayload = (String) incomingMessage.getPayload();
            if (incomingMessagePayload == null) incomingMessagePayload = "";

            System.out.println(messageSender + " - Message received: " + incomingMessagePayload);

            // Send a message confirmation before message will be worked
            try {
                sendMessageConfirmation(incomingMessagePayload);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(incomingMessagePayload.contains("!/lastmessage/!")) {
                try {
                    sendLastMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //terminate the server if client sends exit request
//            if(incomingMessagePayload.contains(("!/exit/!"))) connectionOpen = false;

            // Save message from client in message_store.txt
            messageStore(incomingMessagePayload + " | " + incomingMessage.getTime());
        }

        //close resources
        try {
            objectInputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMessageConfirmation (String text) throws IOException {
        Message outgoingMessage = new Message();

        // Outgoing message text
        String messageText = "Message received: " + text;
        // Fill outgoingMessage with content
        outgoingMessage.setReceiver("Client");
        outgoingMessage.setSender(messageSender);
        outgoingMessage.setTime(Instant.now());
        outgoingMessage.setPayload(messageText);

        objectOutputStream.writeObject(outgoingMessage);
    }

    public void messageStore(String message) {
        FileEditor fileEditor = new FileEditor();
        File messageFile = fileEditor.createFile("message_store.txt");
        fileEditor.writeFile(messageFile, message);
    }

    public void sendLastMessage() throws IOException {
        FileEditor fileEditor = new FileEditor();
        Message outgoingMessage = new Message();

        // Outgoing message text
        String messageText = fileEditor.readLastLine("message_store.txt");
        // Fill outgoingMessage with content
        outgoingMessage.setReceiver("Client");
        outgoingMessage.setSender(messageSender);
        outgoingMessage.setTime(Instant.now());
        outgoingMessage.setPayload(messageText);

        objectOutputStream.writeObject(outgoingMessage);
        objectOutputStream.flush();
    }
}