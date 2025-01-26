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
        binding.centerDanmakuSwitch.setChecked(getSPSet(0));
        binding.centerDanmakuSwitch.setOnClickListener(v -> setSPSet("style",binding.centerDanmakuSwitch.isChecked()));
        binding.danmakuHoldSwitch.setChecked(getSPSet(1));
        binding.danmakuHoldSwitch.setOnClickListener(v -> setSPSet("fold",binding.danmakuHoldSwitch.isChecked()));
        binding.danmakuSameSwitch.setChecked(getSPSet(2));
        binding.danmakuSameSwitch.setOnClickListener(v -> setSPSet("merge",binding.danmakuSameSwitch.isChecked()));
        seekbarAll(binding.danmakuLinesSeekbar,binding.danmakuLinesTv,7,"danmakuLines","显示行数");
        seekbarAll(binding.danmakuSizeSeekbar,binding.danmakuSizeTv,150,"danmakuSize","字号因数");
        seekbarAll(binding.danmakuSpeedSeekbar,binding.danmakuSpeedTv,2,"danmakuSpeed","显示速度");
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("设置");
    }
    private boolean getSPSet(int item) {
        if (item == 0) {
            return sharedPreferences.getBoolean("style", true);
        }else if (item==1) {
            return sharedPreferences.getBoolean("fold", false);
        }else if (item==2) {
            return sharedPreferences.getBoolean("merge", false);}
        throw new IllegalArgumentException("Invalid item: " + item);
    }
    private void setText(String unit, SeekBar seekBar, TextView textView) {
        if (unit.equals("显示行数")) {
            int value = seekBar.getProgress() + 1;
            textView.setText(unit + ":" + value + "行");
        }else if (unit.equals("字号因数")) {
            textView.setText(unit + ":" + ((seekBar.getProgress() + 50) / 100.0f) + "F");
        }else if (unit.equals("显示速度")) {
            if (seekBar.getProgress() == 2) {
                textView.setText(unit + "：快");
            }else if (seekBar.getProgress() == 1) {
                textView.setText(unit + "：中");
            }else if (seekBar.getProgress() == 0) {
                textView.setText(unit + "：慢");
            }
        }
    }
    private void seekbarAll(SeekBar seekBarId, TextView textViewId, int max, String SPId, String unit) {
        seekBarId.setMax(max);
        seekBarId.setProgress(getSeekbarSet(SPId));
        setText(unit,seekBarId,textViewId);
        seekBarId.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setText(unit,seekBarId,textViewId);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setText(unit,seekBarId,textViewId);
                setSPSetString(SPId,seekBarId.getProgress());
            }
        });
    }
    private int getSeekbarSet(String item) {
        if (item.equals("danmakuSize")) {
            return sharedPreferences.getInt(item, 50);
        } else if (item.equals("danmakuSpeed")) {
            return sharedPreferences.getInt(item, 1);
        } else return sharedPreferences.getInt(item, 2);
    }
    private void setSPSet(String id,boolean value) {
        sharedPreferences.edit().putBoolean(id, value).apply();
    }
    private void setSPSetString(String id,int value) {
        sharedPreferences.edit().putInt(id, value).apply();
    }
}