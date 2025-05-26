package ss.colytitse.setappfull;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ss.colytitse.setappfull.app.AppInfoAdapter;
import ss.colytitse.setappfull.app.AppInfoManager;
import ss.colytitse.setappfull.app.AppSettings;
import ss.colytitse.setappfull.app.SettingsActivity;

@SuppressWarnings({"unused"})
@SuppressLint({"UseSwitchCompatOrMaterialCode","UseCompatLoadingForDrawables","SetTextI18n"})
public class MainActivity extends Activity implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, android.text.TextWatcher, TextView.OnEditorActionListener {

    private static final String TAG = "MainActivity";
    private AppInfoManager  mAppInfoManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mAppListView;
    private EditText mSearchEditText;
    private TextView mListTypeName;
    private ImageButton mSearchButton;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppSettings.setActivityStatusBar(this);
        this.setContentView(R.layout.main_layout);
        TextView versionName = findViewById(R.id.version_name);
        versionName.setText(String.format("%s%s",
                versionName.getText().toString().split("->")[0],
                BuildConfig.VERSION_NAME)
        );

        this.mSwipeRefreshLayout = this.findViewById(R.id.swipe_refresh_layout);
        this.mSwipeRefreshLayout.setOnRefreshListener(this);
        this.mAppListView = this.findViewById(R.id.app_items);
        this.mAppListView.setOnItemClickListener(this);
        this.mSearchEditText = this.findViewById(R.id.edit_insearch);
        this.mSearchEditText.addTextChangedListener(this);
        this.mSearchEditText.setOnEditorActionListener(this);
        this.mListTypeName = this.findViewById(R.id.app_list_name);
        this.mSearchButton = this.findViewById(R.id.btn_insearch);
        int switchListType = AppSettings.getSwitchListType(this);
        int stringId = switchListType == AppSettings.USER_VIEW ?
                R.string.list_user : R.string.list_system;
        this.mListTypeName.setText(String.format("(%s)",
                this.getResources().getString(stringId))
        );

        this.mAppInfoManager = new AppInfoManager(this);
        this.updateAppListView();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        TextView pkgNameView = view.findViewById(R.id.app_pkgn);
        String pkgName = pkgNameView.getText().toString();
        LinearLayout itemBac = view.findViewById(R.id.item_root_view);
        TextView modeText = view.findViewById(R.id.mode_state);
        Switch itemSwitch = view.findViewById(R.id.set_switch);
        boolean scopeMode = AppSettings.getScopeMode(this);
        if(!itemSwitch.isChecked() && !scopeMode) {
            itemSwitch.setChecked(true);
            itemBac.setBackground(this.getResources().getDrawable(R.drawable.button_background2, this.getTheme()));
            modeText.setText(R.string.mode_1);
            AppSettings.saveAppMode(this, pkgName);
        }
        else if (AppSettings.getSetMode(this, pkgName) == AppSettings.MODE_1 || (scopeMode && !itemSwitch.isChecked())){
            itemSwitch.setChecked(true);
            AppSettings.saveSystemMode(this, pkgName);
            modeText.setText(R.string.mode_2);
            itemBac.setBackground(this.getResources().getDrawable(R.drawable.button_background3, this.getTheme()));
        }
        else {
            itemSwitch.setChecked(false);
            modeText.setText("");
            itemBac.setBackground(this.getResources().getDrawable(R.drawable.button_background, this.getTheme()));
            AppSettings.deleteSelection(this, pkgName);
        }
        modeText.setVisibility(scopeMode ? View.GONE : View.VISIBLE);
    }

    private void updateAppListView(String search) {
        this.mSwipeRefreshLayout.setRefreshing(true);
        String content = search != null ? search :
                this.mSearchEditText.getText().toString();
        int switchListType = AppSettings.getSwitchListType(this);
        if(switchListType == AppSettings.USER_VIEW) {
            this.mAppListView.setAdapter(new AppInfoAdapter(content.isEmpty()?
                    this.mAppInfoManager.getUserAppList() :
                    this.mAppInfoManager.userAppListFilter(content),
                    this)
            );
        }
        else {
            this.mAppListView.setAdapter(new AppInfoAdapter(content.isEmpty()?
                    this.mAppInfoManager.getSystemAppList() :
                    this.mAppInfoManager.systemAppListFilter(content),
                    this)
            );
        }
        this.mSwipeRefreshLayout.setRefreshing(false);
    }

    private void updateAppListView() {
        this.updateAppListView(null);
    }

    public void onSwitchListView(View view) {
        if(AppSettings.getSwitchListType(this) == AppSettings.USER_VIEW) {
            AppSettings.savonSwitch(this ,AppSettings.SYSTEM_VIEW);
            this.updateAppListView();
            this.mListTypeName.setText(String.format("(%s)",
                    this.getResources().getString(R.string.list_system))
            );
        }else {
            AppSettings.savonSwitch(this, AppSettings.USER_VIEW);
            this.updateAppListView();
            this.mListTypeName.setText(String.format("(%s)",
                    this.getResources().getString(R.string.list_user))
            );
        }
    }

    public void onSearchButtonClick(View view) {
        if(this.mSearchEditText.getVisibility() == View.GONE) {
            this.mSearchEditText.setVisibility(View.VISIBLE);
            Drawable drawable = this.getResources().getDrawable(
                    R.drawable.ic_baseline_close_24, getTheme());
            this.mSearchButton.setImageDrawable(drawable);
        }
        else {
            this.mSearchEditText.setVisibility(View.GONE);
            Drawable drawable = this.getResources().getDrawable(
                    R.drawable.ic_baseline_search_24, getTheme());
            this.mSearchButton.setImageDrawable(drawable);
            this.mSearchEditText.setText("");
            this.updateAppListView();
            this.onEditorAction(null, EditorInfo.IME_ACTION_NEXT, null);
        }
    }

    public void openSettingsActivity(View view) {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void onRefresh() {
        this.mAppInfoManager.update();
        this.updateAppListView();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        this.updateAppListView(s.toString());
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if(actionId == EditorInfo.IME_ACTION_NEXT) {
            var systemService = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            systemService.hideSoftInputFromWindow(this.mSearchButton.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    public void onBoolbarClick(View view) {
        this.updateAppListView();
    }
}
