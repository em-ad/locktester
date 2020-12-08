package com.lock.locklib.blelibrary.EventBean;

public class OtherEvent extends EventBean {
    public static final int state_Close = 1;
    public static final int state_disconnect = 1;
    public static final int state_name_Success = 4;
    public static final int state_name_fail = 5;
    public static final int state_pw_Success = 2;
    public static final int state_pw_fail = 3;
    public String mac;
    public boolean result;
    public int state;

    public OtherEvent(int i, String str) {
        setState(i);
        setMac(str);
    }

    public int getState() {
        return this.state;
    }

    public void setState(int i) {
        this.state = i;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String str) {
        this.mac = str;
    }

    public boolean isResult() {
        return this.result;
    }

    public void setResult(boolean z) {
        this.result = z;
    }
}
