package com.lock.locklib2;

import no.nordicsemi.android.ble.ConnectRequest;

public enum ConnectionReason {
    REASON_UNKNOWN( "REASON_UNKNOWN", -1),
    REASON_SUCCESS( "REASON_SUCCESS", 0),
    REASON_TERMINATE_LOCAL_HOST( "REASON_TERMINATE_LOCAL_HOST", 1),
    REASON_TERMINATE_PEER_USER( "REASON_TERMINATE_PEER_USER", 2),
    REASON_LINK_LOSS( "REASON_LINK_LOSS", 3),
    REASON_NOT_SUPPORTED( "REASON_NOT_SUPPORTED", 4),
    REASON_TIMEOUT( "REASON_TIMEOUT", 10);

    String reason;
    int ordinal;

    ConnectionReason(String reason, int ordinal) {
        this.reason = reason;
        this.ordinal = ordinal;
    }

    public static ConnectionReason getValue(int i){
        switch (i){
            case 0:
                return REASON_SUCCESS;
            case 1:
                return REASON_TERMINATE_LOCAL_HOST;
            case 2:
                return REASON_TERMINATE_PEER_USER;
            case 3:
                return REASON_LINK_LOSS;
            case 4:
                return REASON_NOT_SUPPORTED;
            case 10:
                return REASON_TIMEOUT;
            case -1:
            default:
                return REASON_UNKNOWN;
        }
    }
}
