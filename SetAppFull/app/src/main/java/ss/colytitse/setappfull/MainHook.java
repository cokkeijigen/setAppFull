package ss.colytitse.setappfull;

import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager.LayoutParams;
import de.robv.android.xposed.XposedHelpers;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.RequiresApi;
import androidx.core.view.WindowCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.P)
@SuppressLint({"DiscouragedApi", "InternalInsetResource"})
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "MainHook";
    private List<String> SystemModeList = List.of();
    private List<String> AppModeList = List.of();
    private int statusBarHeightId = 0;
    private boolean IsScopeMode = true;
    private long lastUpdateTime = 0;
    private ScheduledExecutorService mScheduledExecutorService;
    List<LayoutParams> mLayoutParamsList;

    private void update() {
        XSharedPreferences xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID,"config");
        xsp.makeWorldReadable();
        String SystemModeString = xsp.getString("SystemMode", "");
        this.SystemModeList = Arrays.stream(SystemModeString.split("#"))
                .filter(e-> !e.isEmpty())
                .collect(Collectors.toList());
        String AppModeString = xsp.getString("TimelyMode", "");
        this.AppModeList = Arrays.stream(AppModeString.split("#"))
                .filter(e-> !e.isEmpty())
                .collect(Collectors.toList());
        this.IsScopeMode = xsp.getBoolean("scope_mode_switch", true);
        this.lastUpdateTime = System.currentTimeMillis();
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
        } else if(this.IsScopeMode || this.AppModeList.contains(lpparam.packageName)) {
            try{
                onAppMode(lpparam);
            } catch (Throwable ignored){}
        }
    }

    private void onSystemMode(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("com.android.server.wm.DisplayPolicy", lpparam.classLoader, "layoutWindowLw",
                "com.android.server.wm.WindowState", "com.android.server.wm.WindowState", "com.android.server.wm.DisplayFrames",
                new XC_MethodHook() {
                    @Override
                    @RequiresApi(api = Build.VERSION_CODES.P)
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) XposedHelpers.getObjectField(param.args[0], "mAttrs");
                        if (attrs.type > WindowManager.LayoutParams.LAST_APPLICATION_WINDOW)
                            return;
                        if(attrs.packageName.equals(BuildConfig.APPLICATION_ID)) {
                            if(System.currentTimeMillis() - lastUpdateTime >= 35) {
                                update();
                            }
                        }
                        else if (SystemModeList.contains(attrs.packageName)) {
                            attrs.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                        }
                    }
                }
        );
    }


    private void layoutParamsGuardian() {
        if(this.mLayoutParamsList == null || this.mLayoutParamsList.isEmpty()) {
            return;
        }
        this.mLayoutParamsList.forEach(lp -> {
            if (lp.layoutInDisplayCutoutMode == LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
                return;
            }
            lp.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        });
    }

    private void addLayoutParamsGuardian(LayoutParams lp) {
        if(this.mLayoutParamsList == null) {
            this.mLayoutParamsList = Collections.synchronizedList(new ArrayList<>());
        }
        if(!this.mLayoutParamsList.contains(lp)) {
            this.mLayoutParamsList.add(lp);
        }
        if(this.mScheduledExecutorService == null) {
            this.mScheduledExecutorService = Executors.newScheduledThreadPool(1);
            this.mScheduledExecutorService.scheduleWithFixedDelay(
                    this::layoutParamsGuardian, 0, 30,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    private void onAppMode(XC_LoadPackage.LoadPackageParam lpparam) {

        try {
            XposedHelpers.findAndHookMethod("android.view.View", lpparam.classLoader, "setSystemUiVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                }
            });
        } catch (Throwable ignored){}

        try {
            XposedHelpers.findAndHookMethod("android.view.Window", lpparam.classLoader, "setFlags", int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    param.args[1] = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                }
            });
        } catch (Throwable ignored) { }

        try {
            XposedHelpers.findAndHookMethod("android.view.Window", lpparam.classLoader, "addFlags", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                }
            });
        } catch (Throwable ignored){}

        try {
            XposedHelpers.findAndHookMethod("android.view.Window", lpparam.classLoader, "setAttributes",
                    "android.view.WindowManager.LayoutParams",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            WindowManager.LayoutParams lp = (WindowManager.LayoutParams)param.args[0];
                            lp.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                        }
                    }
            );
        } catch (Throwable ignored) { }

        try {
            XposedHelpers.findAndHookMethod("android.view.Window", lpparam.classLoader, "getAttributes",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            var lp = (WindowManager.LayoutParams)param.getResult();
                            addLayoutParamsGuardian(lp);
                        }
                    }
            );
        } catch (Throwable ignored){}

        try {
            XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "onCreate", Bundle.class,
                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            var activity = (Activity) param.thisObject;
                            if(statusBarHeightId == 0) {
                                statusBarHeightId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            var activity = (Activity) param.thisObject;
                            Window window = activity.getWindow();
                            window.setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                WindowInsetsController insetsController = window.getInsetsController();
                                if (insetsController != null) {
                                    WindowCompat.setDecorFitsSystemWindows(window, false);
                                    insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                                }
                            }
                            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                            var lp = (LayoutParams) XposedHelpers.getObjectField(window, "mWindowAttributes");
                            if(lp == null) {
                                lp = window.getAttributes();
                            }
                            if(lp != null) {
                                lp.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                            }
                        }
                    }
            );
        } catch (Throwable ignored){}

        try {
            XposedHelpers.findAndHookMethod("android.content.res.Resources", lpparam.classLoader,"getDimensionPixelSize", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            int resourceId = (int)param.args[0];
                            if(statusBarHeightId != 0 && resourceId == statusBarHeightId ) {
                                param.setResult(0);
                            }
                        }
                    });
        } catch (Throwable ignored) { }

        try {
            XposedHelpers.findAndHookMethod("androidx.core.view.WindowInsetsCompat", lpparam.classLoader, "getDisplayCutout",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(null);
                        }
                    }
            );
        } catch (Throwable ignored) { }
    }
}
