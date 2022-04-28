package ss.colytitse.setappfull;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XSharedPreferences;

@SuppressLint("ApplySharedPref")
public class AppSettings {

    private static final String TAG = "test_";
    private static final String config_name = "config";
    private final SharedPreferences getPrefs;
    public final static int SYSTEM_VIEW = 0;
    public final static int USER_VIEW = 1;
    private String content;

    public AppSettings(Context context) {
        this.getPrefs = context.getSharedPreferences(config_name, Context.MODE_PRIVATE);
        this.content = getPrefs.getString("content", "");
    }

    public void savData(String pkgn){
        StringBuilder data = new StringBuilder(content);
        if(!content.contains("#" + pkgn + "#"))
            data.append("#").append(pkgn).append("#");
        getPrefs.edit().putString("content", data.toString()).apply();
        SuperUser.copyConfigFile(data.toString());
    }

    public void delData(String pkgn){
        getPrefs.edit().putString("content", (this.content = content.replace("#" + pkgn + "#", ""))).apply();
        SuperUser.copyConfigFile(content);
    }

    public boolean getData(String pkgn){
        return content.contains("#" + pkgn + "#");
    }

    public static int getOnSwitchListView(Context context) {
        return new AppSettings(context).getPrefs.getInt("onSwitchListView", USER_VIEW);
    }

    public void savonSwitch(int value) {
        getPrefs.edit().putInt("onSwitchListView", value).apply();
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
