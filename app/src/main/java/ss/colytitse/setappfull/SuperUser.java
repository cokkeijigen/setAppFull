package ss.colytitse.setappfull;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SuperUser {

    private static final String TAG = "test_";

    public static String execShell(String cmd) {
        StringBuilder result = new StringBuilder();
        Process process = null;
        String line;
        try {
            process = Runtime.getRuntime().exec("su -c " + cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null)
                result.append(line).append("\n");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void copyConfigFile(){
        String path = "/data/system/shared_prefs/";
        execShell("mkdir " + path);
        String config = path +"config.xml";
        execShell(((StringBuilder)(new StringBuilder()))
                .append("\\cp ").append(Environment.getDataDirectory())
                .append("/data/").append(BuildConfig.APPLICATION_ID)
                .append("/shared_prefs/config.xml ")
                .append(config)
        .toString());
        execShell("chmod 644 " + config);
    }
}
