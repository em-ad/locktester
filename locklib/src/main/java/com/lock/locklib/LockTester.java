package com.lock.locklib;

import android.content.Context;
import android.util.Log;

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

    public static ChangesDeviceEvent selectedEvent;
    private static ArrayList<ChangesDeviceEvent> bleList;

    public void prepare(){
        EventTool.register(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(EventBean eventBean) {
        if(eventBean instanceof ChangesDeviceListEvent){
            int before = bleList != null ? bleList.size() : 0;
            bleList = ((ChangesDeviceListEvent) eventBean).bleList;
            int after = bleList.size();
            Log.e("tag", "onEvent: from " + before + " to " + after + " now >" + new Gson().toJson(bleList)  );

            if(bleList == null)
                return;

            for (int i = 0; i < bleList.size(); i++) {
                if(selectedEvent.getmBleBase().getAddress().equals(bleList.get(i).mBleBase.getAddress())){
                    BleStatus status = selectedEvent.getmBleStatus();
                    status.setState(bleList.get(i).mBleStatus.getState());
                    selectedEvent.setmBleStatus(status);
                    break;
                }
            }
        } else if(eventBean instanceof ChangesDeviceEvent){
            BleStatus status = selectedEvent.getmBleStatus();
            status.setState(((ChangesDeviceEvent) eventBean).getmBleStatus().getState());
            status.setLOCK_STA(((ChangesDeviceEvent) eventBean).getmBleStatus().LOCK_STA);
            selectedEvent.setmBleStatus(status);
        }
    }

    private static void fetchList(ArrayList<BleBase> prefList){
//        ArrayList<BleBase> prefList = SearchBle.getInstance(context).sharedPreferences.getSaveBle().BaseList != null ? SearchBle.getInstance(context).sharedPreferences.getSaveBle().BaseList : new ArrayList<>();
        if(bleList == null || bleList.size() == 0) {
            ArrayList<ChangesDeviceEvent> res = new ArrayList<>();
            for (int i = 0; i < prefList.size(); i++) {
                res.add(new ChangesDeviceEvent(prefList.get(i), new BleStatus()));
            }
            bleList = res;
        } else {
            for (int i = 0; i < prefList.size(); i++) {
                boolean found = false;
                for (int j = 0; j < bleList.size(); j++) {
                    if(bleList.get(j).getmBleBase().getAddress().equals(prefList.get(i).getAddress())){
                        ChangesDeviceEvent old = bleList.get(j);
                        bleList.remove(bleList.get(j));
                        old.setmBleBase(prefList.get(i));
                        bleList.add(old);
                        found = true;
                    }
                }
                if(!found)
                    bleList.add(new ChangesDeviceEvent(prefList.get(i), new BleStatus()));
            }
        }
    }

    public static void eventSelected(ChangesDeviceEvent event) {
        selectedEvent = event;
        BleBase bleBaseInner = event.getmBleBase();
        bleBaseInner.setPassWord("123456");
        selectedEvent.setmBleBase(bleBaseInner);
    }

    public static void connect(Context context) {
        if (selectedEvent == null)
            return;
        ServiceCommand.connect(context, selectedEvent.getmBleBase(), selectedEvent.getmBleStatus());
    }

    public static void unlock(Context context) {
        if (selectedEvent == null)
            return;
        ServiceCommand.send(context, selectedEvent.getmBleBase(), 1);
    }

    public static void authenticate(Context context) {
        if (selectedEvent == null)
            return;

        ServiceCommand.authenticated(context, selectedEvent.getmBleBase());
    }

    public static String getLockStatus() {
        if (selectedEvent == null)
            return "ERR";
        switch (selectedEvent.getmBleStatus().getState()){
            case 1:
                return "CONNECTED";
            case 3:
                return "LOCKED";
            case 4:
                if(selectedEvent.getmBleStatus().LOCK_STA == 0){
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
