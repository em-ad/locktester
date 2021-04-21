package com.lock.locklib3;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import static com.lock.locklib3.LockLibManager.ByteToString;

public class LockBluetoothDevice {
    private BluetoothDevice device;
    private byte[] token = new byte[4];
    private boolean connected;

    public LockBluetoothDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void disconnected(){
        token = new byte[4];
        connected = false;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public String getTokenAsString(){
        return ByteToString(this.token);
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setToken(int i, byte b){
        this.token[i] = b;
    }

    public boolean hasToken() {
        Log.e("token", "hasToken: " + token );
        return true;
    }
}
