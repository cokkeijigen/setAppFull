package ss.colytitse.setappfull.app;

import static ss.colytitse.setappfull.app.AppSettings.getHelloWorld;
import static ss.colytitse.setappfull.app.AppSettings.getScopeMode;
import static ss.colytitse.setappfull.app.AppSettings.setActivityStatusBar;
import static ss.colytitse.setappfull.app.AppSettings.setHelloWorld;
import static ss.colytitse.setappfull.app.AppSettings.setScopeMode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import ss.colytitse.setappfull.R;

public class SettingsActivity extends Activity {

    private Context mContext;

    @Override @SuppressLint("UseSwitchCompatOrMaterialCode")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityStatusBar(this);
        this. mContext = getApplicationContext();
        setContentView(R.layout.settings_layout);
        Switch scope_mode_switch = findViewById(R.id.scope_mode_switch);
        scope_mode_switch.setChecked(getScopeMode(mContext));
        scope_mode_switch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked != getScopeMode(mContext))
                setScopeMode(mContext, isChecked);
        });
        if(getHelloWorld(mContext)){
            TextView HelloWorld = findViewById(R.id.hello_world_view);
            HelloWorld.setText(String.format("%s\n%s", HelloWorld.getText(), getResources().getString(R.string.hello_world_1)));
            setHelloWorld(mContext);
            setScopeMode(mContext, true);
        }
    }

    public void openGitHub(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/cokkeijigen/setAppFull"));
        startActivity(intent);
    }
}
