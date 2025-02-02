package com.aliangmaker.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.aliangmaker.media.adapter.ViewPageAdapter;
import com.aliangmaker.media.databinding.ActivityMainBinding;
import com.aliangmaker.media.event.ChangeTitleStatue;
import com.aliangmaker.media.event.ServerRequest;
import com.aliangmaker.media.fragment.*;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ChangeTitleStatue changeTitleStatue;
    private ViewPageAdapter viewPageAdapter;

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TitleFragment.setBackGone(true);
        changeTitleStatue = new ChangeTitleStatue(this, binding.mainLl);
        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        binding.mainVp.setAdapter(viewPageAdapter);
        SharedPreferences sharedPreferences = getSharedPreferences("main", MODE_PRIVATE);
        ServerRequest serverRequest = new ServerRequest(this);
        serverRequest.getVersion(new ServerRequest.versionCallBack() {
            boolean isUpdate, isNotice;
            @Override
            public void getVersionSuccess(String lastedVersion, String happyVersion, String noticeVersion) {
                isUpdate = canUpdate(getString(R.string.version), lastedVersion);
                isNotice = !noticeVersion.equals(sharedPreferences.getString("notice", "1"));
                if (isUpdate || isNotice) {
                    serverRequest.getUrl(new ServerRequest.urlCallBack() {
                        @Override
                        public void getUrlSuccess(String upLog, String noticeDetail, String happyName, String happyUrl) {
                            if (isUpdate) {
                                UpdateFragment.setUpdateInfo(upLog);
                            } else {
                                UpdateFragment.setUpdateInfo(noticeDetail);
                                UpdateFragment.isNotice(true);
                                sharedPreferences.edit().putString("notice", noticeVersion).apply();
                            }
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_fl, new UpdateFragment()).addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out).commit();
                        }

                        @Override
                        public void getUrlFail() {

                        }
                    });
                }
            }

            @Override
            public void getVersionFail() {

            }
        });
        if (!sharedPreferences.getString("version", "3.14.10").equals(getString(R.string.version))) {
            getSupportFragmentManager().beginTransaction().addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out).add(R.id.main_fl, new MoreHelpFragment()).commit();
            getSupportFragmentManager().beginTransaction().addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out).add(R.id.main_fl, new MoreUpLogFragment()).commit();
        }
        if (getSharedPreferences("play_set", MODE_PRIVATE).getBoolean("wipe", false)) {
            binding.mainIm0.setVisibility(View.GONE);
            binding.mainIm1.setVisibility(View.GONE);
            binding.mainIm2.setVisibility(View.GONE);
        }

        binding.mainVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                EventBus.getDefault().post(new ChangeTitleStatue(false));
                if (position == 0) {
                    binding.mainIm0.setImageResource(R.drawable.ba_vp_select);
                    binding.mainIm1.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm2.setImageResource(R.drawable.ba_vp_inselect);
                    if (sharedPreferences.getString("version", "3.14.10").equals(getString(R.string.version))) {
                        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        TitleFragment.setTitle("MStore");
                        TitleFragment.setBackGone(true);
                    }
                } else if (position == 1) {
                    getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    binding.mainIm0.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm1.setImageResource(R.drawable.ba_vp_select);
                    binding.mainIm2.setImageResource(R.drawable.ba_vp_inselect);
                    TitleFragment.setBackGone(true);
                    TitleFragment.setTitle("设置");
                } else if (position == 2) {
                    getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    TitleFragment.setTitle("凉腕播放器");
                    binding.mainIm0.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm1.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm2.setImageResource(R.drawable.ba_vp_select);
                    TitleFragment.setBackGone(true);
                }
            }
        });

    }

    private boolean canUpdate(String current, String lasted) {
        if (current.equals(lasted)) return false;
        String[] cur = current.split("\\.");
        String[] las = lasted.split("\\.");
        for (int i = 0; i <= 2; i++) {
            if (Integer.parseInt(cur[i]) > Integer.parseInt(las[i])) return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        changeTitleStatue.remove();
        viewPageAdapter.remove();
    }
}