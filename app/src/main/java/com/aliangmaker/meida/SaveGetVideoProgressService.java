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
    public boolean getSPSet(String item) {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SaveGetVideoProgressService.this);
        if (item.equals("0")) {
            boolean switchState1 = sharedPreferences.getBoolean("switch_state_0", false);
            return switchState1;
        } else if (item.equals("1")) {
            boolean switchState2 = sharedPreferences.getBoolean("switch_state_1", false);
            return switchState2;
        } else if (item.equals("1.display")){
            boolean switchState3 = sharedPreferences.getBoolean("switch_state_display1", false);
            return switchState3;
        } else if (item.equals("3")) {
            boolean single_touch = sharedPreferences.getBoolean("switch_state_2", false);
            return single_touch;
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
                if(intent.getIntExtra("progress",0) == 0) VideoProgress = getVideoProgress(getVideoPath);
                else VideoProgress = intent.getIntExtra("progress",0);
                Intent playIntent = new Intent(this, VideoPlayerActivity.class);
                if (intent.getStringExtra("cookie") != null) playIntent.putExtra("cookie", intent.getStringExtra("cookie"));
                playIntent.putExtra("activity", intent.getBooleanExtra("activity", false));
                playIntent.putExtra("set", getSPSet(String.valueOf(0)));
                playIntent.putExtra("backright", getSPSet(String.valueOf(1)));
                playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                playIntent.putExtra("getVideoProgress", VideoProgress);
                if (intent.getStringExtra("danmakuInternetUrl") != null) playIntent.putExtra("danmakuInternetUrl", intent.getStringExtra("danmakuInternetUrl"));
                playIntent.putExtra("videoName", intent.getStringExtra("videoName"));
                playIntent.putExtra("getVideoPath", getVideoPath);
                playIntent.putExtra("displayland", getSPSet("1.display"));
                playIntent.putExtra("single_touch", getSPSet("3"));
                playIntent.putExtra("internet", intent.getBooleanExtra("internet", false));
                startActivity(playIntent);
                if (VideoProgress > 0) Toast.makeText(this, "继续上一次播放", Toast.LENGTH_SHORT).show();
            }else {
                startActivity(new Intent(SaveGetVideoProgressService.this,PlaySetActivity.class));
                Toast.makeText(this, "请选择视图", Toast.LENGTH_SHORT).show();
            }
            new developer().execute();
        }
        if (todo.equals("save")) {
            String videoName = intent.getStringExtra("saveName");
            int currentProgress = intent.getIntExtra("saveProgress", 0);
            saveVideoProgress(videoName, currentProgress);
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
        Log.e("p",db.getPath());
        Log.e("s", String.valueOf(db.getPageSize()));
        Toast.makeText(this, (int) db.getPageSize(), Toast.LENGTH_SHORT).show();
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