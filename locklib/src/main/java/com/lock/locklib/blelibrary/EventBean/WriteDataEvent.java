package com.lock.locklib.blelibrary.EventBean;

import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.EventBean.EventBean;

public class WriteDataEvent extends EventBean {
    private byte[] data;
    private BleBase mBase;

    public WriteDataEvent(BleBase bleBase, byte[] bArr) {
        setmBase(bleBase);
        setData(bArr);
    }

    public BleBase getmBase() {
        return this.mBase;
    }

    public void setmBase(BleBase bleBase) {
        this.mBase = bleBase;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] bArr) {
        this.data = bArr;
    }
}
