package com.lock.locklib.blelibrary.EventBean;
import com.lock.locklib.blelibrary.EventBean.EventBean;

import org.greenrobot.eventbus.EventBus;

public class EventTool {
    public static void post(EventBean eventBean) {
        EventBus.getDefault().post(eventBean);
    }

    public static void register(Object obj) {
        EventBus.getDefault().register(obj);
    }

    public static void unregister(Object obj) {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(obj);
    }
}
