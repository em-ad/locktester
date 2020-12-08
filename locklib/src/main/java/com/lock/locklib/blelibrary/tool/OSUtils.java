package com.lock.locklib.blelibrary.tool;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class OSUtils {
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    public static final String SYS_EMUI = "sys_emui";
    public static final String SYS_FLYME = "sys_flyme";
    public static final String SYS_MIUI = "sys_miui";
    public static final String SYS_OTHER = "sys_other";

    public enum ROM_TYPE {
        MIUI,
        FLYME,
        EMUI,
        OTHER
    }

    public static ROM_TYPE getSystem(Context context) {
        BleSharedPreferences bleSharedPreferences = new BleSharedPreferences(context);
        String str = bleSharedPreferences.getsysType();
        if (TextUtils.isEmpty(str)) {
            str = SYS_OTHER;
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                if (properties.getProperty(KEY_MIUI_VERSION_CODE, (String) null) == null && properties.getProperty(KEY_MIUI_VERSION_NAME, (String) null) == null) {
                    if (properties.getProperty(KEY_MIUI_INTERNAL_STORAGE, (String) null) == null) {
                        if (properties.getProperty(KEY_EMUI_API_LEVEL, (String) null) == null && properties.getProperty(KEY_EMUI_VERSION, (String) null) == null) {
                            if (properties.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, (String) null) == null) {
                                if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                                    str = SYS_FLYME;
                                }
                                bleSharedPreferences.setsysType(str);
                            }
                        }
                        str = SYS_EMUI;
                        bleSharedPreferences.setsysType(str);
                    }
                }
                str = SYS_MIUI;
                bleSharedPreferences.setsysType(str);
            } catch (IOException e) {
                e.printStackTrace();
                return ROM_TYPE.OTHER;
            }
        }
        char c = 65535;
        int hashCode = str.hashCode();
        if (hashCode != 528833881) {
            if (hashCode != 1956692846) {
                if (hashCode == 1956927330 && str.equals(SYS_MIUI)) {
                    c = 1;
                }
            } else if (str.equals(SYS_EMUI)) {
                c = 0;
            }
        } else if (str.equals(SYS_FLYME)) {
            c = 2;
        }
        if (c == 0) {
            return ROM_TYPE.EMUI;
        }
        if (c == 1) {
            return ROM_TYPE.MIUI;
        }
        if (c != 2) {
            return ROM_TYPE.OTHER;
        }
        return ROM_TYPE.FLYME;
    }

    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty("ro.build.display.id", "");
    }

    private static String getSystemProperty(String str, String str2) {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            return (String) cls.getMethod("get", new Class[]{String.class, String.class}).invoke(cls, new Object[]{str, str2});
        } catch (Exception unused) {
            return str2;
        }
    }
}
