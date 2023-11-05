package com.aliangmaker.meida;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class GetIntentActivity extends AppCompatActivity {
    String danmakuUrl;
    String videoTitleFromBili;
    String videoProgress;
    private void startVideoPlayback(String path) {
        Intent intent2 = new Intent(GetIntentActivity.this, SaveGetVideoProgressService.class);

        intent2.putExtra("videoPath",path);
        intent2.putExtra("play", "play");
        if (videoTitleFromBili != null) {
            if (!(danmakuUrl.equals("null") || danmakuUrl == null)) {
                intent2.putExtra("activity", true);
                intent2.putExtra("danmakuInternetUrl", danmakuUrl);
                if (getIntent().getStringExtra("cookie") != null) intent2.putExtra("cookie", getIntent().getStringExtra("cookie"));
            }
            intent2.putExtra("videoName", videoTitleFromBili);
        } else {
            intent2.putExtra("activity", false);
            intent2.putExtra("videoName", path);
        }
        if (path.startsWith("http://") || path.startsWith("https://"))  intent2.putExtra("internet", true);
        startService(intent2);
        finish();
    }
    private String resolveContentUriToFilePath(String contentUri) {
        String result = decodeUriPath(contentUri);
        int index = result.indexOf("Android");
        result = result.substring(index);
        return result;
    }
    private String decodeUriPath(String encodedUri) {
        return Uri.decode(encodedUri);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String videoUri = String.valueOf(intent.getData());
            Log.e("url", videoUri);
            videoTitleFromBili = intent.getStringExtra("title");
            videoProgress = intent.getStringExtra("videoProgress");
            danmakuUrl = String.valueOf(intent.getStringExtra("danmaku_url"));
            if (videoUri != null) {
                if (videoUri.startsWith("http://") || videoUri.startsWith("https://")) {
                    startVideoPlayback(videoUri);
                } else if (videoUri.startsWith("file://")) {
                    // 处理 file:// 类型的 URL
                    String filePath = videoUri.replace("file://", "");
                    filePath = decodeUriPath(filePath);
                    startVideoPlayback(filePath);
                } else if (videoUri.startsWith("content://")) {
                    // 处理 content:// 类型的 URL
                    videoUri = videoUri.replace("content://", "");
                    videoUri = decodeUriPath(videoUri);
                    String filePath = resolveContentUriToFilePath(videoUri);
                    Log.e("decode", filePath);
                    startVideoPlayback(filePath);
                }
            }
        }
    }

}