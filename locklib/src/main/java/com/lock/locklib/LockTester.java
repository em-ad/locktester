package com.lock.locklib;

import android.content.Context;
import android.util.Log;

import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceListEvent;
import com.lock.locklib.blelibrary.EventBean.EventBean;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
import com.lock.locklib.blelibrary.EventBean.WriteDataEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.main.ServiceCommand;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

public class LockTester implements Serializable {

    public static void connect(Context context, BleBase base, BleStatus status) {
        ServiceCommand.connect(context, base, status);
    }

    public static void unlock(Context context, BleBase base) {
        ServiceCommand.send(context, base, 1);
    }

    public static void authenticate(Context context, BleBase bleBase){
        BleBase bleBaseInner = bleBase;
        bleBaseInner.setPassWord("123456");
        ServiceCommand.authenticated(context, bleBase);
    }

    public static String getLockStatus(BleStatus status){
        return !status.getLOCK_STA() ? "LOCKED" : "UNLOCKED";
    }
}
