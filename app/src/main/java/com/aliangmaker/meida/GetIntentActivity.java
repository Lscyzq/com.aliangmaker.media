package com.aliangmaker.meida;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aliangmaker.media.PlayVideoActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GetIntentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent getIntent = getIntent();
        Intent intent = new Intent(GetIntentActivity.this, PlayVideoActivity.class);
        Map<String,String> headers = new HashMap<>();
        headers.put("Connection","Keep-Alive");
        headers.put("Referer","https://www.bilibili.com/");
        headers.put("Origin","https://www.bilibili.com/");
        headers.put("Cookie",getIntent.getStringExtra("cookie"));

        intent.setData(getIntent.getData());
        intent.putExtra("name", getIntent.getStringExtra("title"));
        intent.putExtra("danmaku", getIntent.getStringExtra("danmaku_url"));
        intent.putExtra("cookie", (Serializable)headers);
        intent.putExtra("agent","Mozilla/5.0 BiliDroid/1.1.1 (bbcallen@gmail.com)");
        startActivity(intent);
        finish();

    }
}