package ss.colytitse.setappfull.app;

import static ss.colytitse.setappfull.app.AppSettings.*;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
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

public class AppInfoAdapter extends BaseAdapter {

    private final static String TAG = "test_";

    private final List<PackageInfo> packageInfo;
    private final Context context;
    public AppInfoAdapter(List<PackageInfo> packageInfo, Context context) {
        this.context = context;
        this.packageInfo = sortOnSwitchList(packageInfo);
    }

    private List<PackageInfo> sortOnSwitchList(List<PackageInfo> appList){
        List<PackageInfo> onSwitch = new ArrayList<>();
        List<PackageInfo> offSwitch = new ArrayList<>();
        List<PackageInfo> allSwitchStatus = new ArrayList<>();
        for (PackageInfo info: appList){
            if(getSetMode(context, info.packageName) != NO_SET)
                onSwitch.add(info);
            else offSwitch.add(info);
        }
        allSwitchStatus.addAll(onSwitch);
        allSwitchStatus.addAll(offSwitch);
        return allSwitchStatus;
    }

    @Override
    public int getCount() {
        return packageInfo.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        TextView app_name = view.findViewById(R.id.app_name);
        TextView app_pkgn = view.findViewById(R.id.app_pkgn);
        TextView app_version = view.findViewById(R.id.app_version);
        ImageView app_icon = view.findViewById(R.id.app_icon);
        Switch onSwitch = view.findViewById(R.id.set_switch);
        AppInfo appInfo = new AppInfo(packageInfo.get(i), context);
        app_version.setText(appInfo.getVersionName() + " (" + appInfo.getVersionCode() + ")");
        app_icon.setImageDrawable(appInfo.getAppIcon());
        app_pkgn.setText(appInfo.getPackageName());
        app_name.setText(appInfo.getAppName());
        int mode = getSetMode(context, appInfo.getPackageName());
        LinearLayout item_bac = view.findViewById(R.id.item_root_view);
        TextView mode_text = view.findViewById(R.id.mode_state);
        onSwitch.setChecked(mode != NO_SET);
        if (mode == NO_SET) {
            mode_text.setText("");
            item_bac.setBackground(context.getResources().getDrawable(R.drawable.button_background, context.getTheme()));
        }
        if (mode == MODE_1){
            item_bac.setBackground(context.getResources().getDrawable(R.drawable.button_background2, context.getTheme()));
            mode_text.setText(R.string.mode_1);
        }
        if (mode == MODE_2){
            item_bac.setBackground(context.getResources().getDrawable(R.drawable.button_background3, context.getTheme()));
            mode_text.setText(R.string.mode_2);
        }
        mode_text.setVisibility(getScopeMode(context) ? View.GONE : View.VISIBLE);
        onSwitch.setClickable(false);
        return view;
    }

    static public class AppInfo{
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
            return packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
        }

        public String getVersionName(){
            return packageInfo.versionName;
        }

        public int getVersionCode(){
            return packageInfo.versionCode;
        }

        public Drawable getAppIcon(){
            return packageInfo.applicationInfo.loadIcon(context.getPackageManager());
        }


    }
}
