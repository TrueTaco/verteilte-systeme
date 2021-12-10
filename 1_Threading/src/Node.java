import java.io.Serializable;
import java.net.Socket;

public class Node implements Serializable {
    private int portClient;
    private boolean isMaster;
    private Socket socketReference;

    public Node(int portClient, boolean isMaster, Socket socketReference) {
        this.portClient = portClient;
        this.isMaster = isMaster;
        this.socketReference = socketReference;
    }
}
