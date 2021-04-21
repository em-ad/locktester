package com.lock.locklib3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.lock.locklib3.callback.BatteryCallback;
import com.lock.locklib3.callback.StatusCallback;
import com.lock.locklib3.callback.UnlockCallback;

import no.nordicsemi.android.ble.observer.ConnectionObserver;

public class BleConnectionManager implements ConnectionObserver {

    private final String TAG = "BLE MGR";

    public LockLibManager manager;
    public BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Context context;
    boolean myDcCommand = false;
    // [...]

    public BleConnectionManager(Context context) {
        this.context = context;
        initialize(context);
        if (manager == null) {
            manager = new LockLibManager(context);
            manager.setConnectionObserver(this);
        }
    }

    public void addDevice(String addr) {
        manager.batteryFailed = 0;
        manager.statusFailed = 0;
        manager.unlockFailed = 0;
        try {
            myDcCommand = true;
            manager.disconnect().enqueue();
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

    public void setTimeOut(long l) {
        if (manager != null)
            manager.setTimeoutLength(l);
    }

    public void unlock(String address, UnlockCallback callback) {
        manager.statusCallback = null;
        manager.batteryCallback = null;
        manager.unlockCallback = null;
        manager.setUnlockCallback(callback);
        addDevice(address);
    }

    public void getBattery(String address, BatteryCallback callback) {
        manager.statusCallback = null;
        manager.batteryCallback = null;
        manager.unlockCallback = null;
        manager.setBatteryCallback(callback);
        addDevice(address);
    }

    public void getStatus(String address, StatusCallback callback) {
        manager.statusCallback = null;
        manager.batteryCallback = null;
        manager.unlockCallback = null;
        manager.setStatusCallback(callback);
        addDevice(address);
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
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceConnected: " + device.getAddress());
        manager.device.setConnected(true);
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
        Log.e(TAG, "onDeviceDisconnected: " + device.getAddress() + " BECAUSE " + ConnectionReason.getValue(reason) + " " + myDcCommand);
        manager.device.setConnected(false);
        manager.device.setToken(new byte[4]);
        if (!myDcCommand) {
            if (manager.unlockCallback != null) {
                if (manager.unlockFailed >= manager.retryCount) {
                    manager.unlockCallback.failed();
                    manager.disconnectDevices();
                } else {
                    manager.unlockFailed++;
                    addDevice(device.getAddress());
                }
            }
            if (manager.statusCallback != null) {
                if (manager.statusFailed >= manager.retryCount) {
                    manager.statusCallback.failed();
                    manager.disconnectDevices();
                } else {
                    manager.statusFailed++;
                    addDevice(device.getAddress());
                }
            }
            if (manager.batteryCallback != null) {
                if (manager.batteryFailed >= manager.retryCount) {
                    manager.batteryCallback.failed();
                    manager.disconnectDevices();
                } else {
                    manager.batteryFailed++;
                    addDevice(device.getAddress());
                }
            }

        } else {
            myDcCommand = false;
        }
    }
}