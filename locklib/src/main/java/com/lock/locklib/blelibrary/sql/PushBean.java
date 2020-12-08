package com.lock.locklib.blelibrary.sql;

public class PushBean {
    public String deviceId = "";
    public Integer message = 0;
    public Long pushDate = 0L;

    public Integer getMessage() {
        return this.message;
    }

    public void setMessage(Integer num) {
        this.message = num;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public Long getPushDate() {
        return this.pushDate;
    }

    public void setPushDate(Long l) {
        this.pushDate = l;
    }
}
