package com.lock.locklib3.callback;

import com.lock.locklib3.LockStatusEnum;

public interface StatusCallback {
    void failed();
    void statusReceived(LockStatusEnum status);
}
