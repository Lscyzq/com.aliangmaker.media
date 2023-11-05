package com.aliangmaker.meida;

import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        handler = new Handler() {
            @SuppressLint({"SuspiciousIndentation", "HandlerLeak"})
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 在这里处理延迟后的操作，如更新UI等
                SharedPreferences sp = getSharedPreferences("stage", Context.MODE_PRIVATE);
                String st = sp.getString("regist", "unregisted");
                String vs = sp.getString("version", "null");
                //延迟
                if (st.equals("registed")) {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    if (!vs.equals(getString(R.string.version))) {
                        sp.edit().putString("version", getString(R.string.version)).apply();
                        Intent intent = new Intent(WelcomeActivity.this, EmptyActivity.class);
                        intent.putExtra("layout", "activity_up_log");
                        startActivity(intent);
                    }
                    finish();
                } else
                    startActivity(new Intent(WelcomeActivity.this, FirstsetActivity.class));
                    finish();
            }
        };        handler.sendEmptyMessageDelayed(0, 700);


    }
    private Handler handler;

    private Runnable goNext = new Runnable() {
        @Override
        public void run() {
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}