package ss.colytitse.setappfull;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SuperUser {

    private static final String TAG = "test_";

    public static String execShell(String cmd) {
        StringBuilder result = new StringBuilder();
        Process process;
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

    public static void copyConfigFile(String content){
        String sys_path = "/data/system/shared_prefs/";
        String data = String.format("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>" +
                "<map><string name=\\\"content\\\">%s</string></map>", content);
        String log = execShell("mkdir " + sys_path);
        log += execShell("echo \"" + data + "\" > " + sys_path + "config.xml");
        Log.d(TAG, "copyConfigFile: " + log);
    }
}
