import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener implements Runnable {
    private ServerNode server;
    private ServerSocket serverSocket;
    public ClientListener(ServerNode serverNode, ServerSocket myServer){
        this.server = serverNode;
        this.serverSocket =myServer;

    }


    @Override
    public void run() {

        CSMessage clientMessage = null;
        try {
            while(true){
                Socket socket = serverSocket.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                clientMessage = (CSMessage) input.readObject();
                int fileN = clientMessage.getFileNumber();
                System.out.println("server "+server.getId()+" receives "+"client #"+clientMessage.getFrom()+" for file#"+fileN);
                // do request on serverNode

                server.request(clientMessage);
                CSMessage reply = new CSMessage(server.getId(),fileN,true);
                output.writeObject(reply);
                output.flush();
                input.close();
                output.close();
                socket.close();
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
