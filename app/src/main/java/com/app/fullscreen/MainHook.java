package com.app.fullscreen;


import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.util.Log;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class MainHook implements IXposedHookLoadPackage {

    boolean information = true;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        if(information){
            Log.d("xposed","模块已加载！");
            XposedBridge.log("\n\n setAppFull 模块已加载！" +
                    "\n 该项目基于BCRfullscreen修改（原项目地址：https://github.com/KitsunePie/BCRfullscreen）" +
                    "\n 其实也没啥多大的改动(*/ω＼*)" +
                    "\n\n ");
            information = false;
        }

        if (!lpparam.packageName.equals(lpparam.processName))
            return;
        if (!lpparam.packageName.equals("android"))
            return;
        try {
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) getObjectField(param.args[0], "mAttrs");
                    if (attrs.type > WindowManager.LayoutParams.LAST_APPLICATION_WINDOW)
                        return;
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
