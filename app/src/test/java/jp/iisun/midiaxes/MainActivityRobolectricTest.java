package jp.iisun.midiaxes;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MainActivityRobolectricTest {

    @Test
    public void testMainActivity_InitialState() {
        // Activityを構築して起動
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();

        // 初期状態で TextView が表示され、メッセージがセットされているか検証
        TextView tv = activity.findViewById(R.id.tv);
        assertNotNull(tv);
        assertEquals("Please connect and touch", tv.getText().toString());
    }

    @Test
    public void testSettingsButton_LaunchesSettingsActivity() {
        // Activityを構築して起動
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();

        // settings ボタンを取得してクリック操作
        Button sendButton = activity.findViewById(R.id.send_button);
        assertNotNull(sendButton);
        sendButton.performClick();

        // SettingsActivity が起動する Intent が発行されたか検証
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();

        assertNotNull("SettingsActivityへのIntentが発行される必要があります", nextIntent);
        assertEquals(SettingsActivity.class.getName(), nextIntent.getComponent().getClassName());
    }

    @Test
    public void testSettingsActivity_CloseButtonFinishesActivity() {
        // SettingsActivityを構築して起動
        SettingsActivity settingsActivity = Robolectric.buildActivity(SettingsActivity.class).setup().get();

        // closeボタンをクリック
        Button closeButton = settingsActivity.findViewById(R.id.close_Button);
        assertNotNull(closeButton);
        closeButton.performClick();

        // Activity が finish されたかを検証
        assertTrue("closeボタン押下でActivityが終了される必要があります", settingsActivity.isFinishing());
    }
}
