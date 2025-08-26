package jp.iisun.midiaxes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;


public class SettingsActivity extends AppCompatActivity {


    private Button closeButton, jSButton, aSButton;
    private SharedPreferences sharedPreferences;
    private AdView adView;

//    private static final String SWITCH_STATE_KEY = "switch_state";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // SharedPreferencesのインスタンスを取得
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Spinnerの設定
        setUpSpinner(R.id.spinner1, R.array.spinner_items1);
        setUpSpinner(R.id.spinner2, R.array.spinner_items1);
        setUpSpinner(R.id.spinner3, R.array.spinner_items3);
        setUpSpinner(R.id.spinner4, R.array.spinner_items3);
        setUpSpinner(R.id.spinner5, R.array.spinner_items5);
        setUpSpinner(R.id.spinner6, R.array.spinner_items5);
        setUpSpinner(R.id.spinner7, R.array.spinner_items7);
        setUpSpinner(R.id.spinner8, R.array.spinner_items7);
        setUpSpinner(R.id.spinner9, R.array.spinner_items9);
        setUpSpinner(R.id.spinner10, R.array.spinner_items9);
        setUpSpinner(R.id.spinner91, R.array.spinner_items91);
//        setUpSwitch(R.id.switch1);
        // SharedPreferencesから保存された値を取得して各スピナーに設定
        setSelectedSpinnerValue(R.id.spinner1);
        setSelectedSpinnerValue(R.id.spinner2);
        setSelectedSpinnerValue(R.id.spinner3);
        setSelectedSpinnerValue(R.id.spinner4);
        setSelectedSpinnerValue(R.id.spinner5);
        setSelectedSpinnerValue(R.id.spinner6);
        setSelectedSpinnerValue(R.id.spinner7);
        setSelectedSpinnerValue(R.id.spinner8);
        setSelectedSpinnerValue(R.id.spinner9);
        setSelectedSpinnerValue(R.id.spinner10);
        setSelectedSpinnerValue(R.id.spinner91);
//        setSelectedSwitchValue(R.id.switch1);
        closeButton = findViewById(R.id.close_Button);
        jSButton = findViewById(R.id.jestureSettings_Button);
        aSButton = findViewById(R.id.actionSettings_Button);
        RequestConfiguration configuration = new RequestConfiguration.Builder()
//                .setTestDeviceIds(Arrays.asList("4007E518B95AA666DD4C3B9F4478F843"))
                .build();
        MobileAds.setRequestConfiguration(configuration);

        // AdMob SDKの初期化
        MobileAds.initialize(this, initializationStatus -> {});

        // AdViewの参照を取得
        adView = findViewById(R.id.adView);

        // 広告リクエストを作成してAdViewに読み込む
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                // 設定画面を閉じる
                finish();
            }
        });

        aSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        });

        jSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
//                Intent intent = new Intent("android.provider.Settings.ACTION_GESTURE_NAVIGATION");
                startActivity(intent);
            }
        });
    }

    private void setUpSpinner(int spinnerId, int arrayId) {
        Spinner spinner = findViewById(spinnerId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        final String key = "selectedItem_" + spinnerId;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                sharedPreferences.edit().putString(key, selectedItem).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // スピナーに保存された値を設定するメソッド
    private void setSelectedSpinnerValue(int spinnerId) {
        Spinner spinner = findViewById(spinnerId);
        String key = "selectedItem_" + spinnerId;
        String selectedValue = sharedPreferences.getString(key, "");
        if (!selectedValue.isEmpty()) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
            int position = adapter.getPosition(selectedValue);
            if (position != -1) {
                spinner.setSelection(position);
            }
        }
    }

//    private void setUpSwitch(int switchId) {
//        Switch sw = findViewById(switchId);
//        final String key = "isSwitched_" + switchId;
//        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // Switch の状態が変更されたら SharedPreferences に保存
//                sharedPreferences.edit().putBoolean(key, isChecked).apply();
//            }
//        });
//    }
//    private void setSelectedSwitchValue(int switchId) {
//        Switch sw = findViewById(switchId);
//        String key = "isSwitched_" + switchId;
//        boolean switchValue = sharedPreferences.getBoolean(key, false);
//        sw.setChecked(switchValue);
//    }

}