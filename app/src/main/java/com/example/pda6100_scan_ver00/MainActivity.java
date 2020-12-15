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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTabHost = getTabHost();
    }
}