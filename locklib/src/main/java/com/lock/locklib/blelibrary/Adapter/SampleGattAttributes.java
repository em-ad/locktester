package com.lock.locklib.blelibrary.Adapter;

import java.util.UUID;

public class SampleGattAttributes {
    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID NotifyCharacteristicUUID = UUID.fromString("000036f6-0000-1000-8000-00805f9b34fb");
    public static final UUID NotifyServiceUUID = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
    public static final UUID WriteCharacteristicUUID = UUID.fromString("000036f5-0000-1000-8000-00805f9b34fb");
    public static final UUID WriteServiceUUID = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
}
