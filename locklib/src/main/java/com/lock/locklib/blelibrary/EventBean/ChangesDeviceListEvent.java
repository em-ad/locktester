package com.lock.locklib.blelibrary.EventBean;
import com.lock.locklib.blelibrary.EventBean.EventBean;

import java.util.ArrayList;

public class ChangesDeviceListEvent extends EventBean {
    public ArrayList<ChangesDeviceEvent> bleList = new ArrayList<>();

    public void Changes(ChangesDeviceEvent changesDeviceEvent) {
        int i = 0;
        while (true) {
            if (i >= this.bleList.size()) {
                i = -1;
                break;
            } else if (changesDeviceEvent.getmBleBase().getAddress().equals(this.bleList.get(i).getmBleBase().getAddress())) {
                break;
            } else {
                i++;
            }
        }
        if (changesDeviceEvent.getmBleStatus().getState() == -2 || changesDeviceEvent.getmBleStatus().getState() == -1) {
            remo(i);
        } else {
            set(changesDeviceEvent, i);
        }
        EventTool.post(this);
    }

    private void set(ChangesDeviceEvent changesDeviceEvent, int i) {
        if (i >= 0) {
            this.bleList.set(i, changesDeviceEvent);
        } else {
            this.bleList.add(changesDeviceEvent);
        }
    }

    private void remo(int i) {
        if (i >= 0) {
            this.bleList.remove(i);
        }
    }

    public ArrayList<ChangesDeviceEvent> getBleList() {
        return this.bleList;
    }

    public void setBleList(ArrayList<ChangesDeviceEvent> arrayList) {
        this.bleList = arrayList;
    }
}
