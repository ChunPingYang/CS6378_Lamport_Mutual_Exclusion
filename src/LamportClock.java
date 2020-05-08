import java.sql.SQLOutput;

public class LamportClock {
    private int clock;
    private int d=1;

    public LamportClock (int d){
        this.d = d;
        clock = 0;
    }

    public LamportClock(){
        clock =0;
    }
    public int getD() {
        return d;
    }

    public int getClock(){
        return clock;
    }

    public void increment(){
        clock+=d;
    }

    public void msgEvent(Message message){
        increment();
        if(message.getClock()+d>clock){
            clock = message.getClock()+d;
        }
    }

    public static void main(String[] args) {
        LamportClock clock = new LamportClock();
        Message msg = new Message(5,2,4,"REQUEST","f1","");
        clock.msgEvent(msg);
        System.out.println(clock.clock);
    }
}
