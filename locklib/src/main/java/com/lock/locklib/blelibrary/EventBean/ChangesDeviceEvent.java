package com.lock.locklib.blelibrary.EventBean;

import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.EventBean.EventBean;

public class ChangesDeviceEvent extends EventBean {
    public BleBase mBleBase;
    public BleStatus mBleStatus;

    public ChangesDeviceEvent(BleBase bleBase, BleStatus bleStatus) {
        this.mBleBase = bleBase;
        this.mBleStatus = bleStatus;
    }

    public BleBase getmBleBase() {
        return this.mBleBase;
    }

    public void setmBleBase(BleBase bleBase) {
        this.mBleBase = bleBase;
    }

    public BleStatus getmBleStatus() {
        return this.mBleStatus;
    }

    public void setmBleStatus(BleStatus bleStatus) {
        this.mBleStatus = bleStatus;
    }
}
