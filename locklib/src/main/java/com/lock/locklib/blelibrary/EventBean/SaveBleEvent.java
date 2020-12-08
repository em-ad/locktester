package com.lock.locklib.blelibrary.EventBean;

import com.lock.locklib.blelibrary.base.BleBase;

import java.util.ArrayList;

public class SaveBleEvent extends EventBean {

    public ArrayList<BleBase> BaseList = new ArrayList<>();

    public ArrayList<BleBase> getBaseList() {
        return this.BaseList;
    }

    public void setBaseList(ArrayList<BleBase> arrayList) {
        this.BaseList = arrayList;
    }
}
