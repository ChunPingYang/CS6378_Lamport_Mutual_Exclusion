import java.io.IOException;
import java.util.*;

/**
 * Algorithm class, each corresponds to a file
 * it has cohesion with servers, try to remove this further
 */
public class LamportMutex {

    private ServerNode server;
    private List<Message> messageQueue;
    // a set to store replies needed from neighbors to enter critical section
    private Set<Integer> pendingReplies;
    // once a request is sent, do not send request until the last request is done
    private boolean sendRequest =false;
    public LamportMutex(ServerNode server){
        this.server = server;
        messageQueue = Collections.synchronizedList(new ArrayList<Message>(){

            public synchronized boolean add(Message message) {
                boolean ret = super.add(message);
                Collections.sort(messageQueue);
                return ret;
            }
        });
    }

    public Message headMessage(){
        return messageQueue.get(0);
    }

    // add the request to its own queue when server is broadcasting to neibs, init pendingReplies
    public synchronized void makeRequest(Message request) throws IOException {
        if(!messageQueue.isEmpty() || sendRequest) {
            System.err.println("last request not finished");
            return;
        }
        messageQueue.add(request);
        pendingReplies = Collections.synchronizedSet(new HashSet<>(server.getNeighbors()));
        server.broadcast(request);
        sendRequest = true;
    }

    public synchronized void getReply(Message reply){
        if(!reply.getType().equals(LamportMessage.REPLY)){
            System.err.println("it's not a reply!");
            return ;
        }
        pendingReplies.remove(reply.getFrom());
    }

    public  synchronized void getRelease(Message release) throws IOException {
        if (release.getType().equals(LamportMessage.RELEASE)){
            // if receives a release message, should also write to file
            server.append_to_file(release.getFileName(),release.getContent());
            Message top = messageQueue.remove(0);
            if(top.getFrom()!=release.getFrom()) {
                System.err.println("message not equal!");
            }
        }
        else{
            System.err.println("it's not a release!");
        }
    }

    public synchronized Message getRequest(Message request){
        if(!request.getType().equals(LamportMessage.REQUEST)){
            System.err.println("it's not a request!");
            return null;
        }
        messageQueue.add(request);
        Message reply = new Message(server.getClocks().get(request.getFileName()).getClock(),
                request.getTo(),request.getFrom(),LamportMessage.REPLY,request.getFileName(),"");
        return reply;
    }

    public synchronized boolean isAvailable(){
        return !sendRequest;
    }

    public synchronized boolean canEnterCriticalSection(){
        if(sendRequest && pendingReplies.isEmpty() && (!messageQueue.isEmpty() &&messageQueue.get(0).getFrom()==server.getId())){
            return true;
        }
        return false;
    }

    public void release(int clock) throws IOException {
        Message toRelease = messageQueue.remove(0);
        // generate release message , need to use new clock time for release
        Message toSend = new Message(clock,server.getId(),0,LamportMessage.RELEASE,toRelease.getFileName(),toRelease.getContent());
        sendRequest = false;
        server.broadcast(toSend);
    }

}
