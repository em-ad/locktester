package com.lock.locklib;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.lock.locklib.blelibrary.CommandCallback;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceListEvent;
import com.lock.locklib.blelibrary.EventBean.EventBean;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
import com.lock.locklib.blelibrary.EventBean.WriteDataEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.main.BleExecutor;
import com.lock.locklib.blelibrary.main.ServiceCommand;
import com.lock.locklib.blelibrary.search.SearchBle;
import com.lock.locklib.OperationStatus.*;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;

import javax.security.auth.DestroyFailedException;

public class LockTester implements Serializable, CommandCallback {

    private static MutableLiveData<ChangesDeviceEvent> selectedEventLiveData = new MutableLiveData<>();
    private static MutableLiveData<ArrayList<ChangesDeviceEvent>> bleListLiveData = new MutableLiveData<>(new ArrayList<>());
    private CountDownTimer listTimer;
    private final static int UNLOCK_CODE = 1;
    private final static int GET_STATUS_CODE = -1;
    private static LockTester instance;
    CommandCallback callback;

    public void setCallback(CommandCallback callback) {
        this.callback = callback;
    }

    public static LockTester getInstance() {
        if(instance == null)
            instance = new LockTester();
        return instance;
    }

    public MutableLiveData<ChangesDeviceEvent> getSelectedEventLiveData() {
        return selectedEventLiveData;
    }

    public MutableLiveData<ArrayList<ChangesDeviceEvent>> getBleListLiveData() {
        return bleListLiveData;
    }

    public void prepare(SearchBle searcher, Context context) {
        Intent intent = new Intent();
        intent.setAction(ServiceCommand.CONNECT_ACTION_START);
        BleExecutor.getInstance().execute(intent, 0, 0, context, instance);

//        EventTool.register(instance);

//        listTimer = new CountDownTimer(1000, 1000) {
//
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                ArrayList<ChangesDeviceEvent> orig = bleListLiveData.getValue();
////                ArrayList<ChangesDeviceEvent> events = new ArrayList<>();
//                for (int i = 0; i < searcher.sharedPreferences.getSaveBle().BaseList.size(); i++) {
//                    BleStatus status = new BleStatus();
//                    boolean found = false;
//                    for (int j = 0; j < orig.size(); j++) {
//                        if(orig.get(j).getmBleBase().getAddress().equals(searcher.sharedPreferences.getSaveBle().BaseList.get(i).getAddress())){
//                            status = orig.get(j).getmBleStatus();
//                            found = true;
//                            break;
//                        }
//                    }
//                    ChangesDeviceEvent event = new ChangesDeviceEvent(searcher.sharedPreferences.getSaveBle().BaseList.get(i), status);
//                    if(!found)
//                        orig.add(event);
//                }
//                bleListLiveData.postValue(orig);
//                instance.start();
//            }
//
//        };
//
//        listTimer.start();
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

    public void eventSelected(ChangesDeviceEvent event) {
        selectedEventLiveData.postValue(event);
    }

    private String getActualCode(String bleCode){
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

    public boolean eventSelected(String longCode) {
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

    public boolean deviceExists(String longCode) {
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

    public boolean connect(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        BleBase base = new BleBase();
        base.setAddress(selectedEvent.getmBleBase().getAddress());
        ServiceCommand.connect(context, base, new BleStatus(), instance);
        return true;
    }

    public boolean connectByAddress(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.connect(context, base, new BleStatus(), instance);
        return true;
    }

    public boolean disconnect(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        ServiceCommand.disconnect(context, selectedEvent.getmBleBase(), instance);
        return true;
    }

    public boolean disconnectByAddress(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.disconnect(context, base, instance);
        return true;
    }

    public boolean unlock(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        ServiceCommand.send(context, selectedEvent.getmBleBase(), UNLOCK_CODE, instance);
        return true;
    }

    public boolean unlockByAddress(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.send(context, base, UNLOCK_CODE, instance);
        return true;
    }

    public boolean unlockByAddressAlt(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.sendAlt(context, base, UNLOCK_CODE, instance);
        return true;
    }

    public void getStatus(Context context){
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return;
        ServiceCommand.send(context, selectedEvent.getmBleBase(), GET_STATUS_CODE, instance);
    }

    public void getStatusByAddress(Context context, String bleCode){
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.send(context, base, GET_STATUS_CODE, instance);
    }

    public boolean authenticate(Context context) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        BleBase bleBaseInner = selectedEvent.getmBleBase();
        bleBaseInner.setPassWord("123456");
        selectedEvent.setmBleBase(bleBaseInner);
        selectedEventLiveData.postValue(selectedEvent);
        ServiceCommand.authenticated(context, selectedEvent.getmBleBase(), instance);
        return true;
    }

    public boolean authenticate(Context context, String password) {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return false;
        BleBase bleBaseInner = selectedEvent.getmBleBase();
        bleBaseInner.setPassWord(password);
        selectedEvent.setmBleBase(bleBaseInner);
        selectedEventLiveData.postValue(selectedEvent);
        ServiceCommand.authenticated(context, selectedEvent.getmBleBase(), instance);
        return true;
    }

    public boolean authenticateByAddress(Context context, String bleCode, String password) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        base.setPassWord(password);
        ServiceCommand.authenticated(context, base, instance);
        return true;
    }

    public boolean authenticateByAddressAlt(Context context, String bleCode, String password) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        base.setPassWord(password);
        ServiceCommand.authenticatedAlt(context, base, instance);
        return true;
    }


    public void getBatteryByAddress(Context context, String bleCode) {
        BleBase base = new BleBase();
        base.setAddress(getActualCode(bleCode));
        ServiceCommand.getBattery(context, base, instance);
    }

    public OperationStatus getLockStatusByStatus(BleStatus bleStatus) {
        if (bleStatus == null)
            return OperationStatus.ERR;
        switch (bleStatus.getState()) {
            case -3:
                return OperationStatus.AUTHENTICATION_FAILED;
            case 1:
                return OperationStatus.CONNECTED;
            case 0:
                return OperationStatus.CONNECTING;
            case 2:
                return OperationStatus.NEW;
            case 3:
                return OperationStatus.AUTHENTICATED;
            case 4:
                if (bleStatus.LOCK_STA == 0) {
                    return OperationStatus.UNLOCKED;
                } else {
                    return OperationStatus.LOCKED;
                }
            case -1:
            case -2:
                return OperationStatus.DISCONNECTED;
            default:
                return OperationStatus.UNKNOWN;
        }
    }

    public OperationStatus getLockStatus() {
        ChangesDeviceEvent selectedEvent = selectedEventLiveData.getValue();
        if (selectedEvent == null)
            return OperationStatus.ERR;
        switch (selectedEvent.getmBleStatus().getState()) {
            case -3:
                return OperationStatus.AUTHENTICATION_FAILED;
            case 1:
                return OperationStatus.CONNECTED;
            case 0:
                return OperationStatus.CONNECTING;
            case 2:
                return OperationStatus.NEW;
            case 3:
                return OperationStatus.AUTHENTICATED;
            case 4:
                if (selectedEvent.getmBleStatus().LOCK_STA == 0) {
                    return OperationStatus.UNLOCKED;
                } else {
                    return OperationStatus.LOCKED;
                }
            case -1:
            case -2:
                return OperationStatus.DISCONNECTED;
            default:
                return OperationStatus.UNKNOWN;
        }
    }

    @Override
    public void commandExecuted(OperationStatus status) {
        this.callback.commandExecuted(status);
    }

    @Override
    public void commandExecuted(OperationStatus status, String code) {
        this.callback.commandExecuted(status, code);
    }
}
