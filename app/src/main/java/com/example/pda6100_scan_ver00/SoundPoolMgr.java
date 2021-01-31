package com.example.pda6100_scan_ver00;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

import java.lang.ref.WeakReference;

public class SoundPoolMgr {

    private static SoundPoolMgr INSTANCE;
    private WeakReference<Context> mWeakReference;
    private SoundPool mSoundPool;
    private SparseIntArray soundmap = new SparseIntArray();

    public static SoundPoolMgr getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SoundPoolMgr(context);
        }
        return INSTANCE;
    }

    private SoundPoolMgr(Context context) {
        mWeakReference = new WeakReference<Context>(context);
        SoundPool.Builder builder = new SoundPool.Builder();
        AudioAttributes.Builder attrBuild = new AudioAttributes.Builder();
        attrBuild.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        builder.setAudioAttributes(attrBuild.build());
        builder.setMaxStreams(4);
        mSoundPool = builder.build();
        load();
    }

    private void load() {
        soundmap.put(1, mSoundPool.load(mWeakReference.get(), R.raw.msg, 1));
    }

    /**
     * Play a sound from a sound ID.
     *
     * @return non-zero streamID if successful, zero if failed
     */
    public void play(int soundId) {
        int streamID = mSoundPool.play(soundmap.get(soundId), 1, 1, 0, 0, 1);
        android.util.Log.i("Huang, SoundPoolMgr", "play() return streamID: " + streamID);
    }

    public void realease() {
        mSoundPool.unload(soundmap.get(1));
        mSoundPool.release();
        mSoundPool = null;
        INSTANCE = null;
    }
}

