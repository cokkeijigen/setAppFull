package ss.colytitse.setappfull;

import static android.view.WindowManager.LayoutParams.*;
import static de.robv.android.xposed.XposedHelpers.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.RequiresApi;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "test_";
    private static final String SystemMode;
    private static final String TimelyMode;
    private static final boolean ScopeMode;
    static {
        XSharedPreferences xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID,"config");
        xsp.makeWorldReadable();
        SystemMode = xsp.getString("SystemMode", "");
        TimelyMode = xsp.getString("TimelyMode", "");
        ScopeMode = xsp.getBoolean("scope_mode_switch", true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID))
            return;
        if (!lpparam.packageName.equals(lpparam.processName))
            return;
        if ((ScopeMode || TimelyMode.contains(lpparam.packageName)) && !lpparam.packageName.equals("android")){
            onTimelyMode();
            return;
        }
        if (lpparam.packageName.equals("android"))
            onSystemMode(lpparam);
    }

    private void onSystemMode(XC_LoadPackage.LoadPackageParam lpparam) {
        XC_MethodHook MethodHook = new XC_MethodHook() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) getObjectField(param.args[0], "mAttrs");
                if (attrs.type > WindowManager.LayoutParams.LAST_APPLICATION_WINDOW)
                    return;
                if (SystemMode.contains(attrs.packageName))
                    attrs.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        };
        try{
            findAndHookMethod("com.android.server.wm.DisplayPolicy", lpparam.classLoader,
                    "layoutWindowLw","com.android.server.wm.WindowState",
                    "com.android.server.wm.WindowState", "com.android.server.wm.DisplayFrames",
                    MethodHook
            );
        }catch (Throwable ignored){}
    }

    private void onTimelyMode() {
        findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override @SuppressLint("NewApi")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Activity activity = (Activity) param.thisObject;
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                activity.getWindow().setAttributes(lp);
            }
        });
    }

}
