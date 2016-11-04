package com.example.arvind.floodwarning;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String dbName = "FloodWarn";
    private static final String id = "id";
    private static final String s1 = "s1";
    private static final String tname = "gak";
    public Context m_context;

    public DatabaseHandler(Context context) {
        super(context, dbName, null, 4);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE gak(id INTEGER,s1 VARCHAR)");
        db.execSQL("INSERT INTO gak (id,s1) VALUES (1,1)");
        db.execSQL("INSERT INTO gak (id,s1) VALUES (2,0)");
        db.execSQL("INSERT INTO gak (id,s1) VALUES (3,0)");
        db.execSQL("INSERT INTO gak (id,s1) VALUES (4,1)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS gak");
        onCreate(db);
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS gak");
        onCreate(db);
        db.close();
    }

    public int selectDB(String s) {
        Cursor ans = getReadableDatabase().rawQuery("SELECT s1 FROM gak WHERE id= " + s, null);
        if (ans.moveToFirst()) {
            return ans.getInt(ans.getColumnIndex(s));
        }
        return -1;
    }
}
