package com.example.pda6100_scan_ver00;

import android.app.Application;

import java.util.ArrayList;

public class MyApplication extends Application {

    private ArrayList<String> sEpcStrList = new ArrayList<>();

    public ArrayList<String> getsEpcStrList() {
        return sEpcStrList;
    }

    public void setsEpcStrList(ArrayList<String> list) {
        sEpcStrList.clear();
        sEpcStrList.addAll(list);
    }
}
