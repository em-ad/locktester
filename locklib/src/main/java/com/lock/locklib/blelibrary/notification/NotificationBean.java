package com.lock.locklib.blelibrary.notification;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationBean implements Parcelable {
    public static final Parcelable.Creator<NotificationBean> CREATOR = new Parcelable.Creator<NotificationBean>() {
        public NotificationBean createFromParcel(Parcel parcel) {
            return new NotificationBean(parcel);
        }

        public NotificationBean[] newArray(int i) {
            return new NotificationBean[i];
        }
    };
    private String className;
    private int icoId;

    public int describeContents() {
        return 0;
    }

    public NotificationBean(int i, String str) {
        this.icoId = i;
        this.className = str;
    }

    protected NotificationBean(Parcel parcel) {
        this.icoId = parcel.readInt();
        this.className = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.icoId);
        parcel.writeString(this.className);
    }

    public int getIcoId() {
        return this.icoId;
    }

    public void setIcoId(int i) {
        this.icoId = i;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String str) {
        this.className = str;
    }
}
