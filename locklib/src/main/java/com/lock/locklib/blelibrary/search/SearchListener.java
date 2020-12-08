package com.lock.locklib.blelibrary.search;

import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;

public class SearchListener {

    public interface ScanDataListener {
        void onLeScan(byte[] bArr, BleBase bleBase, BleStatus bleStatus);
    }

    public interface ScanListener {
        void onLeScan(BleBase bleBase, BleStatus bleStatus);
    }
}
