package com.lock.locklib.blelibrary.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;

public class NotificationReceiver extends BroadcastReceiver {

    public static final int myPid = Process.myPid();
    public static final String ACTION_Close = ("ACTION_Close" + myPid);
    public Context context;
    private Boolean isregister = false;
    private NotificationListener listener;

    public interface NotificationListener {
        void Close();
    }

    public NotificationReceiver(Context context2, NotificationListener notificationListener) {
        this.listener = notificationListener;
        this.context = context2;
    }

    public void registerReceiver() {
        if (!this.isregister.booleanValue()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_Close);
            this.context.registerReceiver(this, intentFilter);
            this.isregister = true;
        }
    }

    public void onDestroy() {
        if (this.isregister.booleanValue()) {
            this.context.unregisterReceiver(this);
            this.isregister = false;
        }
    }

    public void onReceive(Context context2, Intent intent) {
        if (intent.getAction().equals(ACTION_Close)) {
            this.listener.Close();
        }
    }

    public static Intent Close() {
        Intent intent = new Intent();
        intent.setAction(ACTION_Close);
        return intent;
    }
}
