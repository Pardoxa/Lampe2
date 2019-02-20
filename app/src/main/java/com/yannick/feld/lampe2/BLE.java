package com.yannick.feld.lampe2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class BLE {
    private UUID service_uuid, characteristic_uuid_1, characteristic_uuid_2, id_desc_1, id_desc_2;
    private Context context;
    private Activity activity;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    public final static int REQUEST_ENABLE_BT = 1;
    private AdvertiseSettings advertiseSettings;
    private AdvertiseData advertiseData;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer bluetoothGattServer;
    private ArrayList<BluetoothDevice> registeredDevices = new ArrayList<>();

    public BLE(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        service_uuid = UUID.fromString(context.getString(R.string.primary_service_uuid));
        characteristic_uuid_1 = UUID.fromString(context.getString(R.string.c_uuid_1));
        characteristic_uuid_2 = UUID.fromString(context.getString(R.string.c_uuid_2));
        id_desc_1 = UUID.fromString(context.getString(R.string.desc_1));
        id_desc_2 = UUID.fromString(context.getString(R.string.desc_2));
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {

            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }else{
            Log.d("BLE", "adapter not null");
        }

        advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        ParcelUuid id =  new ParcelUuid(service_uuid);
        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(id)
                .addServiceData( id, "Data".getBytes( Charset.forName( "UTF-8" ) ) )
                .build();

    }

    protected void finalize(){
        if(bluetoothGattServer != null)
            bluetoothGattServer.close();
    }

    private void start_Ad(){
        // Starts advertising.
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if(mBluetoothLeAdvertiser != null){
            Log.d("BLE ad", "Advertiser not null");
        }else{
            Log.d("BLE ad", "Advertiser null");
        }
        mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, mAdvertiseCallback);
    }

    public void start_server(){
        bluetoothGattServer = bluetoothManager.openGattServer(context, serverCallback);
        start_Ad();
        bluetoothGattServer.addService(createService());

    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d("BLE", "Start Ad");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d("BLE", "Failed start Ad");
        }
    };

    private BluetoothGattService createService(){
        BluetoothGattService service = new BluetoothGattService(service_uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic readable = new BluetoothGattCharacteristic(characteristic_uuid_1,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor readable_des = new BluetoothGattDescriptor(id_desc_1,
                BluetoothGattDescriptor.PERMISSION_WRITE);

        readable.addDescriptor(readable_des);

        BluetoothGattCharacteristic writable = new BluetoothGattCharacteristic(characteristic_uuid_2,
                 BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        readable.setValue("Hello World");
        writable.setValue("Writable");
        service.addCharacteristic(readable);
        service.addCharacteristic(writable);
        return service;
    }

    private BluetoothGattServerCallback serverCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d("BLE", "onConnectionStateChane");
            switch (newState){
                case BluetoothGatt.STATE_DISCONNECTING:
                case BluetoothGatt.STATE_DISCONNECTED:
                    registeredDevices.remove(device);
                    break;
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d("BLE", "onServiceAdded");
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d("BLE", "onCharacteristicReadRequest");
            byte[] value = characteristic.getValue();
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            characteristic.setValue(value);

            Log.d("BLE write", characteristic.getStringValue(offset));
            if(responseNeeded){
                bluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,value);
            }
            if(characteristic_uuid_1.equals(characteristic.getUuid())){
                for(BluetoothDevice device2 : registeredDevices){
                    bluetoothGattServer.notifyCharacteristicChanged(device2, characteristic, false);
                }
            }
            Log.d("BLE", "onCharacteristicWriteRequest");
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.d("BLE", "onDescriptorReadRequest");
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            Log.d("BLE", "onDescriptorWriteRequest");
            if(id_desc_1.equals(descriptor.getUuid())){
                registeredDevices.add(device);
            }
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            Log.d("BLE", "onExecuteWrite");
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.d("BLE","onNotificationSend");
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            Log.d("BLE", "onMtuChanged");
        }

        @Override
        public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(device, txPhy, rxPhy, status);
            Log.d("BLE", "onPhyUpdate");
        }

        @Override
        public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyRead(device, txPhy, rxPhy, status);
            Log.d("BLE", "onPhyRead");
        }
    };



}
