package ss.colytitse.setappfull.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class AppInfoManager {

    private List<PackageInfo> mSystemAppList = new ArrayList<>();
    private List<PackageInfo> mUserAppList = new ArrayList<>();
    private final Context mContext;

    public AppInfoManager(Context context) {
        this.mContext = context;
        this.update();
    }

    public void update() {
        List<PackageInfo> allAppList =  this.mContext.getPackageManager().getInstalledPackages(0);
        List<PackageInfo> systemAppList = this.mSystemAppList.isEmpty() ? this.mSystemAppList : new ArrayList<>();
        List<PackageInfo> userAppList = this.mUserAppList.isEmpty() ?  this.mUserAppList: new ArrayList<>();
        allAppList.forEach(app -> {
            if(app.applicationInfo == null) {
                return;
            }
            if ((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                userAppList.add(app);
            }
            else {
                systemAppList.add(app);
            }
        });
        if(this.mUserAppList != userAppList && !userAppList.isEmpty())
        {
            this.mUserAppList = userAppList;
        }
        if(this.mSystemAppList != systemAppList && !systemAppList.isEmpty())
        {
            this.mSystemAppList = systemAppList;
        }
    }

    public List<PackageInfo> getSystemAppList() {
        return this.mSystemAppList;
    }

    public List<PackageInfo> getUserAppList() {
        return this.mUserAppList;
    }

    public List<PackageInfo> systemAppListFilter(String content) {
        return this.mSystemAppList.stream().filter(e -> {
            if (e.applicationInfo == null) return false;
            return e.packageName.contains(content) || this.mContext
                    .getPackageManager().getApplicationLabel(e.applicationInfo)
                    .toString().contains(content);
        }).collect(Collectors.toList());
    }

    public List<PackageInfo> userAppListFilter(String content) {
        return this.mUserAppList.stream().filter(e -> {
            if (e.applicationInfo == null) return false;
            return e.packageName.contains(content) || this.mContext
                    .getPackageManager().getApplicationLabel(e.applicationInfo)
                    .toString().contains(content);
        }).collect(Collectors.toList());
    }
}
