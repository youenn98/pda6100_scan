package com.example.pda6100_scan_ver00;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.uhf.scanlable.UHFLib;
import com.uhf.scanlable.UHfData;

public class Setting extends Activity implements View.OnClickListener {

    private TextView tvSoftVersion;
    private TextView tvVersion;
    private Spinner tvpowerdBm;
    private Spinner tvBeep;

    private Button bSetting;
    private Button bRead;

    private Handler mHandler;

    UHFLib uhf = null;

    private SoundPool soundpool = null;
    private int soundid;
    private int tty_speed = 57600;
    private byte addr = (byte) 0xff;
    private String[] strBand = new String[5];
    private String[] strmaxFrm = null;
    private String[] strminFrm = null;
    Spinner spBand;
    Spinner spmaxFrm;
    Spinner spminFrm;

    private ArrayAdapter<String> spada_Band;
    private ArrayAdapter<String> spada_maxFrm;
    private ArrayAdapter<String> spada_minFrm;
    private static final String TAG = "SacnView";

    private static final int MSG_SHOW_PROPERTIES = 0;
    private static final int UPDATE_SPINNER_BAND = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub properties
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        initView();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE_SPINNER_BAND:
                        int band = UHfData.UHfGetData.getUhfBand();
                        SetFreS(band);
                        if (band == 8) {
                            band = band - 4;
                        } else {
                            band = band - 1;
                        }
                        spBand.setSelection(band, false);
                        break;
                    case MSG_SHOW_PROPERTIES:
                        String Version = String
                                .valueOf(UHfData.UHfGetData.getUhfVersion()[0])
                                + "."
                                + String.valueOf(UHfData.UHfGetData.getUhfVersion()[1]);
                        showResult(Version, UHfData.UHfGetData.getUhfBand(),
                                UHfData.UHfGetData.getUhfMinFre()[0],
                                UHfData.UHfGetData.getUhfMaxFre()[0],
                                UHfData.UHfGetData.getUhfdBm()[0],
                                UHfData.UHfGetData.getUhfTime()[0],
                                UHfData.UHfGetData.getUhfBeepEn()[0],
                                UHfData.UHfGetData.getUhfAnt()[0]);
                        break;
                    default:
                        break;
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        onClick(bRead);
    }

    private void initView() {
        //设置版本
        tvSoftVersion = (TextView) findViewById(R.id.soft_version);
        PackageManager packageManager = this.getApplicationContext().getPackageManager();
        try {
            PackageInfo pInfo = packageManager.getPackageInfo(Setting.this.getPackageName(), 0);
            String versionName = pInfo.versionName;
            tvSoftVersion.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tvVersion = (TextView) findViewById(R.id.version);

        //设置功率
        tvpowerdBm = (Spinner) findViewById(R.id.power_spinner);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this, R.array.Power_select,
                android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tvpowerdBm.setAdapter(adapter3);
        tvpowerdBm.setSelection(30, true);

        //设置蜂鸣器
        tvBeep = (Spinner) findViewById(R.id.beep_spinner);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter
                .createFromResource(this, R.array.beep_select,
                        android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tvBeep.setAdapter(adapter4);
        tvBeep.setSelection(0, true);

        //关于频段的设定
        bSetting = (Button) findViewById(R.id.pro_setting);
        bRead = (Button) findViewById(R.id.pro_read);

        //有5个频段可供选择
        bSetting.setOnClickListener(this);
        bRead.setOnClickListener(this);
        strBand[0] = "Chinese band2";
        strBand[1] = "US band";
        strBand[2] = "Korean band";
        strBand[3] = "EU band";
        strBand[4] = "Chinese band1";

        spBand = (Spinner) findViewById(R.id.band_spinner);
        spada_Band = new ArrayAdapter<String>(Setting.this,
                android.R.layout.simple_spinner_item, strBand);
        spada_Band
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBand.setAdapter(spada_Band);
        spBand.setSelection(1, false);
//        SetFre(2);
        //设置频率
        spBand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.setVisibility(View.VISIBLE);
                if (position == 0) {
                    SetFre(1);
                }
                if (position == 1) {
                    SetFre(2);
                }
                if (position == 2) {
                    SetFre(3);
                }
                if (position == 3) {
                    SetFre(4);
                }
                if (position == 4) {
                    SetFre(8);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        //设置数据
        if (view == bSetting) {
            int MaxFre = 0;
            int MinFre = 0;
            int Power = 0;
            int fband = spBand.getSelectedItemPosition();
            int band = 0;
            if (fband == 0) {
                band = 1;
            }
            if (fband == 1) {
                band = 2;
            }
            if (fband == 2) {
                band = 3;
            }
            if (fband == 3) {
                band = 4;
            }
            if (fband == 4) {
                band = 8;
            }
            MinFre = spminFrm.getSelectedItemPosition();
            MaxFre = spmaxFrm.getSelectedItemPosition();
            Power = tvpowerdBm.getSelectedItemPosition();
//            int result = UHfGetData.SetUhfInfo((byte) 3, (byte) 31, (byte) -64, (byte) Power);
            int result = UHfData.UHfGetData.SetUhfInfo((byte) band, (byte) MaxFre, (byte) MinFre, (byte) Power);
            if (result == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.set_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.set_fail),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (view == bRead) {                                                //获得数据
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i = UHfData.UHfGetData.GetUhfInfo();
                    Log.i("Get setting", "GetUhfInfo: " + i);
                    mHandler.removeMessages(MSG_SHOW_PROPERTIES);
                    mHandler.sendEmptyMessage(UPDATE_SPINNER_BAND);
                    try {
                        Thread.sleep(120);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(MSG_SHOW_PROPERTIES);
                }
            }).start();

        }
    }

    private void showResult(String version, int band, int dminfre, int dmaxfre,
                            int powerdbm, int scanTime, int BeepEn, int AntInfo) {
        try {
            tvVersion.setText(version.toUpperCase());

            int frequent = ((dminfre & 0x3F) & 255);
            spminFrm.setSelection(frequent, false);
            int frequent2 = ((dmaxfre & 0x3F) & 255);
            spmaxFrm.setSelection(frequent2, false);

            tvpowerdBm.setSelection(powerdbm, true);
            tvBeep.setSelection(BeepEn, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void SetFre(int m) {
        if (m == 1) {
            strmaxFrm = new String[20];
            strminFrm = new String[20];
            for (int i = 0; i < 20; i++) {
                String temp = "";
                float values = (float) (920.125 + i * 0.25);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
            spmaxFrm.setSelection(19, false);
            spminFrm.setSelection(0, false);
        } else if (m == 2) {
            strmaxFrm = new String[50];
            strminFrm = new String[50];
            for (int i = 0; i < 50; i++) {
                String temp = "";
                float values = (float) (902.75 + i * 0.5);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
            spminFrm.setSelection(0, false);
            spmaxFrm.setSelection(49, false);
        } else if (m == 3) {
            strmaxFrm = new String[32];
            strminFrm = new String[32];
            for (int i = 0; i < 32; i++) {
                String temp = "";
                float values = (float) (917.1 + i * 0.2);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
            spmaxFrm.setSelection(31, false);
            spminFrm.setSelection(0, false);
        } else if (m == 4) {
            strmaxFrm = new String[15];
            strminFrm = new String[15];
            for (int i = 0; i < 15; i++) {
                String temp = "";
                float values = (float) (865.1 + i * 0.2);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
            spmaxFrm.setSelection(14, false);
            spminFrm.setSelection(0, false);
        } else if (m == 8) {
            strmaxFrm = new String[20];
            strminFrm = new String[20];
            for (int i = 0; i < 20; i++) {
                String temp = "";
                float values = (float) (840.125 + i * 0.25);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);
            spmaxFrm.setSelection(19, false);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
            spminFrm.setSelection(0, false);
        }
    }

    private void SetFreS(int m) {
        if (m == 1) {
            strmaxFrm = new String[20];
            strminFrm = new String[20];
            for (int i = 0; i < 20; i++) {
                String temp = "";
                float values = (float) (920.125 + i * 0.25);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
        } else if (m == 2) {
            strmaxFrm = new String[50];
            strminFrm = new String[50];
            for (int i = 0; i < 50; i++) {
                String temp = "";
                float values = (float) (902.75 + i * 0.5);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
        } else if (m == 3) {
            strmaxFrm = new String[32];
            strminFrm = new String[32];
            for (int i = 0; i < 32; i++) {
                String temp = "";
                float values = (float) (917.1 + i * 0.2);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
        } else if (m == 4) {
            strmaxFrm = new String[15];
            strminFrm = new String[15];
            for (int i = 0; i < 15; i++) {
                String temp = "";
                float values = (float) (865.1 + i * 0.2);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
        } else if (m == 8) {
            strmaxFrm = new String[20];
            strminFrm = new String[20];
            for (int i = 0; i < 20; i++) {
                String temp = "";
                float values = (float) (840.125 + i * 0.25);
                temp = String.valueOf(values) + "MHz";
                strminFrm[i] = temp;
                strmaxFrm[i] = temp;
            }
            spmaxFrm = (Spinner) findViewById(R.id.max_spinner);
            spada_maxFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strmaxFrm);
            spada_maxFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spmaxFrm.setAdapter(spada_maxFrm);

            spminFrm = (Spinner) findViewById(R.id.min_spinner);
            spada_minFrm = new ArrayAdapter<String>(Setting.this,
                    android.R.layout.simple_spinner_item, strminFrm);
            spada_minFrm
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spminFrm.setAdapter(spada_minFrm);
        }
    }


    private String getVersion(byte[] b) {
        if (b != null && b.length == 2)
            return b[0] + "." + b[1];
        return "";
    }

    private String getStr(byte[] b) {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }
        return sb.toString();
    }

}
