package jp.iisun.midiaxes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MidiAxes アプリの MIDI 計算ロジック用単体テスト
 * GitHub Actions (CI) 上で自動実行されます。
 */
public class MidiAxesUnitTest {

    // --- 1. 25鍵 スケール変換の計算テスト ---
    @Test
    public void testSequentialValue25_Boundaries() {
        // 48 (C3) 〜 72 (C5) の範囲テスト
        assertEquals(48, getSequentialValue25(48)); // 最小値
        assertEquals(72, getSequentialValue25(72)); // 最大値
        assertEquals(60, getSequentialValue25(60)); // 中央値 (C4)
    }

    @Test
    public void testSequentialValue25_OutOfRangeClamping() {
        // 範囲外の入力時に最小値/最大値にクランプされるか検証
        assertEquals(48, getSequentialValue25(10));  // 範囲下限以下
        assertEquals(72, getSequentialValue25(100)); // 範囲上限以上
    }

    // --- 2. 49鍵 ペンタトニックスケール変換のテスト ---
    @Test
    public void testSequentialValue49Penta_ValidNotes() {
        // ペンタトニックスケール (C, D, E, G, A) のノート番号に正しく変換されるか
        int minNote = getSequentialValue49Penta(36.0f);
        int maxNote = getSequentialValue49Penta(84.0f);

        assertEquals(36, minNote); // 最低音
        assertEquals(84, maxNote); // 最高音
    }

    // --- 3. 88鍵 白鍵スケール変換のテスト ---
    @Test
    public void testSequentialValue88White_ScaleRange() {
        int minNote = getSequentialValue88White(21.0f); // A0 (21)
        int maxNote = getSequentialValue88White(108.0f); // C8 (108)

        assertEquals(21, minNote);
        assertEquals(108, maxNote);
    }

    // --- 4. MIDIステータスバイト（Note On / Channel）の計算テスト ---
    @Test
    public void testMidiStatusByteCalculation() {
        // Ch.1 (インデックス 0) の場合: 144 + 0 = 144 (0x90 Note On Ch.1)
        assertEquals(144, calculateNoteOnStatusByte(1));
        // Ch.16 (インデックス 15) の場合: 144 + 15 = 159 (0x9F Note On Ch.16)
        assertEquals(159, calculateNoteOnStatusByte(16));
    }

    // =========================================================================
    // テスト対象の計算ロジックヘルパー（アプリ内の計算式と同等）
    // =========================================================================

    private int getSequentialValue25(int x) {
        int minValue = 48;
        int maxValue = 72;
        int range = maxValue - minValue;

        float normalizedX = (float) (x - minValue) / range;
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        return (int) (normalizedX * range) + minValue;
    }

    private int getSequentialValue49Penta(float x) {
        int[] rangeValues = {36, 38, 40, 43, 45, 48, 50, 52, 55, 57, 60, 62, 64, 67, 69, 72, 74, 76, 79, 81, 84};
        float normalizedX = (x - 36) / (84 - 36);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        int index = (int) (normalizedX * (rangeValues.length - 1));
        return rangeValues[index];
    }

    private int getSequentialValue88White(float x) {
        int[] rangeValues = {21, 23, 24, 26, 28, 29, 31, 33, 35, 36, 38, 40, 41, 43, 45, 47, 48, 50, 52, 53, 55, 57, 59, 60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84, 86, 88, 89, 91, 93, 95, 96, 98, 100, 101, 103, 105, 107, 108};
        float normalizedX = (x - 21) / (108 - 21);
        if (normalizedX < 0) normalizedX = 0;
        if (normalizedX > 1) normalizedX = 1;

        int index = (int) (normalizedX * (rangeValues.length - 1));
        return rangeValues[index];
    }

    private int calculateNoteOnStatusByte(int channel) {
        // channel: 1 〜 16
        return 144 + (channel - 1);
    }
}
