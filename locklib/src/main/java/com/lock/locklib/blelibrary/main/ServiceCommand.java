package com.lock.locklib.blelibrary.main;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.lock.locklib.LockTester;
import com.lock.locklib.blelibrary.CommandCallback;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.notification.NotificationBean;

public class ServiceCommand {

    public static final String CONNECT_ACTION_AUTHENTICATED = "CONNECT_ACTION_AUTHENTICATED";
    public static final String CONNECT_ACTION_AUTHENTICATED_ALT = "CONNECT_ACTION_AUTHENTICATED_ALT";
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
    public static final String CONNECT_ACTION_SEND_ALT = "CONNECT_ACTION_SEND_ALT";

    public static void start(Context context, NotificationBean notificationBean, CommandCallback callback) {
        Intent intent = new Intent();
        intent.setAction(CONNECT_ACTION_START);
        intent.putExtra(CONNECT_ACTION_START_Bean, notificationBean);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void disconnect(Context context, BleBase bleBase, CommandCallback callback) {
        Intent intent = new Intent();
        intent.setAction(CONNECT_ACTION_DISCONNECT);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void connect(Context context, BleBase bleBase, BleStatus bleStatus, CommandCallback callback) {
        Intent intent = new Intent();
        intent.setAction(CONNECT_ACTION_CONNECT);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_STATUS, bleStatus);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void send(Context context, BleBase bleBase, int i, CommandCallback callback) {
        Intent intent = new Intent();
        if (i != -1)
            intent.setAction(CONNECT_ACTION_SEND);
        else
            intent.setAction(GET_STATUS);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_TYPE, i);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void sendAlt(Context context, BleBase bleBase, int i, CommandCallback callback) {
        Intent intent = new Intent();
        if (i != -1)
            intent.setAction(CONNECT_ACTION_SEND_ALT);
        else
            intent.setAction(GET_STATUS);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_TYPE, i);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void authenticated(Context context, BleBase bleBase, CommandCallback callback) {
        Intent intent = new Intent();
        intent.setAction(CONNECT_ACTION_AUTHENTICATED);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void modify_pw(Context context, BleBase bleBase, String str, String str2, CommandCallback callback) {
        Intent intent = new Intent();
        intent.setAction(CONNECT_ACTION_MODIFY_PW);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_OLD_PW, str);
        intent.putExtra(CONNECT_DATA_NEW_PW, str2);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void modify_name(Context context, BleBase bleBase, String str, CommandCallback callback) {
        Intent intent = new Intent();
        intent.setAction(CONNECT_ACTION_MODIFY_NAME);
        intent.putExtra(CONNECT_DATA_BASE, bleBase);
        intent.putExtra(CONNECT_DATA_NAME, str);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }

    public static void authenticatedAlt(Context context, BleBase base, CommandCallback callback) {
        Intent intent = new Intent();
        Log.e("tag", "authenticatedAlt: " + new Gson().toJson(base));
        intent.setAction(CONNECT_ACTION_AUTHENTICATED_ALT);
        intent.putExtra(CONNECT_DATA_BASE, base);
        BleExecutor.getInstance().execute(intent, 0, 0, context, callback);
    }
}
