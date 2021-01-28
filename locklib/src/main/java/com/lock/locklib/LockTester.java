package com.lock.locklib;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceListEvent;
import com.lock.locklib.blelibrary.EventBean.EventBean;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
import com.lock.locklib.blelibrary.EventBean.WriteDataEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.main.ServiceCommand;
import com.lock.locklib.blelibrary.search.SearchBle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;

import javax.security.auth.DestroyFailedException;

public class LockTester implements Serializable {

    private static MutableLiveData<ChangesDeviceEvent> selectedEventLiveData = new MutableLiveData<>();
    private static MutableLiveData<ArrayList<ChangesDeviceEvent>> bleListLiveData = new MutableLiveData<>(new ArrayList<>());
    private CountDownTimer listTimer;
    private final static int UNLOCK_CODE = 1;
    private final static int GET_STATUS_CODE = -1;

    public static MutableLiveData<ChangesDeviceEvent> getSelectedEventLiveData() {
        return selectedEventLiveData;
    }

    public static MutableLiveData<ArrayList<ChangesDeviceEvent>> getBleListLiveData() {
        return bleListLiveData;
    }

    public void prepare(SearchBle searcher) {
        EventTool.register(this);

        listTimer = new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                ArrayList<ChangesDeviceEvent> orig = bleListLiveData.getValue();
//                ArrayList<ChangesDeviceEvent> events = new ArrayList<>();
                for (int i = 0; i < searcher.sharedPreferences.getSaveBle().BaseList.size(); i++) {
                    BleStatus status = new BleStatus();
                    boolean found = false;
                    for (int j = 0; j < orig.size(); j++) {
                        if(orig.get(j).getmBleBase().getAddress().equals(searcher.sharedPreferences.getSaveBle().BaseList.get(i).getAddress())){
                            status = orig.get(j).getmBleStatus();
                            found = true;
                            break;
                        }
                    }
                    ChangesDeviceEvent event = new ChangesDeviceEvent(searcher.sharedPreferences.getSaveBle().BaseList.get(i), status);
                    if(!found)
                        orig.add(event);
                }
                bleListLiveData.postValue(orig);
                this.start();
            }

        };

        listTimer.start();
    }

    public void destroy(){
        EventTool.unregister(this);
        listTimer.cancel();
        listTimer = null;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(EventBean eventBean) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return;
        if (eventBean instanceof ChangesDeviceListEvent) {
            bleListLiveData.postValue(((ChangesDeviceListEvent) eventBean).bleList);
            ArrayList<ChangesDeviceEvent> bleList = bleListLiveData.getValue();
            if(bleList == null)
                bleList = new ArrayList<>();
            for (int i = 0; i < bleList.size(); i++) {
                if (selectedEvent.getmBleBase().getAddress().equals(bleList.get(i).mBleBase.getAddress())) {
                    BleStatus status = selectedEvent.getmBleStatus();
                    status.setState(bleList.get(i).mBleStatus.getState());
                    selectedEvent.setmBleStatus(status);
                    selectedEventLiveData.postValue(selectedEvent);
                    break;
                }
            }
        } else if (eventBean instanceof ChangesDeviceEvent) {
            BleStatus status = selectedEvent.getmBleStatus();
            status.setState(((ChangesDeviceEvent) eventBean).getmBleStatus().getState());
            status.setLOCK_STA(((ChangesDeviceEvent) eventBean).getmBleStatus().LOCK_STA);
            selectedEvent.setmBleStatus(status);
            selectedEventLiveData.postValue(selectedEvent);
        }
    }

    public static void eventSelected(ChangesDeviceEvent event) {
        selectedEventLiveData.postValue(event);
    }

    private static String getActualCode(String bleCode){
        String code = bleCode;
        if(code.length() > 4)
            code = code.substring(4).toUpperCase();
        String res = "";
        for (int i = 0; i < code.length(); i+=2) {
            res = res + code.charAt(i);
                if(code.length() > i + 1)
            res = res + code.charAt(i + 1);
                if(code.length() > i + 2)
            res = res + ":";
        }
        return res;
    }

    public static boolean eventSelected(String longCode) {
        String code = longCode;
        if(longCode.length() >= 12)
        code = longCode.substring(longCode.length() - 12);
        if(getBleListLiveData().getValue() == null)
            return false;
        ArrayList<ChangesDeviceEvent> events = getBleListLiveData().getValue();
        for (int i = 0; i < events.size(); i++) {
            if(events.get(i).getmBleBase().getAddress().replace(":","").toLowerCase().equals(code) || events.get(i).getmBleBase().getAddress().toLowerCase().equals(code)) {
                eventSelected(events.get(i));
                return true;
            }
        }
        return false;
    }

    public static boolean deviceExists(String longCode) {
        String code = longCode;
        if(longCode.length() >= 12)
            code = longCode.substring(longCode.length() - 12);
        if(getBleListLiveData().getValue() == null)
            return false;
        ArrayList<ChangesDeviceEvent> events = getBleListLiveData().getValue();
        for (int i = 0; i < events.size(); i++) {
            if(events.get(i).getmBleBase().getAddress().replace(":","").toLowerCase().equals(code) || events.get(i).getmBleBase().getAddress().toLowerCase().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static boolean connect(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        BleBase base = new BleBase();
        base.setAddress(selectedEvent.getmBleBase().getAddress());
        ServiceCommand.connect(context, base, new BleStatus());
        return true;
    }

    public static boolean connectByAddress(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.connect(context, base, new BleStatus());
        return true;
    }

    public static boolean disconnect(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        ServiceCommand.disconnect(context, selectedEvent.getmBleBase());
        return true;
    }

    public static boolean disconnectByAddress(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.disconnect(context, base);
        return true;
    }

    public static boolean unlock(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        ServiceCommand.send(context, selectedEvent.getmBleBase(), UNLOCK_CODE);
        return true;
    }

    public static boolean unlockByAddress(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.send(context, base, UNLOCK_CODE);
        return true;
    }

    public static void getStatus(Context context){
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return;
        ServiceCommand.send(context, selectedEvent.getmBleBase(), GET_STATUS_CODE);
    }

    public static void getStatusByAddress(Context context, String bleCode){
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.send(context, base, GET_STATUS_CODE);
    }

    public static boolean authenticate(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        BleBase bleBaseInner = selectedEvent.getmBleBase();
        bleBaseInner.setPassWord("123456");
        selectedEvent.setmBleBase(bleBaseInner);
        selectedEventLiveData.postValue(selectedEvent);
        ServiceCommand.authenticated(context, selectedEvent.getmBleBase());
        return true;
    }

    public static boolean authenticate(Context context, String password) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        BleBase bleBaseInner = selectedEvent.getmBleBase();
        bleBaseInner.setPassWord(password);
        selectedEvent.setmBleBase(bleBaseInner);
        selectedEventLiveData.postValue(selectedEvent);
        ServiceCommand.authenticated(context, selectedEvent.getmBleBase());
        return true;
    }

    public static boolean authenticateByAddress(Context context, String bleCode, String password) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        base.setPassWord(password);
        ServiceCommand.authenticated(context, base);
        return true;
    }

    public static String getLockStatusByStatus(BleStatus bleStatus) {
        if (bleStatus == null)
            return "ERR";
        switch (bleStatus.getState()) {
            case -3:
                return "AUTHENTICATION_FAILED";
            case 1:
                return "CONNECTED";
            case 0:
                return "CONNECTING";
            case 2:
                return "NEW";
            case 3:
                return "AUTHENTICATED";
            case 4:
                if (bleStatus.LOCK_STA == 0) {
                    return "UNLOCKED";
                } else {
                    return "LOCKED";
                }
            case -1:
            case -2:
                return "DISCONNECTED";
            default:
                return "UNKNOWN " + bleStatus.getState();
        }
    }

    public static String getLockStatus() {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return "ERR";
        switch (selectedEvent.getmBleStatus().getState()) {
            case -3:
                return "AUTHENTICATION_FAILED";
            case 1:
                return "CONNECTED";
            case 0:
                return "CONNECTING";
            case 2:
                return "NEW";
            case 3:
                return "AUTHENTICATED";
            case 4:
                if (selectedEvent.getmBleStatus().LOCK_STA == 0) {
                    return "UNLOCKED";
                } else {
                    return "LOCKED";
                }
            case -1:
            case -2:
                return "DISCONNECTED";
            default:
                return "UNKNOWN " + selectedEvent.getmBleStatus().getState();
        }
    }
}
