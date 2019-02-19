package com.yannick.feld.lampe2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import android.os.Handler;

public class BLE {
    private BluetoothAdapter bluetoothAdapter;
    final BluetoothManager bluetoothManager;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner scanner;


    Activity activity;

    public BLE(Context context, Activity activity){

        this.activity = activity;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {

            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
    }

    private ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ParcelUuid[] uuids = result.getDevice().getUuids();
            String ids = uuids == null? "null" : uuids.toString();
            Log.d("BLE", ids);
            String name = result.getDevice().getName();
            if(name == null){
                name = "NoName";
            }
            Log.d("BleName", name);
        }

    };

    public boolean scan(){

        return scan(callback);
    }

    private boolean scan(ScanCallback callback){
        if(bluetoothAdapter == null){
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            return false;
        }
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        if( null != scanner){
            Handler handler = new Handler();
            handler.postDelayed(this::stopScan, SCAN_PERIOD);
            scanner.startScan(callback);
            return true;
        }else {
            return false;
        }
    }

    private void stopScan(){
        scanner.stopScan(callback);
    }

}
