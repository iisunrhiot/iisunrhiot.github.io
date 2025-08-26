package jp.iisun.midiaxes;

import android.view.MotionEvent;

public class LastTouchedPointerTouchEvent {

    public static MotionEvent getLastTouchedPointerTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex(); // 最後に追加されたポインタのインデックスを取得

        // 最後に追加されたポインタの座標で新しい MotionEvent を作成して返す
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
                event.getActionMasked(), event.getX(pointerIndex), event.getY(pointerIndex), event.getMetaState());
    }
}
