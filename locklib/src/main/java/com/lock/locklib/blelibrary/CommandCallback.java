package com.lock.locklib.blelibrary;

import com.lock.locklib.OperationStatus;

public interface CommandCallback {
    default void commandExecuted(OperationStatus status) {}
    default void commandExecuted(OperationStatus status, String code) {}
}
