package com.lock.locklib.blelibrary.main;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lock.locklib.blelibrary.Adapter.BleAdapter;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.notification.NotificationBean;
import com.lock.locklib.blelibrary.notification.NotificationUtils;

public class BluetoothLeService extends Service {
    private String Tag = "BluetoothLeService";
    private BleAdapter mBleAdapter;
    private NotificationUtils notificationUtils;

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.e(this.Tag, "onCreate");
        this.mBleAdapter = new BleAdapter(this);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        Log.e(this.Tag, "onStartCommand");
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            String action = intent.getAction();
            char c = 65535;
            switch (action.hashCode()) {
                case -1370382770:
                    if (action.equals(ServiceCommand.CONNECT_ACTION_START)) {
                        c = 0;
                        break;
                    }
                    break;
                case -1341670404:
                    if (action.equals(ServiceCommand.CONNECT_ACTION_MODIFY_NAME)) {
                        c = 6;
                        break;
                    }
                    break;
                case -842477029:
                    if (action.equals(ServiceCommand.CONNECT_ACTION_AUTHENTICATED)) {
                        c = 4;
                        break;
                    }
                    break;
                case -497484872:
                    if (action.equals(ServiceCommand.CONNECT_ACTION_MODIFY_PW)) {
                        c = 5;
                        break;
                    }
                    break;
                case 170689494:
                    if (action.equals(ServiceCommand.CONNECT_ACTION_CONNECT)) {
                        c = 1;
                        break;
                    }
                    break;
                case 509969404:
                    if (action.equals(ServiceCommand.CONNECT_ACTION_SEND)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1075567120:
                    if (action.equals(ServiceCommand.CONNECT_ACTION_DISCONNECT)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    if (this.notificationUtils == null) {
                        this.notificationUtils = new NotificationUtils(this);
                        this.notificationUtils.sendNotification((NotificationBean) intent.getParcelableExtra(ServiceCommand.CONNECT_ACTION_START_Bean));
                    }
                    if (this.mBleAdapter == null) {
                        this.mBleAdapter = new BleAdapter(this);
                    }
                    this.mBleAdapter.start();
                    break;
                case 1:
                    this.mBleAdapter.connect((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), (BleStatus) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_STATUS));
                    break;
                case 2:
                    this.mBleAdapter.disconnect((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE));
                    break;
                case 3:
                    this.mBleAdapter.sendType((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), intent.getIntExtra(ServiceCommand.CONNECT_DATA_TYPE, 0));
                    break;
                case 4:
                    this.mBleAdapter.sendToken((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE));
                    break;
                case 5:
                    this.mBleAdapter.Modify((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), intent.getStringExtra(ServiceCommand.CONNECT_DATA_OLD_PW), intent.getStringExtra(ServiceCommand.CONNECT_DATA_NEW_PW));
                    break;
                case 6:
                    this.mBleAdapter.Modify((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), intent.getStringExtra(ServiceCommand.CONNECT_DATA_NAME));
                    break;
            }
        }
        return super.onStartCommand(intent, i, i2);
    }

    public void onDestroy() {
        Log.e(this.Tag, "onDestroy");
        super.onDestroy();
        this.mBleAdapter.onDestroy();
        this.mBleAdapter = null;
    }
}
