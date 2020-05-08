import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerListenner implements Runnable{
    private ServerNode serverNode;
    private ObjectInputStream ois;
    public ServerListenner(ServerNode server, ObjectInputStream ois){
        this.serverNode = server;
        this.ois = ois;

    }

    @Override
    public void run() {
        try {
            // can not new a ObjectInputStream here, one socket can only been initialized with one inputStream
//            ObjectInputStream ois = new ObjectInputStream(neibor.getInputStream());

            System.out.println("serverListener "+serverNode.getId()+" starts listening");
            while(true){
                Message received = (Message)ois.readObject();
                System.out.println("Server receives message from server"+ received.getFrom());
                serverNode.processMessage(received);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
