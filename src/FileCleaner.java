import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileCleaner {

    public static void main(String[] args) {

        try {

            File dir = new File(System.getProperty("user.dir"));
            File[] servers = dir.listFiles();
            for (File server : servers) {
                System.out.println(server.getName());
                if (server.isDirectory() && server.getName().startsWith("file")) {
                    File[] files = server.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        PrintWriter writer = new PrintWriter(files[i].getPath());
                        writer.print("");
                        writer.close();
                    }
                }
            }

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

    }

}
