package com.example.pda6100_scan_ver00;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static boolean showWarning(Context context, int resRes) {
        Toast.makeText(context, resRes, Toast.LENGTH_LONG).show();
        return false;
    }

    public static boolean isEtEmpty(EditText editText) {
        String str = editText.getText().toString();
        return str == null || str.equals("");
    }

    public static boolean isLenLegal(EditText editText) {
        if (isEtEmpty(editText))
            return false;
        String str = editText.getText().toString();
        return str != null && str.length() % 2 == 0;
    }

    public static boolean isPwdLenLegal(EditText editText) {
        if (isEtEmpty(editText))
            return false;
        String str = editText.getText().toString();
        return str != null && str.length()  == 8;
    }

    public static boolean isEtsLegal(EditText[] ets) {
        for (EditText et : ets) {
            if (isLenLegal(et))
                return true;
        }
        return false;
    }




    public static SoundPool sp ;
    public static Map<Integer, Integer> suondMap;
    public static Context context;

    //初始化声音池
    public static void initSoundPool(Context context){
        Utils.context = context;
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        suondMap = new HashMap<Integer, Integer>();
        suondMap.put(1, sp.load(context, R.raw.msg0, 1));
    }

    public static boolean soundfinished;
    //播放声音池声音
    public static  void play(int sound, int number){
        soundfinished=true;
        AudioManager am = (AudioManager)Utils.context.getSystemService(Utils.context.AUDIO_SERVICE);
        //返回当前AlarmManager最大音量
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //返回当前AudioManager对象的音量值
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolume/audioMaxVolume;
        sp.play(
                suondMap.get(sound), //播放的音乐Id
                audioCurrentVolume, //左声道音量
                audioCurrentVolume, //右声道音量
                1, //优先级，0为最低
                number, //循环次数，0无不循环，-1无永远循环
                1);//回放速度，值在0.5-2.0之间，1为正常速度
        SystemClock.sleep(200);
        soundfinished=false;
    }

    //获取上一次保存的,电源用","隔开
    public static String getPowers(Context context){
        SharedPreferences shared = context.getSharedPreferences("config", Context.MODE_PRIVATE) ;
        return shared.getString("power", "rfid power")  ;
    }

    //保存上一次保存的
    public static void savePower(Context context, String powers){
        SharedPreferences shared = context.getSharedPreferences("config", Context.MODE_PRIVATE) ;
        SharedPreferences.Editor editor = shared.edit() ;
        editor.putString("power", powers) ;
        editor.commit() ;
    }
}
