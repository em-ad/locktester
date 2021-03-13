package com.lock.locklib.blelibrary.main;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.lock.locklib.blelibrary.Adapter.BleAdapter;
import com.lock.locklib.blelibrary.CommandCallback;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;

public class BleExecutor {
    private static String TAG = "BleExecutor";
    public BleAdapter mBleAdapter;
    static BleExecutor instance;

    public static BleExecutor getInstance() {
        if(instance == null)
            instance = new BleExecutor();
        return instance;
    }

    public int execute(Intent intent, int i, int i2, Context context, CommandCallback callback) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            String action = intent.getAction();
            char c = 65535;
            if(action.equals(ServiceCommand.GET_STATUS)){
                c = 99;
            } else if (action.equals(ServiceCommand.CONNECT_ACTION_AUTHENTICATED_ALT)){
                c = 101;
            } else if (action.equals(ServiceCommand.CONNECT_ACTION_SEND_ALT)){
                c = 102;
            } else if (action.equals(ServiceCommand.GET_BATTERY)){
                c = 103;
            }
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
                    if (this.mBleAdapter == null) {
                        this.mBleAdapter = new BleAdapter(context, callback);
                    }
                    this.mBleAdapter.start();
                    Log.e("tag", "execute: " + " adapter init" );
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
                case 102:
                    this.mBleAdapter.sendTypeAlt((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), intent.getIntExtra(ServiceCommand.CONNECT_DATA_TYPE, 0));
                    break;
                case 103:
                    this.mBleAdapter.getBattery((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), intent.getIntExtra(ServiceCommand.CONNECT_DATA_TYPE, 0));
                    break;
                case 99:
                    this.mBleAdapter.sendType((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), -1);
                    break;
                case 4:
                    this.mBleAdapter.sendToken((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE));
                    break;
                case 101:
                    this.mBleAdapter.sendTokenAlt((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE));
                    break;
                case 5:
                    this.mBleAdapter.Modify((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), intent.getStringExtra(ServiceCommand.CONNECT_DATA_OLD_PW), intent.getStringExtra(ServiceCommand.CONNECT_DATA_NEW_PW));
                    break;
                case 6:
                    this.mBleAdapter.Modify((BleBase) intent.getParcelableExtra(ServiceCommand.CONNECT_DATA_BASE), intent.getStringExtra(ServiceCommand.CONNECT_DATA_NAME));
                    break;
            }
        }
        return -1;
    }

    public void onDestroy() {
        this.mBleAdapter.onDestroy();
        this.mBleAdapter = null;
    }
}
