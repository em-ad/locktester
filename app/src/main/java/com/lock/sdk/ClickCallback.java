package com.lock.sdk;

import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.base.BleBase;

interface ClickCallback {
    public void bleClicked(ChangesDeviceEvent event);
}
