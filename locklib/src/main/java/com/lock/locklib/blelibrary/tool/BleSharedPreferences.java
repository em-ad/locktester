package com.lock.locklib.blelibrary.tool;

import android.content.Context;
import android.content.SharedPreferences;

import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.SaveBleEvent;
import com.google.gson.Gson;

public class BleSharedPreferences {

    private String SETTING_INFOS = "BleSharedPreferences";
    private Gson gson;
    private SharedPreferences settings;
    private String shared_SaveBle = "shared_SaveBle";
    private String shared_sysType = "sysType";

    public BleSharedPreferences(Context context) {
        this.settings = context.getSharedPreferences(SETTING_INFOS, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void setsysType(String str) {
        this.settings.edit().putString(this.shared_sysType, str).apply();
    }

    public String getsysType() {
        return this.settings.getString(this.shared_sysType, "");
    }

    public void setSaveBle(SaveBleEvent saveBleEvent) {
        this.settings.edit().putString(this.shared_SaveBle, this.gson.toJson((Object) saveBleEvent)).apply();
        EventTool.post(saveBleEvent);
    }

    public SaveBleEvent getSaveBle() {
        SaveBleEvent saveBleEvent = (SaveBleEvent) this.gson.fromJson(this.settings.getString(this.shared_SaveBle, ""), SaveBleEvent.class);
        return saveBleEvent == null ? new SaveBleEvent() : saveBleEvent;
    }
}
