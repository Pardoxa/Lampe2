package com.yannick.feld.lampe2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.util.Set;

public class bluetooth_connect {
    Context context;
    Activity activity;
    private final static String TAG = "Blue";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private IconChangeCallback callback;


    public void findRaspberry() {
        if(callback != null)
            callback.callback(1);
        boolean found = false;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if(device.getName().equals("unit2")){
                if(callback != null)
                    callback.callback(0);
                this.mDevice=device;
                found = true;
            }

        }
        if(!found){
            callback.callback(-1);
        }
    }

    private void initBluetooth() {
        Log.d(TAG, "Checking Bluetooth...");
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Device does not support Bluetooth");

        } else{
            Log.d(TAG, "Bluetooth supported");
        }
        if (!mBluetoothAdapter.isEnabled()) {

            Log.d(TAG, "Bluetooth not enabled");
            if(callback != null)
                callback.callback(-1);
        }
        else{
            Log.d(TAG, "Bluetooth enabled");
        }
    }


    public void onSend(String message) {

        new MessageThread(mDevice, message).start();
    }


    public bluetooth_connect(Context context, Activity activity, IconChangeCallback callback) {
        this.context = context;
        this.activity = activity;
        this.callback = callback;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initBluetooth();
        findRaspberry();

    }
}
