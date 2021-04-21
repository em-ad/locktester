package com.lock.locklib3.callback;

public interface BatteryCallback {
    void failed();
    void batteryReceived(String result);
}
