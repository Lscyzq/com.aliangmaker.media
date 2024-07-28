package com.aliangmaker.media;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.aliangmaker.media.databinding.ActivityPlayVideoBinding;

public class TestActivity extends AppCompatActivity {
    SharedPreferences SharedPreferencesUtil;
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferencesUtil = newBase.getSharedPreferences("display", MODE_PRIVATE);
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.00f);
        if(dpiTimes != 1.0F) {
            Resources res = newBase.getResources();
            Configuration configuration = res.getConfiguration();
            WindowManager windowManager = (WindowManager) newBase.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            int dpi = metrics.densityDpi;
            configuration.densityDpi = (int) (dpi * dpiTimes);
            Context confBase =  newBase.createConfigurationContext(configuration);
            super.attachBaseContext(confBase);
        }
        else super.attachBaseContext(newBase);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPlayVideoBinding binding = ActivityPlayVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.pvBack.setOnClickListener(view -> finish());
        binding.pvTvTitle.setText("测试界面--测试界面--测试界面--测试界面");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferencesUtil.edit().putFloat("dpi",1.00f).apply();
    }
}