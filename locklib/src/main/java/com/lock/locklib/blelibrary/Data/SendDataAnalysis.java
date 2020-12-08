package com.lock.locklib.blelibrary.Data;

import android.content.Context;
import com.lock.locklib.blelibrary.Adapter.BleItem;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import java.io.ByteArrayOutputStream;

public class SendDataAnalysis {
    public static final int Auto_Unlock_Close = 1002;
    public static final int Auto_Unlock_Open = 1001;
    public static final int Consignment_Close = 3002;
    public static final int Consignment_Open = 3001;
    public static final int Unlock = 1;
    public static final int Vibration_Close = 2002;
    public static final int Vibration_Open = 2001;
    public static final int inform_Close = 4002;
    public static final int inform_Open = 4001;
    public static final int lock = 2;

    public static byte[] SendToken(BleBase bleBase) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(BleCommon.mToKen, 0, BleCommon.mToKen.length);
        int i = 0;
        while (i < bleBase.getPassWord().length()) {
            int i2 = i + 1;
            byteArrayOutputStream.write(bleBase.getPassWord().substring(i, i2).getBytes(), 0, bleBase.getPassWord().substring(i, i2).getBytes().length);
            i = i2;
        }
        return BleCommon.addCrcAndEnd(byteArrayOutputStream);
    }

    public static byte[] Modify_Name1(BleStatus bleStatus, byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(BleCommon.mNAME1, 0, BleCommon.mNAME1.length);
        byteArrayOutputStream.write(bArr, 0, bArr.length);
        byteArrayOutputStream.write(bleStatus.getToken(), 0, bleStatus.getToken().length);
        return BleCommon.addCrcAndEnd(byteArrayOutputStream);
    }

    public static byte[] Modify_Name2(BleStatus bleStatus, byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(BleCommon.mNAME2, 0, BleCommon.mNAME2.length);
        byteArrayOutputStream.write(bArr, 0, bArr.length);
        byteArrayOutputStream.write(bleStatus.getToken(), 0, bleStatus.getToken().length);
        return BleCommon.addCrcAndEnd(byteArrayOutputStream);
    }

    public static byte[] Modify_old_pw(BleStatus bleStatus, String str) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(BleCommon.mOldPW, 0, BleCommon.mOldPW.length);
        int i = 0;
        while (i < str.length()) {
            int i2 = i + 1;
            byteArrayOutputStream.write(str.substring(i, i2).getBytes(), 0, str.substring(i, i2).getBytes().length);
            i = i2;
        }
        byteArrayOutputStream.write(bleStatus.getToken(), 0, bleStatus.getToken().length);
        return BleCommon.addCrcAndEnd(byteArrayOutputStream);
    }

    public static byte[] Modify_new_pw(BleStatus bleStatus, String str) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(BleCommon.mNewPW, 0, BleCommon.mNewPW.length);
        int i = 0;
        while (i < str.length()) {
            int i2 = i + 1;
            byteArrayOutputStream.write(str.substring(i, i2).getBytes(), 0, str.substring(i, i2).getBytes().length);
            i = i2;
        }
        byteArrayOutputStream.write(bleStatus.getToken(), 0, bleStatus.getToken().length);
        return BleCommon.addCrcAndEnd(byteArrayOutputStream);
    }

    public static byte[] SendOther(Context context, BleItem bleItem, int i) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (i == 1) {
            byteArrayOutputStream.write(BleCommon.mUnlock, 0, BleCommon.mUnlock.length);
            ReadDataAnalysis.saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 1);
        } else if (i == 2) {
            byteArrayOutputStream.write(BleCommon.mlock, 0, BleCommon.mlock.length);
            ReadDataAnalysis.saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 0);
        } else if (i == 1001) {
            byteArrayOutputStream.write(BleCommon.mAutoUnlockOpen, 0, BleCommon.mAutoUnlockOpen.length);
        } else if (i == 1002) {
            byteArrayOutputStream.write(BleCommon.mAutoUnlockClose, 0, BleCommon.mAutoUnlockClose.length);
        } else if (i == 2001) {
            byteArrayOutputStream.write(BleCommon.mVibrationOpen, 0, BleCommon.mVibrationOpen.length);
        } else if (i == 2002) {
            byteArrayOutputStream.write(BleCommon.mVibrationClose, 0, BleCommon.mVibrationClose.length);
        } else if (i == 3001) {
            byteArrayOutputStream.write(BleCommon.mConsignmentOpen, 0, BleCommon.mConsignmentOpen.length);
        } else if (i == 3002) {
            byteArrayOutputStream.write(BleCommon.mConsignmentClose, 0, BleCommon.mConsignmentClose.length);
        }
        byteArrayOutputStream.write(bleItem.changesData.getmBleStatus().getToken(), 0, bleItem.changesData.getmBleStatus().getToken().length);
        return BleCommon.addCrcAndEnd(byteArrayOutputStream);
    }
}
