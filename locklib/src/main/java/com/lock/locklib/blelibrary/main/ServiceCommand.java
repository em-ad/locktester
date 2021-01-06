package com.lock.locklib.blelibrary.main;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.notification.NotificationBean;

public class ServiceCommand {
    public static final String CONNECT_ACTION_AUTHENTICATED = "CONNECT_ACTION_AUTHENTICATED";
    public static final String CONNECT_ACTION_CONNECT = "CONNECT_ACTION_CONNECT";
    public static final String CONNECT_ACTION_DISCONNECT = "CONNECT_ACTION_DISCONNECT";
    public static final String CONNECT_ACTION_MODIFY_NAME = "CONNECT_ACTION_MODIFY_NAME";
    public static final String CONNECT_ACTION_MODIFY_PW = "CONNECT_ACTION_MODIFY_PW";
    public static final String CONNECT_ACTION_SEND = "CONNECT_ACTION_SEND";
    public static final String CONNECT_ACTION_START = "CONNECT_ACTION_START";
    public static final String CONNECT_ACTION_START_Bean = "CONNECT_ACTION_START_Bean";
    public static final String CONNECT_DATA_BASE = "CONNECT_DATA_Base";
    public static final String CONNECT_DATA_NAME = "CONNECT_DATA_NAME";
    public static final String CONNECT_DATA_NEW_PW = "CONNECT_DATA_NEW_PW";
    public static final String CONNECT_DATA_OLD_PW = "CONNECT_DATA_OLD_PW";
    public static final String CONNECT_DATA_STATUS = "CONNECT_DATA_Status";
    public static final String CONNECT_DATA_TYPE = "CONNECT_DATA_TYPE";
    public static final String GET_STATUS = "GET_STATUS";

    private static void startService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, BluetoothLeService.class));
    }

    public static void start(Context context, NotificationBean notificationBean) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.setAction(CONNECT_ACTION_START);
        intent.putExtra(CONNECT_ACTION_START_Bean, notificationBean);
        startService(context, intent);
    }

    public static void disconnect(Context context, BleBase bleBase) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.setAction(CONNECT_ACTION_DISCONNECT);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        startService(context, intent);
    }

    public static void connect(Context context, BleBase bleBase, BleStatus bleStatus) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.setAction(CONNECT_ACTION_CONNECT);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_STATUS, bleStatus);
        startService(context, intent);
    }

    public static void send(Context context, BleBase bleBase, int i) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        if (i != -1)
            intent.setAction(CONNECT_ACTION_SEND);
        else
            intent.setAction(GET_STATUS);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_TYPE, i);
        startService(context, intent);
    }

    public static void authenticated(Context context, BleBase bleBase) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.setAction(CONNECT_ACTION_AUTHENTICATED);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        startService(context, intent);
    }

    public static void modify_pw(Context context, BleBase bleBase, String str, String str2) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.setAction(CONNECT_ACTION_MODIFY_PW);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_OLD_PW, str);
        intent.putExtra(CONNECT_DATA_NEW_PW, str2);
        startService(context, intent);
    }

    public static void modify_name(Context context, BleBase bleBase, String str) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.setAction(CONNECT_ACTION_MODIFY_NAME);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_NAME, str);
        startService(context, intent);
    }
}
