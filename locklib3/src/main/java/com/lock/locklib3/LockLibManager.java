package com.lock.locklib3;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.lock.locklib3.callback.BatteryCallback;
import com.lock.locklib3.callback.StatusCallback;
import com.lock.locklib3.callback.UnlockCallback;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.data.Data;

import static no.nordicsemi.android.ble.ConnectionPriorityRequest.CONNECTION_PRIORITY_BALANCED;

public class LockLibManager extends BleManager {

    public static final byte[] defaultkey = {32, 87, 47, 82, 54, 75, 63, 71, 48, 80, 65, 88, 17, 99, 45, 43};
    public static final UUID writeServiceUUID = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
    public static final UUID writeCharacteristicUUID = UUID.fromString("000036f5-0000-1000-8000-00805f9b34fb");
    public static final UUID readCharacteristicUUID = UUID.fromString("000036f6-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    protected LockBluetoothDevice device;

    public BatteryCallback batteryCallback;
    public StatusCallback statusCallback;
    public UnlockCallback unlockCallback;

    public static final byte[] mToKenAlt = {6, 1, 1, 1};
    public static final byte[] mUnlockAlt = {5, 1, 6};
    public static final byte[] mStatus = {5, 14, 1, 1};
    public static final byte[] mBattery = {2, 1, 1, 1};
    private static final String password = "000000";
    private static long timeoutLength = 4000L;

    BluetoothGatt gatt;

    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mReadCharacteristic;

    CountDownTimer timeout = new CountDownTimer(timeoutLength, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("TAG", "onTick: " );
        }

        @Override
        public void onFinish() {
            if(batteryCallback != null)
                batteryCallback.failed();
            if(statusCallback != null)
                statusCallback.failed();
            if(unlockCallback != null)
                unlockCallback.failed();
        }
    };

    @SuppressLint({"HandlerLeak"})
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.e("TAG", "handleMessage: " + new Gson().toJson(msg.getData().getByteArray("key")));
        }
    };

    public LockLibManager(@NonNull Context context) {
        super(context);
    }

    public static void setTimeoutLength(long timeoutLength) {
        LockLibManager.timeoutLength = timeoutLength;
    }

    public void setBatteryCallback(BatteryCallback batteryCallback) {
        this.batteryCallback = batteryCallback;
        this.statusCallback = null;
        this.unlockCallback = null;
    }

    public void setStatusCallback(StatusCallback statusCallback) {
        this.statusCallback = statusCallback;
        this.batteryCallback = null;
        this.unlockCallback = null;
    }

    public void setUnlockCallback(UnlockCallback unlockCallback) {
        this.unlockCallback = unlockCallback;
        this.batteryCallback = null;
        this.statusCallback = null;
    }

    public LockLibManager(@NonNull Context context, @NonNull Handler handler) {
        super(context, handler);
    }

    public void disconnectDevices() {
        disconnect().enqueue();
    }

    public void connectDevice(BluetoothDevice s) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
        timeout.start();
        if (device == null || !device.getDevice().getAddress().equals(s.getAddress()))
            device = new LockBluetoothDevice(s);
        if( device != null && device.isConnected() && device.hasToken())
            return;
        connect(s)
                .timeout(3000)
                .retry(3, 300)
                .done(connecting -> Log.e("?", "connect command sent"))
                .enqueue();
    }


    public void unlockBlack() {
        if (gatt == null || device == null) {
            Log.e("TAG", "unlockBlack: ");
        }
        final BluetoothGattService service = gatt.getService(writeServiceUUID);
        if (service != null) {
            mWriteCharacteristic = service.getCharacteristic(writeCharacteristicUUID);
            mReadCharacteristic = service.getCharacteristic(readCharacteristicUUID);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16);
        byteArrayOutputStream.write(mUnlockAlt, 0, mUnlockAlt.length);
        int j = 0;
        while (j < 6) {
            int i2 = j + 1;
            byteArrayOutputStream.write("000000".substring(j, i2).getBytes(), 0, "000000".substring(j, i2).getBytes().length);
            j = i2;
        }
        byteArrayOutputStream.write(device.getToken(), 0, device.getToken().length);

        mWriteCharacteristic.getWriteType();
        mWriteCharacteristic.setValue(addCrcAndEnd(byteArrayOutputStream));
        mWriteCharacteristic.setWriteType(1);
        beginReliableWrite()
                .add(writeCharacteristic(mWriteCharacteristic, mWriteCharacteristic.getValue()))
                .done(callback -> Log.e("TAG", "unlocked? black"))
                .enqueue();
    }

    public void getStatus() {
        if (gatt == null) {
            Log.e("TAG", "getStatus: " + " fuck gatt is null");
        }
        final BluetoothGattService service = gatt.getService(writeServiceUUID);
        if (service != null) {
            mWriteCharacteristic = service.getCharacteristic(writeCharacteristicUUID);
            mReadCharacteristic = service.getCharacteristic(readCharacteristicUUID);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16);
        byteArrayOutputStream.write(mStatus, 0, mStatus.length);
        byteArrayOutputStream.write(device.getToken(), 0, device.getToken().length);
        mWriteCharacteristic.getWriteType();
        mWriteCharacteristic.setValue(addCrcAndEnd(byteArrayOutputStream));
        mWriteCharacteristic.setWriteType(1);
        beginReliableWrite()
                .add(writeCharacteristic(mWriteCharacteristic, mWriteCharacteristic.getValue()))
                .done(callback -> Log.e("TAG", "get status sent"))
                .enqueue();
    }

    public void getBattery() {
        if (gatt == null) {
            Log.e("TAG", "getBattery: " + " fuck gatt is null");
        }
        final BluetoothGattService service = gatt.getService(writeServiceUUID);
        if (service != null) {
            mWriteCharacteristic = service.getCharacteristic(writeCharacteristicUUID);
            mReadCharacteristic = service.getCharacteristic(readCharacteristicUUID);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16);
        byteArrayOutputStream.write(mBattery, 0, mBattery.length);
        byteArrayOutputStream.write(device.getToken(), 0, device.getToken().length);
        mWriteCharacteristic.getWriteType();
        mWriteCharacteristic.setValue(addCrcAndEnd(byteArrayOutputStream));
        mWriteCharacteristic.setWriteType(1);
        beginReliableWrite()
                .add(writeCharacteristic(mWriteCharacteristic, mWriteCharacteristic.getValue()))
                .done(callback -> Log.e("TAG", "get battery sent"))
                .enqueue();
    }

    public boolean authenticateBlack() {
        if (device == null) {
            return false;
        } else {
            final BluetoothGattService service = gatt.getService(writeServiceUUID);
            if (service != null) {
                mWriteCharacteristic = service.getCharacteristic(writeCharacteristicUUID);
                mReadCharacteristic = service.getCharacteristic(readCharacteristicUUID);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(mToKenAlt, 0, mToKenAlt.length);
            mWriteCharacteristic.getWriteType();
            mWriteCharacteristic.setValue(addCrcAndEnd(byteArrayOutputStream));
            mWriteCharacteristic.setWriteType(1);
            beginAtomicRequestQueue()
                    .add(writeCharacteristic(mWriteCharacteristic, mWriteCharacteristic.getValue()).with(new DataSentCallback() {
                        @Override
                        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
                            Log.e("TAG", "onDataSent: " + device.getName() + " " + Decrypt(data.getValue()));
                        }
                    }))
                    .done(callback -> Log.e("tag", "Authenticate Command Sent!"))
                    .enqueue();

            beginReliableWrite().add(readCharacteristic(mReadCharacteristic).with(new DataReceivedCallback() {
                @Override
                public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                    Log.e("TAG", "onDataReceived: " + device.getAddress() + " " + data.getValue() + " " + data.getValue().length);
                }
            })).enqueue();
            return true;
        }
    }

    @NonNull
    @Override
    public BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }

    private class MyManagerGattCallback extends BleManagerGattCallback {

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            LockLibManager.this.gatt = gatt;
            final BluetoothGattService service = gatt.getService(writeServiceUUID);
            if (service != null) {
                mWriteCharacteristic = service.getCharacteristic(writeCharacteristicUUID);
                mReadCharacteristic = service.getCharacteristic(readCharacteristicUUID);
            }
            boolean writeRequest = true;
            gatt.setCharacteristicNotification(mReadCharacteristic, true);
            BluetoothGattDescriptor descriptor = mReadCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);

            return mWriteCharacteristic != null && writeRequest;
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        @Override
        protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            Log.e("TAG", "isOptionalServiceSupported: " + gatt.getDevice().getAddress());
            return super.isOptionalServiceSupported(gatt);
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        @Override
        protected void initialize() {
            beginReliableWrite()
                    .add(requestMtu(23)
                            .with((device, mtu) -> Log.i("TAG", "mtu set to : " + mtu))
                            .fail((device, status) -> Log.i("TAG", "Requested MTU not supported: " + status)))
                    .add(requestConnectionPriority(CONNECTION_PRIORITY_BALANCED))
                    .done(device -> Log.e("TAG", "initialized! "))
                    .enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            device.setConnected(false);
            device.setToken(new byte[4]);
//            mWriteCharacteristic = null;
        }


        @Override
        protected void onCharacteristicNotified(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicNotified(gatt, characteristic);
            byte[] result = Decrypt(characteristic.getValue());
            Log.e("TAG", "onCharacteristicNotified: " + ByteToString(result));
            handleResult(result);
        }
    }

    private void handleResult(byte[] result) {
        if (result[0] == 6 && result[1] == 2 && result[2] == 8) {
            device.setToken(0, result[3]);
            device.setToken(1, result[4]);
            device.setToken(2, result[5]);
            device.setToken(3, result[6]);
            if(batteryCallback != null){
                getBattery();
            } else if (statusCallback != null){
                getStatus();
            } else if (unlockCallback != null){
                unlockBlack();
            }
        } else if ((result[0] == 5 && result[1] == 15 && result[2] == 1 && result[3] == 0) ||
                (result[0] == 5 && result[1] == 2 && result[2] == 1 && result[3] == 0)) {
            if (unlockCallback != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        unlockCallback.unlocked();
                        cancelTimer();
                        unlockCallback = null;                    }
                });
            }
            if (statusCallback != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        cancelTimer();
                        statusCallback.statusReceived(LockStatusEnum.UNLOCKED);
                        statusCallback = null;                   }
                });
            }
        } else if ((result[0] == 5 && result[1] == 15 && result[2] == 1 && result[3] == 1) ||
                (result[0] == 5 && result[1] == 8 && result[2] == 1 && result[3] == 0)) {
            if (statusCallback != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        cancelTimer();
                        statusCallback.statusReceived(LockStatusEnum.LOCKED);
                        statusCallback = null;                }
                });
            }
        } else if (result[0] == 2 && result[1] == 2 && result[2] == 1) {
            if (batteryCallback != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        cancelTimer();
                        batteryCallback.batteryReceived(ByteToString(result).substring(6, 8));
                        batteryCallback = null;
                    }
                });
            }
        }
    }

    public static byte[] addCrcAndEnd(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] bArr = new byte[(16 - byteArrayOutputStream.size())];
        new Random().nextBytes(bArr);
        byteArrayOutputStream.write(bArr, 0, bArr.length);
        Log.e("TAG", "Encrypting " + ByteToString(byteArrayOutputStream.toByteArray()));
        byte[] Encrypt = Encrypt(byteArrayOutputStream.toByteArray());
        return Encrypt;
    }

    public static byte[] Encrypt(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(defaultkey, "AES");
            Cipher instance = Cipher.getInstance("AES/ECB/NoPadding");
            instance.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return instance.doFinal(bArr);
        } catch (Exception unused) {
            Log.e("TAG", "Encrypt EXCEPTION: " + unused.getMessage());
            return null;
        }
    }

    public static byte[] Decrypt(byte[] bArr) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(defaultkey, "AES");
            Cipher instance = Cipher.getInstance("AES/ECB/NoPadding");
            instance.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return instance.doFinal(bArr);
        } catch (Exception unused) {
            Log.e("TAG", "Decrypt EXCEPTION: " + unused.getMessage());
            return null;
        }
    }

    public void cancelTimer(){
        if(timeout == null)
            return;
        timeout.cancel();
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
}
