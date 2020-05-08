import java.io.Serializable;

public class CSMessage implements Serializable {
    private int fileNumber;
    private boolean isSuccess=false;
    private String content;
    private int from;
    CSMessage(int from,int fileNumber,String content){
        this.fileNumber = fileNumber;
        this.content = content;
        this.from = from;
    }

    CSMessage(int from ,int fileNumber,boolean success){
        this.from = from;
        this.fileNumber =fileNumber;
        this.isSuccess = success;
    }

    public int getFrom() {
        return this.from;
    }

    public int getFileNumber() {
        return fileNumber;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString(){
        return fileNumber+","+content+","+isSuccess;
    }
}
