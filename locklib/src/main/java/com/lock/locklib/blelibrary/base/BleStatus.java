package com.lock.locklib.blelibrary.base;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.lock.locklib.R;

public class BleStatus implements Parcelable {
    public static final Parcelable.Creator<BleStatus> CREATOR = new Parcelable.Creator<BleStatus>() {
        public BleStatus createFromParcel(Parcel parcel) {
            return new BleStatus(parcel);
        }

        public BleStatus[] newArray(int i) {
            return new BleStatus[i];
        }
    };
    public static final int state_DataBLE = 5;
    public static final int state_DataUp = 4;
    public static final int state_ServicesDiscovered = 2;
    public static final int state_Unbind = -2;
    public static final int state_authenticated = 3;
    public static final int state_authenticated_fail = -3;
    public static final int state_connected = 1;
    public static final int state_connecting = 0;
    public static final int state_disconnect = -1;
    public int Auto_Unlock;
    public int Consignment;
    public int LOCK_STA;
    public int POWER;
    public int Vibration;
    public int state = -2;
    public byte[] token;

    public int describeContents() {
        return 0;
    }

    public BleStatus() {
    }

    public boolean isAuthenticated() {
        int state2 = getState();
        return state2 == 3 || state2 == 4 || state2 == 5;
    }

    protected BleStatus(Parcel parcel) {
        this.state = parcel.readInt();
        this.token = parcel.createByteArray();
        this.Vibration = parcel.readInt();
        this.POWER = parcel.readInt();
        this.LOCK_STA = parcel.readInt();
        this.Auto_Unlock = parcel.readInt();
        this.Consignment = parcel.readInt();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.state);
        parcel.writeByteArray(this.token);
        parcel.writeInt(this.Vibration);
        parcel.writeInt(this.POWER);
        parcel.writeInt(this.LOCK_STA);
        parcel.writeInt(this.Auto_Unlock);
        parcel.writeInt(this.Consignment);
    }

    public int getState() {
        return this.state;
    }

    public String getStateText(Context context) {
        switch (this.state) {
            case -3:
            case -2:
            case -1:
            case 0:
            case 1:
            case 2:
                return context.getString(R.string.state_disconnect);
            case 3:
            case 4:
            case 5:
                return context.getString(R.string.state_connected);
            default:
                return "";
        }
    }

    public void setState(int i) {
        this.state = i;
    }

    public byte[] getToken() {
        return this.token;
    }

    public void setToken(byte[] bArr) {
        this.token = bArr;
    }

    public Boolean getVibration() {
        return Boolean.valueOf(this.Vibration == 0);
    }

    public void setVibration(int i) {
        this.Vibration = i;
    }

    public int getPOWER() {
        return this.POWER;
    }

    public void setPOWER(int i) {
        this.POWER = i;
    }

    public Boolean getLOCK_STA() {
        return Boolean.valueOf(this.LOCK_STA == 0);
    }

    public void setLOCK_STA(int i) {
        this.LOCK_STA = i;
    }

    public Boolean getAuto_Unlock() {
        return Boolean.valueOf(this.Auto_Unlock == 0);
    }

    public void setAuto_Unlock(int i) {
        this.Auto_Unlock = i;
    }

    public Boolean getConsignment() {
        return Boolean.valueOf(this.Consignment == 0);
    }

    public void setConsignment(int i) {
        this.Consignment = i;
    }
}
