package com.lock.locklib2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.PhyRequest;

public class LockLibManager extends BleManager {

    public static final byte[] defaultkey = {32, 87, 47, 82, 54, 75, 63, 71, 48, 80, 65, 88, 17, 99, 45, 43};
    public static final UUID WriteCharacteristicUUID = UUID.fromString("000036f5-0000-1000-8000-00805f9b34fb");
    public static final UUID WriteServiceUUID = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic serverCharacteristic;

    public LockLibManager(@NonNull Context context) {
        super(context);
    }

    public LockLibManager(@NonNull Context context, @NonNull Handler handler) {
        super(context, handler);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }

    private class MyManagerGattCallback extends BleManagerGattCallback {

        @Override
        protected void onServerReady(@NonNull final BluetoothGattServer server) {
            // Obtain your server attributes.
            serverCharacteristic = server
                    .getService(WriteServiceUUID)
                    .getCharacteristic(WriteCharacteristicUUID);

            // Set write callback, if you need. It will be called when the remote device writes
            // something to the given server characteristic.
            setWriteCallback(serverCharacteristic)
                    .with((device, data) -> Log.e("tag", "onServerReady: " + device.getAddress() + " " + data.toString()));
        }

        // This method will be called when the device is connected and services are discovered.
        // You need to obtain references to the characteristics and descriptors that you will use.
        // Return true if all required services are found, false otherwise.
        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(WriteServiceUUID);
            if (service != null) {
                mWriteCharacteristic = service.getCharacteristic(WriteCharacteristicUUID);
            }
            // Validate properties
            boolean notify = true;
//            if (writeCharacteristic != null) {
//                final int properties = dataCharacteristic.getProperties();
//                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
//            }
            boolean writeRequest = true;
//            if (writeCharacteristic != null) {
//                final int properties = controlPointCharacteristic.getProperties();
//                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
//                writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//            }
            // Return true if all required services have been found
            return mWriteCharacteristic != null && notify && writeRequest;
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        @Override
        protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            return super.isOptionalServiceSupported(gatt);
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        @Override
        protected void initialize() {
            // You may enqueue multiple operations. A queue ensures that all operations are
            // performed one after another, but it is not required.
            beginAtomicRequestQueue()
                    .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with((device, mtu) -> log(Log.ERROR, "MTU set to " + mtu))
                            .fail((device, status) -> log(Log.ERROR, "Requested MTU not supported: " + status)))
                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
                            .fail((device, status) -> log(Log.ERROR, "Requested PHY not supported: " + status)))
                    .add(enableNotifications(mWriteCharacteristic))
                    .done(device -> log(Log.INFO, "Target initialized"))
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
            // Device disconnected. Release your references here.
            mWriteCharacteristic = null;
        }
    }

    public static byte[] addCrcAndEnd(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] bArr = new byte[(16 - byteArrayOutputStream.size())];
        new Random().nextBytes(bArr);
        byteArrayOutputStream.write(bArr, 0, bArr.length);
        Log.i("TAG", "Encrypting " + ByteToString(byteArrayOutputStream.toByteArray()));
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
            return null;
        }
    }

    private static byte[] Decrypt(byte[] bArr) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(defaultkey, "AES");
            Cipher instance = Cipher.getInstance("AES/ECB/NoPadding");
            instance.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return instance.doFinal(bArr);
        } catch (Exception unused) {
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
