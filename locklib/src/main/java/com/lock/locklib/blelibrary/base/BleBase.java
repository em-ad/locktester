package com.lock.locklib.blelibrary.base;

import android.os.Parcel;
import android.os.Parcelable;

public class BleBase implements Parcelable {
    public static final Parcelable.Creator<BleBase> CREATOR = new Parcelable.Creator<BleBase>() {
        public BleBase createFromParcel(Parcel parcel) {
            return new BleBase(parcel);
        }

        public BleBase[] newArray(int i) {
            return new BleBase[i];
        }
    };
    public String Address;
    public String Name;
    public String PassWord;
    public boolean inform = false;
    public int rssi;

    public int describeContents() {
        return 0;
    }

    public BleBase() {
    }

    protected BleBase(Parcel parcel) {
        boolean z = false;
        this.Name = parcel.readString();
        this.Address = parcel.readString();
        this.PassWord = parcel.readString();
        this.rssi = parcel.readInt();
        this.inform = parcel.readByte() != 0 ? true : z;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.Name);
        parcel.writeString(this.Address);
        parcel.writeString(this.PassWord);
        parcel.writeInt(this.rssi);
        parcel.writeByte(this.inform ? (byte) 1 : 0);
    }

    public boolean isInform() {
        return this.inform;
    }

    public void setInform(boolean z) {
        this.inform = z;
    }

    public String getPassWord() {
        return this.PassWord;
    }

    public void setPassWord(String str) {
        this.PassWord = str;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String str) {
        this.Name = str;
    }

    public String getAddress() {
        return this.Address;
    }

    public void setAddress(String str) {
        this.Address = str;
    }

    public int getRssi() {
        return this.rssi;
    }

    public void setRssi(int i) {
        this.rssi = i;
    }
}
