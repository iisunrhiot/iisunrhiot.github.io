package jp.iisun.midiaxes;

import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;

import java.io.IOException;

public class MyMidiDeviceCallback extends MidiManager.DeviceCallback {

    protected MainActivity mainActivity;
//    protected boolean closeFlg;

    // MainActivityのインスタンスを受け取るコンストラクタ
    public MyMidiDeviceCallback(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    @Override
    public void onDeviceAdded(MidiDeviceInfo device) {
        // MIDI デバイスが追加された場合に呼び出されます
        // ここで MIDI デバイスへの接続を試みます
        try {
            mainActivity.closeDevice();
//            closeFlg = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mainActivity.connectToDevice();
    }

    @Override
    public void onDeviceRemoved(MidiDeviceInfo device) {
        // MIDI デバイスが削除された場合に呼び出されます
        // ここで適切な処理を行います
        try {
            mainActivity.closeDevice();
//            closeFlg = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
