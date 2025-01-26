package com.aliangmaker.media.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.aliangmaker.media.MainActivity;
import com.aliangmaker.media.R;
import com.aliangmaker.media.databinding.FragmentInitialSetBinding;

public class SetInitFragment extends Fragment {
    private FragmentInitialSetBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInitialSetBinding.inflate(getLayoutInflater(),container,false);
        binding.initTv1.setVisibility(View.GONE);
        binding.initRg1.setVisibility(View.GONE);
        binding.initTv.setVisibility(View.GONE);
        TitleFragment.setTitle("基础设置");
        doAnim(binding.initTv0,binding.initRg0,binding.initTv4);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("play_set", Context.MODE_PRIVATE);
        binding.initRg0.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.init_rb00) sharedPreferences.edit().putBoolean("view", true).apply();
            else sharedPreferences.edit().putBoolean("view", false).apply();
            if (!view) {
                binding.initRg1.setVisibility(View.VISIBLE);
                binding.initTv1.setVisibility(View.VISIBLE);
                binding.initTv.setVisibility(View.VISIBLE);
                doAnim(binding.initTv1, binding.initRg1, binding.initTv);
            }
            view = true;
        });
        binding.initRg1.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.init_rb10) sharedPreferences.edit().putBoolean("tap_scale", false).apply();
            else sharedPreferences.edit().putBoolean("tap_scale", true).apply();
            if (!volume) {
                binding.initCl.setVisibility(View.VISIBLE);
                AlphaAnimation alphaAnim = new AlphaAnimation(0f, 1f);
                alphaAnim.setDuration(600);
                TranslateAnimation translateAnim = new TranslateAnimation(50f, 0f, 0f, 0f);
                translateAnim.setDuration(1000);
                translateAnim.setFillAfter(true);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnim);
                animationSet.addAnimation(translateAnim);
                binding.initCl.startAnimation(animationSet);
                TranslateAnimation translateAnim3 = new TranslateAnimation(0f, 40f, 0f, 0f);
                translateAnim3.setDuration(1100);
                translateAnim3.setFillAfter(true);
                binding.initIm0.startAnimation(translateAnim3);
            }
            volume = true;
        });
        binding.initCl.setOnClickListener(v -> {
            getActivity().getSharedPreferences("main",Context.MODE_PRIVATE).edit().putBoolean("signed", true).apply();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });
        return binding.getRoot();
    }
    private boolean view = false,volume = false;
    private void doAnim(TextView tv1, RadioGroup rg, TextView tv2) {
        AlphaAnimation alphaAnim = new AlphaAnimation(0f, 1f);
        alphaAnim.setDuration(600);
        TranslateAnimation translateAnim = new TranslateAnimation(50f, 0f, 0f, 0f);
        translateAnim.setDuration(1000);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnim);
        animationSet.addAnimation(translateAnim);
        animationSet.setFillAfter(true);
        tv1.startAnimation(animationSet);
        rg.startAnimation(animationSet);
        tv2.startAnimation(animationSet);
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setTitle("隐私协议");
    }
}