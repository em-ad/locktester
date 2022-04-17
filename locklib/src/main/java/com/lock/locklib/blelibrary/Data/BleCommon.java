package com.lock.locklib.blelibrary.Data;

import android.util.Log;
import com.lock.locklib.blelibrary.tool.BleTool;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class BleCommon {

    private static final String TAG = "BleCommon";
    public static final byte[] defaultkey = {32, 87, 47, 82, 54, 75, 63, 71, 48, 80, 65, 88, 17, 99, 45, 43};
    public static final byte[] mAutoUnlockClose = {5, 39, 1, 1};
    public static final byte[] mAutoUnlockOpen = {5, 39, 1, 0};
    public static final byte[] mConsignmentClose = {5, 48, 1, 1};
    public static final byte[] mConsignmentOpen = {5, 48, 1, 0};
    public static final byte[] mNAME1 = {5, 45, 8};
    public static final byte[] mNAME2 = {5, 46, 8};
    public static final byte[] mNewPW = {5, 4, 6};
    public static final byte[] mOldPW = {5, 3, 6};
    public static final byte[] mToKen = {6, 1, 6};
    public static final byte[] mToKenAlt = {6, 1, 1, 1};
    public static final byte[] mUnlock = {5, 1, 1, 1};
    public static final byte[] mUnlockAlt = {5, 1, 6};
    public static final byte[] mStatus = {5, 14, 1, 1};
    public static final byte[] mBatteryQuery = {2, 1, 1, 1};
    public static final byte[] mVibrationClose = {5, 42, 1, 1};
    public static final byte[] mVibrationOpen = {5, 42, 1, 0};
    public static final byte[] mlock = {5, 7, 1, 1};

    public static byte[] addCrcAndEnd(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] bArr = new byte[(16 - byteArrayOutputStream.size())];
        new Random().nextBytes(bArr);
        byteArrayOutputStream.write(bArr, 0, bArr.length);
        Log.e(TAG, "Encrypting " + BleTool.ByteToString(byteArrayOutputStream.toByteArray()));
        byte[] Encrypt = Encrypt(byteArrayOutputStream.toByteArray());
        return Encrypt;
    }

    public static byte[] read(byte[] bArr) {
        return Decrypt(bArr);
    }

    public static byte[] Encrypt(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(defaultkey, "AES");
            Cipher instance = Cipher.getInstance("AES/ECB/NoPadding");
            instance.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return instance.doFinal(bArr);
        } catch (Exception unused) {
            return null;
        }
    }

    private static byte[] Decrypt(byte[] bArr) {
        Log.e(TAG, "Decrypt: " + bArr.length + " " + bArr );
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(defaultkey, "AES");
            Cipher instance = Cipher.getInstance("AES/ECB/NoPadding");
            instance.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return instance.doFinal(bArr);
        } catch (Exception unused) {
            return null;
        }
    }
}
