package com.lock.locklib.blelibrary.Adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.lock.locklib.blelibrary.Data.ReadDataAnalysis;
import com.lock.locklib.blelibrary.Data.SendDataAnalysis;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.WriteDataEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.tool.BleTool;
import java.io.ByteArrayOutputStream;

public class BleItem {
    private static final String TAG = "BleItem";
    private int RssiNum = 1;
    public ChangesDeviceEvent changesData;
    private Context context;
    private final int hanConnected = 1001;
    @SuppressLint({"HandlerLeak"})
    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 1001 && BleItem.this.mBluetoothGatt != null && !BleItem.this.mBluetoothGatt.discoverServices()) {
                BleItem.this.mBluetoothGatt.disconnect();
            }
        }
    };
    /* access modifiers changed from: private */
    public BluetoothGatt mBluetoothGatt;
    private int maxRssi = -95;
    private int maxRssiNum = 3;
    public String name = "";
    public String password = "";

    public BleItem(Context context2, BluetoothGatt bluetoothGatt, BleBase bleBase, BleStatus bleStatus) {
        this.mBluetoothGatt = bluetoothGatt;
        connect();
        this.context = context2;
        bleStatus.setState(0);
        this.changesData = new ChangesDeviceEvent(bleBase, bleStatus);
        EventTool.post(this.changesData);
    }

    public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int i, int i2) {
        if (i > this.maxRssi) {
            this.RssiNum = 1;
            return;
        }
        int i3 = this.RssiNum;
        if (i3 >= this.maxRssiNum) {
            disconnect();
        } else {
            this.RssiNum = i3 + 1;
        }
    }

    public boolean readRemoteRssi() {
        if (this.changesData.getmBleStatus().getState() >= 2) {
            return this.mBluetoothGatt.readRemoteRssi();
        }
        return false;
    }

    public boolean isDevice(String str) {
        return this.changesData.getmBleBase().getAddress().equals(str);
    }

    private boolean connect() {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null) {
            return false;
        }
        return bluetoothGatt.connect();
    }

    public void Connected() {
        this.changesData.getmBleStatus().setState(1);
        EventTool.post(this.changesData);
        this.handler.sendEmptyMessageDelayed(1001, 100);
    }

    public void onDestroy() {
        this.handler.removeMessages(1001);
        disconnect();
        close();
    }

    private void disconnect() {
        Log.e(TAG, "Disconnect");
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        EventTool.post(this.changesData);
    }

    public void close() {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        if (this.changesData.getmBleStatus().isAuthenticated()) {
            this.changesData.getmBleStatus().setState(-2);
        } else {
            this.changesData.getmBleStatus().setState(-1);
        }
        EventTool.post(this.changesData);
    }

    public void sendToken(BleBase bleBase) {
        this.changesData.setmBleBase(bleBase);
        EventTool.post(new WriteDataEvent(bleBase, SendDataAnalysis.SendToken(bleBase)));
    }

    public void sendToken() {
        if (!TextUtils.isEmpty(this.changesData.getmBleBase().getPassWord())) {
            EventTool.post(new WriteDataEvent(this.changesData.getmBleBase(), SendDataAnalysis.SendToken(this.changesData.getmBleBase())));
        }
    }

    public void readData(byte[] bArr) {
        ReadDataAnalysis.Read(this.context, this, bArr);
        EventTool.post(this.changesData);
    }

    public void setInform(int i) {
        if (i == 4001) {
            this.changesData.getmBleBase().setInform(true);
            this.changesData.getmBleStatus().setState(5);
            EventTool.post(this.changesData);
            ReadDataAnalysis.saveChat(this.context, this.changesData.getmBleBase().getAddress(), 6);
        } else if (i == 4002) {
            this.changesData.getmBleBase().setInform(false);
            this.changesData.getmBleStatus().setState(5);
            EventTool.post(this.changesData);
            ReadDataAnalysis.saveChat(this.context, this.changesData.getmBleBase().getAddress(), 7);
        }
    }

    public void send(int i) {
        if (this.changesData.getmBleStatus().getToken() != null) {
            EventTool.post(new WriteDataEvent(this.changesData.getmBleBase(), SendDataAnalysis.SendOther(this.context, this, i)));
        }
    }

    public void Unlock() {
        if (this.changesData.getmBleStatus().getToken() != null) {
            EventTool.post(new WriteDataEvent(this.changesData.getmBleBase(), SendDataAnalysis.SendOther(this.context, this, 1)));
        }
    }

    public void getStatus() {
        if (this.changesData.getmBleStatus().getToken() != null) {
            EventTool.post(new WriteDataEvent(this.changesData.getmBleBase(), SendDataAnalysis.SendOther(this.context, this, -1)));
        }
    }

    public void Modify(String str, final String str2) {
        if (this.changesData.getmBleStatus().getToken() != null) {
            this.password = str2;
            EventTool.post(new WriteDataEvent(this.changesData.getmBleBase(), SendDataAnalysis.Modify_old_pw(this.changesData.getmBleStatus(), str)));
            this.handler.postDelayed(new Runnable() {
                public void run() {
                    EventTool.post(new WriteDataEvent(BleItem.this.changesData.getmBleBase(), SendDataAnalysis.Modify_new_pw(BleItem.this.changesData.getmBleStatus(), str2)));
                }
            }, 200);
        }
    }

    public void Modify(String str) {
        if (this.changesData.getmBleStatus().getToken() != null) {
            this.name = str;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int length = str.getBytes().length; length < 16; length++) {
                str = str + " ";
            }
            byteArrayOutputStream.write(str.getBytes(), 0, 8);
            EventTool.post(new WriteDataEvent(this.changesData.getmBleBase(), SendDataAnalysis.Modify_Name1(this.changesData.getmBleStatus(), byteArrayOutputStream.toByteArray())));
            byteArrayOutputStream.reset();
            byteArrayOutputStream.write(str.getBytes(), 8, str.length() - 8);
            final byte[] byteArray = byteArrayOutputStream.toByteArray();
            this.handler.postDelayed(new Runnable() {
                public void run() {
                    EventTool.post(new WriteDataEvent(BleItem.this.changesData.getmBleBase(), SendDataAnalysis.Modify_Name2(BleItem.this.changesData.getmBleStatus(), byteArray)));
                }
            }, 200);
        }
    }

    public void LostWriteData(byte[] bArr) {
        BluetoothGatt bluetoothGatt;
        BluetoothGattService service;
        BluetoothGattCharacteristic characteristic;
        if (bArr != null && (bluetoothGatt = this.mBluetoothGatt) != null && (service = bluetoothGatt.getService(SampleGattAttributes.WriteServiceUUID)) != null && (characteristic = service.getCharacteristic(SampleGattAttributes.WriteCharacteristicUUID)) != null) {
            characteristic.getWriteType();
            characteristic.setValue(bArr);
            characteristic.setWriteType(1);
            boolean writeCharacteristic = this.mBluetoothGatt.writeCharacteristic(characteristic);
            String str = TAG;
            Log.e(str, writeCharacteristic + " WRITING LostWriteData " + BleTool.ByteToString(bArr));
        }
    }

    public Boolean enableLostNoti() {
        BluetoothGattCharacteristic characteristic;
        this.changesData.getmBleStatus().setState(2);
        EventTool.post(this.changesData);
        BluetoothGattService service = this.mBluetoothGatt.getService(SampleGattAttributes.NotifyServiceUUID);
        if (service == null || (characteristic = service.getCharacteristic(SampleGattAttributes.NotifyCharacteristicUUID)) == null) {
            return false;
        }
        Boolean valueOf = Boolean.valueOf(this.mBluetoothGatt.setCharacteristicNotification(characteristic, true));
        if (SampleGattAttributes.NotifyCharacteristicUUID.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            this.mBluetoothGatt.writeDescriptor(descriptor);
        }
        return valueOf;
    }
}
