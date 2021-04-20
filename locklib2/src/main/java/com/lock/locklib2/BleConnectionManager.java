package com.lock.locklib2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.observer.ConnectionObserver;

public class BleConnectionManager implements ConnectionObserver {

    private final String TAG = "BLE MGR";

    public LockLibManager manager;
    public BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private CommandCallback callback;
    private Context context;
    // [...]

    public BleConnectionManager(Context context, CommandCallback callback) {
        this.context = context;
        this.callback = callback;
        initialize(context);
        if (manager == null) {
            manager = new LockLibManager(context, this.callback);
            manager.setConnectionObserver(this);
        }
    }

    public void addDevice(String addr) {
        try {
            manager.disconnect().enqueue();
            manager.close();
        } catch (NullPointerException npe) {
            npe.getStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.connectDevice(mBluetoothAdapter.getRemoteDevice(addr));
            }
        }, 800);
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
        this.mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
//                Log.e(TAG, "onLeScan: " + Decrypt(bytes) );
            }
        });

        return true;
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceConnecting: " + device.getAddress());
        if (callback != null)
            callback.commandExecuted(OperationStatus.CONNECTING);
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceConnected: " + device.getAddress());
        if (callback != null)
            callback.commandExecuted(OperationStatus.CONNECTED);
    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        Log.e(TAG, "onDeviceFailedToConnect: " + device.getAddress() + " BECAUSE " + ConnectionReason.getValue(reason));
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceReady: " + device.getAddress());
//        manager.authenticateBlack();
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceDisconnecting: " + device.getAddress());
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        Log.e(TAG, "onDeviceDisconnected: " + device.getAddress() + " BECAUSE " + ConnectionReason.getValue(reason));
        if (callback != null)
            callback.commandExecuted(OperationStatus.DISCONNECTED);
    }
}