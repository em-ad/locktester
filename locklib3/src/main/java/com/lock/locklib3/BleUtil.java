package com.lock.locklib3;

import android.util.Log;

public class BleUtil {

    public static String convertAddress(String address) {
        String oldAddress = address.toUpperCase().substring(4);
        String res = "";
        for (int i = 0; i < 12; i++) {
            if (i % 2 == 0 && i != 0) {
                res = res + ":";
            }
            res = res + oldAddress.substring(i, i + 1);
        }
        Log.e("TAG", "convertAddress: " + res);
        return res;
    }
}

