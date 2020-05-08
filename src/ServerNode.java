import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerNode {

    private int Id;
    private final int port = 5000;
    private ServerSocket myServer ;
    private List<String> fileList;
    private Map<Integer,Socket> incomingNeibs;
    private Map<Integer,Socket> outcomingNeibs;
    private Map<Integer,ObjectInputStream> incomingChannels;
    private Map<Integer,ObjectOutputStream> outcomingChannels;
    private Map<String,LamportClock> clocks;
    private Map<String, LamportMutex> mutexes;
    private String[] serverAdd;
    private int[] serverPort;
    private Set<Integer> neighbors;
    private static int numNeighbors;
    private String FILEPREFIX="";

    public ServerNode(int id){
        this.Id = id;
        FILEPREFIX="/Users/chunpingyang/Documents/jx180016/file"+Id;
    }

    public synchronized Map<Integer, Socket> getIncomingNeibs() {
        return incomingNeibs;
    }

    public synchronized Set<Integer> getNeighbors() {
        return neighbors;
    }

    public synchronized Map<String, LamportClock> getClocks() {
        return clocks;
    }

    public synchronized int getId() {
        return Id;
    }

    public void readServerConfig(String[] adds, int[] ports){
        this.serverAdd = adds;
        this.serverPort = ports;
    }

    public void initServerToClient( int[] ids,int index) throws IOException {
        numNeighbors = ids.length-1;
        incomingNeibs = Collections.synchronizedMap(new HashMap<>());
        outcomingNeibs =  Collections.synchronizedMap(new HashMap<>());
        incomingChannels = Collections.synchronizedMap(new HashMap<>());
        outcomingChannels = Collections.synchronizedMap(new HashMap<>());
        neighbors = Collections.synchronizedSet(new HashSet<>());
        clocks = Collections.synchronizedMap(new HashMap<>());
        mutexes =  Collections.synchronizedMap(new HashMap<>());
        for(String file:fileList){
            clocks.put(file,new LamportClock());
            mutexes.put(file,new LamportMutex(this));
        }
        //
        numNeighbors = this.serverAdd.length-1;

        try {
            myServer = new ServerSocket(this.serverPort[Id]);
        } catch (IOException e) {
            e.printStackTrace();
        }


        for(int i=0;i<ids.length;i++){
            if(ids[i]==Id) continue;
            else{
                final int other = i;
                // this is not called immediately, the new Thread's operation will not stuck the main function
                // even if started the server after this loop, the function still works
                // knowing how to jump to it- Debug skill
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isConnected = false;
                        while(!isConnected){
                            try {
                                // connect to other servers and store it as outcoming neibors
                                Socket socket = new Socket(serverAdd[other],serverPort[other]);
                                outcomingNeibs.put(ids[other],socket);
                                outcomingChannels.put(ids[other],new ObjectOutputStream(socket.getOutputStream()));
                                isConnected = true;
                                System.out.println("connect to "+other);
                            } catch (IOException e) {
                                try{
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                System.out.println("waiting for other servers to start");
                                isConnected = false;
                            }
                        }
                    }
                }).start();
            }
        }

        for(int i=0;i<ids.length;i++){
            try {
                if (ids[i] != Id) {
                    Socket socket = myServer.accept();
                    incomingNeibs.put(ids[i], socket);
                    incomingChannels.put(ids[i],new ObjectInputStream(socket.getInputStream()));
                    neighbors.add(ids[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("get Incoming neibors ready!");
        System.out.println("server connections ready!");

        //start serverListener, lister to neibors's message(Lamport Message)
        for(int i=0;i<ids.length;i++){
            if(ids[i]!=Id)
            new Thread(new ServerListenner(this,incomingChannels.get(ids[i]))).start();
        }

        //start clientListener, listen to each clients' request message
        new Thread((new ClientListener(this,myServer))).start();
    }

    // when ClientListener receives a message from a client, first try to check if the critical section of giving file is available
    // if it is empty, do request and broadcast
    public  void request(CSMessage received) throws InterruptedException, IOException {
        int fileNumber = received.getFileNumber();
        String fileName = fileList.get(fileNumber);
        LamportClock clock = clocks.get(fileName);
        clock.increment();
        LamportMutex mutex = mutexes.get(fileName);
        while(!mutex.isAvailable()){
            Thread.sleep(10);
        }
        Message request = new Message(clock.getClock(),Id,Id,LamportMessage.REQUEST,fileName,received.getContent());
        mutex.makeRequest(request);
    }

    // broadcast for release and request
    // for request, it may wait for a time to be broadcast(last operation not finished), so the clock time should be the time of message
    // but not the current clock
    public synchronized void broadcast(Message message) throws IOException {
        String fileName = message.getFileName();
        for(int neib:neighbors){
            Message toSend = new Message(message.getClock(),this.getId(),neib,message.getType(),fileName,message.getContent());

            outcomingChannels.get(neib).writeObject(toSend);
            outcomingChannels.get(neib).flush();
        }
    }

    // wrapper method for different types of message
    // server process messages according to its type
    public synchronized void processMessage(Message received) throws IOException {
        String fileName = received.getFileName();
        LamportClock clock = this.clocks.get(fileName);
        clock.msgEvent(received);
        String type = received.getType();
        LamportMutex mutex = mutexes.get(fileName);

        switch (type) {
            case LamportMessage.REQUEST:
                Message reply = mutex.getRequest(received);
                sendReply(reply);
                break;
            case LamportMessage.RELEASE:
                mutex.getRelease(received);
                break;
            case LamportMessage.REPLY:
                mutex.getReply(received);
                break;
            default:
                System.err.println("not correct type!");
                break;
        }
        // after release or reply message, server might go to critical section
        if(mutex.canEnterCriticalSection()){
            executeCriticalSection(mutex);
        }

    }

    public synchronized void sendReply(Message reply) throws IOException {
        int outNeib = reply.getTo();
        try {
            outcomingChannels.get(outNeib).writeObject(reply);
            outcomingChannels.get(outNeib).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // critical section execution is not concurrent
    public void executeCriticalSection(LamportMutex mutex) throws IOException {
        Message toProcess = mutex.headMessage();
        String fileName = toProcess.getFileName();
        System.out.println("server execute critical section for file "+fileName);
        append_to_file(fileName,toProcess.getContent());
        LamportClock clock = clocks.get(fileName);
        clock.increment();
        mutex.release(clock.getClock());

    }

    // critical section execution is not concurrent
    public  void append_to_file(String fileName,String content) throws IOException {
        File file =new File(FILEPREFIX+"/"+fileName);
        if(!file.exists()) {
            System.out.println("file not exist!");
        }
        FileWriter fw = new FileWriter(file.getAbsolutePath(),true);
        System.out.println("Path: "+file.getPath());
        System.out.println("write content "+content+" on file "+fileName);
        fw.write(content+ "on f"+fileName+" on Server"+this.getId()+"\n");
        fw.close();
    }

    public List<String> getFileList(){
        List<String> fileList = new ArrayList<>();
        File folder = new File(FILEPREFIX);
        File[] files = folder.listFiles();
        for(File file: files){
                fileList.add(file.getName());
        }
        return fileList;
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        int index =in.nextInt();
        int[] ids = new int[]{0,1,2};
        ServerNode server = new ServerNode(ids[index]);
        String[] serverList = {"127.0.0.1","127.0.0.1","127.0.0.1"};
        int[] portsArr = new int[]{5000,5001,5002};
        server.readServerConfig(serverList,portsArr);
        server.fileList = server.getFileList();
        System.out.println(server.fileList);
        server.initServerToClient(ids,index);
    }
}
