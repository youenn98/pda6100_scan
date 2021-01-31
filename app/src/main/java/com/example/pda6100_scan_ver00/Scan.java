package com.example.pda6100_scan_ver00;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.uhf.scanlable.UHfData;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Scan extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSoundPoolMgr = SoundPoolMgr.getInstance(this);
        setContentView(R.layout.query);
        try {
            Button btnGoRw = (Button) findViewById(R.id.button_goto_rw);
            btnGoRw.setOnClickListener(new View.OnClickListener() {

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
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
    }

    int AntNum = 0;

    int count = 0;

    private SoundPoolMgr mSoundPoolMgr;
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
            // TODO Auto-generated method stub
            ItemView iv = null;
            if (view == null) {
                iv = new ItemView();
                view = layoutInflater.inflate(R.layout.list, null);
                iv.tvId = (TextView) view.findViewById(R.id.id_text);
                iv.tvEpc = (TextView) view.findViewById(R.id.epc_text);
                // iv.tvTime = (TextView) view.findViewById(R.id.times_text);
                //iv.tvRssi = (TextView) view.findViewById(R.id.rssi_text);
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

}
