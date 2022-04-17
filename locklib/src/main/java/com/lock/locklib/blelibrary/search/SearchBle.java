package com.lock.locklib.blelibrary.search;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.lock.locklib.blelibrary.EventBean.SaveBleEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.tool.BleSharedPreferences;
import com.lock.locklib.blelibrary.tool.BleTool;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchBle {
    private static SearchBle instance;
    /* access modifiers changed from: private */
    public Boolean SearchHas = false;
    private ScanFilter filter;
    private List<ScanFilter> filters = new ArrayList();
    /* access modifiers changed from: private */
    public String hanKey_Ble = "mBle";
    public String hanKey_scanRecord = "scanRecord";
    @SuppressLint({"HandlerLeak"})
    Handler hanLeScan = new Handler() {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Bundle data = message.getData();
            BleBase bleBase = (BleBase) data.getParcelable(SearchBle.this.hanKey_Ble);
            if (SearchBle.this.mSearchListener != null) {
                SearchBle.this.mSearchListener.onLeScan(bleBase, new BleStatus());
            }
            if (bleBase != null) {
                byte[] byteArray = data.getByteArray(SearchBle.this.hanKey_scanRecord);
//                Log.e("TAG", "handleMessage: " + byteArray.length );
                if (BleTool.ByteToString(byteArray).contains(BleTool.ByteToString(SearchBle.this.type))) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int i = 0;
                    while (true) {
                        if (i < byteArray.length - 2) {
                            if ((byteArray[i] & 255) == (SearchBle.this.type[0] & 255) && (byteArray[i + 1] & 255) == (SearchBle.this.type[1] & 255) && (byteArray[i + 2] & 255) == (SearchBle.this.type[2] & 255)) {
                                byteArrayOutputStream.write(byteArray, i, byteArray.length - i);
                                byteArray = byteArrayOutputStream.toByteArray();
//                                Log.e("TAG", "handleMessage: " + byteArray + " " + byteArrayOutputStream );
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                    SaveBleEvent saveBle = SearchBle.this.sharedPreferences.getSaveBle();
                    if (SearchBle.this.SearchHas.booleanValue()) {
                        Iterator<BleBase> it = saveBle.getBaseList().iterator();
                        while (it.hasNext()) {
                            BleBase next = it.next();
                            if (next.getAddress().equals(bleBase.getAddress())) {
                                bleBase.setPassWord(next.getPassWord());
                                if (byteArray.length > 30) {
                                    BleStatus bleStatus = new BleStatus();
                                    bleStatus.setVibration(byteArray[9]);
                                    bleStatus.setPOWER(byteArray[10]);
                                    bleStatus.setLOCK_STA(byteArray[11]);
                                    bleStatus.setAuto_Unlock(byteArray[12]);
                                    bleStatus.setConsignment(byteArray[13]);
                                    if (SearchBle.this.mSearchListener != null) {
                                        SearchBle.this.mSearchListener.onLeScan(bleBase, bleStatus);
                                    }
                                    if (SearchBle.this.scanDataListener != null) {
                                        SearchBle.this.scanDataListener.onLeScan(byteArray, bleBase, bleStatus);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            }
                        }
                        return;
                    }
                    Iterator<BleBase> it2 = saveBle.getBaseList().iterator();
                    while (it2.hasNext()) {
                        if (it2.next().getAddress().equals(bleBase.getAddress())) {
                            return;
                        }
                    }
                    if (byteArray.length > 30) {
                        BleStatus bleStatus2 = new BleStatus();
                        bleStatus2.setVibration(byteArray[9]);
                        bleStatus2.setPOWER(byteArray[10]);
                        bleStatus2.setLOCK_STA(byteArray[11]);
                        bleStatus2.setAuto_Unlock(byteArray[12]);
                        bleStatus2.setConsignment(byteArray[13]);
                        if (SearchBle.this.mSearchListener != null) {
                            SearchBle.this.mSearchListener.onLeScan(bleBase, bleStatus2);
                        }
                        if (SearchBle.this.scanDataListener != null) {
                            SearchBle.this.scanDataListener.onLeScan(byteArray, bleBase, bleStatus2);
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean isstart = false;
    public BleTool mBleTool;
    private Context mContext;
    /* access modifiers changed from: private */
    public SearchListener.ScanListener mSearchListener;
    /* access modifiers changed from: private */
    public int minrssi = -90;
    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            if (i >= SearchBle.this.minrssi && !TextUtils.isEmpty(bluetoothDevice.getName())) {
//                Log.e("abc", bluetoothDevice.getName() + "----------" + BleTool.ByteToString(bArr));
//                Log.e("abc", bluetoothDevice.getAddress() + "----------" + BleTool.ByteToString(bArr) + " " + bArr.length);
                Bundle bundle = new Bundle();
                BleBase bleBase = new BleBase();
                bleBase.setName(bluetoothDevice.getName());
                bleBase.setAddress(bluetoothDevice.getAddress());
                bleBase.setRssi(i);
                bundle.putParcelable(SearchBle.this.hanKey_Ble, bleBase);
                bundle.putByteArray(SearchBle.this.hanKey_scanRecord, bArr);
                Message message = new Message();
                message.setData(bundle);
                SearchBle.this.hanLeScan.sendMessage(message);
            }
        }
    };
    /* access modifiers changed from: private */
    public SearchListener.ScanDataListener scanDataListener;
    private BluetoothLeScanner scanner;
    /* access modifiers changed from: private */
    public BleSharedPreferences sharedPreferences;
    /* access modifiers changed from: private */
    public int stopNum = 0;
    private Timer timer;
    private Timer timer_stop;
    /* access modifiers changed from: private */
    public byte[] type = {-1, 7, -121};

    private boolean isNew2() {
        return true;
    }

    static /* synthetic */ int access$008(SearchBle searchBle) {
        int i = searchBle.stopNum;
        searchBle.stopNum = i + 1;
        return i;
    }

    private SearchBle(Context context) {
        this.mContext = context;
        Log.e("abc", "SearchBle-----");
        this.mBleTool = new BleTool(context);
        this.sharedPreferences = new BleSharedPreferences(context);
        startSearch();
    }

    public static SearchBle getInstance(Context context) {
        if (instance == null) {
            synchronized (SearchBle.class) {
                if (instance == null) {
                    instance = new SearchBle(context);
                }
            }
        }
        return instance;
    }

    private void startSearch() {
        if (isNew2()) {
            this.timer_stop = new Timer();
            this.timer_stop.schedule(new TimerTask() {
                public void run() {
                    SearchBle.access$008(SearchBle.this);
                    if (SearchBle.this.isstart && SearchBle.this.stopNum < 10) {
                        return;
                    }
                    if (!SearchBle.this.isstart && SearchBle.this.stopNum < 3) {
                        return;
                    }
                    if (SearchBle.this.isstart) {
                        SearchBle.this.stop();
                        boolean unused = SearchBle.this.isstart = false;
                        int unused2 = SearchBle.this.stopNum = 0;
                        return;
                    }
                    boolean unused3 = SearchBle.this.search();
                    boolean unused4 = SearchBle.this.isstart = true;
                    int unused5 = SearchBle.this.stopNum = 0;
                }
            }, 0, 1000);
            return;
        }
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            public void run() {
                boolean unused = SearchBle.this.search();
            }
        }, 0, 5000);
    }

    public void onDestroy() {
        Timer timer2 = this.timer;
        if (timer2 != null) {
            timer2.cancel();
            this.timer = null;
        }
        Timer timer3 = this.timer_stop;
        if (timer3 != null) {
            timer3.cancel();
            this.timer_stop = null;
        }
        stop();
        instance = null;
    }

    public void setSearchHas(Boolean bool) {
        this.SearchHas = bool;
    }

    public void setListener(SearchListener.ScanListener scanListener) {
        this.mSearchListener = scanListener;
    }

    public void setDataListener(SearchListener.ScanDataListener scanDataListener2) {
        this.scanDataListener = scanDataListener2;
    }

    /* access modifiers changed from: private */
    public boolean search() {
        if (!this.mBleTool.hasBleOpen() || !this.mBleTool.isBleOpen()) {
            return false;
        }
        this.mBleTool.GetAdapter().stopLeScan(this.scanCallback);
        return this.mBleTool.GetAdapter().startLeScan(this.scanCallback);
    }

    /* access modifiers changed from: private */
    public void stop() {
        try {
            this.mBleTool.GetAdapter().stopLeScan(this.scanCallback);
        } catch (Exception unused) {
        }
    }
}
