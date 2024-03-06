package com.aliangmaker.media.event;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AsyncBiliList {
    private List<String[]> videoInfoList = new ArrayList<>();
    private List<Bitmap> bitmaps = new ArrayList<>();

    public AsyncBiliList() {
        new Thread(() -> {
            getFiles("/sdcard/Android/media/cn.luern0313.wristbilibili/download/");
            new BiliVideoBean(videoInfoList, bitmaps);
            EventBus.getDefault().post(new BiliVideoBean(true));
        }).start();
    }

    private void getFiles(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    getFiles(f.getAbsolutePath());
                } else if (f.getName().toLowerCase().endsWith("mp4")) {
                    String name = "video.mp4";
                    File info = new File(f.getParent(), "info.json");
                    Bitmap bitmap = BitmapFactory.decodeFile(f.getParent() + "/cover.png");
                    if (info.exists()) {
                        try {
                            FileInputStream fis = new FileInputStream(info);
                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            br.close();
                            fis.close();
                            JSONObject jsonObject = new JSONObject(sb.toString());
                            name = jsonObject.getString("video_title");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    String videoPath = f.getPath();
                    videoInfoList.add(new String[]{name, videoPath});
                    bitmaps.add(bitmap);
                }
            }
        }
    }
}
