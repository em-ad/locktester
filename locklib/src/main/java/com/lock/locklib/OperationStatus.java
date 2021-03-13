package com.lock.locklib;

public enum OperationStatus {
    LOCKED("LOCKED"),
    UNLOCKED("UNLOCKED"),
    CONNECTED("CONNECTED"),
    DISCONNECTED("DISCONNECTED"),
    NEW("NEW"),
    UNKNOWN("UNKNOWN"),
    ERR("ERR"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED"),
    AUTHENTICATED("AUTHENTICATED"),
    CONNECTING("CONNECTING"),
    BATTERY("BATTERY");

    String name;

    OperationStatus(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
