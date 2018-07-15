package com.inventory;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huang
 */
public class SoundUtil {
    public static SoundPool sp;
    private static SparseIntArray suondMap;
    private static WeakReference<Context> mWeakReference;

    public static void initSoundPool(Context context) {
        mWeakReference = new WeakReference<>(context);
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        suondMap = new SparseIntArray(3);
        suondMap.put(1, sp.load(context, R.raw.msg, 1));
        suondMap.put(2, sp.load(context, R.raw.warning, 1));
    }

    public static void play(int sound, int number) {
        AudioManager am = (AudioManager) mWeakReference.get().getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        sp.play(
                suondMap.get(sound),
                audioCurrentVolume,
                audioCurrentVolume,
                1,
                number,
                1);
    }

    public static void pasue() {
        sp.stop(suondMap.get(1));
    }

}
