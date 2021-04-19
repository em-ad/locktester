package com.lock.locklib2;

public interface CommandCallback {
    default void commandExecuted(OperationStatus status) {}
    default void commandExecuted(OperationStatus status, String code) {}
}
