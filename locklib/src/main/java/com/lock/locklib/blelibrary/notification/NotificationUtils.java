package com.lock.locklib.blelibrary.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.lock.locklib.blelibrary.main.BluetoothLeService;

public class NotificationUtils {

    public static final int Notificationid = 1;

    /* renamed from: id */
    public static final String f44id = "Bluetooth";
    public static final String name = "Bluetooth Service";
    private NotificationManager manager;
    private Notification notification;
    private BluetoothLeService service;
    private NotificationView view;

    public NotificationUtils(BluetoothLeService bluetoothLeService) {
        this.service = bluetoothLeService;
        this.view = new NotificationView(bluetoothLeService);
    }

    @RequiresApi(api = 26)
    public void createNotificationChannel() {
        getManager().createNotificationChannel(new NotificationChannel(f44id, name, NotificationManager.IMPORTANCE_HIGH));
    }

    private NotificationManager getManager() {
        if (this.manager == null) {
            this.manager = (NotificationManager) this.service.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return this.manager;
    }

    public PendingIntent getContentIntent(String str) {
        try {
            Intent intent = new Intent(this.service, Class.forName(str));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return PendingIntent.getActivity(this.service, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        } catch (Exception unused) {
            return null;
        }
    }

    @RequiresApi(api = 26)
    public Notification.Builder getChannelNotification(NotificationBean notificationBean) {
        return new Notification.Builder(this.service, f44id).setContent(this.view.getView(this.service, notificationBean)).setSmallIcon(notificationBean.getIcoId());
    }

    public Notification.Builder getNotification_25(NotificationBean notificationBean) {
        return new Notification.Builder(this.service).setContent(this.view.getView(this.service, notificationBean)).setSmallIcon(notificationBean.getIcoId());
    }

    public void setText(String str) {
        Log.e("abc", "text=" + str);
        NotificationView notificationView = this.view;
        if (notificationView != null) {
            notificationView.setText(str);
            this.service.startForeground(1, this.notification);
        }
    }

    public void sendNotification(NotificationBean notificationBean) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
            this.notification = new Notification.Builder(this.service, f44id).build();
            this.service.startForeground(1, this.notification);
            this.notification = getChannelNotification(notificationBean).build();
            this.service.startForeground(1, this.notification);
            return;
        }
        this.notification = getNotification_25(notificationBean).build();
        this.service.startForeground(1, this.notification);
    }
}
