import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigReader {
    public static List<String> read(String file) {
        List<String> config = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = null;
            while((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                String serverID = parts[0];
                String ipAddress = parts[1];
                config.add(parts[1]);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return config;
    }

}