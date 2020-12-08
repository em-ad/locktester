package com.lock.locklib.blelibrary.sql;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.PushEvent;
import java.util.ArrayList;

public class ChatDB {
    public static void addstu(Context context, PushBean pushBean) throws Exception {
        SQLiteDatabase readableDatabase = new Mysqlite(context).getReadableDatabase();
        readableDatabase.execSQL("insert into myChat(message,deviceId,pushDate) values(?,?,?)", new Object[]{pushBean.getMessage(), pushBean.getDeviceId(), pushBean.getPushDate()});
        readableDatabase.close();
        InspectStudent(context, pushBean.getDeviceId());
    }

    public static void delstu(Context context, String str) {
        SQLiteDatabase readableDatabase = new Mysqlite(context).getReadableDatabase();
        readableDatabase.execSQL("delete from myChat where deviceId=?", new String[]{str});
        readableDatabase.close();
    }

    public static void InspectStudent(Context context, String str) {
        SQLiteDatabase readableDatabase = new Mysqlite(context).getReadableDatabase();
        readableDatabase.execSQL(" delete from myChat where deviceId=? and (select count(pushDate) from myChat)> 100 and pushDate in (select pushDate from myChat order by pushDate desc limit (select count(pushDate) from myChat) offset 100 )", new String[]{str});
        readableDatabase.close();
    }

    @SuppressLint({"StaticFieldLeak"})
    public static void queryStudent(final Context context, String str) {
        new AsyncTask<String, Integer, PushEvent>() {
            /* access modifiers changed from: protected */
            public PushEvent doInBackground(String... strArr) {
                String str = strArr[0];
                SQLiteDatabase readableDatabase = new Mysqlite(context).getReadableDatabase();
                String[] strArr2 = {str};
                ArrayList arrayList = new ArrayList();
                Cursor rawQuery = readableDatabase.rawQuery("SELECT * FROM myChat where deviceId=? order by pushDate desc", strArr2);
                while (rawQuery.moveToNext()) {
                    PushBean pushBean = new PushBean();
                    String[] columnNames = rawQuery.getColumnNames();
                    for (int i = 0; i < columnNames.length; i++) {
                        Log.e("abc", "names=" + columnNames[i]);
                    }
                    pushBean.setPushDate(Long.valueOf(rawQuery.getLong(rawQuery.getColumnIndex("pushDate"))));
                    pushBean.setMessage(Integer.valueOf(rawQuery.getInt(rawQuery.getColumnIndex("message"))));
                    pushBean.setDeviceId(rawQuery.getString(rawQuery.getColumnIndex("deviceId")));
                    arrayList.add(pushBean);
                }
                rawQuery.close();
                readableDatabase.close();
                return new PushEvent(arrayList);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(PushEvent pushEvent) {
                super.onPostExecute(pushEvent);
                EventTool.post(pushEvent);
            }
        }.execute(new String[]{str});
    }
}
