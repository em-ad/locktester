package com.lock.locklib.blelibrary.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.lock.locklib.R;


public class NotificationView {
    private Context context;
    private RemoteViews mRemoteViews;

    public NotificationView(Context context2) {
        this.context = context2;
    }

    public RemoteViews getView(Context context2, NotificationBean notificationBean) {
        if (this.mRemoteViews == null) {
            this.mRemoteViews = new RemoteViews(context2.getPackageName(), R.layout.notificationlayout);
            this.mRemoteViews.setImageViewResource(R.id.image, notificationBean.getIcoId());
            this.mRemoteViews.setOnClickPendingIntent(R.id.lin, getContentIntent(notificationBean.getClassName()));
            this.mRemoteViews.setOnClickPendingIntent(R.id.button, PendingIntent.getBroadcast(context2, 2, NotificationReceiver.Close(), 134217728));
        }
        return this.mRemoteViews;
    }

    public void setText(String str) {
        RemoteViews remoteViews = this.mRemoteViews;
        if (remoteViews != null) {
            remoteViews.setTextViewText(R.id.tv, str);
        }
    }

    private PendingIntent getContentIntent(String str) {
        try {
            Intent intent = new Intent(this.context, Class.forName(str));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        } catch (Exception unused) {
            return null;
        }
    }
}
