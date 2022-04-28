package ss.colytitse.setappfull;

import static ss.colytitse.setappfull.app.AppSettings.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.ArrayList;
import java.util.List;

import ss.colytitse.setappfull.app.AppInfoAdapter;
import ss.colytitse.setappfull.app.AppSettings;

@SuppressLint({"UseSwitchCompatOrMaterialCode","UseCompatLoadingForDrawables","SetTextI18n"})
public class MainActivity extends Activity {

    private static final String TAG = "test_";
    private List<PackageInfo> systemAppList;
    private List<PackageInfo> userAppList;
    private boolean EditSearchInit = false;
    private Context mContext;

    private void initApplicationList() {
        List<PackageInfo> allAppList = getPackageManager().getInstalledPackages(0);
        systemAppList = new ArrayList<>();
        userAppList = new ArrayList<>();
        for (PackageInfo app : allAppList){
            // 非系统应用
            if ((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                userAppList.add(app);
            // 系统应用
            else systemAppList.add(app);
        }
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Runtime.getRuntime().exec("su");
        }catch (Throwable ignored){}
        setActivityStatusBar(this);
        setContentView(R.layout.main_layout);
        mContext = getApplicationContext();
        initApplicationList();
        initMainActivity();
    }

    private void initMainActivity() {
        int AppViewList = getOnSwitchListView(mContext);
        TextView setVersionName = findViewById(R.id.version_name);
        TextView appListName = findViewById(R.id.app_list_name);
        setVersionName.setText(String.format("%s%s",
                setVersionName.getText().toString().split("->")[0], getVersionName(mContext)));
        appListName.setText(String.format("(%s)", AppViewList == USER_VIEW ? getResources().getString(R.string.list_user) : getResources().getString(R.string.list_system)));
        initMainActivityListView(AppViewList == USER_VIEW ? userAppList : systemAppList);
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
            Toast.makeText(mContext,
                    getResources().getString(R.string.show_message),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    public void onSwitchListView(View view) {
        EditText edit_insearch = findViewById(R.id.edit_insearch);
        String inText = edit_insearch.getText().toString();
        TextView appListName = findViewById(R.id.app_list_name);
        if(getOnSwitchListView(mContext) == USER_VIEW){
            new AppSettings(mContext).savonSwitch(SYSTEM_VIEW);
            initMainActivityListView(searchAppView(inText));
            appListName.setText(String.format("(%s)", getResources().getString(R.string.list_system)));
        }else {
            new AppSettings(mContext).savonSwitch(USER_VIEW);
            initMainActivityListView(searchAppView(inText));
            appListName.setText(String.format("(%s)", getResources().getString(R.string.list_user)));
        }
    }

    public void insearch(View view) {
        EditText edit_insearch = findViewById(R.id.edit_insearch);
        ImageButton btn_insearch = findViewById(R.id.btn_insearch);
        initEditaTextAction(edit_insearch);
        if(edit_insearch.getVisibility() == View.GONE){
            edit_insearch.setVisibility(View.VISIBLE);
            btn_insearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_close_24, getTheme()));
            edit_insearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
                return false;
            });
            return;
        }
        edit_insearch.setVisibility(View.GONE);
        initMainActivityListView(getOnSwitchListView(mContext) == USER_VIEW ? userAppList : systemAppList);
        edit_insearch.setText("");
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
        btn_insearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_search_24, getTheme()));
    }

    private void initEditaTextAction(EditText edit_insearch) {
        if (!EditSearchInit){
            EditSearchInit = true;
            edit_insearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void afterTextChanged(Editable editable) {
                    String inText = editable.toString();
                    initMainActivityListView(searchAppView(inText));
                }
            });
        }
    }

    private List<PackageInfo> searchAppView(String intext) {
        List<PackageInfo> appList = getOnSwitchListView(mContext) == USER_VIEW ? userAppList : systemAppList;
        if (intext.length() < 1) return appList;
        List<PackageInfo> result = new ArrayList<>();
        for (PackageInfo info : appList)
            if (info.packageName.contains(intext) || getPackageManager().getApplicationLabel(info.applicationInfo).toString().contains(intext))
                result.add(info);
        return result;
    }
}
