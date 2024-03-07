package com.aliangmaker.media.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CompoundButton;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.R;
import com.aliangmaker.media.databinding.FragmentSetHandleBinding;

public class SetHandleFragment extends Fragment {
    private FragmentSetHandleBinding binding;
    private SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSetHandleBinding.inflate(getLayoutInflater(),container,false);
        sharedPreferences = getActivity().getSharedPreferences("play_set", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("restart", false)) binding.shSwRestart.setChecked(true);
        if (sharedPreferences.getBoolean("back_gesture", true)) binding.shSwCanBack.setChecked(true);
        if (sharedPreferences.getBoolean("back_finish", true)) binding.sdSwFinishBack.setChecked(true);
        if (sharedPreferences.getBoolean("tap_scale", false)) binding.shRbTap.setChecked(true);
        if (sharedPreferences.getBoolean("none_start", false)) binding.shSwAudio.setChecked(true);
        if (sharedPreferences.getBoolean("background", false)) binding.shSwCt.setChecked(true);
        if (sharedPreferences.getBoolean("no_border",false)) binding.shSwBorder.setChecked(true);
        if (sharedPreferences.getBoolean("deep",false)) binding.shSwDeep.setChecked(true);
        if (sharedPreferences.getBoolean("watch_back",false)) binding.shSwWatch.setChecked(true);
        if (!sharedPreferences.getBoolean("tap_scale",false)) binding.shRbTwo.setChecked(true);
        else binding.shRbTap.setChecked(true);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("操作设置");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.shRgScaleMode.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.sh_rb_tap) sharedPreferences.edit().putBoolean("tap_scale", true).apply();
            else sharedPreferences.edit().putBoolean("tap_scale", false).apply();
        });
        binding.shSwBorder.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("no_border",b).apply());
        binding.shSwAudio.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("none_start",b).apply());
        binding.shSwCt.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("background",b).apply());
        binding.shSwWatch.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("watch_back",b).apply());
        binding.shSwCanBack.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("back_gesture",b).apply());
        binding.sdSwFinishBack.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("back_finish",b).apply());
        binding.shSwRestart.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("restart",b).apply());
        binding.shSwDeep.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("deep",b).apply());
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("设置");
    }
}