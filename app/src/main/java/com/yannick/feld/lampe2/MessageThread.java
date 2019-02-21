package com.yannick.feld.lampe2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

public class MessageThread extends Thread {
    private final static String TAG="MessageThread";
    private final static String MY_UUID ="00001101-0000-1000-8000-00805f9b34fb";
    private BluetoothSocket mSocket=null;
    private String mMessage;
    private IconChangeCallback callback;


    public MessageThread(BluetoothDevice device, String message, IconChangeCallback callback) {
        Log.d(TAG,"Trying to send message...");
        this.callback = callback;
        this.mMessage=message;
        try {
            UUID uuid = UUID.fromString(MY_UUID);
            mSocket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) throws IOException {
        Log.d(TAG,"Connection successful");
        if(callback != null){
            callback.toastCallBack("Sending");
        }
        OutputStream os=socket.getOutputStream();
        PrintStream sender = new PrintStream(os);
        sender.print(mMessage);
        Log.d(TAG,"Message sent");
        InputStream is=socket.getInputStream();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is));
        Log.d(TAG,"Received: "+ reader.readLine());
        if(callback != null){
            callback.toastCallBack("send");
            callback.callback(0);
        }
    }

    public void run() {
        try{
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            mSocket.connect();
            manageConnectedSocket(mSocket);
            mSocket.close();
        } catch (IOException e) {
            if(callback != null){
                callback.callback(1);
                callback.toastCallBack("Lamp not found");
            }
            e.printStackTrace();
        }
    }
}