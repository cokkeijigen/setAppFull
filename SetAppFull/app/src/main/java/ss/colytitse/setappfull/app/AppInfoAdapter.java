package ss.colytitse.setappfull.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ss.colytitse.setappfull.R;

@SuppressWarnings({"unused"})
@SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n", "UseCompatLoadingForDrawables"})
public class AppInfoAdapter extends BaseAdapter {

    private final static String TAG = "test_";

    private final List<PackageInfo> packageInfo;
    private final Context context;

    public AppInfoAdapter(List<PackageInfo> packageInfo, Context context) {
        this.context = context;
        this.packageInfo = this.sortOnSwitchList(packageInfo);
    }

    private List<PackageInfo> sortOnSwitchList(List<PackageInfo> appList) {
        List<PackageInfo> result = new ArrayList<>();
        List<PackageInfo> offSwitch = new ArrayList<>();
        appList.forEach(info -> {
            if(AppSettings.getSetMode(context, info.packageName) != AppSettings.NO_SET)
            {
                result.add(info);
            }
            else {
                offSwitch.add(info);
            }
        });
        result.addAll(offSwitch);
        return result;
    }

    @Override
    public int getCount() {
        return this.packageInfo.size();
    }

    @Override
    public PackageInfo getItem(int i) {
        if(i >= this.packageInfo.size()) {
            return null;
        }
        return this.packageInfo.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                    R.layout.list_item, viewGroup, false
            );
        }
        TextView appName = view.findViewById(R.id.app_name);
        TextView appPkgn = view.findViewById(R.id.app_pkgn);
        TextView appVersion = view.findViewById(R.id.app_version);
        ImageView appIcon = view.findViewById(R.id.app_icon);
        Switch onSwitch = view.findViewById(R.id.set_switch);
        AppInfo appInfo = new AppInfo(packageInfo.get(i), context);
        appVersion.setText(appInfo.getVersionName() + " (" + appInfo.getVersionCode() + ")");
        appIcon.setImageDrawable(appInfo.getAppIcon());
        appPkgn.setText(appInfo.getPackageName());
        appName.setText(appInfo.getAppName());
        int mode = AppSettings.getSetMode(context, appInfo.getPackageName());
        LinearLayout item_bac = view.findViewById(R.id.item_root_view);
        TextView modeText = view.findViewById(R.id.mode_state);
        onSwitch.setChecked(mode != AppSettings.NO_SET);
        if (mode ==AppSettings.NO_SET) {
            modeText.setText("");
            item_bac.setBackground(context.getResources()
                    .getDrawable(R.drawable.button_background, context.getTheme()));
        }
        if (mode == AppSettings.MODE_1) {
            item_bac.setBackground(context.getResources()
                    .getDrawable(R.drawable.button_background2, context.getTheme()));
            modeText.setText(R.string.mode_1);
        }
        if (mode == AppSettings.MODE_2) {
            item_bac.setBackground(context.getResources()
                    .getDrawable(R.drawable.button_background3, context.getTheme()));
            modeText.setText(R.string.mode_2);
        }
        modeText.setVisibility(AppSettings.getScopeMode(context) ? View.GONE : View.VISIBLE);
        onSwitch.setClickable(false);
        return view;
    }

    static public class AppInfo {
        private final PackageInfo packageInfo;
        private final Context context;

        public AppInfo(PackageInfo packageInfo, Context context) {
            this.packageInfo = packageInfo;
            this.context = context;
        }

        public String getPackageName(){
            return packageInfo.packageName;
        }

        public String getAppName(){
            if(packageInfo.applicationInfo != null) {
                return packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            }
            return null;
        }

        public String getVersionName(){
            return packageInfo.versionName;
        }

        public int getVersionCode(){
            return packageInfo.versionCode;
        }

        public Drawable getAppIcon() {
            if(packageInfo.applicationInfo != null) {
                return packageInfo.applicationInfo.loadIcon(context.getPackageManager());
            }
            return null;
        }


    }
}
