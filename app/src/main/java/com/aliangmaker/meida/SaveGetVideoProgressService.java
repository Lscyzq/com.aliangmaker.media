package com.aliangmaker.meida;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SaveGetVideoProgressService extends Service {
    private VideoProgressDBHelper dbHelper;
    public boolean getSPSet(int item) {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SaveGetVideoProgressService.this);
        if (item == 0) {
            return sharedPreferences.getBoolean("switch_state_0", false);
        } else if (item == 1) {
            return sharedPreferences.getBoolean("switch_state_1", false);
        } else if (item == 4){
            return sharedPreferences.getBoolean("switch_state_display1", false);
        } else if (item == 3) {
            return sharedPreferences.getBoolean("switch_state_2", false);
        } else if (item == 5) {
            return sharedPreferences.getBoolean("switch_state_display3",false);
        } else if (item == 6) {
            return sharedPreferences.getBoolean("switch_state_3",false);
        }
        throw new IllegalArgumentException("Invalid item: " + item);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String getVideoPath;
        dbHelper = new VideoProgressDBHelper(this);
        String todo = intent.getStringExtra("play");
        if (todo.equals("play")) {
            SharedPreferences sharedPreferences = getSharedPreferences("play_set",MODE_PRIVATE);
            if (!sharedPreferences.getString("view","null").equals("null")) {
                getVideoPath = intent.getStringExtra("videoPath");
                int VideoProgress;
                Intent playIntent = new Intent(this, VideoPlayerActivity.class);
                String danmakuInternetUrl = intent.getStringExtra("danmakuInternetUrl");
                playIntent.putExtra("getVideoPath", getVideoPath);
                if (danmakuInternetUrl != null) {
                    playIntent.putExtra("danmakuInternetUrl", danmakuInternetUrl);
                    getVideoPath = intent.getStringExtra("videoName");
                }
                if(intent.getIntExtra("progress",0) == 0) VideoProgress = getVideoProgress(getVideoPath);
                else VideoProgress = intent.getIntExtra("progress",0);
                if (intent.getStringExtra("cookie") != null) playIntent.putExtra("cookie", intent.getStringExtra("cookie"));
                playIntent.putExtra("activity", intent.getBooleanExtra("activity", false));
                playIntent.putExtra("set", getSPSet(0));
                playIntent.putExtra("backright", getSPSet(1));
                playIntent.putExtra("getVideoProgress", VideoProgress);
                playIntent.putExtra("videoName", intent.getStringExtra("videoName"));
                playIntent.putExtra("displayland", getSPSet(4));
                playIntent.putExtra("single_touch", getSPSet(3));
                playIntent.putExtra("volume_hide", getSPSet(5));
                playIntent.putExtra("start_low",getSPSet(6));
                playIntent.putExtra("internet", intent.getBooleanExtra("internet", false));
                playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(playIntent);
                if (VideoProgress > 0) Toast.makeText(this, "继续上一次播放", Toast.LENGTH_SHORT).show();
            }else {
                Intent intentSet = new Intent(this, PlaySetActivity.class);
                intentSet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentSet);
                Toast.makeText(this, "请选择视图", Toast.LENGTH_SHORT).show();
            }
            new developer().execute();
        }else if (todo.equals("save")) {
            saveVideoProgress(intent.getStringExtra("saveName"), intent.getIntExtra("saveProgress", 0));
        }

        stopSelf(); // 停止服务
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private int getVideoProgress(String videoName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {VideoProgressDBHelper.COLUMN_PROGRESS};
        String selection = VideoProgressDBHelper.COLUMN_VIDEO_NAME + "=?";
        String[] selectionArgs = {videoName};
        Cursor cursor = db.query(
                VideoProgressDBHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        int progress = 0;
        if (cursor != null && cursor.moveToFirst()) {
            int progressColumnIndex = cursor.getColumnIndexOrThrow(VideoProgressDBHelper.COLUMN_PROGRESS);
            progress = cursor.getInt(progressColumnIndex);
            cursor.close();
        }
        db.close();
        return progress;
    }
    private static class VideoProgressDBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "video_progress.db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "video_progress";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_VIDEO_NAME = "video_name";
        private static final String COLUMN_PROGRESS = "progress";

        public VideoProgressDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_VIDEO_NAME + " TEXT, " +
                    COLUMN_PROGRESS + " INTEGER)";
            db.execSQL(createTableQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 在数据库版本更新时的操作，如果有需要可以进行相应的处理
        }
    }
    private class developer extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {

            String deviceName = Build.MODEL;
            try {
                SharedPreferences sharedPreferencess = getSharedPreferences("stage",Context.MODE_PRIVATE);
                URL url = new URL(sharedPreferencess.getString("server","http://aliangmaker.top/")+"com.media/developer");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(deviceName.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.e("suc",deviceName);
                } else {
                    Log.e("fail",deviceName);
                }
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private void saveVideoProgress(String videoName, int progress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VideoProgressDBHelper.COLUMN_VIDEO_NAME, videoName);
        values.put(VideoProgressDBHelper.COLUMN_PROGRESS, progress);
        // 检查数据库中是否已存在该视频的进度记录
        String selection = VideoProgressDBHelper.COLUMN_VIDEO_NAME + "=?";
        String[] selectionArgs = {videoName};
        Cursor cursor = db.query(
                VideoProgressDBHelper.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // 已存在记录，更新数据
            db.update(
                    VideoProgressDBHelper.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );
        } else {
            // 不存在记录，插入新数据
            db.insert(
                    VideoProgressDBHelper.TABLE_NAME,
                    null,
                    values
            );
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }
}