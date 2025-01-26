package com.aliangmaker.media.event;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteOpenHelper extends android.database.sqlite.SQLiteOpenHelper {
    private static final String DB_NAME = "videoProgress.db";
    private static final String TABLE_NAME = "videoProgress";

    public SQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_sql_table = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (name VARCHAR PRIMARY KEY, progress INTEGER, scale FLOAT);";
        db.execSQL(create_sql_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN scale FLOAT");
        }
    }

    public float[] getVideoInfo(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"progress","scale"}, "name=?", new String[]{name}, null, null, null);
        float progress = 0;
        float scale = 1;
        if (cursor != null && cursor.moveToFirst()) {
            progress = cursor.getInt(cursor.getColumnIndexOrThrow("progress"));
            scale = cursor.getFloat(cursor.getColumnIndexOrThrow("scale"));
            cursor.close();
        }
        db.close();
        return new float[]{progress, scale};
    }

    public void saveVideoProgress(String name, int progress, float scale) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("progress", progress);
        contentValues.put("scale", scale);
        db.replace(TABLE_NAME, null, contentValues);
        db.close();
    }
}
