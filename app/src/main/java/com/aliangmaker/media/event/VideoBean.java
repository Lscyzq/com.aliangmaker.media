package com.aliangmaker.media.event;

import android.graphics.Bitmap;

import java.util.List;

public class VideoBean {
    static List<String[]> videoInfo;
    static List<Bitmap> bitmaps;

    static boolean isFinished = false;
    public VideoBean(List<String[]> videoInfo, List<Bitmap> bitmaps) {
        this.videoInfo = videoInfo;
        this.bitmaps = bitmaps;

        isFinished = true;
    }
    public static List<String[]> getVideo() {
        return videoInfo;
    }

    public static List<Bitmap> getBitmaps() {
        return bitmaps;
    }
    public VideoBean(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public static boolean getStatue() {
        return isFinished;
    }
}
