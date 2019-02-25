package com.xa.iot_car;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 1.进入界面横屏
 * 2.弹出打开蓝牙对话框（注册蓝牙广播）
 * 3.显示已经配对的蓝牙设备
 * 4.选择连接蓝牙设备
 * 5.数据通信
 * 6.断开连接
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{

    public Button mBtnGo,mBtnRight,mBtnLeft,mBtnBack,mBtnAddRun,mBtnDownRun,mBtnBuzzer,mBtnSteeringGear,mBtnHeadlight,mBtnReset;//舵机,车灯
    public TextView mTvBlueState;
    public TextView mTvTemp;//温度
    public TextView mTvHumidity;//湿度
    public TextView mTvLight;//光照
    public TextView mTvDistance;//距离
    public TextView mTvFlame;//火焰
    public TextView mTvCo;//二氧化碳
    public TextView mTvAlcohol;//酒精
    public TextView mTvInfrared;//红外
    public TextView mTvSmoke;
    public SeekBar mSbRun;

    //进入页面开始弹窗
    public AlertDialog mAlertDialog;//蓝牙设置对话框
    public AlertDialog.Builder mBuilder;

    public AlertDialog mBlueListAlertDialog;//选择蓝牙连接对话框

    //蓝牙
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket mBluetoothSocket;

    //请求Code
    private final static int ENABLE_BLU = 1;//打开蓝牙权限请求码

    private List<String> mBluMatchedList = new ArrayList<>();//配对过的蓝牙设备
    private List<String> mBluMatchedAddressList = new ArrayList<>();//配对过的蓝牙设备地址

    public String mBlueAddress;

    private myHandle mMyHandle = new myHandle();

    private class myHandle extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://返回的BluetoothSocket
                    mBluetoothSocket = (BluetoothSocket) msg.obj;
                    break;
                case 1://温度
                    mTvTemp.setText("温度："+(String)msg.obj);
                    break;
                case 2://湿度
                    mTvHumidity.setText("湿度："+(String)msg.obj);
                    break;
                case 3://烟雾
                    mTvSmoke.setText("烟雾："+(String)msg.obj);
                    break;
                case 4://酒精
                    mTvAlcohol.setText("酒精："+(String)msg.obj);
                    break;
                case 5://一氧化碳
                    mTvCo.setText("CO："+(String)msg.obj);
                    break;
                case 6://光照
                    mTvLight.setText("光照："+(String)msg.obj);
                    break;
                case 7://火焰
                    mTvFlame.setText("火焰："+(String)msg.obj);
                    break;
                case 8://超声波测距
                    break;
                case 9://红外
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册蓝牙状态广播
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        InitView();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            AlertView();//弹出蓝牙设置Alert
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null){
                return;
            }
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED://蓝牙的开启状态
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            mTvBlueState.setText("蓝牙：正在开启");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            mTvBlueState.setText("蓝牙：已开启");
                            BlueDeviceListView();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            mTvBlueState.setText("蓝牙：正在关闭");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            mTvBlueState.setText("蓝牙：已关闭");
                            break;
                    }
                    break;

                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED://蓝牙的连接状态
                    int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,1);
                    switch (connectionState){
                        case BluetoothAdapter.STATE_DISCONNECTED:
                            mTvBlueState.setText("蓝牙：已断开连接");
                            break;
                        case BluetoothAdapter.STATE_CONNECTING:
                            mTvBlueState.setText("蓝牙：正在连接");
                            break;
                        case BluetoothAdapter.STATE_CONNECTED:
                            mTvBlueState.setText("蓝牙：已连接");
                            break;
                        case BluetoothAdapter.STATE_DISCONNECTING:
                            mTvBlueState.setText("蓝牙：正在断开连接");
                            break;
                    }
                    break;
            }
        }
    };

    /**
     * 按键震动
     */
    public void onVibrator(){
        Vibrator vib = (Vibrator) MainActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(100);
    }

    /**
     * 控制小车运动
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.main_btn_go:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 01 3B");
                    onVibrator();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 05 3B");
                }
                break;
            case R.id.main_btn_back:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 02 3B");
                    onVibrator();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 05 3B");
                }
                break;
            case R.id.main_btn_left:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 03 3B");
                    onVibrator();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 05 3B");
                }
                break;
            case R.id.main_btn_right:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 04 3B");
                    onVibrator();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 05 3B");
                }
                break;
            case R.id.main_btn_buzzer:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 DA 01 3B");
                    onVibrator();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 DA 02 3B");
                }
                break;
        }
        return false;
    }

    /**
     * 进入界面弹出蓝牙设置对话框
     */
    private void AlertView(){
        mBuilder = new AlertDialog.Builder(this)
                .setTitle("蓝牙")
                .setMessage("请打开蓝牙")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mBluetoothAdapter == null){
                            Toast.makeText(MainActivity.this,"请检查蓝牙设备是否正常！",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        if (!mBluetoothAdapter.isEnabled()){
                            mBluetoothAdapter.enable();
                        }
                    }
                })
                .setNeutralButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        mAlertDialog = mBuilder.show();
    }

    /**
         * 弹出已经配对的蓝牙设备
         * 并选择与其通信
         */
        private void BlueDeviceListView(){
            //获取已连接配对的设备
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (!mBluMatchedAddressList.contains(device.getAddress())) {
                        mBluMatchedList.add(device.getName() + "\n" + device.getAddress());
                        mBluMatchedAddressList.add(device.getAddress());

                    }
                }
            }
        ListView listView = new ListView(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mBluMatchedList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBlueListAlertDialog.dismiss();
                mBlueAddress = mBluMatchedAddressList.get(position);
                new Thread(new BlueConnectionThread(mBluetoothAdapter,mBlueAddress,mMyHandle)).start();
            }
        });
        mBlueListAlertDialog = new AlertDialog.Builder(this)
                .setTitle("蓝牙已配对列表")
                .setView(listView)
                .show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitView() {
        mBtnGo = findViewById(R.id.main_btn_go);
        mBtnBack = findViewById(R.id.main_btn_back);
        mBtnLeft = findViewById(R.id.main_btn_left);
        mBtnRight = findViewById(R.id.main_btn_right);
        mBtnBuzzer = findViewById(R.id.main_btn_buzzer);
        mBtnHeadlight = findViewById(R.id.main_btn_headlight);
        mBtnSteeringGear = findViewById(R.id.main_btn_steering_gear);
        mBtnReset = findViewById(R.id.main_btn_reset);
        mBtnAddRun = findViewById(R.id.main_btn_add);
        mBtnDownRun = findViewById(R.id.main_btn_down);

        mBtnHeadlight.setVisibility(View.GONE);
        mBtnSteeringGear.setVisibility(View.GONE);
        mBtnAddRun.setVisibility(View.GONE);
        mBtnDownRun.setVisibility(View.GONE);

        mBtnReset.setOnClickListener(this);
        mBtnSteeringGear.setOnClickListener(this);
        mBtnHeadlight.setOnClickListener(this);
        mBtnBuzzer.setOnClickListener(this);
        mBtnDownRun.setOnClickListener(this);
        mBtnAddRun.setOnClickListener(this);

        mBtnGo.setOnTouchListener(this);
        mBtnBack.setOnTouchListener(this);
        mBtnLeft.setOnTouchListener(this);
        mBtnRight.setOnTouchListener(this);
        mBtnBuzzer.setOnTouchListener(this);

        mTvBlueState = findViewById(R.id.main_tv_blue_state);
        mTvTemp =findViewById(R.id.main_tv_temp);
        mTvHumidity = findViewById(R.id.main_tv_humidity);
        mTvInfrared = findViewById(R.id.main_tv_infrared);//红外
        mTvCo = findViewById(R.id.main_tv_co);
        mTvAlcohol = findViewById(R.id.main_tv_alcohol);
        mTvFlame = findViewById(R.id.main_tv_flame);
        mTvLight = findViewById(R.id.main_tv_light);
        mTvDistance = findViewById(R.id.main_tv_distance);//距离
        mTvSmoke = findViewById(R.id.main_tv_smoke);

        mSbRun = findViewById(R.id.main_sb_run);//速度显示条
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_btn_headlight:
                new BlueSendTack(mBluetoothSocket,this).execute("");
                break;
            case R.id.main_btn_steering_gear:
                new BlueSendTack(mBluetoothSocket,this).execute("");
                break;
            case R.id.main_btn_reset:
                if (mBluetoothSocket !=null) {
                    new BlueSendTack(mBluetoothSocket, this).execute("B3 D1 05 3B");
                }
                BlueDeviceListView();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.disable();
        }
    }
}
