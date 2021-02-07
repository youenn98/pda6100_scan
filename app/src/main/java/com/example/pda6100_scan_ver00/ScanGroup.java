package com.example.pda6100_scan_ver00;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class ScanGroup extends ActivityGroup {
    public ActivityGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        group = this;
    }

    @Override
    public void onBackPressed() {
        group.getLocalActivityManager().getCurrentActivity().onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, Scan.class);
        Window w = group.getLocalActivityManager().startActivity(
                "FirstActivity", intent);
        View view = w.getDecorView();
        intent.putExtra(MainActivity.EXTRA_MODE, getIntent().getStringExtra(MainActivity.EXTRA_MODE));
        group.setContentView(view);
    }
}
