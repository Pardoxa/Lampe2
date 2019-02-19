package com.yannick.feld.lampe2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import android.os.Handler;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTING;
import static android.content.ContentValues.TAG;

public class BLE {
    private BluetoothAdapter bluetoothAdapter;
    final BluetoothManager bluetoothManager;
    private BluetoothGatt gatt;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner scanner;
    private BluetoothDevice device = null;
    private BluetoothGattCharacteristic characteristic = null;
    private String devName;
    private UUID serviceUUID, characteristicUUID;
    private Context context;


    Activity activity;

    public BLE(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {

            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
        devName = context.getString(R.string.lampName);
        serviceUUID = UUID.fromString(context.getString(R.string.primary_service_uuid));
        characteristicUUID = UUID.fromString(context.getString(R.string.c_uuid));
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

            }else if (name.equals(devName)){
                device = result.getDevice();
                scanner.stopScan(callback);
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
        if(device != null){
            registerGatt();
        }
    }

    private void registerGatt(){
        Log.d("BLE", "registerGatt");
        gatt = device.connectGatt(context, true, gattCallback);
    }

    private String toWrite;

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,
                                    String data) {
        if (bluetoothAdapter == null || gatt == null) {
            Log.w("BLE", "BluetoothAdapter not initialized");
            return;
        }

        Log.i("BLE", "characteristic " + characteristic.toString());
        try {
            Log.i("BLE", "data " + URLEncoder.encode(data, "utf-8"));
            final int maxSize = 20;
            if(data.length() > maxSize){
                toWrite = data.substring(maxSize);
                data = data.substring(0,maxSize);
            }else{
                toWrite = null;
            }
            Log.d("BLE DATA", data);

          //  characteristic.setValue(URLEncoder.encode(data.substring(0,10), "utf-8"));

            characteristic.setValue(data);

           // gatt.beginReliableWrite();
            // TODO
            gatt.writeCharacteristic(characteristic);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("BLE", "onConnectionStateChange");
            switch(newState){
                case STATE_CONNECTED:
                    Log.d("BLE", "discoverServices");
                    gatt.discoverServices();
                    break;
                case STATE_DISCONNECTING:
                    Log.d("BLE", "disconnecting");
                    break;
                case STATE_DISCONNECTED:
                    Log.d("BLE", "Disconnected");
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            try{
                Log.d("BLE", "onServiceDiscovered");
                Log.d("BLE", gatt.getServices().toString());
                characteristic = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
                writeCharacteristic(characteristic,"Nothing is more usefull than thinking that 1 or the other this is a test bla hahahaha");

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("BLE", "onCharacteristicWrite");
            if(status == BluetoothGatt.GATT_SUCCESS && toWrite != null){
                writeCharacteristic(characteristic, toWrite);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

}
