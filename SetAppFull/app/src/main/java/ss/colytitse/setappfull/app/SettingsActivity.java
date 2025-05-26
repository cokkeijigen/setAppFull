package ss.colytitse.setappfull.app;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import ss.colytitse.setappfull.R;

public class SettingsActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    @Override @SuppressLint("UseSwitchCompatOrMaterialCode")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppSettings.setActivityStatusBar(this);
        this.setContentView(R.layout.settings_layout);
        Switch scopeModeSwitch = findViewById(R.id.scope_mode_switch);
        scopeModeSwitch.setChecked(AppSettings.getScopeMode(this));
        scopeModeSwitch.setOnCheckedChangeListener(this);
        if(AppSettings.getHelloWorld(this)){
            TextView HelloWorld = findViewById(R.id.hello_world_view);
            HelloWorld.setText(String.format("%s\n%s", HelloWorld.getText(),
                    getResources().getString(R.string.hello_world_1)));
            AppSettings.setHelloWorld(this);
            AppSettings.setScopeMode(this, true);
        }
    }

    public void openGitHub(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/cokkeijigen/setAppFull"));
        this.startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked != AppSettings.getScopeMode(this)) {
            AppSettings.setScopeMode(this, isChecked);
        }
    }
}
