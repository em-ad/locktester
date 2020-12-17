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
                ArrayList<ChangesDeviceEvent> events = new ArrayList<>();
                for (int i = 0; i < searcher.sharedPreferences.getSaveBle().BaseList.size(); i++) {
                    ChangesDeviceEvent event = new ChangesDeviceEvent(searcher.sharedPreferences.getSaveBle().BaseList.get(i), new BleStatus());
                    events.add(event);
                }
                bleListLiveData.postValue(events);
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
        ServiceCommand.connect(context, selectedEvent.getmBleBase(), selectedEvent.getmBleStatus());
        return true;
    }

    public static boolean unlock(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        ServiceCommand.send(context, selectedEvent.getmBleBase(), 1);
        return true;
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

    public static String getLockStatus() {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return "ERR";
        switch (selectedEvent.getmBleStatus().getState()) {
            case 1:
                return "CONNECTED";
            case 3:
                return "LOCKED";
            case 4:
                if (selectedEvent.getmBleStatus().LOCK_STA == 0) {
                    return "UNLOCKED";
                } else {
                    return "LOCKED";
                }
            case -2:
                return "DISCONNECTED";
            default:
                return "UNKNOWN";
        }
    }
}
