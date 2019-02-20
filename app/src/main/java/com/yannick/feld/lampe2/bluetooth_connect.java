package com.yannick.feld.lampe2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Set;

public class bluetooth_connect {
    Context context;
    Activity activity;
    private final static String TAG = "BLE";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;


    private void findRaspberry() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if(device.getName().equals("unit2"))
                this.mDevice=device;
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
        }
        else{
            Log.d(TAG, "Bluetooth enabled");
        }
    }


    public void onSend(String message) {

        new MessageThread(mDevice, message).start();
    }


    public bluetooth_connect(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initBluetooth();
        findRaspberry();

    }
}
