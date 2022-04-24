package ss.colytitse.setappfull;

import static ss.colytitse.setappfull.AppSettings.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"UseSwitchCompatOrMaterialCode","UseCompatLoadingForDrawables","SetTextI18n"})
public class MainActivity extends Activity {

    private static final String TAG = "test_";
    public final Context mContext = this;
    public List<PackageInfo> AllAppList;
    List<PackageInfo> userAppList = new ArrayList<>();
    List<PackageInfo> systemAppList = new ArrayList<>();

    private void initApplicationList() {
        this.AllAppList = getPackageManager().getInstalledPackages(0);
        for (PackageInfo app : AllAppList){
            // 非系统应用
            if ((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                userAppList.add(app);
            // 系统应用
            else systemAppList.add(app);
        }
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        setActivityStatusBar(this);
        initApplicationList();
        TextView setVersionName = findViewById(R.id.version_name);
        setVersionName.setText("当前版本：" + getVersionName(mContext));
        initMainActivityListView(new AppSettings(mContext).getOnSwitchListView() ? userAppList : systemAppList);

        try {
            Runtime.getRuntime().exec("su");
        }catch (Throwable ignored){}
    }

    public void initMainActivityListView(List<PackageInfo> appList){
        ListView listView = findViewById(R.id.app_items);
        listView.setAdapter(new AppInfoAdapter(appList, mContext));
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            TextView app_pkgn = view.findViewById(R.id.app_pkgn);
            String text = app_pkgn.getText().toString();
            Switch onSwitch = view.findViewById(R.id.set_switch);
            if(!onSwitch.isChecked()){
                onSwitch.setChecked(true);
                new AppSettings(mContext).savData(text);
            } else {
                onSwitch.setChecked(false);
                new AppSettings(mContext).delData(text);
            }
            Toast.makeText(this,"保存成功，重启系统后生效！", Toast.LENGTH_SHORT).show();
           // Log.d("test_", "已点击：" + text);
        });
    }

    public void onSwitchListView(View view) {
        if(new AppSettings(mContext).getOnSwitchListView()){
            new AppSettings(mContext).savonSwitch(false);
            initMainActivityListView(systemAppList);
            Toast.makeText(this,"切换到系统应用", Toast.LENGTH_SHORT).show();
        }else {
            new AppSettings(mContext).savonSwitch(true);
            initMainActivityListView(userAppList);
            Toast.makeText(this,"切换到用户应用", Toast.LENGTH_SHORT).show();
        }
    }

    public void insearch(View view) {
        EditText edit_insearch = findViewById(R.id.edit_insearch);
        ImageButton btn_insearch = findViewById(R.id.btn_insearch);
        if(edit_insearch.getVisibility() == View.GONE){
            edit_insearch.setVisibility(View.VISIBLE);
            btn_insearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_close_24, getTheme()));
            edit_insearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(view.getWindowToken(), 0);
                    String intext = textView.getText().toString();
                    if (intext.length() > 0)
                        initMainActivityListView(searchAppView(intext));
                    return true;
                }
                return false;
            });
            return;
        }
        edit_insearch.setVisibility(View.GONE);
        initMainActivityListView(new AppSettings(mContext).getOnSwitchListView() ? userAppList : systemAppList);
        edit_insearch.setText("");
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
        btn_insearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_search_24, getTheme()));
    }

    private List<PackageInfo> searchAppView(String intext) {
        List<PackageInfo> result = new ArrayList<>();
        for (PackageInfo info : (new AppSettings(mContext).getOnSwitchListView() ? userAppList : systemAppList))
            if (info.packageName.contains(intext) || getPackageManager().getApplicationLabel(info.applicationInfo).toString().contains(intext))
                result.add(info);
        return result;
    }
}
