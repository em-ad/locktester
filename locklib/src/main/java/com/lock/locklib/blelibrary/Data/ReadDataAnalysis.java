package com.lock.locklib.blelibrary.Data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.lock.locklib.OperationStatus;
import com.lock.locklib.blelibrary.Adapter.BleItem;
import com.lock.locklib.blelibrary.CommandCallback;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
import com.lock.locklib.blelibrary.sql.ChatDB;
import com.lock.locklib.blelibrary.sql.PushBean;
import com.lock.locklib.blelibrary.tool.BleTool;
import java.io.ByteArrayOutputStream;

public class ReadDataAnalysis {
    public static final byte Auto_Unlock_Close = 41;
    public static final byte Auto_Unlock_Open = 40;
    public static final byte Consignment_Close = 50;
    public static final byte Consignment_Open = 49;
    private static final byte Success = 0;
    private static final String TAG = "ReadBle";
    public static final byte Vibration_Close = 44;
    public static final byte Vibration_Open = 43;
    private static final byte check = 6;
    private static final byte fail = 1;
    private static final byte inspect = 2;
    private static final byte lock = 8;
    public static final byte mName = 47;
    public static final byte mPW = 5;
    public static final byte mlock = 15;
    private static final byte power = 2;
    private static final byte read = 5;
    private static final byte token = 2;
    private static final byte token_fail = 3;
    private static final byte unlock = 2;

    public static void Read(Context context, BleItem bleItem, byte[] bArr, CommandCallback callback) {
        if (bArr.length >= 16) {
            byte[] read2 = BleCommon.read(bArr);
            Log.e(TAG, "BYTES READ =" + BleTool.ByteToString(read2));
            String res = BleTool.ByteToString2(read2);
            if(res.toLowerCase().startsWith("05020100")){
                callback.commandExecuted(OperationStatus.UNLOCKED);
            } else if (res.toLowerCase().startsWith("05020101")){
                callback.commandExecuted(OperationStatus.CONNECTING);
            } else if (res.toLowerCase().startsWith("050f0100")){
                callback.commandExecuted(OperationStatus.UNLOCKED);
            } else if (res.toLowerCase().startsWith("050f0101") || res.toLowerCase().startsWith("05080100")){
                callback.commandExecuted(OperationStatus.LOCKED);
            } else if (res.toLowerCase().startsWith("020201")){
                callback.commandExecuted(OperationStatus.BATTERY, res.substring(6, 8));
                return;
            }

            byte b = read2[0];
            if (b != 2) {
                if (b == 5) {
                    byte b2 = read2[1];
                    if (b2 != 2) {
                        if (b2 != 5) {
                            if (b2 != 8) {
                                if (b2 == 15) {
                                    bleItem.changesData.getmBleStatus().setState(4);
                                    bleItem.changesData.getmBleStatus().setLOCK_STA(read2[3]);
                                } else if (b2 != 47) {
                                    if (b2 != 40) {
                                        if (b2 != 41) {
                                            if (b2 != 43) {
                                                if (b2 != 44) {
                                                    if (b2 != 49) {
                                                        if (b2 == 50 && read2[3] == 0) {
                                                            bleItem.changesData.getmBleStatus().setState(4);
                                                            bleItem.changesData.getmBleStatus().setConsignment(1);
                                                            saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 13);
                                                        }
                                                    } else if (read2[3] == 0) {
                                                        bleItem.changesData.getmBleStatus().setState(4);
                                                        bleItem.changesData.getmBleStatus().setConsignment(0);
                                                        saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 12);
                                                    }
                                                } else if (read2[3] == 0) {
                                                    bleItem.changesData.getmBleStatus().setState(4);
                                                    bleItem.changesData.getmBleStatus().setVibration(1);
                                                    saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 5);
                                                }
                                            } else if (read2[3] == 0) {
                                                bleItem.changesData.getmBleStatus().setState(4);
                                                bleItem.changesData.getmBleStatus().setVibration(0);
                                                saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 4);
                                            }
                                        } else if (read2[3] == 0) {
                                            bleItem.changesData.getmBleStatus().setState(4);
                                            bleItem.changesData.getmBleStatus().setAuto_Unlock(1);
                                            saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 3);
                                        }
                                    } else if (read2[3] == 0) {
                                        bleItem.changesData.getmBleStatus().setState(4);
                                        bleItem.changesData.getmBleStatus().setAuto_Unlock(0);
                                        saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 2);
                                    }
                                } else if (read2[3] == 0) {
                                    bleItem.changesData.getmBleBase().setName(bleItem.name);
                                    bleItem.changesData.getmBleStatus().setState(5);
                                    EventTool.post(new OtherEvent(4, bleItem.changesData.getmBleBase().getAddress()));
                                    saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 10);
                                } else {
                                    EventTool.post(new OtherEvent(5, bleItem.changesData.getmBleBase().getAddress()));
                                    saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 11);
                                }
                            } else if (read2[3] == 0) {
                                bleItem.changesData.getmBleStatus().setState(4);
                                bleItem.changesData.getmBleStatus().setLOCK_STA(1);
                            }
                        } else if (read2[3] == 0) {
                            bleItem.changesData.getmBleBase().setPassWord(bleItem.password);
                            bleItem.changesData.getmBleStatus().setState(5);
                            EventTool.post(new OtherEvent(2, bleItem.changesData.getmBleBase().getAddress()));
                            saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 8);
                        } else {
                            EventTool.post(new OtherEvent(3, bleItem.changesData.getmBleBase().getAddress()));
                            saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 9);
                        }
                    } else if (read2[3] == 0) {
                        bleItem.changesData.getmBleStatus().setState(4);
                        bleItem.changesData.getmBleStatus().setLOCK_STA(0);
                        if (bleItem.changesData.getmBleStatus().getAuto_Unlock().booleanValue()) {
                            saveChat(context, bleItem.changesData.getmBleBase().getAddress(), 14);
                        }
                    }
                } else if (b == 6) {
                    byte b3 = read2[1];
                    if (b3 != 2) {
                        if (b3 == 3 && read2[3] == 1) {
                            bleItem.changesData.getmBleStatus().setState(-3);
                            callback.commandExecuted(OperationStatus.AUTHENTICATION_FAILED);
                        }
                    } else if (read2[2] == 1) {
                        bleItem.changesData.getmBleStatus().setState(-3);
                        callback.commandExecuted(OperationStatus.AUTHENTICATION_FAILED);
                    } else {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byteArrayOutputStream.write(read2, 3, 4);
                        if (TextUtils.isEmpty(BleTool.ByteToString(bleItem.changesData.getmBleStatus().getToken()))) {
                            bleItem.changesData.getmBleStatus().setToken(byteArrayOutputStream.toByteArray());
                            bleItem.changesData.getmBleStatus().setState(3);
                            callback.commandExecuted(OperationStatus.AUTHENTICATED);
                            return;
                        }
                        bleItem.changesData.getmBleStatus().setState(4);
                    }
                }
            } else if (read2[1] == 2 && read2[3] != 255) {
                bleItem.changesData.getmBleStatus().setState(4);
                bleItem.changesData.getmBleStatus().setPOWER(read2[3]);
            }
        }
    }

    public static void saveChat(Context context, String str, int i) {
        PushBean pushBean = new PushBean();
        pushBean.setDeviceId(str);
        pushBean.setMessage(Integer.valueOf(i));
        pushBean.setPushDate(Long.valueOf(System.currentTimeMillis()));
        try {
            ChatDB.addstu(context, pushBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
