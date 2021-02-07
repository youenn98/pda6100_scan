package com.example.pda6100_scan_ver00;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.TabHost;
import android.widget.Toast;
import cn.pda.serialport.SerialPort;
import cn.pda.serialport.Tools;
import com.uhf.scanlable.UHfData;

public class MainActivity extends TabActivity {
    private String[] tableMenu = { "Scan", "18000-6C" };
    private Intent[] tableIntents;

    public static final String EXTRA_MODE = "mode";
    public static final String TABLE_18000 = "18000-6C";
    public static final String SET_TAG = "设置";
    public static final String WORK_TAG = "工作";
    private TabHost myTabHost;

    private int MESSAGE_SUCCESS = 0;
    private int MESSAGE_FAIL = 1;
    private String devport = "/dev/ttyMT1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        myTabHost = getTabHost();
        Intent intent0 = new Intent(this, Setting.class);
        Intent intent1 = new Intent(this, ScanGroup.class);
        intent1.putExtra(EXTRA_MODE, TABLE_18000);

        TabHost.TabSpec tabSpec0 = myTabHost.newTabSpec(SET_TAG)
                .setIndicator(SET_TAG).setContent(intent0);
        TabHost.TabSpec tabSpec1 = myTabHost.newTabSpec(WORK_TAG)
                .setIndicator(WORK_TAG).setContent(intent1);

        myTabHost.addTab(tabSpec0);
        myTabHost.addTab(tabSpec1);
        myTabHost.setCurrentTab(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    @Override
    protected void onStart() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    int result = UHfData.UHfGetData.OpenUHf(devport, 57600);
                    if (result == 0) {
                        Thread.sleep(200);
                        int i = UHfData.UHfGetData.GetUhfInfo();
                        Log.i("Huang, MainActivity", "GetUhfInfo: " + i);
                        if (i == 0){
                            mHandler.sendEmptyMessage(MESSAGE_SUCCESS);
                        } else {
                            mHandler.sendEmptyMessage(MESSAGE_FAIL);
                        }
                    } else {
                        mHandler.sendEmptyMessage(MESSAGE_FAIL);
                    }
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(MESSAGE_FAIL);
                }
            }
        }).start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        UHfData.UHfGetData.CloseUHf();
        super.onStop();
    }

    private ConnectHandler mHandler = new ConnectHandler(this);

    private static class ConnectHandler extends Handler {
        private WeakReference<MainActivity> mReference;
        private MainActivity mActivity;

        ConnectHandler(MainActivity activity) {
            mReference = new WeakReference<MainActivity>(activity);
            mActivity = mReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == mActivity.MESSAGE_SUCCESS) {
                Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.port_connect_success),
                        Toast.LENGTH_SHORT).show();
            } else if (msg.what == mActivity.MESSAGE_FAIL) {
                Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.port_connect_fail),
                        Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    }
}