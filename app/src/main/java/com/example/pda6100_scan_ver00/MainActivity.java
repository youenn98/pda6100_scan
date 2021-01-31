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
}