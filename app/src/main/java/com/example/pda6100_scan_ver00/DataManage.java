package com.example.pda6100_scan_ver00;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pda.serialport.Tools;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

public class DataManage extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final String TAG = DataManage.class.getSimpleName();

    private int mode;
    private String passwd = "00000000";
    private  int max_word = 2;
    private  int sell_excel = 0;
    private  int buy_excel = 1;


    Spinner mSpinnerEpcs;
    int selectedEd = 3;
    int selectedWhenPause = 0;

    Spinner c_mem;
    EditText c_good;
    EditText c_good_code;
    EditText c_ware;
    EditText c_unit;
    EditText c_quan;
    EditText c_lot;
    EditText c_price;
    EditText c_tax_rate;

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

        c_unit = (EditText) findViewById(R.id.et_unit);
        c_unit.setText("\n");
        c_quan = (EditText) findViewById(R.id.et_quan);
        c_quan.setText("\n");
        c_lot = (EditText) findViewById(R.id.et_lot);
        c_lot.setText("\n");
        c_price = (EditText) findViewById(R.id.et_price);
        c_price.setText("\n");



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

                result = set_data(index,c_quan);index += max_word;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_lot);index += max_word;
                if(result != 0) {showToast("写数据失败");return;}

                result = set_data(index,c_price);index += max_word;
                if(result != 0) {showToast("写数据失败");return;}


            } catch (Exception e) {
                e.printStackTrace();
                showToast("写数据失败");
                return;
            }
            showToast("写数据成功");
        } else if (view == sellButton) {
            Date date = new Date(); // 今日の日付
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strFile = dateFormat.format(date).concat("sell.xls");

            File root = new File(Environment.getExternalStoragePublicDirectory("Movies"), "Sell_Excel");
            if(!root.exists()){
                root.mkdirs();
            }

            File file = new File(root,strFile);
            if(!file.exists()){
                try {
                    file.createNewFile();
                    init_excel(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try{
                // 获取excel文件流
                InputStream is = new FileInputStream(file);
                write_excel(is,file.getAbsolutePath());

            }catch (FileNotFoundException e){
                e.printStackTrace();

            }catch (Exception ex) {
                ex.printStackTrace();
            }
            showToast("写销售表成功");
        } else if (view == buyButton) {
            try {
                Date date = new Date(); // 今日の日付
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String strFile = dateFormat.format(date).concat("buy.xls");
                File root = new File(Environment.getExternalStoragePublicDirectory("Movies"), "Buy_Excel");
                if(!root.exists()){
                    root.mkdirs();
                }

                File file = new File(root,strFile);
                if(!file.exists()){
                    try {
                        file.createNewFile();
                        init_excel(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try{
                    // 获取excel文件流
                    InputStream is = new FileInputStream(file);
                    write_excel(is,file.getAbsolutePath());

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            showToast("写采购表成功");
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
                c_unit.setText(temp);

                result = get_data(index);index += max_word;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_quan.setText(temp);

                result = get_data(index);index += max_word;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_lot.setText(temp);

                result = get_data(index);index += max_word;
                if(result != 0) {showToast("读数据失败");return;}
                temp = convertB_to_S(UHfData.UHfGetData.getRead6Cdata());
                c_price.setText(temp);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            showToast("读数据成功");

        }
    }

    private void write_excel(InputStream is,String filepath) throws IOException {
        int index = 0;
        try {
            // 获取workbook对象
            HSSFWorkbook workbook = new HSSFWorkbook(is);
            // 获取sheet对象
            Sheet sheet = workbook.getSheetAt(0);
            // 获取row对象
            Row HeadRow = sheet.getRow(0);
            // 获取cell对象
            Cell cell_cnt = HeadRow.getCell(8);
            int start_row = Integer.valueOf(cell_cnt.getStringCellValue());
            Row row = sheet.createRow(start_row);
            for(int i = 0; i < 8;i++){
                row.createCell(i);
            }

            //设置行号
            Cell cell_it = row.getCell(index++);
            cell_it.setCellValue(start_row);
            //good
            cell_it = row.getCell(index++);
            cell_it.setCellValue(c_good.getText().toString());
            //good code
            cell_it = row.getCell(index++);
            cell_it.setCellValue(c_good_code.getText().toString());
            //ware
            cell_it = row.getCell(index++);
            cell_it.setCellValue(c_ware.getText().toString());
            //Rest can be added same.
            //unit
            cell_it = row.getCell(index++);
            cell_it.setCellValue(c_unit.getText().toString());
            //lot
            cell_it = row.getCell(index++);
            cell_it.setCellValue(c_lot.getText().toString());
            //quantity
            cell_it = row.getCell(index++);
            cell_it.setCellValue(c_quan.getText().toString());
            //price
            cell_it = row.getCell(index++);
            cell_it.setCellValue(c_price.getText().toString());

            start_row++;
            cell_cnt.setCellValue(String.valueOf(start_row));
            //close the input stream
            is.close();
            OutputStream outputStream = new FileOutputStream(filepath);
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void init_excel(File file) throws IOException {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri);
        this.sendBroadcast(intent);
        int index = 0;
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("sheet0");
        Row row = sheet.createRow(0);;
        for(int i = 0;i < 9;i++){
            row.createCell(i);
        }
        Cell cell = row.getCell(index++);
        cell.setCellValue("LINE NO.");
        cell = row.getCell(index++);
        cell.setCellValue("STOCK NO.");
        cell = row.getCell(index++);
        cell.setCellValue("FULL NAME OF STOCK.");
        cell = row.getCell(index++);
        cell.setCellValue("WAREHOUSE");
        cell = row.getCell(index++);
        cell.setCellValue("UNIT");
        cell = row.getCell(index++);
        cell.setCellValue("LOT");
        cell = row.getCell(index++);
        cell.setCellValue("QUANTITY");
        cell = row.getCell(index++);
        cell.setCellValue("UNITPRICE");

        cell = row.getCell(8);
        cell.setCellValue("1");
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
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
