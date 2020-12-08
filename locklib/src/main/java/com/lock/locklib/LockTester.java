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

    public static BleStatus currentStatus = null;

    public static void connect(Context context, String address, String name) {
        BleStatus status = new BleStatus();
        status.Auto_Unlock = 1;
        status.setConsignment(1);
        status.setLOCK_STA(1);
        status.setPOWER(100);
        status.setVibration(1);
        status.setState(-2);

        BleBase bleBase = new BleBase();
        bleBase.Address = address;
        bleBase.Name = name;
        bleBase.rssi = -60;
        ServiceCommand.connect(context, bleBase, status);
    }

    public static void unlock(Context context, String address, String name, String password) {
        BleBase bleBase = new BleBase();
        bleBase.Address = address;
        bleBase.inform = false;
        bleBase.Name = name;
        bleBase.rssi = -62;
        bleBase.setPassWord(password);
        ServiceCommand.send(context, bleBase, 1);
    }

    public static void authenticate(Context context, String address, String name, String password){
        BleBase bleBase = new BleBase();
        bleBase.Address = address;
        bleBase.Name = name;
        bleBase.setPassWord(password);
        bleBase.rssi = -60;
        ServiceCommand.authenticated(context, bleBase);
    }

    public static String getLockStatus(){
        return !currentStatus.getLOCK_STA() ? "LOCKED" : "UNLOCKED";
    }
}
