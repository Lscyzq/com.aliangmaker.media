package com.aliangmaker.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import android.util.DisplayMetrics;
import androidx.appcompat.app.AppCompatActivity;
import com.aliangmaker.media.databinding.ActivityFirstSetBinding;
import com.aliangmaker.media.event.Bean0;
import com.aliangmaker.media.event.ChangeTitleStatue;
import com.aliangmaker.media.fragment.*;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public  class SetFirstActivity extends AppCompatActivity {
    private static ActivityFirstSetBinding binding;
    private ChangeTitleStatue changeTitleStatue;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirstSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TitleFragment.setTitle("隐私安全");
        changeTitleStatue = new ChangeTitleStatue(this, binding.fsLl);
        EventBus.getDefault().register(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.fs_fl, new SafeFragment()).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        changeTitleStatue.remove();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent0(Bean0 event0) {
        int value = event0.getValue();
        if (value == 1) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    .add(R.id.fs_fl, new AgreementFragment())
                    .commit();
        } else if (value == 2) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    .add(R.id.fs_fl, new SetInitFragment())
                    .commit();
        } else if (value == 0) {
            TitleFragment.setTitle("隐私安全");
        } else if (value == 3) {
            TitleFragment.setTitle("使用协议");
        }
    }
}