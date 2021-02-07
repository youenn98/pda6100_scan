package com.example.pda6100_scan_ver00;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pda6100_scan_ver00.LogUtils;
import com.example.pda6100_scan_ver00.SoundPoolMgr;
import com.uhf.scanlable.UHfData;

public class Scan extends Activity implements OnClickListener, OnItemClickListener, OnItemSelectedListener {

    private final String TAG = Scan.class.getSimpleName();

    private String mode;
    private static List<UHfData.InventoryTagMap> data;

    Button scan;
    ListView listView;
    TextView txNum;
    CheckBox cb;
    private Timer timer;
    private MyAdapter myAdapter;
    private Handler mHandler;
    private boolean isCanceled = true;
    Spinner s_mem;
    Spinner querySpinner;
    private static final int SCAN_INTERVAL = 5;

    private static final int MSG_UPDATE_LISTVIEW = 0;
    private static final int MODE_18000 = 1;
    private static boolean Scanflag = false;
    int selectedEd = 0;
    int TidFlag = 0;
    int AntIndex = 0;
    public static int mTidFlag;
    public static int mEpcPosition;

    /**
     * key receiver
     */
    private long startTime = 0;
    private boolean keyUpFalg = true;
    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            // H941
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyUpFalg && keyDown && System.currentTimeMillis() - startTime > 500) {
                keyUpFalg = false;
                startTime = System.currentTimeMillis();
                if (keyCode == KeyEvent.KEYCODE_F3) {
                    onClick(scan);
                }
                return;
            } else if (keyDown) {
                startTime = System.currentTimeMillis();
            } else {
                keyUpFalg = true;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // new UHfData(this);
        mSoundPoolMgr = SoundPoolMgr.getInstance(this);
        View viewToLoad = LayoutInflater.from(this.getParent()).inflate(R.layout.query, null);
        this.setContentView(viewToLoad);

        try {
            Button btnGoRw = (Button) findViewById(R.id.button_goto_rw);
            btnGoRw.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Scan.this, DataManage.class);
                    mTidFlag = querySpinner.getSelectedItemPosition();
                    intent.putExtra("tidFlag", querySpinner.getSelectedItemPosition());
                    goActivity(intent);
                }
            });
            scan = (Button) findViewById(R.id.button_scan);
            scan.setOnClickListener(this);

            listView = (ListView) findViewById(R.id.tag_real_list_view);
            listView.setOnItemClickListener(this);
            data = new ArrayList<UHfData.InventoryTagMap>();
            txNum = (TextView) findViewById(R.id.tx_num);

            s_mem = (Spinner) findViewById(R.id.mem_s);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.men_s,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s_mem.setAdapter(adapter);
            s_mem.setSelection(0, true);
            s_mem.setOnItemSelectedListener(this);

            querySpinner = (Spinner)findViewById(R.id.query_spinner);

            cb = (CheckBox) this.findViewById(R.id.checktid);
            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    if (arg1)
                        s_mem.setSelection(1, true);
                }
            });

            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (isCanceled)
                        return;
                    switch (msg.what) {
                        case MSG_UPDATE_LISTVIEW:
                            data = UHfData.lsTagList;
                            if (myAdapter == null) {
                                myAdapter = new MyAdapter(Scan.this, new ArrayList(data));
                                listView.setAdapter(myAdapter);
                            } else {
                                myAdapter.mList = new ArrayList(data);
                            }
                            txNum.setText(String.valueOf(myAdapter.getCount()));
                            myAdapter.notifyDataSetChanged();
                            if (UHfData.mIsNew) {
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // if (!Util.soundfinished)
                                        // Util.play(1, 0);
                                        if (data.size() > 0) {
                                            mSoundPoolMgr.play(1);
                                            UHfData.mIsNew = false;
                                        }
                                    }
                                }).start();
                            }
                            break;
                        default:
                            break;
                    }
                    super.handleMessage(msg);
                }

            };
        } catch (Exception e) {
            Log.e(TAG, "onCreate exception >>>>>> " + e.getStackTrace());
        }
        Utils.initSoundPool(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        txNum.setText("0");
        UHfData.lsTagList.clear();
        UHfData.dtIndexMap.clear();
        if (myAdapter != null) {
            myAdapter.mList.clear();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        registerReceiver(keyReceiver, filter);
        LogUtils.e(TAG, "onResume: ");
    }

    int AntNum = 0;

    int count = 0;

    private SoundPoolMgr mSoundPoolMgr;

    @Override
    public void onClick(View arg0) {
        try {
            if (timer == null) {
                if (myAdapter != null) {
                    txNum.setText("0");
                    UHfData.lsTagList.clear();
                    UHfData.dtIndexMap.clear();
                    myAdapter.mList.clear();
                    myAdapter.notifyDataSetChanged();
//					mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
//					mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                }
                selectedEd = s_mem.getSelectedItemPosition();
                if (cb.isChecked())
                    TidFlag = 1;
                else
                    TidFlag = 0;
                if (selectedEd == 2)
                    selectedEd = 255;
                AntIndex = 0;
                isCanceled = false;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (Scanflag)
                            return;
                        Scanflag = true;
                        int position = querySpinner.getSelectedItemPosition();
                        if (position == 0) {
                            // Inventory EPC of TAG
                            UHfData.Inventory_6c(selectedEd, 0);
                        } else if (position == 1) {
                            // Inventory TID of TAG
                            UHfData.Inventory_6c(selectedEd, 1);
                        }

                        // UHfData.Inventory_6c_Mask((byte)0, 16, 0,
                        // UHfGetData.hexStringToBytes("E200"));
//						mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
                        mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                        Scanflag = false;
                    }
                }, 0, SCAN_INTERVAL);
                scan.setText(R.string.stop);
            } else {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                    scan.setText(R.string.scan);
                }
                UHfData.mIsNew = false;
                isCanceled = true;
            }
        } catch (Exception e) {
        }
    }

    private void cancelScan() {
        isCanceled = true;
        mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
        if (timer != null) {
            timer.cancel();
            timer = null;
            scan.setText(R.string.scan);
            UHfData.lsTagList.clear();
            UHfData.dtIndexMap.clear();
            if (myAdapter != null) {
                myAdapter.mList.clear();
                myAdapter.notifyDataSetChanged();
            }
            txNum.setText("0");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        String id = myAdapter.mList.get(position).strEPC;
        Intent intent = new Intent(this, DataManage.class);
        intent.putExtra(MainActivity.EXTRA_MODE, mode);
        intent.putExtra("POSITION", position);
        intent.putExtra("tidFlag", querySpinner.getSelectedItemPosition());
        mTidFlag = querySpinner.getSelectedItemPosition();
        mEpcPosition = position;
        // intent.putExtra(MainHFActivity.EXTRA_EPC, myAdapter.mList.get(position));
        UHfData.setuhf_id(myAdapter.mList.get(position).strEPC);
        goActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelScan();
        unregisterReceiver(keyReceiver);
        LogUtils.e(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release sound resource
        mSoundPoolMgr.realease();
        mSoundPoolMgr = null;
        Log.i(TAG, "onDestroy release resource");
    }

    private void goActivity(Intent intent) {
        try {
            ArrayList<String> arrayList = new ArrayList<>();
            List<UHfData.InventoryTagMap> lsTagList = UHfData.lsTagList;
            for (UHfData.InventoryTagMap map : lsTagList) {
                arrayList.add(map.strEPC);
            }
            MyApplication myApplication = (MyApplication) getApplication();
            myApplication.setsEpcStrList(arrayList);
            Window w = ((ActivityGroup) getParent()).getLocalActivityManager().startActivity("SecondActivity", intent);
            View view = w.getDecorView();
            ((ActivityGroup) getParent()).setContentView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyAdapter extends BaseAdapter {

        private Context mContext;
        private List<UHfData.InventoryTagMap> mList;
        private LayoutInflater layoutInflater;

        public MyAdapter(Context context, List<UHfData.InventoryTagMap> list) {
            mContext = context;
            mList = list;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mList.get(position);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewParent) {
            ItemView iv = null;
            if (view == null) {
                iv = new ItemView();
                view = layoutInflater.inflate(R.layout.list, null);
                iv.tvId = (TextView) view.findViewById(R.id.id_text);
                iv.tvEpc = (TextView) view.findViewById(R.id.epc_text);
                iv.tvTime = (TextView) view.findViewById(R.id.times_text);
                iv.tvRssi = (TextView) view.findViewById(R.id.rssi_text);
                view.setTag(iv);
            } else {
                iv = (ItemView) view.getTag();
            }
            String epc = mList.get(position).strEPC;
            Integer findIndex;
            if (querySpinner.getSelectedItemPosition() == 1) {
                String tid = mList.get(position).strTID;
                findIndex = UHfData.dtIndexMap.get(tid);
            } else {
                findIndex = UHfData.dtIndexMap.get(epc);
            }
            if (findIndex != null) {
                iv.tvId.setText(String.valueOf(findIndex));
                if (querySpinner.getSelectedItemPosition() == 0) {
                    iv.tvEpc.setText("EPC:" + epc);
                } else if (querySpinner.getSelectedItemPosition() == 1) {
                    String tid = mList.get(position).strTID;
                    iv.tvEpc.setText("TID:" + tid);
                    LogUtils.e(TAG, "getView, tid >>>>>> " + tid);
                } else {
                    String tid = mList.get(position).strTID;
                    iv.tvEpc.setText("EPC:" + epc + "\n");
                    iv.tvEpc.append("TID:" + tid);
                }
                iv.tvTime.setText(String.valueOf(mList.get(position).nReadCount));
                iv.tvRssi.setText(mList.get(position).strRSSI);
            }

            return view;
        }

        public class ItemView {
            TextView tvId;
            TextView tvEpc;
            TextView tvTime;
            TextView tvRssi;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
        // TODO Auto-generated method stub
        if (arg0 == s_mem) {
            if (position == 2) {
                cb.setChecked(false);
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

}

