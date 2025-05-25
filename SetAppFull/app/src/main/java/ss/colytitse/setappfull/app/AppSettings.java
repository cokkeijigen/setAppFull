package ss.colytitse.setappfull.app;

import static android.content.Context.MODE_WORLD_READABLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import androidx.core.content.ContextCompat;

import ss.colytitse.setappfull.R;

@SuppressLint({"ApplySharedPref", "WorldReadableFiles"})
public class AppSettings {

    private static final String TAG = "test_";
    private static final String config_name = "config";
    private static final String configFileName = "app_config";
    public final static int SYSTEM_VIEW = 0;
    public final static int USER_VIEW = 1;
    public final static int MODE_1 = 1;
    public final static int MODE_2 = 2;
    public final static int NO_SET = -1;
    private String content;

    public static SharedPreferences configData(Context context){
        return context.getSharedPreferences(config_name, MODE_WORLD_READABLE);
    }

    public static void deleteSelection(Context context, String pkgn){
        SharedPreferences sharedPreferences = configData(context);
        String SystemMode = sharedPreferences.getString("SystemMode", "");
        putSystemMode(sharedPreferences, remove(SystemMode, pkgn));
    }

    private static String remove(String contentText, String packageName){
        String package_Name = String.format("#%s#", packageName);
        if (!contentText.contains(package_Name)) return contentText;
        return contentText.replace(package_Name, "");
    }

    private static String getSystemMode(SharedPreferences sharedPreferences){
        return sharedPreferences.getString("SystemMode", "");
    }

    private static String getAppMode(SharedPreferences sharedPreferences){
        return sharedPreferences.getString("AppMode", "");
    }

    private static void putAppMode(SharedPreferences sharedPreferences, String content){
        sharedPreferences.edit().putString("AppMode", content).apply();
    }

    private static void putSystemMode(SharedPreferences sharedPreferences, String content){
        sharedPreferences.edit().putString("SystemMode", content).apply();
    }

    public static void saveSystemMode(Context context, String packageName){
        SharedPreferences sharedPreferences = configData(context);
        String AppMode = getAppMode(sharedPreferences);
        String systemMode = getSystemMode(sharedPreferences);
        putAppMode(sharedPreferences, remove(AppMode, packageName));
        putSystemMode(sharedPreferences, String.format("%s#%s#", systemMode, packageName));
    }

    public static void saveAppMode(Context context, String packageName){
        SharedPreferences sharedPreferences = configData(context);
        String ContentText = sharedPreferences.getString("AppMode", "");
        String package_Name = String.format("#%s#", packageName);
        if (ContentText.contains(package_Name)) return;
        ContentText = String.format("%s%s", ContentText, package_Name);
        putAppMode(sharedPreferences, ContentText);
    }

    public static int getSetMode(Context context, String packageName){
        SharedPreferences sharedPreferences = configData(context);
        String SystemMode = sharedPreferences.getString("SystemMode", "");
        String AppMode = sharedPreferences.getString("AppMode", "");
        String package_Name = String.format("#%s#", packageName);
        if (AppMode.contains(package_Name)) return MODE_1;
        if (SystemMode.contains(package_Name)) return MODE_2;
        return NO_SET;
    }

    public static int getOnSwitchListView(Context context) {
        return configData(context).getInt("onSwitchListView", USER_VIEW);
    }

    public static void savonSwitch(Context context,int value) {
        configData(context).edit().putInt("onSwitchListView", value).apply();
    }

    public static void setScopeMode(Context context, boolean scopeMode){
        configData(context).edit().putBoolean("scope_mode_switch", scopeMode).apply();
    }

    public static boolean getScopeMode(Context context){
       return configData(context).getBoolean("scope_mode_switch", true);
    }

    public static boolean getHelloWorld(Context context){
        return configData(context).getBoolean("hello_world", true);
    }

    public static void setHelloWorld(Context context){
        configData(context).edit().putBoolean("hello_world", false).apply();
    }

    public static void setStatusBarColor(Activity activity, int color){
        activity.getWindow().setStatusBarColor(
                ContextCompat.getColor(activity.getApplicationContext(), color));
    }

    public static void setActivityStatusBar(Activity activity){
        setStatusBarColor(activity, R.color.toolbar);
        if(activity.getApplicationContext().getResources().getConfiguration().uiMode == 0x11)
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
