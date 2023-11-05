package com.aliangmaker.meida;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class FirstsetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firstset);
        Button bexit = findViewById(R.id.bexit);
        Button bbegin = findViewById(R.id.bbegin);
        TextView ystk = findViewById(R.id.ystk1);
        bexit.setOnClickListener(view -> finish());
        bbegin.setOnClickListener(view -> {
            Intent i = new Intent();
            i.setClass(FirstsetActivity.this, MainActivity.class);

            startActivity(i);

            SharedPreferences sp = getSharedPreferences("stage", Context.MODE_PRIVATE);
            sp.edit().putString("regist", "registed").apply();
            sp.edit().putString("version", getString(R.string.version)).apply();
            sp.edit().putString("server","http://aliangmaker.top/").apply();
            finish();
        });
        ystk.setOnClickListener(v -> {
            Intent intent = new Intent(FirstsetActivity.this,EmptyActivity.class);
            intent.putExtra("layout","activity_privacy_policy");
            startActivity(intent);
        });
    }
}