import java.io.Serializable;

public class Message implements Serializable,Comparable<Message> {

    enum MessageType{
        REQUEST, REPLY, RELEASE
    };
    private final int clock;
    private  final int from;
    private final int to;
    private final String type ;
    private String content = "";
    private final String fileName;

    public Message(int clock, int from , int to, String type, String fileName,String content){
        this.clock = clock;
        this.from = from;
        this.to=to;
        this.type = type;
        this.fileName = fileName;
        this.content = content;
    }

    public int getClock(){
        return this.clock;
    }
    public int getFrom() {
        return this.from;
    }
    public int getTo() {
        return this.to;
    }
    public String getType() {
        return this.type;
    }

    public String getContent() {
        return content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(Message o) {
        if(this.clock <o.getClock()){
            return -1;
        }
        else if (this.clock>o.getClock()){
            return 1;
        }
        else{
            return this.from - o.getFrom();
        }
    }
    @Override
    public String toString(){
        return clock + "," + from  + "," + to + "," + type + "," + fileName;
    }
}
