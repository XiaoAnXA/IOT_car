package com.xa.iot_car;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;


/**
 * 异步发送数据
 */
public class BlueSendTack extends AsyncTask<String,Void,Boolean> {

    private BluetoothSocket mBluetoothSocket;
    private Context mContext;
    private BufferedOutputStream mBufferedOutputStream;

    public BlueSendTack(BluetoothSocket socket, Context context){
        mBluetoothSocket = socket;
        mContext = context;
    }

    /**
     * 将十六进制的字符串转换成字节数组
     *
     * @param hexString
     * @return
     */

    public byte[] hexStrToBinaryStr(String hexString) {
        if (TextUtils.isEmpty(hexString)) {
            return null;
        }
        hexString = hexString.replaceAll(" ", "");
        int len = hexString.length();
        int index = 0;
        byte[] bytes = new byte[len / 2];
        while (index < len) {
            String sub = hexString.substring(index, index + 2);
            bytes[index/2] = (byte)Integer.parseInt(sub,16);
            index += 2;
        }
        return bytes;
    }
    /**
     * 发送十六进制数据包
     * @param strings
     * @return
     */
    @Override
    protected Boolean doInBackground(String... strings) {
        if (mBluetoothSocket == null){
            return false;
        }
        try {
            mBufferedOutputStream = new BufferedOutputStream(mBluetoothSocket.getOutputStream());
            byte[] b = hexStrToBinaryStr(strings[0]);
            mBufferedOutputStream.write(b);
            mBufferedOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (!aBoolean) {
            Toast.makeText(mContext, "发送失败", Toast.LENGTH_SHORT).show();
        }
    }
}
