package com.lock.locklib2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;

import static no.nordicsemi.android.ble.ConnectionPriorityRequest.CONNECTION_PRIORITY_BALANCED;

public class LockLibManager extends BleManager {

    public static final byte[] defaultkey = {32, 87, 47, 82, 54, 75, 63, 71, 48, 80, 65, 88, 17, 99, 45, 43};
    public static final UUID writeServiceUUID = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
    public static final UUID writeCharacteristicUUID = UUID.fromString("000036f5-0000-1000-8000-00805f9b34fb");
    public static final UUID readCharacteristicUUID = UUID.fromString("000036f6-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    protected BluetoothDevice device;

    public static final byte[] mToKenAlt = {6, 1, 1, 1};
    public static final byte[] mUnlockAlt = {5, 1, 6};
    private static final String password = "000000";

    BluetoothGatt gatt;

    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mReadCharacteristic;

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

    public LockLibManager(@NonNull Context context, @NonNull Handler handler) {
        super(context, handler);
    }

    public void disconnectDevices() {
        disconnect().enqueue();
    }

    public void connectDevice(BluetoothDevice s) {
        connect(s)
                .timeout(3000)
                .retry(3, 100)
                .setHandler(mHandler)
                .done(connecting -> authenticateBlack())
                .enqueue();
        device = s;
    }

    public void obtainToken() {

    }

    public void unlockBlack() {
        if (gatt == null) {
            Log.e("TAG", "unlockBlack: " + " fuck gatt is null");
        }
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream(16);
        byteArrayOutputStream2.write(mUnlockAlt, 0, mUnlockAlt.length);
        int j = 0;
        while (j < 6) {
            int i2 = j + 1;
            byteArrayOutputStream2.write("000000".substring(j, i2).getBytes(), 0, "000000".substring(j, i2).getBytes().length);
            j = i2;
        }
        mWriteCharacteristic.getWriteType();
        mWriteCharacteristic.setValue(addCrcAndEnd(byteArrayOutputStream2));
        mWriteCharacteristic.setWriteType(1);
        beginReliableWrite()
                .add(writeCharacteristic(mWriteCharacteristic, mWriteCharacteristic.getValue()))
                .done(callback -> Log.e("TAG", "unlocked? black"))
                .enqueue();
    }

    public boolean authenticateBlack() {
        if (device == null) {
            return false;
        } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(mToKenAlt, 0, mToKenAlt.length);
            Log.e("TAG", "authenticateBlack unc: " + ByteToString(byteArrayOutputStream.toByteArray()));
            mWriteCharacteristic.getWriteType();
            mWriteCharacteristic.setValue(addCrcAndEnd(byteArrayOutputStream));
            mWriteCharacteristic.setWriteType(1);
//            BluetoothGatt gatt = device.connectGatt(getContext(), false, new BluetoothGattCallback() {
//                @Override
//                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//                    super.onCharacteristicChanged(gatt, characteristic);
//                    Log.e("TAG", "onCharacteristicChanged2: " );
//                }
//            });
            beginAtomicRequestQueue()
                    .add(writeCharacteristic(mWriteCharacteristic, mWriteCharacteristic.getValue()).with(new DataSentCallback() {
                        @Override
                        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
                            Log.e("TAG", "onDataSent: " + device.getName() + " " + Decrypt(data.getValue()) );
                        }
                    }))
                    .done(callback -> Log.e("tag", "Authenticated Black"))
//                    .add(writeCharacteristic(mWriteCharacteristic, addCrcAndEnd(byteArrayOutputStream2)))
//                    .done(callback -> Log.e("TAG", "unlocked black! "))
                    .setHandler(mHandler)
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

        // This method will be called when the device is connected and services are discovered.
        // You need to obtain references to the characteristics and descriptors that you will use.
        // Return true if all required services are found, false otherwise.
        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            LockLibManager.this.gatt = gatt;
            Log.e("TAG", "isRequiredServiceSupported: " + gatt.getDevice().getAddress());
            final BluetoothGattService service = gatt.getService(writeServiceUUID);
            if (service != null) {
                mWriteCharacteristic = service.getCharacteristic(writeCharacteristicUUID);
                mReadCharacteristic = service.getCharacteristic(readCharacteristicUUID);
            }

//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16);
//            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream(16);
//            byteArrayOutputStream.write(mToKenAlt, 0, mToKenAlt.length);
//            byteArrayOutputStream2.write(mUnlockAlt, 0, mUnlockAlt.length);
//            int j = 0;
//            while (j < 6) {
//                int i2 = j + 1;
//                byteArrayOutputStream2.write("000000".substring(j, i2).getBytes(), 0, "000000".substring(j, i2).getBytes().length);
//                j = i2;
//            }
//            Log.e("TAG", "authenticateBlack unc: " + ByteToString(byteArrayOutputStream.toByteArray()));
//            Log.e("TAG", "unlock unc: " + ByteToString(byteArrayOutputStream2.toByteArray()));
//            mWriteCharacteristic.getWriteType();
//            mWriteCharacteristic.setValue(addCrcAndEnd(byteArrayOutputStream));
//            mWriteCharacteristic.setWriteType(1);
//
//            gatt.writeCharacteristic(mWriteCharacteristic);

            boolean writeRequest = true;
//            if (writeCharacteristic != null) {
//                final int properties = controlPointCharacteristic.getProperties();
//                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
//                writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//            }
            // Return true if all required services have been found

            gatt.setCharacteristicNotification(mReadCharacteristic, true);
            BluetoothGattDescriptor descriptor = mReadCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);/*.done(new SuccessCallback() {
                @Override
                public void onRequestCompleted(@NonNull BluetoothDevice device) {
                    Log.e("TAG", "onRequestCompleted: writeDescriptor" );
                }
            }).fail(new FailCallback() {
                @Override
                public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                    Log.e("TAG", "onRequestFailed: " + "writeDescriptor" + " " + status );
                }
            }).enqueue();*/

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
            // You may enqueue multiple operations. A queue ensures that all operations are
            // performed one after another, but it is not required.
            beginReliableWrite()
                    .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with((device, mtu) -> Log.e("TAG", "mtu set to : " + mtu))
                            .fail((device, status) -> Log.e("TAG", "Requested MTU not supported: " + status)))
                    .add(requestConnectionPriority(CONNECTION_PRIORITY_BALANCED))
                    .done(device -> Log.e("TAG", "initialized! "))
                    .enqueue();
            // You may easily enqueue more operations here like such:
//            writeCharacteristic(mWriteCharacteristic, "Hello World!".getBytes())
//                    .done(device -> log(Log.INFO, "Greetings sent"))
//                    .enqueue();


            // Set a callback for your notifications. You may also use waitForNotification(...).
            // Both callbacks will be called when notification is received.
//            setNotificationCallback(writeCharacteristic);
            // If you need to send very long data using Write Without Response, use split()
            // or define your own splitter in split(DataSplitter splitter, WriteProgressCallback cb).
//            writeCharacteristic(secondCharacteristic, "Very, very long data that will no fit into MTU")
//                    .split()
//                    .enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            Log.e("TAG", "onDeviceDisconnected: ");
            // Device disconnected. Release your references here.
//            mWriteCharacteristic = null;
        }

        @Override
        protected void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicRead(gatt, characteristic);
            Log.e("TAG", "onCharacteristicRead: " + characteristic.getValue().toString());
        }

        @Override
        protected void onCharacteristicWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicWrite(gatt, characteristic);
            Log.e("tag", "onCharacteristicWrite: " + characteristic.getValue().toString());
        }

        @Override
        protected void onCharacteristicIndicated(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicIndicated(gatt, characteristic);
            Log.e("TAG", "onCharacteristicIndicated: " + characteristic.getValue().toString());
        }

        @Override
        protected void onDescriptorWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor) {
            super.onDescriptorWrite(gatt, descriptor);
            Log.e("TAG", "onDescriptorWrite: " + descriptor.getValue().toString());
        }

        @Override
        protected void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor) {
            super.onDescriptorRead(gatt, descriptor);
            Log.e("TAG", "onDescriptorRead: " + descriptor.getValue().toString());
        }

        @Override
        protected void onCharacteristicNotified(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicNotified(gatt, characteristic);
            Log.e("TAG", "onCharacteristicNotified: " + ByteToString(Decrypt(characteristic.getValue())));
        }
    }

    public static byte[] addCrcAndEnd(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] bArr = new byte[(16 - byteArrayOutputStream.size())];
        new Random().nextBytes(bArr);
        byteArrayOutputStream.write(bArr, 0, bArr.length);
        Log.e("TAG", "Encrypting " + ByteToString(byteArrayOutputStream.toByteArray()));
        byte[] Encrypt = Encrypt(byteArrayOutputStream.toByteArray());
        Log.e("TAG", "addCrcAndEnd: " + Encrypt);
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
        Log.e("TAG", "Decrypt: " + bArr + " " + bArr.length);
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
