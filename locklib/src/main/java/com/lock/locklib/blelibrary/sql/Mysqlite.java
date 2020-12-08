package com.lock.locklib.blelibrary.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Mysqlite extends SQLiteOpenHelper {
    public Mysqlite(Context context) {
        super(context, "lockPlus.db", (SQLiteDatabase.CursorFactory) null, 2);
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE myChat ( pushDate  INTEGER PRIMARY KEY,message  INTEGER,deviceId  VARCHAR)");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i != i2) {
            sQLiteDatabase.execSQL("drop table myChat");
            onCreate(sQLiteDatabase);
        }
    }
}
