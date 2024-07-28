package com.aliangmaker.media;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;


//以下代码来自开源项目（BiliClient）并稍作修改
public class BaseActivity extends FragmentActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences SharedPreferencesUtil = newBase.getSharedPreferences("display", MODE_PRIVATE);
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.5F);





        int paddingH_percent = SharedPreferencesUtil.getInt("paddingH",20);
        int paddingV_percent = SharedPreferencesUtil.getInt("paddingV",0);
        if(paddingH_percent != 0 || paddingV_percent != 0) {
            Log.e("debug","调整边距");
            View rootView = this.getWindow().getDecorView().getRootView();
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            int paddingV = metrics.heightPixels * paddingV_percent / 100;
            int paddingH = metrics.widthPixels * paddingH_percent / 100;
            rootView.setPadding(paddingH, paddingV, paddingH, paddingV);
        }






        if(dpiTimes != 1.0F) {    //似乎有些低版本设备不支持，所以如果默认值就不要调整以免闪退    //后来发现这是错误的，详见SplashActivity
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this,WelcomeActivity.class));
    }
}
