package com.example.pda6100_scan_ver00;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.uhf.scanlable.UHfData;

import com.example.pda6100_scan_ver00.Utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cn.pda.serialport.Tools;

public class DataManage extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final String TAG = DataManage.class.getSimpleName();

    private int mode;
    private String passwd = "00000000";
    private  int max_word = 2;

    Spinner mSpinnerEpcs;
    int selectedEd = 3;
    int selectedWhenPause = 0;

    Spinner c_mem;
    EditText c_good;
    EditText c_good_code;
    EditText c_len;
    EditText c_ware;
    EditText c_specification;
    EditText c_unit;
    EditText c_aux_unit;
    EditText c_aux_unit1;
    EditText c_quan;
    EditText c_aux_quan;
    EditText c_aux_quan1;
    EditText c_lot;
    EditText c_whole_price;
    EditText c_price;
    EditText c_tax_rate;
    EditText c_tax;

    EditText content;
    Button buyButton;
    Button sellButton;
    Button wButton;
    Button rButton;
    private static final int CHECK_W_6B = 0;
    private static final int CHECK_R_6B = 1;
    private static final int CHECK_W_6C = 2;
    private static final int CHECK_R_6C = 3;
    private int mTidFlag;
    private int mEpcPosition;

    private String mSelectedEpc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View viewToLoad = LayoutInflater.from(this.getParent()).inflate(R.layout.rw_6c, null);
        this.setContentView(viewToLoad);
        initView();

        Utils.initSoundPool(this);

        LogUtils.e(TAG, "onCreate: ");
    }

    @Override
    protected void onResume() {

        mTidFlag = Scan.mTidFlag;
        mEpcPosition = Scan.mEpcPosition;
        if (mTidFlag == 1) {
            mCheckBoxBroad.setChecked(true);
        }
        mEpcItems.clear();
        if (mTidFlag != 1) {
            // If there is no epc, there is no options
            MyApplication myApplication = (MyApplication) getApplication();
            ArrayList<String> tagList = myApplication.getsEpcStrList();
//				List<InventoryTagMap> lsTagList = UHfData.lsTagList;
            mEpcItems.addAll(tagList);
        }
        mEpcSpinnerAdapter.notifyDataSetChanged();
        mSpinnerEpcs.setSelection(mEpcPosition);
//		if (mEpcPosition != 0) {
        if (mEpcItems.size() < 1) {
            mCheckBoxBroad.setChecked(true);
            return;
        }
        mSelectedEpc = mEpcItems.get(mEpcPosition);
//		}
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        selectedWhenPause = selectedEd;
    }

    @Override
    protected void onDestroy() {
        LogUtils.e(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private void initView() {
        mSpinnerEpcs = (Spinner) findViewById(R.id.epc0);
        mEpcItems = new ArrayList<String>();
        mEpcSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mEpcItems);
        mEpcSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerEpcs.setAdapter(mEpcSpinnerAdapter);
        mSpinnerEpcs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.e(TAG, "onItemSelected, position >>>>>> " + position);
                mSelectedEpc = mEpcItems.get(position);
                LogUtils.e(TAG, "onItemSelected, selectedEpc >>>>>> " + mSelectedEpc);
            }
        });


        c_mem = (Spinner) findViewById(R.id.mem_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.men_select,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        c_mem.setAdapter(adapter);
        c_mem.setSelection(3, true);
        c_mem.setOnItemSelectedListener(this);

        c_good = (EditText) findViewById(R.id.et_good);
        c_good.setText("\n");
        c_good_code = (EditText) findViewById(R.id.et_good_code);
        c_good.setText("\n");
        c_ware = (EditText) findViewById(R.id.et_ware);
        c_ware.setText("\n");
        c_specification = (EditText) findViewById(R.id.et_specification);
        c_specification.setText("\n");
        c_unit = (EditText) findViewById(R.id.et_unit);
        c_unit.setText("\n");
        c_aux_unit = (EditText) findViewById(R.id.et_aux_unit);
        c_aux_unit.setText("\n");
        c_aux_unit1 = (EditText) findViewById(R.id.et_aux_unit1);
        c_aux_unit.setText("\n");
        c_quan = (EditText) findViewById(R.id.et_quan);
        c_quan.setText("\n");
        c_aux_quan = (EditText) findViewById(R.id.et_aux_quan);
        c_aux_quan.setText("\n");
        c_aux_quan1 = (EditText) findViewById(R.id.et_aux_quan1);
        c_aux_quan1.setText("\n");
        c_lot = (EditText) findViewById(R.id.et_lot);
        c_lot.setText("\n");
        c_price = (EditText) findViewById(R.id.et_price);
        c_price.setText("\n");
        c_whole_price = (EditText)findViewById(R.id.et_whole_price);
        c_whole_price.setText("\n");
        c_tax_rate = (EditText)findViewById(R.id.et_tax_rate);
        c_tax_rate.setText("\n");
        c_tax = (EditText)findViewById(R.id.et_tax);
        c_tax.setText("\n");



        buyButton = (Button) findViewById(R.id.button_write_buy);
        sellButton = (Button) findViewById(R.id.button_write_sell_6c);
        wButton = (Button) findViewById(R.id.button_write_6c);
        rButton = (Button) findViewById(R.id.button_read_6c);

        buyButton.setOnClickListener(this);
        wButton.setOnClickListener(this);
        rButton.setOnClickListener(this);
        sellButton.setOnClickListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Scan.class);
        Window w = ((ActivityGroup) getParent()).getLocalActivityManager().startActivity("FirstActivity", intent);
        View view = w.getDecorView();
        ((ActivityGroup) getParent()).setContentView(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View view) {
        if (view == wButton) {
            int result = -1;
            int index = 0;
            try{
                //c_good
                result = set_data(index,c_good);index += max_word;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_good_code);index += max_word;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_ware);index += max_word;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_specification);index += max_word;
                if(result != 0) {showToast("写数据失败");return;}
                /*
                result = set_data(index,c_unit);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_aux_unit);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_aux_unit1);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_quan);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_aux_quan);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_aux_quan1);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_lot);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_price);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_whole_price);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_tax_rate);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_tax);index += max_len;
                if(result != 0) {showToast("写数据失败");return;}
                */
            } catch (Exception e) {
                e.printStackTrace();
                showToast("写数据失败");
                return;
            }
            showToast("写数据成功");
        } else if (view == sellButton) {
           /* Log.i("zhouxin", "----onclick---- rButton");
            if (!checkContent(CHECK_R_6C))
                return;
            try {
                int wordPtr = Integer.valueOf(c_wordPtr.getText().toString());
                byte[] word = Tools.intToByte(wordPtr);
                int result = -1;
                if (mCheckBoxBroad.isChecked()) {
                    result = UHfData.UHfGetData.Read6C((byte) 0,
                            new byte[]{}, (byte) selectedEd, word,
                            Byte.valueOf(c_len.getText().toString()),
                            UHfData.UHfGetData.hexStringToBytes(c_pwd.getText().toString()));
                } else {
                    result = UHfData.UHfGetData.Read6C((byte) ((mSelectedEpc.length()) / 4),
                            UHfData.UHfGetData.hexStringToBytes(mSelectedEpc), (byte) selectedEd, word,
                            Byte.valueOf(c_len.getText().toString()),
                            UHfData.UHfGetData.hexStringToBytes(c_pwd.getText().toString()));
                }
                String temp = UHfData.UHfGetData
                        .bytesToHexString(UHfData.UHfGetData.getRead6Cdata(), 0, Byte.valueOf(c_len.getText().toString()) * 2)
                        .toUpperCase();
                if (result != 0) {
                    content.setText("");
                    LogUtils.e(TAG, "onClick, read Fail >>>>>> ");
                    showToast(getString(R.string.read_fail));
                } else {
                    content.setText(temp.toUpperCase());
                    showToast(getString(R.string.read_success));
                    Utils.play(1, 0);
                }
            } catch (Exception ex) {
            }*/
        } else if (view == buyButton) {
           /* if (!checkContent(CHECK_W_6C))
                return;
            try {
                int result = UHfData.UHfGetData.WriteEPC((byte) ((mSelectedEpc.length()) / 4),
                        UHfData.UHfGetData.hexStringToBytes(c_pwd.getText().toString()),
                        UHfData.UHfGetData.hexStringToBytes(mSelectedEpc),
                        UHfData.UHfGetData.hexStringToBytes(content.getText().toString()));
                if (result != 0) {
                    showToast(getString(R.string.write_epc_fail));
                }
                else {
                    showToast(getString(R.string.write_epc_success));
                }
            } catch (Exception ex) {
            }*/
        }else if(view == rButton){
            Log.i("yao", "----onclick---- rButton");
            int result = -1;
            int index = 0;
            String temp;
            try{
                //c_good
                result = get_data(index);index += max_word;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_good.setText(temp);

                result = get_data(index);index += max_word;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_good_code.setText(temp);

                result = get_data(index);index += max_word;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_ware.setText(temp);

                result = get_data(index);index += max_word;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_specification.setText(temp);
                /*
                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_unit.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_aux_unit.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_aux_unit1.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_quan.setText(temp);
                */
                /*
                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_aux_quan.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_aux_quan1.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_lot.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_price.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_whole_price.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_tax_rate.setText(temp);

                result = get_data(index);index += max_len;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_tax.setText(temp);
                */
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            showToast("读数据成功");

        }
    }

    private Toast mToast;

    private List<String> mEpcItems;

    private CheckBox mCheckBoxBroad;

    private ArrayAdapter<String> mEpcSpinnerAdapter;

    private void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(content);
        }
        mToast.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
        selectedEd = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private boolean checkContent(int check) {
        if (Utils.isEtEmpty(c_len))
            return Utils.showWarning(this, R.string.length_empty_warning);

        Log.i("Huang, ReadWrit", "check end");
        return true;
    }

    private  int get_data(int index){
        byte[] word = Tools.intToByte(index);
        return UHfData.UHfGetData.Read6C((byte) ((mSelectedEpc.length()) / 4),
                UHfData.UHfGetData.hexStringToBytes(mSelectedEpc), (byte) selectedEd, word,
                (byte) max_word,
                UHfData.UHfGetData.hexStringToBytes(passwd));
    }

    private int set_data(int index,EditText w_content) throws UnsupportedEncodingException {
        byte[] word = Tools.intToByte(index);
        int result;
        result = UHfData.UHfGetData.Write6c((byte) max_word,
                (byte) (mSelectedEpc.length() / 4),
                UHfData.UHfGetData.hexStringToBytes(mSelectedEpc), (byte) selectedEd, word,
                convertS_to_B(w_content.getText().toString()),
                UHfData.UHfGetData.hexStringToBytes(passwd));
        return result;
    }


    public byte[] convertS_to_B(String str) throws UnsupportedEncodingException {
        byte[]  byteArray = str.getBytes(StandardCharsets.US_ASCII);
        byte ret[] = new byte[max_word*2];

        for(int i = 0;i < max_word * 2;i++){
            if(i < byteArray.length){
                ret[i] = byteArray[i];
            }else{
                ret[i] = '\0';
            }
        }
        return ret;
    }
    public String convertB_to_S(byte[] bt) throws UnsupportedEncodingException {
        String str = new String(bt, StandardCharsets.US_ASCII);
        if(str.length() == 0) return "no data";
        int end_index = -1;
        for(int i = 0;i < max_word * 2;i++){
            if(str.charAt(i) == '\0'){
                end_index = i;
                break;
            }
        }
        if(end_index == 0) return "nodt";
        else if(end_index == -1) return str.substring(0,max_word*2);
        else return str.substring(0,end_index);
    }

}
