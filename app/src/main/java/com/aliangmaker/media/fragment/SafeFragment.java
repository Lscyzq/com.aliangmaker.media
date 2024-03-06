package com.aliangmaker.media.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.databinding.FragmentSafeBinding;
import com.aliangmaker.media.event.AsyncVideoList;
import com.aliangmaker.media.event.ChangeTitleStatue;
import com.aliangmaker.media.event.Bean0;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

public class SafeFragment extends Fragment {
    private FragmentSafeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSafeBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().post(new ChangeTitleStatue(false));
        EventBus.getDefault().post(new Bean0(0));
        TranslateAnimation translateAnim = new TranslateAnimation(0f, 0f, 0f, -15f);
        translateAnim.setDuration(700);
        translateAnim.setFillAfter(true);

        AlphaAnimation alphaAnim = new AlphaAnimation(0f, 1f);
        alphaAnim.setDuration(700);
        alphaAnim.setFillAfter(true);
        binding.sfIm.startAnimation(alphaAnim);
        binding.sfTv0.startAnimation(alphaAnim);
        binding.getRoot().startAnimation(alphaAnim);

        binding.sfTv0.setText(Html.fromHtml("<font color='#99CC00'>凉腕</font>隐私"));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> {
                    binding.sfIm.startAnimation(translateAnim);
                    binding.sfTv0.startAnimation(translateAnim);
                });
            }
        },700);
        translateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                binding.sfCl.setVisibility(View.VISIBLE);
                binding.sfTv1.setVisibility(View.VISIBLE);
                binding.sfTv1.startAnimation(alphaAnim);
                TranslateAnimation translateAnim = new TranslateAnimation(0f, 0f, 20f, 0f);
                translateAnim.setDuration(1200);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnim);
                animationSet.addAnimation(translateAnim);
                animationSet.setFillAfter(true);
                binding.sfCl.startAnimation(animationSet);
            }
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        binding.sfTv2.setText(Html.fromHtml("我已阅读并同意<font color='#99CC00'>《隐私协议》《用户使用约定》《免责声明》</font>"));
        binding.sfTv2.setOnClickListener(v -> {
            EventBus.getDefault().post(new Bean0(1));
        });
        binding.sfRbtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkPermission();
                binding.sfBtnNext.setVisibility(View.VISIBLE);
                binding.sfImNext.setVisibility(View.VISIBLE);
                binding.sfImNext.startAnimation(alphaAnim);
                binding.sfBtnNext.startAnimation(alphaAnim);;
            }
        });
        binding.sfCo0.setOnClickListener(v -> {
            EventBus.getDefault().post(new Bean0(2));
            new AsyncVideoList(getContext(),getContext().getSharedPreferences("playSet", Context.MODE_PRIVATE).getBoolean("strongFile",false));
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = PackageManager.PERMISSION_GRANTED;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            // 通过权限数组检查是否都开启了这些权限
            for (String permission : permissions) {
                check = ContextCompat.checkSelfPermission(getContext(), permission);
                if (check != PackageManager.PERMISSION_GRANTED) {
                }
            }
            if (check != PackageManager.PERMISSION_GRANTED) {
                // 未开启该权限，则请求系统弹窗，好让用户选择是否立即开启权限
                ActivityCompat.requestPermissions(getActivity(),permissions,520);
            }
        }
    }
}