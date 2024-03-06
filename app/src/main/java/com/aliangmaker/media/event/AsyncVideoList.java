package com.aliangmaker.media.event;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AsyncVideoList {
    List<String[]> videoInfoList0 = new ArrayList<>();
    List<Bitmap> bitmaps = new ArrayList<>();
    private void getFiles(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    getFiles(f.getAbsolutePath());
                } else if (isVideo(f.getName().toLowerCase())){
                    videoInfoList0.add(new String[]{f.getName(),f.getPath()});
                    bitmaps.add(null);
                }
            }
        }
    }
    private boolean isVideo(String extension) {
        return extension.endsWith(".mp4") || extension.endsWith(".avi") || extension.endsWith(".mkv") || extension.endsWith(".mov") || extension.endsWith(".wmv");
    }
    public AsyncVideoList(Context context,boolean strong) {
        new Thread(() -> {
            List<String[]> videoInfoList;
            videoInfoList = videoInfoList0;
            if (strong) getFiles("/sdcard/Movies/");
            String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Thumbnails.DATA};
            Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String[] videoInfo = {
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))};
                    if (canAdd(videoInfo[0])) {
                        bitmaps.add(MediaStore.Video.Thumbnails.getThumbnail(
                                context.getContentResolver(),
                                Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))),
                                MediaStore.Video.Thumbnails.MINI_KIND,
                                null
                        ));
                        videoInfoList.add(videoInfo);
                    }
                } while (cursor.moveToNext());
            }
            EventBus.getDefault().post(new VideoBean(true));
            cursor.close();
            new VideoBean(videoInfoList, bitmaps);
        }).start();
    }

    private boolean canAdd(String path) {
        for (String[] item : videoInfoList0) {
            if (item[0].equals(path)) return false;
        }
        return true;
    }
}
