package com.aliangmaker.media.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.databinding.FragmentSetDanmakuBinding;

public class SetDanmakuFragment extends Fragment {
    private FragmentSetDanmakuBinding binding;
    SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSetDanmakuBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("弹幕设置");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("danmaku_set", MODE_PRIVATE);
        binding.danmakuSpeedSameSwitch.setChecked(sharedPreferences.getBoolean("speed_same", false));
        binding.danmakuSpeedSameSwitch.setOnClickListener(view -> setSPSet("speed_same",binding.danmakuSpeedSameSwitch.isChecked()));
        binding.centerDanmakuSwitch.setChecked(sharedPreferences.getBoolean("style",true));
        binding.centerDanmakuSwitch.setOnClickListener(v -> setSPSet("style",binding.centerDanmakuSwitch.isChecked()));
        seekbarAll(binding.danmakuLinesSeekbar,binding.danmakuLinesTv,7,"danmakuLines","显示行数");
        seekbarAll(binding.danmakuSizeSeekbar,binding.danmakuSizeTv,150,"size","字号因数");
        seekbarAll(binding.danmakuSpeedSeekbar,binding.danmakuSpeedTv,180,"speed","显示速度");
        seekbarAll(binding.danmakuSbTransparency,binding.danmakuTvTransparency,80,"transparency","%");
        seekbarAll(binding.danmakuSbInnerMargin,binding.danmakuTvInnerMargin,20,"inner","内间距");
        seekbarAll(binding.danmakuSbTopMargin,binding.danmakuTvTopMargin,150,"top","顶间距");
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("设置");
    }

    private void setText(String unit, int progress, TextView textView) {
        if (unit.equals("显示行数")) {
            int value = progress + 1;
            textView.setText(value + "行");
        } else if (unit.equals("字号因数")) {
            textView.setText((progress + 20) + "%");
        } else if (unit.equals("显示速度")) {
            textView.setText((progress + 20) + "%");
        } else if (unit.equals("%")) {
            textView.setText((progress + 20) + "%");
        } else if (unit.equals("顶间距")) {
            textView.setText(progress + "");
        } else
            textView.setText(progress + "");
    }
    private void seekbarAll(SeekBar seekBarId, TextView textViewId, int max, String SPId, String unit) {
        seekBarId.setMax(max);
        seekBarId.setProgress(getSeekbarSet(SPId));
        setText(unit,seekBarId.getProgress(),textViewId);
        seekBarId.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setText(unit,progress,textViewId);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setText(unit,seekBar.getProgress(),textViewId);
                setSPSetString(SPId,seekBar.getProgress());
            }
        });
    }
    private int getSeekbarSet(String item) {
        if (item.equals("size")) {
            return sharedPreferences.getInt(item, 45);
        } else if (item.equals("speed")) {
            return sharedPreferences.getInt(item, 80);
        } else if (item.equals("transparency")) {
            return sharedPreferences.getInt(item, 60);
        } else if (item.equals("inner"))
            return sharedPreferences.getInt(item, 0);
        else if (item.equals("danmakuLines")) {
            return sharedPreferences.getInt(item, 2);
        } else
            return sharedPreferences.getInt(item, 0);
    }
    private void setSPSet(String id,boolean value) {
        sharedPreferences.edit().putBoolean(id, value).apply();
    }
    private void setSPSetString(String id,int value) {
        sharedPreferences.edit().putInt(id, value).apply();
    }
}