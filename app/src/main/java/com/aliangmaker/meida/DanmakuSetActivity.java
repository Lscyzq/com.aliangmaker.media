package com.aliangmaker.meida;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliangmaker.meida.databinding.ActivityDanmakuSetBinding;

public class DanmakuSetActivity extends AppCompatActivity {
    private ActivityDanmakuSetBinding binding;
    private boolean getSPSet(String item) {
        SharedPreferences sharedPreferences = getSharedPreferences("danmakuSet", MODE_PRIVATE);
        boolean stage;
        if (item.equals("0")) {
            stage = sharedPreferences.getBoolean("style", true);
            return stage;
        }
        if (item.equals("1")) {
            stage = sharedPreferences.getBoolean("fold", false);
            return stage;
        }
        if (item.equals("2")) {
            stage = sharedPreferences.getBoolean("merge", false);
            return stage;
        }
        throw new IllegalArgumentException("Invalid item: " + item);
    }

    private void setText(String unit, SeekBar seekBar, TextView textView) {
        float value2 = seekBar.getProgress() + 50;
        float floatValue = value2 / 100.0f; // Map to float range 0.0 - 1.0
        if (unit.equals("显示行数")) {
            int value = seekBar.getProgress() + 1;
            textView.setText(unit + ":" + value + "行");
        }
        if (unit.equals("字号因数")) {
            textView.setText(unit + ":" + floatValue + "F");
        }
        if (unit.equals("显示速度")) {
            if (seekBar.getProgress() == 2) {
                textView.setText(unit + "：快");
            }
            if (seekBar.getProgress() == 1) {
                textView.setText(unit + "：中");
            }
            if (seekBar.getProgress() == 0) {
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
        SharedPreferences sharedPreferences = getSharedPreferences("danmakuSet", MODE_PRIVATE);
        if (item.equals("danmakuSize")) {
            return sharedPreferences.getInt(item, 50);
        } else if (item.equals("danmakuSpeed")) {
            return sharedPreferences.getInt(item, 1);
        } else return sharedPreferences.getInt(item, 2);
    }
    private void setSPSet(String id,boolean value) {
        SharedPreferences sharedPreferences = getSharedPreferences("danmakuSet", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(id, value).apply();
    }
    private void setSPSetString(String id,int value) {
        SharedPreferences sharedPreferences = getSharedPreferences("danmakuSet", MODE_PRIVATE);
        sharedPreferences.edit().putInt(id, value).apply();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDanmakuSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if (f instanceof BaseTitleFragment) {
                    ((BaseTitleFragment) f).setTextViewText("弹幕设置");
                }
            }
        }, false);
        binding.centerDanmakuSwitch.setChecked(getSPSet("0"));
        binding.centerDanmakuSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               setSPSet("style",binding.centerDanmakuSwitch.isChecked());
            }
        });
        binding.danmakuHoldSwitch.setChecked(getSPSet("1"));
        binding.danmakuHoldSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSPSet("fold",binding.danmakuHoldSwitch.isChecked());
            }
        });
        binding.danmakuSameSwitch.setChecked(getSPSet("2"));
        binding.danmakuSameSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSPSet("merge",binding.danmakuSameSwitch.isChecked());
            }
        });
        seekbarAll(binding.danmakuLinesSeekbar,binding.danmakuLinesTv,7,"danmakuLines","显示行数");
        seekbarAll(binding.danmakuSizeSeekbar,binding.danmakuSizeTv,150,"danmakuSize","字号因数");
        seekbarAll(binding.danmakuSpeedSeekbar,binding.danmakuSpeedTv,2,"danmakuSpeed","显示速度");
    }
}