package com.lock.locklib.blelibrary.tool;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class BleTool {
    public static final int requestCode = 8989;
    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    public BleTool(Context context2) {
        this.context = context2;
        initialize(context2);
    }

    public BluetoothAdapter GetAdapter() {
        return this.mBluetoothAdapter;
    }

    @SuppressLint("WrongConstant")
    public boolean initialize(Context context2) {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) context2.getSystemService("bluetooth");
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

    public boolean hasBleOpen() {
        return this.context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    public boolean isBleOpen() {
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }

    public boolean openBLE() {
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.enable();
    }

    public void sysOpenBLE(AppCompatActivity appCompatActivity) {
        appCompatActivity.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), requestCode);
    }

    public static String ByteToString(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bArr.length);
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X ", new Object[]{Byte.valueOf(bArr[i])}));
        }
        return sb.toString();
    }

    public static String ByteToString2(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bArr.length);
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(bArr[i])}));
        }
        return sb.toString();
    }
}
