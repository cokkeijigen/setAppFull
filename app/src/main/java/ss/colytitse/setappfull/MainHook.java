package ss.colytitse.setappfull;

import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "test_";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(lpparam.processName) && !lpparam.packageName.equals("android"))
            return;
        Log.d(TAG, "SetAppFull: 运行成功！");
        XSharedPreferences xSharedPreferences = null;
        String rule = "";
        try {
            xSharedPreferences = new XSharedPreferences(
                    new File("/data/system/shared_prefs/config.xml")
            );
            rule = xSharedPreferences.getString("content", "");
            Log.d(TAG, "内容？: "+ xSharedPreferences.getAll().toString());

        }catch (Throwable t) {
            Log.d(TAG, "错误: " + t);
        }

        try {
            String finalRule = rule;
            XC_MethodHook hook = new XC_MethodHook() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws FileNotFoundException {
                    WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) getObjectField(param.args[0], "mAttrs");
                    if (attrs.type > WindowManager.LayoutParams.LAST_APPLICATION_WINDOW)
                        return;
                    if (finalRule.contains(attrs.packageName))
                        attrs.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

                }
            };
            findAndHookMethod(
                    "com.android.server.wm.DisplayPolicy",
                    lpparam.classLoader,
                    "layoutWindowLw",
                    "com.android.server.wm.WindowState",
                    "com.android.server.wm.WindowState",
                    "com.android.server.wm.DisplayFrames",
                    hook
            );
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

}
