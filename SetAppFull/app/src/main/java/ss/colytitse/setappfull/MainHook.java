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
import androidx.core.view.WindowInsetsCompat;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("unused")
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "debug_test_";
    private String SystemModeList = "";
    private String AppModeList = "";
    private boolean IsScopeMode = true;

    private void update()
    {
        XSharedPreferences xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID,"config");
        xsp.makeWorldReadable();
        SystemModeList = xsp.getString("SystemMode", "");
        AppModeList = xsp.getString("TimelyMode", "");
        IsScopeMode = xsp.getBoolean("scope_mode_switch", true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID))
            return;
        if (!lpparam.packageName.equals(lpparam.processName))
            return;
        if(lpparam.packageName.equals("android")) {
            try{
                update();
                onSystemMode(lpparam);
            } catch (Throwable ignored){}
        } else if(IsScopeMode || AppModeList.contains(lpparam.packageName)) {
            try{
                onAppMode();
            } catch (Throwable ignored){}
        }
    }

    private void onSystemMode(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod("com.android.server.wm.DisplayPolicy", lpparam.classLoader,
                "layoutWindowLw", "com.android.server.wm.WindowState",
                "com.android.server.wm.WindowState", "com.android.server.wm.DisplayFrames",
                new XC_MethodHook() {
                    @Override
                    @RequiresApi(api = Build.VERSION_CODES.P)
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) getObjectField(param.args[0], "mAttrs");
                        if (attrs.type > WindowManager.LayoutParams.LAST_APPLICATION_WINDOW)
                            return;
                        if(attrs.packageName.equals(BuildConfig.APPLICATION_ID)) {
                            update();
                        }
                        else if (SystemModeList.contains(attrs.packageName)) {
                            attrs.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                        }
                    }
                }
        );

    }

    private void onAppMode() {

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

        findAndHookMethod(WindowInsetsCompat.class, "getDisplayCutout", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });
    }

}
