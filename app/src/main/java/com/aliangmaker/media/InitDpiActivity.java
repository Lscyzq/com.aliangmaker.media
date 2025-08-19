package com.aliangmaker.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;

import android.widget.SeekBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aliangmaker.media.databinding.ActivityBaseBinding;
import com.aliangmaker.media.fragment.TitleFragment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


//以下代码参考开源项目BiliClient并作修改
public class InitDpiActivity extends AppCompatActivity {
    private ActivityBaseBinding binding;
    Handler handler = new Handler();
    SharedPreferences sharedPreferences;
    private String dpi = "1.00";
    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                @SuppressLint("PrivateApi") Class clsPkgParser = Class.forName("android.content.pm.PackageParser$Package");
                Constructor constructor = clsPkgParser.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);

                @SuppressLint("PrivateApi") Class clsActivityThread = Class.forName("android.app.ActivityThread");
                Method method = clsActivityThread.getDeclaredMethod("currentActivityThread");
                method.setAccessible(true);
                Object activityThread = method.invoke(null);
                Field hiddenApiWarning = clsActivityThread.getDeclaredField("mHiddenApiWarningShown");
                hiddenApiWarning.setAccessible(true);
                hiddenApiWarning.setBoolean(activityThread, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SharedPreferences SharedPreferencesUtil = newBase.getSharedPreferences("display", MODE_PRIVATE);
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0F);
        if (dpiTimes == 1.0F) super.attachBaseContext(newBase);
        else try {
            DisplayMetrics displayMetrics = newBase.getResources().getDisplayMetrics();
            Configuration configuration = newBase.getResources().getConfiguration();
            configuration.densityDpi = (int) (displayMetrics.densityDpi * dpiTimes);
            super.attachBaseContext(newBase.createConfigurationContext(configuration));
        } catch (Exception e) {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("display",MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("init", false)) {
            binding = ActivityBaseBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            TitleFragment.setTitle("界面设置");
            binding.bsTv.setText(Html.fromHtml("拖动下方进度条改变显示大小（dpi）<font color='#99CC00'>双击</font>「查看预览」，<font color='#99CC00'>长按</font>「确定设置」"));
            initSeeBar();
            initTap();
        } else {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        }



//        int paddingH_percent = SharedPreferencesUtil.getInt("paddingH",20);
//        int paddingV_percent = SharedPreferencesUtil.getInt("paddingV",0);
//        if(paddingH_percent != 0 || paddingV_percent != 0) {
//            Log.e("debug","调整边距");
//            View rootView = this.getWindow().getDecorView().getRootView();
//            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//            Display display = windowManager.getDefaultDisplay();
//            DisplayMetrics metrics = new DisplayMetrics();
//            display.getRealMetrics(metrics);
//            int paddingV = metrics.heightPixels * paddingV_percent / 100;
//            int paddingH = metrics.widthPixels * paddingH_percent / 100;
//            rootView.setPadding(paddingH, paddingV, paddingH, paddingV);
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
    int clickCount;
    private void initTap() {
        binding.bsCl.setOnClickListener(view -> {
            clickCount++;
            handler.postDelayed(() -> {
                if (clickCount == 2) {
                    sharedPreferences.edit().putFloat("dpi",Float.valueOf(dpi)).apply();
                    startActivity(new Intent(InitDpiActivity.this,TestActivity.class));
                }
                clickCount = 0;
            }, 330);
        });
        binding.bsCl.setOnLongClickListener(view -> {
            sharedPreferences.edit().putBoolean("init", true).apply();
            sharedPreferences.edit().putFloat("dpi",Float.valueOf(dpi)).apply();
            InitDpiActivity.this.recreate();
            return true;
        });
    }

    private void initSeeBar() {
        binding.bsSb.setMax(270);
        binding.bsSb.setProgress(70);
        binding.bsTv2.setText("因数：1.00F");
        binding.bsSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                dpi = String.format("%.2f",0.3+ (float)i/100);
                binding.bsTv2.setText("因数："+dpi+"F");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
