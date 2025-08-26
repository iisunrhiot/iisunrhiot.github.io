package jp.iisun.midiaxes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

//    private MainActivity mainActivity;
    private Activity activity;
    private Context context;

    public static Fragment newInstance(String str) {
        // Fragemnt01 インスタンス生成
        SettingsFragment fragment = new SettingsFragment ();

        // Bundle にパラメータを設定
        Bundle barg = new Bundle();
        barg.putString("Message", str);
        //Bundleから getArguments() で取り出す
//        Bundle args = getArguments();
//        String str = args.getString("Message");
        fragment.setArguments(barg);

        //Activityに戻る
//        fragmentTransaction.addToBackStack(null);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
//        activity = (Activity) context;

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

}
