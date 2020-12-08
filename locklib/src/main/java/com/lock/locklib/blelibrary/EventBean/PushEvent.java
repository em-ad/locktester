package com.lock.locklib.blelibrary.EventBean;

import android.content.Context;

import com.lock.locklib.blelibrary.sql.PushBean;
import com.lock.locklib.R;

import java.util.ArrayList;

public class PushEvent extends EventBean {
    public ArrayList<PushBean> list;

    public PushEvent(ArrayList<PushBean> arrayList) {
        this.list = arrayList;
    }

    public ArrayList<PushBean> getList() {
        ArrayList<PushBean> arrayList = this.list;
        return arrayList == null ? new ArrayList<>() : arrayList;
    }

    public void setList(ArrayList<PushBean> arrayList) {
        this.list = arrayList;
    }

    public static String getType(Context context, int i) {
        switch (i) {
            case 0:
                return context.getString(R.string.LS_OperationContent_0_Lock);
            case 1:
                return context.getString(R.string.LS_OperationContent_1_Unlock);
            case 2:
                return context.getString(R.string.LS_OperationContent_2_OpenAutoLock);
            case 3:
                return context.getString(R.string.LS_OperationContent_3_CloseAutoLock);
            case 4:
                return context.getString(R.string.LS_OperationContent_4_OpenAlert);
            case 5:
                return context.getString(R.string.LS_OperationContent_5_CloseAlert);
            case 6:
                return context.getString(R.string.LS_OperationContent_6_OpenNotification);
            case 7:
                return context.getString(R.string.LS_OperationContent_7_CloseNotification);
            case 8:
                return context.getString(R.string.LS_OperationContent_8_ModifyPasswordOK);
            case 9:
                return context.getString(R.string.LS_OperationContent_9_ModifyPasswordNG);
            case 10:
                return context.getString(R.string.LS_OperationContent_10_ModifyNameOK);
            case 11:
                return context.getString(R.string.LS_OperationContent_11_ModifyNameNG);
            case 12:
                return context.getString(R.string.LS_OperationContent_12_OpenConsign);
            case 13:
                return context.getString(R.string.LS_OperationContent_13_CloseConsign);
            case 14:
                return context.getString(R.string.LS_OperationContent_14_AutoLock);
            default:
                return "";
        }
    }
}
