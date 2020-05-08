import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


class Marker {
    private AtomicInteger num;
    private AtomicInteger counter;
    public boolean start;

    public Marker() {
        this.num = new AtomicInteger();
        this.counter = new AtomicInteger(); //
    }

    public synchronized void counter_add() {
        this.counter.incrementAndGet();
    }

    public synchronized int get_counter() {
        return this.counter.intValue();
    }

    public synchronized void add() {
        this.num.incrementAndGet();
    }

    public synchronized int get() {
        return num.intValue();
    }
}


public class ClientNode implements Runnable {
    private static String[] serverList;
    private static int[] serverPorts;
    private int pid;

    private Marker marker;
    public ClientNode(int pid){
        marker = new Marker();
        this.pid = pid;
        serverList = new String[]{"127.0.0.1", "127.0.0.1", "127.0.0.1"};
        serverPorts = new int[]{5000,5001,5002};
    }

    @Override
    public void run() {
        int n_time=100;
        try{
            while(n_time-- > 0){
                int rand = new Random().nextInt(3);
                Socket socket = new Socket(serverList[rand],serverPorts[rand]);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Thread.sleep(100);
                // need 3 servers to start to be running
                int fileNum =new Random().nextInt(4);
                CSMessage clientMessage =  new CSMessage(this.pid,fileNum,"client"+pid+" append "+marker.get_counter()+" time");
                output.writeObject(clientMessage);
                output.flush();
                System.out.println("client"+pid+" message #"+marker.get_counter()+" to server"+rand+" on file "+fileNum);
                marker.counter_add();
                CSMessage received = (CSMessage)input.readObject();
                System.out.println("client" +pid+" received" + (received.isSuccess()?"success":"fail")+"from server "+received.getFrom());
                output.close();
                input.close();
                socket.close();
            }
            System.out.println("------------------------------------------\n process "+this.pid+" gracefully terminated! ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner in =new Scanner(System.in);
        int id = in.nextInt();
        ClientNode client = new ClientNode(id);
        new Thread(client).start();
    }
}
