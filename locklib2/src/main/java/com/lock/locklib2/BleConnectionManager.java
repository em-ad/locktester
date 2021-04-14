package com.lock.locklib2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.observer.ConnectionObserver;

public class BleConnectionManager implements ConnectionObserver {

    private final String TAG = "BLE MGR";

    private LockLibManager manager;
    public BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    // [...]

    public void connect(@NonNull final BluetoothDevice device, Context context) {
        manager = new LockLibManager(context);
        manager.setConnectionObserver(this);
        manager.connect(device)
                .timeout(10000)
                .retry(3, 100)
                .done(connecting -> Log.i("TAG", "Device initiated"))
                .enqueue();
    }

    public BleConnectionManager(Context context) {
        initialize(context);
    }

    public boolean initialize(Context context) {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (this.mBluetoothManager == null) {
                return false;
            }
        }
        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceConnecting: " + device.getAddress());
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceConnected: " + device.getAddress());
    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        Log.e(TAG, "onDeviceFailedToConnect: " + device.getAddress() + " BECAUSE " + reason);
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceReady: " + device.getAddress());
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceDisconnecting: " + device.getAddress());
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        Log.e(TAG, "onDeviceDisconnected: " + device.getAddress() + " BECAUSE " + reason);
    }
}