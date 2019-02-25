package com.xa.iot_car;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * 接收数据线程
 */
public class BlueReceiveRunnable implements Runnable {

    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private StringBuilder mStringBuilder;

    public Handler mHandler;

    public BlueReceiveRunnable(BluetoothSocket bluetoothSocket,Handler handler){
        mBluetoothSocket = bluetoothSocket;
        mHandler = handler;
        mStringBuilder = new StringBuilder();
    }

    @Override
    public void run() {
        try {
            mInputStream = mBluetoothSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            int len;
            //byte[] bytes = new byte[1024];
            StringBuilder buff = new StringBuilder();
            while (mBluetoothSocket != null) {

                len = mInputStream.read();
                String  data= Integer.toHexString(len);
                if (data.length()<=1){
                            buff.append("0");
                }

                if (data.equals("3b")){
                    buff.append(data);
                    handlerReceiveData(new String(buff));
                    buff.delete(0,buff.length());
                    continue;
                }
                buff.append(data);

//                len = mInputStream.read(bytes);
//                for (int i = 0;i < len;i++){
//                    Log.e("TAG", "run: "+len );
//                    String  data= Integer.toHexString(bytes[i]&0xff);
//                    if (data.length()<=1){
//                        buff.append("0");
//                    }
//                    buff.append(data);
//                }
//                Log.e("TAG", "run:开始接收的数据：---> "+new String(buff));
//
//                int address = buff.indexOf("b3");//包头的位置
//                int addressBack = buff.indexOf("3b");//包尾的位置
////                if (buff.)
////                handlerReceiveData()
//                buff.delete(0,buff.length());

//                while ((len = mInputStream.read(bytes, 0, bytes.length)) != -1) {
//                    for (int i = 0;i < len;i++){
//                        String  data= Integer.toHexString(bytes[i]&0xff);
//                        if (data.length()<=1){
//                            buff.append("0");
//                        }
//                        buff.append(data);
//                    }
//                    Log.e("TAG", "run:开始接收的数据：---> "+new String(buff));
//                    buff.delete(0,buff.length());
//                }
            }
            destroy();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                mInputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 处理接收的十六进制数据
     * 解析传过来的数据包
     * @param string
     */
    public String handlerReceiveData(String string){
        Log.e("TAG", "handlerReceiveData: "+string);
        if (string.substring(0,2).equals("b3") && string.endsWith("3b")){//包头包尾监测
            switch (string.substring(2,4)){
                case "d2"://温湿度
                    mStringBuilder.append(string.substring(7,8))
                            .append(string.substring(9,10));
                    sendReceiveData(new String(mStringBuilder),1);
                    mStringBuilder.delete(0,mStringBuilder.length());
                    mStringBuilder.append(string.substring(11,12))
                            .append(string.substring(13,14));
                    sendReceiveData(new String(mStringBuilder),2);
                    break;
                case "d3"://烟雾
                    mStringBuilder.append(string.substring(7,8).equals("0")?"":string.substring(7,8))
                            .append(string.substring(9,10).equals("0")?"":string.substring(9,10))
                            .append(string.substring(11,12));
                    sendReceiveData(new String(mStringBuilder),3);
                    break;
                case "d4"://酒精
                    mStringBuilder.append(string.substring(7,8).equals("0")?"":string.substring(7,8))
                            .append(string.substring(9,10).equals("0")?"":string.substring(9,10))
                            .append(string.substring(11,12));
                    sendReceiveData(new String(mStringBuilder),4);

                    break;
                case "d5"://一氧化碳
                    mStringBuilder.append(string.substring(7,8).equals("0")?"":string.substring(7,8))
                            .append(string.substring(9,10).equals("0")?"":string.substring(9,10))
                            .append(string.substring(11,12));
                    sendReceiveData(new String(mStringBuilder),5);
                    break;
                case "d6"://光照
                    mStringBuilder.append(string.substring(7,8).equals("0")?"":string.substring(7,8))
                            .append(string.substring(9,10).equals("0")?"":string.substring(9,10))
                            .append(string.substring(11,12));
                    sendReceiveData(new String(mStringBuilder),6);
                    break;
                case "d7"://火焰
                    mStringBuilder.append(string.substring(7,8).equals("0")?"":string.substring(7,8))
                            .append(string.substring(9,10).equals("0")?"":string.substring(9,10))
                            .append(string.substring(11,12));
                    sendReceiveData(new String(mStringBuilder),7);
                    break;
                case "d8"://超声波测距
                    break;
                case "d9"://红外
                    break;
                case "da"://蜂鸣器
                    break;


            }
            mStringBuilder.delete(0,mStringBuilder.length());
        }


        return null;
    }

    /**
     * 把接收数据的发送给主线程处理
     * @param data
     * @param what
     */
    public void sendReceiveData(String data,int what){
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.obj = data;
        mHandler.sendMessage(message);
    }

    public void destroy(){

        if (mInputStream != null){
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mBluetoothSocket != null){
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
