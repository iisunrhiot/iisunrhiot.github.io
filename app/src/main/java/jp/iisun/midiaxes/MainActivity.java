package jp.iisun.midiaxes;

import static android.media.midi.MidiManager.TRANSPORT_MIDI_BYTE_STREAM;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button mSendButton;
    protected MidiManager mMidiManager;
    protected MidiDevice mMidiDevice;
    protected MidiDeviceInfo midiDeviceInfo;
    protected MidiInputPort inputPort;
//    private PaintView paintView;
//    private Path path;
//    private Paint paint;
    private RelativeLayout mSendMatrix, mSendRelease;
    protected MyMidiDeviceCallback midiDeviceCallback;
    protected int numInputs, numOutputs;
    protected int latestPortNumber;
    protected Executor executor;
    protected int absoluteXIndex;
    protected int absoluteYIndex;
    private int temp1X = -1;
    private int temp1Y = -1;
    private int temp2X = -1;
    private int temp2Y = -1;
    protected View v;
    private int lastPointerId = -1;
    private float lastX, lastY;
    private SharedPreferences sharedPreferences;
    private String spinner1Value, spinner2Value,
            spinner3Value, spinner4Value,
            spinner5Value, spinner6Value,
            spinner7Value, spinner8Value,
            spinner9Value, spinner10Value,
            spinner91Value;
    private TextView tx;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_TIME_KEY = "firstTime";
    private AdView adView;
//    private GestureDetectorCompat gestureDetector;
//    private static final String SWITCH_STATE_KEY = "switch_state";
//    private boolean isSwipeEnabled = true; // 初期状態はスワイプを有効にする
//    protected Switch switch1;
//    private View mExclusionView;
//    private WindowInsetsController mInsetsController;


    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings); // 設定画面のレイアウトファイルを指定 その他の初期化処理を行う
        // Switchの状態を取得して、スワイプの有効/無効を設定
//        Switch switch1 = findViewById(R.id.switch1);
//        switch1 = findViewById(R.id.switch1);
//        boolean switchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
//        boolean switchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
//        switch1.setChecked(switchState);
//        setSwipeEnabled(!switchState); // Switchの状態に応じてスワイプを有効/無効にする
//
//        // GestureDetectorCompatを初期化
//        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());
        setContentView(R.layout.activity_main);
//        // SharedPreferencesのインスタンスを取得
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        //1)idを取得
        tx = findViewById(R.id.tv);
        mSendButton = findViewById(R.id.send_button);
//        paintView =(PaintView)findViewById(R.id.paintView);
        mSendMatrix = findViewById(R.id.matrix);
        mSendRelease = findViewById(R.id.release);
        // MIDI機能がサポートされているかチェック
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            // MidiManagerを取得
            mMidiManager = (MidiManager) getSystemService(Context.MIDI_SERVICE);
            if (mMidiManager == null) {
                Log.e(TAG, "MidiManager is null");
                Toast.makeText(this, "MIDI service unavailable.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            // MIDI機能がサポートされていない場合の処理
            Toast.makeText(this, "Your Android does not support MIDI functionality.Looks like you'll have to use another Android...", Toast.LENGTH_SHORT).show();
            // 必要に応じて代替処理やユーザーへの通知を行う
        }

        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Handler(Looper.getMainLooper()).post(command);
            }
        };

//        // 必要な権限チェックとリクエスト
//        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
//            return;
//        }
        midiDeviceCallback = new MyMidiDeviceCallback(this);
//        connectToDevice();

        // 設定ボタンにonClickイベントを設定
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMidiDevice != null) {
                    if (inputPort != null) {
                        if (spinner91Value.equals("CC#120 All Sound Off")) {
                            sAllSoundOff();
                        }
                        if (spinner91Value.equals("CC#121 Reset All Controllers")) {
                            sResetAllControllers();
                        }
                        if (spinner91Value.equals("CC#123 All Notes Off")) {
                            sAllNotesOff();
                        }
                    }
                } else {
                    Log.e(TAG, "mMidiDevice is not ready yet.");
                    try {
                        closeDevice();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    connectToDevice();
                }
                // 設定画面へのインテントを作成
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                // 設定画面を起動
                startActivity(intent);
            }
        });

        // LastTouchedPointerTouchEventクラスのインスタンスを作成
        LastTouchedPointerTouchEvent lastTouchedPointerTouchEvent = new LastTouchedPointerTouchEvent();

        // mSendReleaseのonTouchListener
        mSendRelease.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // ACTION_DOWNが検知されたら、mSendMatrixのタッチイベントを有効にする
                        mSendMatrix.setEnabled(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        // ACTION_DOWNが検知されたら、mSendMatrixのタッチイベントを有効にする
                        // ACTION_MOVEが検知された場合、mSendMatrixにタッチイベントを渡す
                        mSendMatrix.dispatchTouchEvent(event);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // 追加のタッチを無効にする
                        break;
                }
                return true;
            }
        });

        mSendMatrix.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
//                tx.setText("押しました2。");
                int action = event.getActionMasked();
                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                lastX = event.getX();
                lastY = event.getY();
                v = view;

                //(3-2)タッチの処理
                switch (action) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // 新しい指がタッチされた場合、最後にタッチされた指の情報を更新する
                        lastPointerId = pointerId;
                        lastX = event.getX(pointerIndex);
                        lastY = event.getY(pointerIndex);
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        if (mMidiDevice != null) {
                            if (inputPort != null) {
                                if (spinner1Value.length() >= 4 && spinner1Value.substring(0, 4).equals("Note")){
                                    sNoteOffX();
                                }
                                if (spinner2Value.length() >= 4 && spinner2Value.substring(0, 4).equals("Note")){
                                    sNoteOffY();
                                }
                                tx.setText("X: " + absoluteXIndex + "\nY: " + absoluteYIndex);
                            } else {
                                Log.e(TAG, "mMidiDevice is not ready yet.");
                                try {
                                    closeDevice();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                connectToDevice();
                            }
                        } else {
                            Log.e(TAG, "mMidiDevice is not ready yet.");
                            try {
                                closeDevice();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            connectToDevice();
                        }
                        // 指が離れた場合、最後にタッチされた指の情報をクリアする
//                        if (pointerId == lastPointerId) {
//                            lastPointerId = -1;
//                        }
                        // 指が離れた場合、他の指がまだタッチされているならlastPointerIdを他の指に移す
                        if (pointerId == lastPointerId) {
                            int newPointerIndex = (pointerIndex == 0) ? 1 : 0;
                            lastPointerId = event.getPointerId(newPointerIndex);
                            lastX = event.getX(newPointerIndex);
                            lastY = event.getY(newPointerIndex);
                        }
                        MotionEvent downEvent = MotionEvent.obtain(
                                event.getDownTime(),
                                SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_MOVE,
                                lastX,
                                lastY,
                                0 // Meta stateは0で初期化
                        );
                        // MotionEvent.ACTION_DOWNをViewに渡してイベントを発生させる
                        dispatchTouchEvent(downEvent);
                        downEvent.recycle(); // 使用したMotionEventはリサイクルする
                        break;

                    case MotionEvent.ACTION_DOWN:
//                        tx.setText("押しました3。");
//                        path.moveTo(x, y);
//                        v.invalidate();
                        if (inputPort != null) {
                            if (mMidiDevice != null) {
                                if (spinner1Value.equals("Pitch Bend")) {
                                    sPitchX();
                                }
                                if (spinner2Value.equals("Pitch Bend")) {
                                    sPitchY();
                                }
                                if (spinner1Value.equals("Note ALL")) {
                                    sNoteAllX();
                                }
                                if (spinner2Value.equals("Note ALL")) {
                                    sNoteAllY();
                                }
                                if (spinner1Value.length() >= 7 && spinner1Value.substring(0, 7).equals("Note 25")){
                                    sN25X();
                                }
                                if (spinner2Value.length() >= 7 && spinner2Value.substring(0, 7).equals("Note 25")){
                                    sN25Y();
                                }
                                if (spinner1Value.length() >= 29 && spinner1Value.substring(0, 29).equals("Note C Major Pentatonic of 49")){
                                    sN49PentaX();
                                }
                                if (spinner2Value.length() >= 29 && spinner2Value.substring(0, 29).equals("Note C Major Pentatonic of 49")){
                                    sN49PentaY();
                                }
                                if (spinner1Value.length() >= 21 && spinner1Value.substring(0, 21).equals("Note White keys of 49")){
                                    sN49WhiteX();
                                }
                                if (spinner2Value.length() >= 21 && spinner2Value.substring(0, 21).equals("Note White keys of 49")){
                                    sN49WhiteY();
                                }
                                if (spinner1Value.equals("Note 88keys")) {
                                    sN88X();
                                }
                                if (spinner2Value.equals("Note 88keys")) {
                                    sN88Y();
                                }
                                if (spinner1Value.equals("Note C Major Pentatonic of 88")) {
                                    sN88PentaX();
                                }
                                if (spinner2Value.equals("Note C Major Pentatonic of 88")) {
                                    sN88PentaY();
                                }
                                if (spinner1Value.equals("Note White keys of 88")) {
                                    sN88WhiteX();
                                }
                                if (spinner2Value.equals("Note White keys of 88")) {
                                    sN88WhiteY();
                                }
                                if (spinner1Value.length() >= 3 && spinner1Value.substring(0, 3).equals("CC#")){
                                    sCcX();
                                }
                                if (spinner2Value.length() >= 3 && spinner2Value.substring(0, 3).equals("CC#")){
                                    sCcY();
                                }
                                // 画面に表示
                                tx.setText("X: " + absoluteXIndex + "\nY: " + absoluteYIndex);
                            }else {
                                Log.e(TAG, "mMidiDevice is not ready yet.");
                                try {
                                    closeDevice();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            Log.e(TAG, "mMidiDevice is not ready yet.");
                            try {
                                closeDevice();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
//                        connectToDevice();
                        break;

                    case MotionEvent.ACTION_MOVE:
//                        tx.setText("押しました4。");
//                        path.lineTo(x, y);
//                        v.invalidate();
                        // 最後にタッチされた指の移動を処理する
                        int lastPointerIndex = event.findPointerIndex(lastPointerId);
                        if (lastPointerIndex != -1) {
                            lastX = event.getX(lastPointerIndex);
                            lastY = event.getY(lastPointerIndex);
                        }
                        if (mMidiDevice != null) {
                            if (inputPort != null) {
                                if (spinner1Value.equals("Pitch Bend")) {
                                    sPitchX();
                                }
                                if (spinner2Value.equals("Pitch Bend")) {
                                    sPitchY();
                                }
                                if (spinner1Value.equals("Note ALL")) {
                                    sNoteAllX();
                                }
                                if (spinner2Value.equals("Note ALL")) {
                                    sNoteAllY();
                                }
                                if (spinner1Value.length() >= 7 && spinner1Value.substring(0, 7).equals("Note 25")){
                                    sN25X();
                                }
                                if (spinner2Value.length() >= 7 && spinner2Value.substring(0, 7).equals("Note 25")){
                                    sN25Y();
                                }
                                if (spinner1Value.length() >= 29 && spinner1Value.substring(0, 29).equals("Note C Major Pentatonic of 49")){
                                    sN49PentaX();
                                }
                                if (spinner2Value.length() >= 29 && spinner2Value.substring(0, 29).equals("Note C Major Pentatonic of 49")){
                                    sN49PentaY();
                                }
                                if (spinner1Value.length() >= 21 && spinner1Value.substring(0, 21).equals("Note White keys of 49")){
                                    sN49WhiteX();
                                }
                                if (spinner2Value.length() >= 21 && spinner2Value.substring(0, 21).equals("Note White keys of 49")){
                                    sN49WhiteY();
                                }
                                if (spinner1Value.equals("Note 88keys")) {
                                    sN88X();
                                }
                                if (spinner2Value.equals("Note 88keys")) {
                                    sN88Y();
                                }
                                if (spinner1Value.equals("Note C Major Pentatonic of 88")) {
                                    sN88PentaX();
                                }
                                if (spinner2Value.equals("Note C Major Pentatonic of 88")) {
                                    sN88PentaY();
                                }
                                if (spinner1Value.equals("Note White keys of 88")) {
                                    sN88WhiteX();
                                }
                                if (spinner2Value.equals("Note White keys of 88")) {
                                    sN88WhiteY();
                                }
                                if (spinner1Value.length() >= 3 && spinner1Value.substring(0, 3).equals("CC#")){
                                    sCcX();
                                }
                                if (spinner2Value.length() >= 3 && spinner2Value.substring(0, 3).equals("CC#")){
                                    sCcY();
                                }
                                // 画面に表示
                                tx.setText("X: " + absoluteXIndex + "\nY: " + absoluteYIndex);
                            } else {
                                Log.e(TAG, "mMidiDevice is not ready yet.");
                                try {
                                    closeDevice();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                connectToDevice();
                                tx.setText("USB MIDI Device is not found. " +
                                        "\nPlease check the connection. " +
                                        "\n" +
                                        "\nSomething that might give you a hint... " +
                                        "\n・Is the Android USB connection usage setting set to MIDI? " +
                                        "\n・Depending on your Android model, you may need to check the OTG settings. " +
                                        "\n・Is the USB cable capable of transmitting data? " +
                                        "\n・Is the power supply sufficient? " +
                                        "\n・Are the host and device orientations of the USB connection correct? " +
                                        "\n・Are each cable or cable outlet broken? " +
                                        "\n・If it is not recognized even after you insert it, wait about 10 seconds and then try reinserting it.");
                            }
                        } else {
                            Log.e(TAG, "mMidiDevice is not ready yet.");
                            try {
                                closeDevice();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            connectToDevice();
                            tx.setText("USB MIDI Device is not found. " +
                                    "\nPlease check the connection. " +
                                    "\n" +
                                    "\nSomething that might give you a hint... " +
                                    "\n・Is the Android USB connection usage setting set to MIDI? " +
                                    "\n・Depending on your Android model, you may need to check the OTG settings. " +
                                    "\n・Is the USB cable capable of transmitting data? " +
                                    "\n・Is the power supply sufficient? " +
                                    "\n・Are the host and device orientations of the USB connection correct? " +
                                    "\n・Are each cable or cable outlet broken? " +
                                    "\n・If it is not recognized even after you insert it, wait about 10 seconds and then try reinserting it.");
                        }
                        break;

                    case MotionEvent.ACTION_UP:
//                        tx.setText("押しました5。");
                        if (mMidiDevice != null) {
                            if (inputPort != null) {
                                if (spinner1Value.equals("Pitch Bend")) {
                                    sPitch0X();
                                }
                                if (spinner2Value.equals("Pitch Bend")) {
                                    sPitch0Y();
                                }
                                if (spinner1Value.length() >= 4 && spinner1Value.substring(0, 4).equals("Note")){
                                    sNoteOffX();
                                }
                                if (spinner2Value.length() >= 4 && spinner2Value.substring(0, 4).equals("Note")){
                                    sNoteOffY();
                                }
                                if (spinner1Value.length() >= 3 && spinner1Value.substring(0, 3).equals("CC#")){
                                    if (spinner5Value.equals("do nothing when released(CC)")) {
                                    }
                                    else{
                                        sCcUpX();
                                    }
                                }
                                if (spinner2Value.length() >= 3 && spinner2Value.substring(0, 3).equals("CC#")){
                                    if (spinner6Value.equals("do nothing when released(CC)")) {
                                    }
                                    else{
                                        sCcUpY();
                                    }
                                }
                                tx.setText("X: " + absoluteXIndex + "\nY: " + absoluteYIndex);
                            } else {
                                Log.e(TAG, "mMidiDevice is not ready yet.");
                                try {
                                    closeDevice();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                connectToDevice();
                            }
                        } else {
                            Log.e(TAG, "mMidiDevice is not ready yet.");
                            try {
                                closeDevice();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            connectToDevice();
                        }
                        break;

                    default:
//                        throw new IllegalStateException("Unexpected value: " + event.getAction());
                        break;
                }
                return true;
            }
        });
        // 必要な権限チェックとリクエスト
//        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
//            return;
//        }
    }

    protected void sPitchX() {
        int xIndex = (int) (lastX / v.getWidth() * 16384);
        if (xIndex > 0) {
            if (xIndex < 16384) {
                absoluteXIndex = xIndex -8192;
                temp1X = xIndex & 0x007F;
                temp2X = (xIndex >> 7) & 0x007F;
            } else {
                absoluteXIndex = 8191;
            }
        } else {
            absoluteXIndex = -8192;
        }
        byte[] messageX = new byte[]{(byte) mPChX(), (byte) temp1X, (byte) temp2X};
        try {
//                                inputPort = mMidiDevice.openInputPort(0);
            inputPort.send(messageX, 0, messageX.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sPitchY() {
//        int yIndex = (int) (lastY / v.getHeight() * 16384);
        int yIndex = (int) ((lastY / v.getHeight() * 16384));
        if (yIndex > 0) {
            if (yIndex < 16384) {
//                absoluteYIndex = 1 - yIndex + 8192;
//                temp1Y = (1 - yIndex) & 0x007F;
//                temp2Y = ((1 - yIndex) >> 7) & 0x007F;
                absoluteYIndex = 1 - yIndex + 8192;
                temp1Y = (1-yIndex) & 0x007F;
                temp2Y = ((1 - yIndex) >> 7) & 0x007F;
            } else {
                absoluteYIndex = -8192;
            }
        } else {
            absoluteYIndex = 8191;
        }
        byte[] messageY = new byte[]{(byte) mPChY(), (byte) temp1Y, (byte) temp2Y};
        try {
//                                inputPort = mMidiDevice.openInputPort(0);
            inputPort.send(messageY, 0, messageY.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sPitch0X() {
        absoluteXIndex = 0;
        temp1X = 0;
        temp2X = 64;
        byte[] messageX = new byte[]{(byte) mPChX(), (byte) temp1X, (byte) temp2X};
        try {
//                                inputPort = mMidiDevice.openInputPort(0);
            inputPort.send(messageX, 0, messageX.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sPitch0Y() {
        absoluteYIndex = 0;
        temp1Y = 0;
        temp2Y = 64;
        byte[] messageY = new byte[]{(byte) mPChY(), (byte) temp1Y, (byte) temp2Y};
        try {
//                                inputPort = mMidiDevice.openInputPort(0);
            inputPort.send(messageY, 0, messageY.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sNoteAllX() {
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 128);
        if (xIndex > 0) {
            if (xIndex < 128) {
                absoluteXIndex = Math.abs(xIndex - 128);
            } else {
                absoluteXIndex = 0;
            }
        } else {
            absoluteXIndex = 127;
        }
        if (temp1X != -1) {
            if (temp1X != absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2X == 0) {
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
            try {
                inputPort.send(messageX, 0, messageX.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1X = absoluteXIndex;
    }

    protected void sNoteAllY() {
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 128);
        if (yIndex > 0) {
            if (yIndex < 128) {
                absoluteYIndex = Math.abs(yIndex - 128);
            } else {
                absoluteYIndex = 0;
            }
        } else {
            absoluteYIndex = 127;
        }
        if (temp1Y != -1) {
            if (temp1Y != absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2Y == 0) {
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageY, 0, messageY.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sN25X() {
        String inputString = spinner1Value;
        // 文字列から数字を取り出す
        char digit1 = inputString.charAt(12);
        char digit2 = inputString.charAt(13);
        int oct = 0;
        if(digit2 <= 54) {
            oct = (Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2))) * 12;
        }
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 25);
        if (xIndex > 0 && xIndex < 25) {
            absoluteXIndex = Math.abs(xIndex - 25) +47;
        } else {
            absoluteXIndex = (xIndex <= 0) ? 72 : 48;
        }
        // 48〜72までの連番の数字を取得
        absoluteXIndex = getSequentialValue25(absoluteXIndex) + mKeyX();
        // oct分を増減
        if(digit2 <= 54) {
            absoluteXIndex = absoluteXIndex + oct;
        }
        if (temp1X != -1) {
            if (temp1X != absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2X == 0) {
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
            try {
                inputPort.send(messageX, 0, messageX.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1X = absoluteXIndex;
    }

    protected void sN25Y() {
        String inputString = spinner2Value;
        // 文字列から数字を取り出す
        char digit1 = inputString.charAt(12);
        char digit2 = inputString.charAt(13);
        int oct = 0;
        if(digit2 <= 54) {
            oct = (Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2))) * 12;
        }
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 25);
        if (yIndex > 0 && yIndex < 25) {
            absoluteYIndex = Math.abs(yIndex - 25) +47;
        } else {
            absoluteYIndex = (yIndex <= 0) ? 72 : 48;
        }
        // 48〜72までの連番の数字を取得
        absoluteYIndex = getSequentialValue25(absoluteYIndex) + mKeyY();
        // oct分を増減
        if(digit2 <= 54) {
            absoluteYIndex = absoluteYIndex + oct;
        }

        if (temp1Y != -1) {
            if (temp1Y != absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2Y == 0) {
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageY, 0, messageY.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sN49PentaX() {
        String inputString = spinner1Value;
        // 文字列から数字を取り出す
        char digit1 = inputString.charAt(30);
        char digit2 = inputString.charAt(31);
        int oct = 0;
        if(digit2 <= 54) {
            oct = (Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2))) * 12;
        }
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 49);
        if (xIndex > 0 && xIndex < 49) {
            absoluteXIndex = Math.abs(xIndex - 49) +36;
        } else {
            absoluteXIndex = (xIndex <= 0) ? 84 : 36;
        }
        // pentaの数字を取得
        absoluteXIndex = getSequentialValue49Penta(absoluteXIndex) + mKeyX();
        // oct分を増減
        if(digit2 <= 54) {
            absoluteXIndex = absoluteXIndex + oct;
        }
        if (temp1X != -1) {
            if (temp1X != absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2X == 0) {
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageX, 0, messageX.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1X = absoluteXIndex;
    }


    protected void sN49PentaY() {
        String inputString = spinner2Value;
        // 文字列から数字を取り出す
        char digit1 = inputString.charAt(30);
        char digit2 = inputString.charAt(31);
        int oct = 0;
        if(digit2 <= 54) {
            oct = (Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2))) * 12;
        }
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 49);
        if (yIndex > 0 && yIndex < 49) {
            absoluteYIndex = Math.abs(yIndex - 49) +36;
        } else {
            absoluteYIndex = (yIndex <= 0) ? 84 : 36;
        }
        // pentaの数字を取得
        absoluteYIndex = getSequentialValue49Penta(absoluteYIndex) + mKeyY();
        // oct分を増減
        if(digit2 <= 54) {
            absoluteYIndex = absoluteYIndex + oct;
        }
        if (temp1Y != -1) {
            if (temp1Y != absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2Y == 0) {
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageY, 0, messageY.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sN49WhiteX() {
        String inputString = spinner1Value;
        // 文字列から数字を取り出す
        char digit1 = inputString.charAt(22);
        char digit2 = inputString.charAt(23);
        int oct = 0;
        if(digit2 <= 54) {
            oct = (Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2))) * 12;
        }
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 49);
        if (xIndex > 0 && xIndex < 49) {
            absoluteXIndex = Math.abs(xIndex - 49) +36;
        } else {
            absoluteXIndex = (xIndex <= 0) ? 84 : 36;
        }
        // pentaの数字を取得
        absoluteXIndex = getSequentialValue49White(absoluteXIndex) + mKeyX();
        // oct分を増減
        if(digit2 <= 54) {
            absoluteXIndex = absoluteXIndex + oct;
        }
        if (temp1X != -1) {
            if (temp1X != absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2X == 0) {
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageX, 0, messageX.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1X = absoluteXIndex;
    }

    protected void sN49WhiteY() {
        String inputString = spinner2Value;
        // 文字列から数字を取り出す
        char digit1 = inputString.charAt(22);
        char digit2 = inputString.charAt(23);
        int oct = 0;
        if(digit2 <= 54) {
            oct = (Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2))) * 12;
        }
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 49);
        if (yIndex > 0 && yIndex < 49) {
            absoluteYIndex = Math.abs(yIndex - 49) +36;
        } else {
            absoluteYIndex = (yIndex <= 0) ? 84 : 36;
        }
        // pentaの数字を取得
        absoluteYIndex = getSequentialValue49White(absoluteYIndex) + mKeyY();
        // oct分を増減
        if(digit2 <= 54) {
            absoluteYIndex = absoluteYIndex + oct;
        }
        if (temp1Y != -1) {
            if (temp1Y != absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2Y == 0) {
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageY, 0, messageY.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sN88X() {
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 88);
        if (xIndex > 0 && xIndex < 88) {
            absoluteXIndex = Math.abs(xIndex - 88) +21;
        } else {
            absoluteXIndex = (xIndex <= 0) ? 108 : 21;
        }
        // 21〜108までの連番の数字を取得
        absoluteXIndex = getSequentialValue88(absoluteXIndex) + mKeyX();

        if (temp1X != -1) {
            if (temp1X != absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2X == 0) {
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
            try {
                inputPort.send(messageX, 0, messageX.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1X = absoluteXIndex;
    }

    protected void sN88Y() {
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 88);
        if (yIndex > 0 && yIndex < 88) {
            absoluteYIndex = Math.abs(yIndex - 88) +21;
        } else {
            absoluteYIndex = (yIndex <= 0) ? 108 : 21;
        }
        // 21〜108までの連番の数字を取得
        absoluteYIndex = getSequentialValue88(absoluteYIndex) + mKeyY();

        if (temp1Y != -1) {
            if (temp1Y != absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2Y == 0) {
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageY, 0, messageY.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sN88PentaX() {
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 88);
        if (xIndex > 0 && xIndex < 88) {
            absoluteXIndex = Math.abs(xIndex - 88) +21;
        } else {
            absoluteXIndex = (xIndex <= 0) ? 108 : 21;
        }
        // pentaの数字を取得
        absoluteXIndex = getSequentialValue88Penta(absoluteXIndex) + mKeyX();
        if (temp1X != -1) {
            if (temp1X != absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2X == 0) {
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
            try {
                inputPort.send(messageX, 0, messageX.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1X = absoluteXIndex;
    }

    protected void sN88PentaY() {
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 88);
        if (yIndex > 0 && yIndex < 88) {
            absoluteYIndex = Math.abs(yIndex - 88) +21;
        } else {
            absoluteYIndex = (yIndex <= 0) ? 108 : 21;
        }
        // pentaの数字を取得
        absoluteYIndex = getSequentialValue88Penta(absoluteYIndex) + mKeyY();
        if (temp1Y != -1) {
            if (temp1Y != absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2Y == 0) {
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageY, 0, messageY.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sN88WhiteX() {
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 88);
        if (xIndex > 0 && xIndex < 88) {
            absoluteXIndex = Math.abs(xIndex - 88) +21;
        } else {
            absoluteXIndex = (xIndex <= 0) ? 108 : 21;
        }
        // 白鍵の数字を取得
        absoluteXIndex = getSequentialValue88White(absoluteXIndex) + mKeyX();
        if (temp1X != -1) {
            if (temp1X != absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2X == 0) {
                messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageX = new byte[]{(byte) mOnChX(), (byte) absoluteXIndex, (byte) mVeloX()};
            try {
                inputPort.send(messageX, 0, messageX.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1X = absoluteXIndex;
    }

    protected void sN88WhiteY() {
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 88);
        if (yIndex > 0 && yIndex < 88) {
            absoluteYIndex = Math.abs(yIndex - 88) +21;
        } else {
            absoluteYIndex = (yIndex <= 0) ? 108 : 21;
        }
        // 白鍵の数字を取得
        absoluteYIndex = getSequentialValue88White(absoluteYIndex) + mKeyY();
        if (temp1Y != -1) {
            if (temp1Y != absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if(temp2Y == 0) {
                messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
                try {
//                                inputPort = mMidiDevice.openInputPort(0);
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            messageY = new byte[]{(byte) mOnChY(), (byte) absoluteYIndex, (byte) mVeloY()};
            try {
//                                inputPort = mMidiDevice.openInputPort(0);
                inputPort.send(messageY, 0, messageY.length);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sNoteOffX() {
        byte[] messageX;
        if (temp1X != -1) {
            if (temp1X == absoluteXIndex) {
                messageX = new byte[]{(byte) mOffChX(), (byte) temp1X, (byte) 0};
                try {
                    inputPort.send(messageX, 0, messageX.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        temp1X = absoluteXIndex;
    }

    protected void sNoteOffY() {
        byte[] messageY;
        if (temp1Y != -1) {
            if (temp1Y == absoluteYIndex) {
                messageY = new byte[]{(byte) mOffChY(), (byte) temp1Y, (byte) 0};
                try {
                    inputPort.send(messageY, 0, messageY.length);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
                } catch (IOException e) {
//            throw new RuntimeException(e);
                    Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        temp1Y = absoluteYIndex;
    }

    protected void sCcX() {
        byte[] messageX;
        int xIndex = (int) ((1 - lastX / v.getWidth()) * 128);
        if (xIndex > 0) {
            if (xIndex < 128) {
                absoluteXIndex = Math.abs(xIndex - 128);
            } else {
                absoluteXIndex = 0;
            }
        } else {
            absoluteXIndex = 127;
        }
        messageX = new byte[]{(byte) mCcChX(), (byte) mCcNumX(), (byte) absoluteXIndex};
        try {
            inputPort.send(messageX, 0, messageX.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        temp1X = absoluteXIndex;
    }

    protected void sCcY() {
        byte[] messageY;
        int yIndex = (int) (lastY / v.getHeight() * 128);
        if (yIndex > 0) {
            if (yIndex < 128) {
                absoluteYIndex = Math.abs(yIndex - 128);
            } else {
                absoluteYIndex = 0;
            }
        } else {
            absoluteYIndex = 127;
        }
        messageY = new byte[]{(byte) mCcChY(), (byte) mCcNumY(), (byte) absoluteYIndex};
        try {
//                                inputPort = mMidiDevice.openInputPort(0);
            inputPort.send(messageY, 0, messageY.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        temp1Y = absoluteYIndex;
    }

    protected void sCcUpX() {
        byte[] messageX;
        absoluteXIndex = mCcUpX();
        messageX = new byte[]{(byte) mCcChX(), (byte) mCcNumX(), (byte) absoluteXIndex};
        try {
            inputPort.send(messageX, 0, messageX.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sCcUpY() {
        byte[] messageY;
        absoluteYIndex = mCcUpY();
        messageY = new byte[]{(byte) mCcChY(), (byte) mCcNumY(), (byte) absoluteYIndex};
        try {
            inputPort.send(messageY, 0, messageY.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sAllSoundOff() {
        byte[] messageX;
        messageX = new byte[]{(byte) mAllOffChX(), (byte) 120, (byte) 0};
        try {
            inputPort.send(messageX, 0, messageX.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        byte[] messageY;
        messageY = new byte[]{(byte) mAllOffChY(), (byte) 120, (byte) 0};
        try {
            inputPort.send(messageY, 0, messageY.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sResetAllControllers() {
        byte[] messageX;
        messageX = new byte[]{(byte) mAllOffChX(), (byte) 121, (byte) 0};
        try {
            inputPort.send(messageX, 0, messageX.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        byte[] messageY;
        messageY = new byte[]{(byte) mAllOffChY(), (byte) 121, (byte) 0};
        try {
            inputPort.send(messageY, 0, messageY.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void sAllNotesOff() {
        byte[] messageX;
        messageX = new byte[]{(byte) mAllOffChX(), (byte) 123, (byte) 0};
        try {
            inputPort.send(messageX, 0, messageX.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        byte[] messageY;
        messageY = new byte[]{(byte) mAllOffChY(), (byte) 123, (byte) 0};
        try {
            inputPort.send(messageY, 0, messageY.length);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to send Midi message: " + e.getMessage());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected int mPChX() {
        String inputString = spinner3Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 224 + ch;
        return onVal;
    }

    protected int mPChY() {
        String inputString = spinner4Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 224 + ch;
        return onVal;
    }

    protected int mOffChX() {
        String inputString = spinner3Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int offVal = 128 + ch;
        temp2X = 0;
        return offVal;
    }

    protected int mOnChX() {
        String inputString = spinner3Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 144 + ch;
        temp2X = 1;
        return onVal;
    }

    protected int mOffChY() {
        String inputString = spinner4Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int offVal = 128 + ch;
        temp2Y = 0;
        return offVal;
    }

    protected int mOnChY() {
        String inputString = spinner4Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 144 + ch;
        temp2Y = 1;
        return onVal;
    }

    private int getSequentialValue25(int x) {
        // 範囲を48から72までの連番の数字に分割する
        int minValue = 48;
        int maxValue = 72;
        int range = maxValue - minValue;

        // xを0から1の範囲に正規化する
        float normalizedX = (float) (x - minValue) / (maxValue - minValue);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        // 範囲内での連番の数字を返す
        return (int) (normalizedX * range) + minValue;
    }


    private int getSequentialValue49Penta(float x) {
        int[] rangeValues = {36, 38, 40, 43, 45, 48, 50, 52, 55, 57, 60, 62, 64, 67, 69, 72, 74, 76, 79, 81, 84};
        int numSteps = rangeValues.length;

        // xを0から1の範囲に正規化する
        float normalizedX = (x - 36) / (84 - 36);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        // 範囲内での不規則な値を返す
        int index = (int) (normalizedX * (numSteps - 1));
        return rangeValues[index];
    }

    private int getSequentialValue49White(float x) {
        int[] rangeValues = {36, 38, 40, 41, 43, 45, 47, 48, 50, 52, 53, 55, 57, 59, 60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84};
        int numSteps = rangeValues.length;

        // xを0から1の範囲に正規化する
        float normalizedX = (x - 36) / (84 - 36);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        // 範囲内での不規則な値を返す
        int index = (int) (normalizedX * (numSteps - 1));
        return rangeValues[index];
    }

    private int getSequentialValue88(int x) {
        // 範囲を21から108までの連番の数字に分割する
        int minValue = 21;
        int maxValue = 108;
        int range = maxValue - minValue;

        // xを0から1の範囲に正規化する
        float normalizedX = (float) (x - minValue) / (maxValue - minValue);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        // 範囲内での連番の数字を返す
        return (int) (normalizedX * range) + minValue;
    }

    private int getSequentialValue88Penta(float x) {
        int[] rangeValues = {21, 24, 26, 28, 31, 33, 36, 38, 40, 43, 45, 48, 50, 52, 55, 57, 60, 62, 64, 67, 69, 72, 74, 76, 79, 81, 84, 86, 88, 91, 93, 96, 98, 100, 103, 105, 108};
        int numSteps = rangeValues.length;

        // xを0から1の範囲に正規化する
        float normalizedX = (x - 21) / (108 - 21);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        // 範囲内での不規則な値を返す
        int index = (int) (normalizedX * (numSteps - 1));
        return rangeValues[index];
    }

    private int getSequentialValue88White(float x) {
        int[] rangeValues = {21, 23, 24, 26, 28, 29, 31, 33, 35, 36, 38, 40, 41, 43, 45, 47, 48, 50, 52, 53, 55, 57, 59, 60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84, 86, 88, 89, 91, 93, 95, 96, 98, 100, 101, 103, 105, 107, 108};
        int numSteps = rangeValues.length;

        // xを0から1の範囲に正規化する
        float normalizedX = (x - 21) / (108 - 21);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        // 範囲内での不規則な値を返す
        int index = (int) (normalizedX * (numSteps - 1));
        return rangeValues[index];
    }

    protected int mCcChX() {
        String inputString = spinner3Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 176 + ch;
        return onVal;
    }

    protected int mCcChY() {
        String inputString = spinner4Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 176 + ch;
        return onVal;
    }

    protected int mCcNumX() {
        String inputString = spinner1Value;
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
        char digit3 = inputString.charAt(5);
// 取り出した文字を数値に変換
        int num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        return num;
    }

    protected int mCcNumY() {
        String inputString = spinner2Value;
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
        char digit3 = inputString.charAt(5);
// 取り出した文字を数値に変換
        int num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        return num;
    }

    protected int mCcUpX() {
        String inputString = spinner5Value;
        char digit1 = inputString.charAt(0);
        char digit2 = inputString.charAt(1);
        char digit3 = inputString.charAt(2);
// 取り出した文字を数値に変換
        int num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        return num;
    }

    protected int mCcUpY() {
        String inputString = spinner6Value;
        char digit1 = inputString.charAt(0);
        char digit2 = inputString.charAt(1);
        char digit3 = inputString.charAt(2);
// 取り出した文字を数値に変換
        int num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        return num;
    }

    protected int mVeloX() {
        String inputString = spinner7Value;
        char digit1 = inputString.charAt(9);
        char digit2 = inputString.charAt(10);
        char digit3 = inputString.charAt(11);
// 取り出した文字を数値に変換
        int num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        return num;
    }

    protected int mVeloY() {
        String inputString = spinner8Value;
        char digit1 = inputString.charAt(9);
        char digit2 = inputString.charAt(10);
        char digit3 = inputString.charAt(11);
// 取り出した文字を数値に変換
        int num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        return num;
    }

    protected int mKeyX() {
        String inputString = spinner9Value;
        char digit1 = inputString.charAt(4);
        char digit2 = inputString.charAt(5);
        char digit3 = inputString.charAt(6);
        int num = 0;
        if(digit2 < 58) {
// 取り出した文字を数値に変換
            num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        }
        return num;
    }

    protected int mKeyY() {
        String inputString = spinner10Value;
        char digit1 = inputString.charAt(4);
        char digit2 = inputString.charAt(5);
        char digit3 = inputString.charAt(6);
        int num = 0;
        if(digit2 < 58) {
// 取り出した文字を数値に変換
            num = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2) + String.valueOf(digit3));
        }
        return num;
    }

    protected int mAllOffChX() {
        String inputString = spinner3Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 176 + ch;
        return onVal;
    }

    protected int mAllOffChY() {
        String inputString = spinner4Value;
// 文字列から4文字目と5文字目の数字を取り出す
        char digit1 = inputString.charAt(3);
        char digit2 = inputString.charAt(4);
// 取り出した文字を数値に変換
// -1して16進数として使用
        int ch = Integer.parseInt(String.valueOf(digit1) + String.valueOf(digit2)) -1;
        int onVal = 176 + ch;
        return onVal;
    }

    protected void connectToDevice() {
        if (mMidiManager == null) {
            return;
        }
        // MIDI デバイスへの接続を確立するコードをここに記述
        // 接続が成功したら、midiDevice と midiInputPort を初期化する
        // トランスポートMIDIデバイスを取得し、最初のデバイスを開く
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Set<MidiDeviceInfo> midiDeviceInfos = mMidiManager.getDevicesForTransport(TRANSPORT_MIDI_BYTE_STREAM);
                if (midiDeviceInfos.isEmpty()) {
                    Log.e(TAG, "No transport MIDI devices found");
                    Toast.makeText(this, "No transport MIDI devices found", Toast.LENGTH_SHORT).show();
                    try {
                        closeDevice();
                    } catch (IOException e) {
//            throw new RuntimeException(e);
                        Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                midiDeviceInfo = midiDeviceInfos.iterator().next();
                numInputs = midiDeviceInfo.getInputPortCount();
                numOutputs = midiDeviceInfo.getOutputPortCount();
//            numInputs--;
                latestPortNumber = 0;
                for (MidiDeviceInfo info : midiDeviceInfos) {
                    latestPortNumber += info.getInputPortCount();
                }
                latestPortNumber--;
                Bundle properties = midiDeviceInfo.getProperties();
                String manufacturer = properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER);
                MidiDeviceInfo.PortInfo[] portInfos = midiDeviceInfo.getPorts();
                String portName = portInfos[0].getName();
                if (portInfos[0].getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                    Toast.makeText(this, "Attempting to connect: " + manufacturer + portName, Toast.LENGTH_SHORT).show();
                }
            } else {
                MidiDeviceInfo[] midiDeviceInfos = mMidiManager.getDevices();
                if (midiDeviceInfos.length == 0) {
                    Log.e(TAG, "No transport MIDI devices found");
                    Toast.makeText(this, "No transport MIDI devices found", Toast.LENGTH_SHORT).show();
                    try {
                        closeDevice();
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to close the MIDI device: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                // 最初のデバイスを取得
                MidiDeviceInfo midiDeviceInfo = midiDeviceInfos[0];
                int numInputs = midiDeviceInfo.getInputPortCount();
                int numOutputs = midiDeviceInfo.getOutputPortCount();
                // 全ポートの数を計算
                int totalPorts = 0;
                for (MidiDeviceInfo info : midiDeviceInfos) {
                    totalPorts += info.getInputPortCount();
                }
                Bundle properties = midiDeviceInfo.getProperties();
                String manufacturer = properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER);
                MidiDeviceInfo.PortInfo[] portInfos = midiDeviceInfo.getPorts();
                String portName = portInfos[0].getName();
                // ポートの種類が入力の場合、接続を試みる
                if (portInfos[0].getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                    Toast.makeText(this, "Attempting to connect: " + manufacturer + " " + portName, Toast.LENGTH_SHORT).show();
                }
            }


        } catch (SecurityException e) {
            Log.e(TAG, "Failed to open MidiDevice: " + e.getMessage());
            return;
        }

        mMidiManager.openDevice(midiDeviceInfo, new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice device) {
                        if (device == null) {
                            Log.e(TAG, "could not open device " + midiDeviceInfo);
//                            connectToDevice();
                        } else {
//                            numInputs = midiDeviceInfo.getInputPortCount();
//                            if (midiDeviceCallback.closeFlg == false) {
//                                numInputs--;
//                            }
                            mMidiDevice = device;
//                            portIndex = midiDeviceInfo.getInputPortCount();
                            inputPort = mMidiDevice.openInputPort(latestPortNumber);
//                            openCount ++;
                        }
                    }
                },new Handler(Looper.getMainLooper())
        );
        tx.setText("Connected. Please touch.");
    }

    protected void closeDevice() throws IOException {
        // MIDI デバイスへの接続を閉じるコードをここに記述
        // 例:
        if (inputPort != null) {
            inputPort.close();
            inputPort = null;
        }
        if (mMidiDevice != null) {
            mMidiDevice.close();
            mMidiDevice = null;
        }
    }

    // 画面端からのスワイプを判定するメソッド
//    private boolean isEdgeSwipe(@NonNull MotionEvent event) {
//        float x = event.getX();
//        float y = event.getY();
//        View decorView = getWindow().getDecorView();
//        int width = decorView.getWidth();
//        int height = decorView.getHeight();
//        return (x < EDGE_SWIPE_THRESHOLD || x > width - EDGE_SWIPE_THRESHOLD ||
//                y < EDGE_SWIPE_THRESHOLD || y > height - EDGE_SWIPE_THRESHOLD);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        connectToDevice();

        // コールバックを登録する
        if (mMidiManager != null && midiDeviceCallback != null) {
//            mMidiManager.registerDeviceCallback(midiDeviceCallback, null);
//            This method was deprecated in API level 33. Use registerDeviceCallback(int, java.util.concurrent.Executor, android.media.midi.MidiManager.DeviceCallback) instead.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mMidiManager.registerDeviceCallback(TRANSPORT_MIDI_BYTE_STREAM, executor, midiDeviceCallback);
            }
            else{
                mMidiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
                    // コールバックのメソッドを実装する
                }, new Handler(Looper.getMainLooper()));
            }
        }
        // SharedPreferencesのインスタンスを取得
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spinner1Value = sharedPreferences.getString("selectedItem_" + R.id.spinner1, "");
        spinner2Value = sharedPreferences.getString("selectedItem_" + R.id.spinner2, "");
        spinner3Value = sharedPreferences.getString("selectedItem_" + R.id.spinner3, "");
        spinner4Value = sharedPreferences.getString("selectedItem_" + R.id.spinner4, "");
        spinner5Value = sharedPreferences.getString("selectedItem_" + R.id.spinner5, "");
        spinner6Value = sharedPreferences.getString("selectedItem_" + R.id.spinner6, "");
        spinner7Value = sharedPreferences.getString("selectedItem_" + R.id.spinner7, "");
        spinner8Value = sharedPreferences.getString("selectedItem_" + R.id.spinner8, "");
        spinner9Value = sharedPreferences.getString("selectedItem_" + R.id.spinner9, "");
        spinner10Value = sharedPreferences.getString("selectedItem_" + R.id.spinner10, "");
        spinner91Value = sharedPreferences.getString("selectedItem_" + R.id.spinner91, "");

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(FIRST_TIME_KEY, true);

        if (isFirstTime) {
            // 初回起動時の処理
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FIRST_TIME_KEY, false);
            SharedPreferences.Editor editor1 = sharedPreferences.edit();
            editor1.putString("selectedItem_" + R.id.spinner1, "Pitch Bend");
            editor1.putString("selectedItem_" + R.id.spinner2, "Note C Major Pentatonic of 49 +1oct.");
            editor1.putString("selectedItem_" + R.id.spinner3, "CH#01");
            editor1.putString("selectedItem_" + R.id.spinner4, "CH#01");
            editor1.putString("selectedItem_" + R.id.spinner5, "do nothing when released(CC)");
            editor1.putString("selectedItem_" + R.id.spinner6, "do nothing when released(CC)");
            editor1.putString("selectedItem_" + R.id.spinner7, "velocity 100(Note)");
            editor1.putString("selectedItem_" + R.id.spinner8, "velocity 100(Note)");
            editor1.putString("selectedItem_" + R.id.spinner9, "key default(Note)");
            editor1.putString("selectedItem_" + R.id.spinner10, "key default(Note)");
            editor1.putString("selectedItem_" + R.id.spinner91, "CC#120 All Sound Off");
            editor.apply();
            editor1.apply();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                // 設定画面を起動
            startActivity(intent);

        }
//        isSwipeEnabled = sharedPreferences.getBoolean("isSwitched_" + R.id.switch1, Boolean.parseBoolean(""));
//        setContentView(R.layout.activity_settings); // 設定画面のレイアウトファイルを指定 その他の初期化処理を行う
//        // Switchの状態を取得して、スワイプの有効/無効を設定
//        Switch switch1 = findViewById(R.id.switch1);
////        switch1 = findViewById(R.id.switch1);
////        boolean switchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
//        boolean switchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
//        switch1.setChecked(switchState);
//        setSwipeEnabled(!switchState); // Switchの状態に応じてスワイプを有効/無効にする
        // GestureDetectorCompatを初期化
//        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());
        // dispatchTouchEvent()メソッドを呼び出す
//        dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // コールバックを解除する
        if (mMidiManager != null && midiDeviceCallback != null) {
            mMidiManager.unregisterDeviceCallback(midiDeviceCallback);
        }
        try {
            closeDevice();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // コールバックを登録する
//        if (mMidiManager != null && midiDeviceCallback != null) {
//            mMidiManager.registerDeviceCallback(TRANSPORT_MIDI_BYTE_STREAM, executor, midiDeviceCallback);
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // コールバックを解除する
//        if (mMidiManager != null && midiDeviceCallback != null) {
//            mMidiManager.unregisterDeviceCallback(midiDeviceCallback);
//        }
//        try {
//            closeDevice();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Midiデバイスを閉じる
        if (mMidiDevice != null) {
            try {
                mMidiDevice.close();
            } catch (IOException e) {
//            throw new RuntimeException(e);
                Toast.makeText(this, "Failed to send Midi message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}