package com.xa.iot_car;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.UUID;

public class BlueConnectionThread implements Runnable {

    private BluetoothSocket mBluetoothSocket = null;
    private Handler mHandler;

    /**
     * 初始化得到远处蓝牙设备
     * 与其连接
     * 开启接收数据线程
     * 并且返回BluetoothSocket给主线程用于发送数据
     * @param mBluetoothAdapter 本地蓝牙设备
     * @param blueAddress 蓝牙地址
     */

    BlueConnectionThread(BluetoothAdapter mBluetoothAdapter, String blueAddress, Handler handler) {
        this.mHandler = handler;
        BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(blueAddress);
        try {
            mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            mBluetoothSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new BlueReceiveRunnable(mBluetoothSocket, mHandler)).start();
        Message message = mHandler.obtainMessage();
        message.what = 0;
        message.obj = mBluetoothSocket;
        mHandler.sendMessage(message);
    }
}
