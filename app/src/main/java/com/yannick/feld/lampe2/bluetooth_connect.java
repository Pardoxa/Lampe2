package com.yannick.feld.lampe2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

public class bluetooth_connect {
    private Context context;
    private final static String TAG = "Blue";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private IconChangeCallback callback;


    public void findRaspberry() {
        if(mBluetoothAdapter != null){
            if(callback != null)
                callback.callback(1);
            boolean found = false;
            String devName = context.getResources().getString(R.string.raspberry);
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {

                if(device.getName().equals(devName)){
                    if(callback != null)
                        callback.callback(1);
                    this.mDevice=device;
                    found = true;
                }

            }
            if(!found && callback != null){
                callback.callback(-1);
            }
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
            if (!mBluetoothAdapter.isEnabled())
            {

            }
            if(callback != null)
                callback.callback(-1);
        }
        else{
            Log.d(TAG, "Bluetooth enabled");
        }
    }


    public void onSend(String message, boolean with_toast) {
        if(mBluetoothAdapter != null && mDevice != null){
            new MessageThread(mDevice, message, with_toast, callback).start();
        }else{
            Toast.makeText(context, "Bluetooth disconnected", Toast.LENGTH_SHORT).show();
        }

    }


    public bluetooth_connect(Context context, IconChangeCallback callback) {
        this.context = context;
        this.callback = callback;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initBluetooth();
        findRaspberry();
        onSend("echo", false);

    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(callback != null){
                            callback.callback(-1);
                        }
                        mDevice = null;
                        mBluetoothAdapter = null;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        initBluetooth();
                        findRaspberry();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
}
