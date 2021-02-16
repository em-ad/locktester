package com.lock.locklib.blelibrary.Adapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.lock.locklib.OperationStatus;
import com.lock.locklib.blelibrary.CommandCallback;
import com.lock.locklib.blelibrary.EventBean.EventBean;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceListEvent;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
import com.lock.locklib.blelibrary.EventBean.SaveBleEvent;
import com.lock.locklib.blelibrary.EventBean.WriteDataEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.search.SearchBle;
import com.lock.locklib.blelibrary.search.SearchListener;
import com.lock.locklib.blelibrary.sql.ChatDB;
import com.lock.locklib.blelibrary.tool.BleSharedPreferences;
import com.lock.locklib.blelibrary.tool.BleTool;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import com.lock.locklib.blelibrary.Adapter.BleItem;

public class BleAdapter extends BluetoothGattCallback {
    private static final String TAG = "tag";
    private String Connecting = "";
    private ChangesDeviceListEvent changesBLE = new ChangesDeviceListEvent();
    private Context context;
    private BleTool mBleTool;
    private CommandCallback callback;
    /* access modifiers changed from: private */
    public CopyOnWriteArrayList<BleItem> mList = new CopyOnWriteArrayList<>();
    private int rssiTime = 300;
    private SaveBleEvent saveBLE = new SaveBleEvent();
    private SearchBle searchBle;
    private BleSharedPreferences sharedPreferences;
    private Timer timer;

    public BleAdapter(Context context2, CommandCallback callback) {
        this.context = context2;
        EventTool.register(this);
        this.mBleTool = new BleTool(context2);
        this.sharedPreferences = new BleSharedPreferences(context2);
        this.saveBLE = this.sharedPreferences.getSaveBle();
        this.searchBle = SearchBle.getInstance(context2);
        this.callback = callback;
        startRSSI();
    }

    public void start() {
        this.searchBle.setListener(new SearchListener.ScanListener() {
            public void onLeScan(BleBase bleBase, BleStatus bleStatus) {
//                Log.e(TAG, "onLeScan BASE: " + new Gson().toJson(bleBase) );
                boolean existing = false;
                ArrayList<BleBase> list = searchBle.sharedPreferences.getSaveBle().BaseList;
                for (int i = 0; i < list.size(); i++) {
                    if(list.get(i).Address.equals(bleBase.Address)) {
                        existing = true;
                    }
                }
                if(!existing) {
                    list.add(bleBase);
                    SaveBleEvent event1 = new SaveBleEvent();
                    event1.BaseList = list;
                    searchBle.sharedPreferences.setSaveBle(event1);
                }
//                BleAdapter.this.connect(bleBase, bleStatus);
            }
        });
        this.searchBle.setDataListener(new SearchListener.ScanDataListener() {
            public void onLeScan(byte[] bArr, BleBase bleBase, BleStatus bleStatus) {
            }
        });
        this.searchBle.setSearchHas(true);
    }

    public boolean connect(BleBase bleBase, BleStatus bleStatus) {
        if (this.mBleTool.GetAdapter() == null || bleBase == null || TextUtils.isEmpty(bleBase.getAddress())) { //!TextUtils.isEmpty(this.Connecting) ||
            return false;
        }
        this.mList.clear();
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            String str = TAG;
            Log.e(str, "STATE >>>> " + bleStatus.state);
            if (it.next().isDevice(bleBase.getAddress())) {
                return false;
            }
        }
        BluetoothDevice remoteDevice = this.mBleTool.GetAdapter().getRemoteDevice(bleBase.getAddress());
        if (remoteDevice == null) {
            return false;
        }
        this.Connecting = bleBase.getAddress();
        Context context2 = this.context;
        this.mList.add(new BleItem(context2, remoteDevice.connectGatt(context2, false, this), bleBase, bleStatus));
        for (int i = 0; i < sharedPreferences.getSaveBle().BaseList.size(); i++) {
            if(sharedPreferences.getSaveBle().BaseList.get(i).Address.equals(bleBase.Address)){
                Log.e(TAG, "found: " + bleBase.Name + new Gson().toJson(bleStatus) );
            }
        }
        return true;
    }

    public void disconnect(BleBase bleBase) {
        if (bleBase.getAddress().equals(this.Connecting)) {
            this.Connecting = "";
        }
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            BleItem next = it.next();
            if (next.isDevice(bleBase.getAddress())) {
                Log.e(TAG, "disconnect_1");
                callback.commandExecuted(OperationStatus.DISCONNECTED);
                next.onDestroy();
                this.mList.remove(next);
            }
        }
        Iterator<BleBase> it2 = this.saveBLE.getBaseList().iterator();
        while (it2.hasNext()) {
            BleBase next2 = it2.next();
            if (next2.getAddress().equals(bleBase.getAddress())) {
                Log.e(TAG, "disconnect_2");
                this.saveBLE.getBaseList().remove(next2);
                this.sharedPreferences.setSaveBle(this.saveBLE);
                ChatDB.delstu(this.context, bleBase.getAddress());
                return;
            }
        }
    }

    public void sendType(BleBase bleBase, int i) {
        if (i == -1) {
            Iterator<BleItem> it = this.mList.iterator();
            while (it.hasNext()) {
                BleItem next = it.next();
                if (next.isDevice(bleBase.getAddress())) {
                    next.getStatus();
                }
            }
        } else if (i == 1) {
            Iterator<BleItem> it = this.mList.iterator();
            while (it.hasNext()) {
                BleItem next = it.next();
                if (next.isDevice(bleBase.getAddress())) {
                    next.Unlock();
                }
            }
        } else if (i == 2 || i == 1001 || i == 1002 || i == 2001 || i == 2002 || i == 3001 || i == 3002) {
            Iterator<BleItem> it2 = this.mList.iterator();
            while (it2.hasNext()) {
                BleItem next2 = it2.next();
                if (next2.isDevice(bleBase.getAddress())) {
                    next2.send(i);
                }
            }
        } else if (i == 4001 || i == 4002) {
            Iterator<BleItem> it3 = this.mList.iterator();
            while (it3.hasNext()) {
                BleItem next3 = it3.next();
                if (next3.isDevice(bleBase.getAddress())) {
                    next3.setInform(i);
                }
            }
        }
    }

    public void sendToken(BleBase bleBase) {
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            BleItem next = it.next();
            if (next.isDevice(bleBase.getAddress())) {
                next.sendToken(bleBase);
            }
        }
    }

    public void Modify(BleBase bleBase, String str, String str2) {
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            BleItem next = it.next();
            if (next.isDevice(bleBase.getAddress())) {
                next.Modify(str, str2);
            }
        }
    }

    public void Modify(BleBase bleBase, String str) {
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            BleItem next = it.next();
            if (next.isDevice(bleBase.getAddress())) {
                next.Modify(str);
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(EventBean eventBean) {
        if (eventBean != null) {
            if (eventBean instanceof WriteDataEvent) {
                WriteDataEvent writeDataEvent = (WriteDataEvent) eventBean;
                Iterator<BleItem> it = this.mList.iterator();
                while (it.hasNext()) {
                    BleItem next = it.next();
                    if (next.isDevice(writeDataEvent.getmBase().getAddress())) {
                        next.LostWriteData(writeDataEvent.getData());
                    }
                }
            } else if (eventBean instanceof ChangesDeviceEvent) {
                ChangesDeviceEvent changesDeviceEvent = (ChangesDeviceEvent) eventBean;
                if (changesDeviceEvent.getmBleStatus().getState() == 3 || changesDeviceEvent.getmBleStatus().getState() == 5) {
                    boolean z = false;
                    int i = 0;
                    while (true) {
                        if (i >= this.saveBLE.getBaseList().size()) {
                            break;
                        } else if (this.saveBLE.getBaseList().get(i).getAddress().equals(changesDeviceEvent.getmBleBase().getAddress())) {
                            changesDeviceEvent.getmBleBase().setInform(this.saveBLE.getBaseList().get(i).isInform());
                            this.saveBLE.getBaseList().set(i, changesDeviceEvent.getmBleBase());
                            z = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (!z) {
                        this.saveBLE.getBaseList().add(changesDeviceEvent.getmBleBase());
                    }
                    this.sharedPreferences.setSaveBle(this.saveBLE);
                }
                this.changesBLE.Changes(changesDeviceEvent);
            }
        }
    }

    public void onDestroy() {
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            it.next().onDestroy();
        }
        this.mList.clear();
        EventTool.unregister(this);
        this.searchBle.onDestroy();
        stopRSSI();
    }

    private final static int STATE_CLOSED = 0;
    private final static int STATE_CONNECTED = 2;

    public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
        super.onConnectionStateChange(bluetoothGatt, i, i2);
        String str = TAG;
        if (i2 == STATE_CLOSED) {
            if (bluetoothGatt.getDevice().getAddress().equals(this.Connecting)) {
                this.Connecting = "";
            }
            Iterator<BleItem> it = this.mList.iterator();
            while (it.hasNext()) {
                BleItem next = it.next();
                if (next.isDevice(bluetoothGatt.getDevice().getAddress())) {
                    Log.e(TAG, "STATE_DISCONNECTED");
                    callback.commandExecuted(OperationStatus.DISCONNECTED);
                    next.close();
                    this.mList.remove(next);
                    return;
                }
            }
            Log.e(TAG, "STATE_DISCONNECTED_close");
            bluetoothGatt.close();
            EventTool.post(new OtherEvent(1, bluetoothGatt.getDevice().getAddress()));
        } else if (i2 == STATE_CONNECTED) {
            Iterator<BleItem> it2 = this.mList.iterator();
            while (it2.hasNext()) {
                BleItem next2 = it2.next();
                if (next2.isDevice(bluetoothGatt.getDevice().getAddress())) {
                    String str2 = TAG;
                    Log.e(str2, "STATE_CONNECTED" + next2.changesData.getmBleStatus().getState());
                    callback.commandExecuted(OperationStatus.CONNECTED);
                    if (next2.changesData.getmBleStatus().getState() <= 1) {
                        next2.Connected();
                        return;
                    }
                    return;
                }
            }
            Log.e(TAG, "STATE_CONNECTED_disconnect");
            bluetoothGatt.disconnect();
            EventTool.post(new OtherEvent(1, bluetoothGatt.getDevice().getAddress()));
        }
    }

    public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
        super.onServicesDiscovered(bluetoothGatt, i);
        String str = TAG;
        Log.e(str, "onServicesDiscovered" + i);
        if (bluetoothGatt.getDevice().getAddress().equals(this.Connecting)) {
            this.Connecting = "";
        }
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            BleItem next = it.next();
            if (next.isDevice(bluetoothGatt.getDevice().getAddress())) {
                Boolean enableLostNoti = next.enableLostNoti();
                if (!enableLostNoti.booleanValue()) {
                    bluetoothGatt.disconnect();
                }
                String str2 = TAG;
                Log.e(str2, "isNotification=" + enableLostNoti);
            }
        }
    }

    public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        super.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            BleItem next = it.next();
            if (next.isDevice(bluetoothGatt.getDevice().getAddress())) {
                next.readData(bluetoothGattCharacteristic.getValue(), callback);
                Log.e(TAG, "onCharacteristicChanged: " + BleTool.ByteToString(bluetoothGattCharacteristic.getValue()) );
            }
        }
    }

    public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
        super.onDescriptorRead(bluetoothGatt, bluetoothGattDescriptor, i);
        Log.e(TAG, "onDescriptorRead");
    }

    public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
        super.onDescriptorWrite(bluetoothGatt, bluetoothGattDescriptor, i);
        String str = TAG;
        Log.e(str, "onDescriptorWrite=" + bluetoothGattDescriptor.getUuid());
        Iterator<BleItem> it = this.mList.iterator();
        while (it.hasNext()) {
            BleItem next = it.next();
            if (next.isDevice(bluetoothGatt.getDevice().getAddress())) {
                next.sendToken();
            }
        }
    }

    public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
        super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, i);
    }

    public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
        super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, i);
        Log.e(TAG, "onCharacteristicRead");
    }

    public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int i, int i2) {
        super.onReadRemoteRssi(bluetoothGatt, i, i2);
        if (i2 == 0) {
            Iterator<BleItem> it = this.mList.iterator();
            while (it.hasNext()) {
                BleItem next = it.next();
                if (next.isDevice(bluetoothGatt.getDevice().getAddress())) {
                    next.onReadRemoteRssi(bluetoothGatt, i, i2);
                }
            }
        }
    }

    private void startRSSI() {
        if (this.timer == null) {
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                public void run() {
                    Iterator it = BleAdapter.this.mList.iterator();
                    while (it.hasNext()) {
                        ((BleItem) it.next()).readRemoteRssi();
                    }
                }
            }, 0, (long) this.rssiTime);
        }
    }

    private void stopRSSI() {
        Timer timer2 = this.timer;
        if (timer2 != null) {
            timer2.cancel();
            this.timer = null;
        }
    }
}
