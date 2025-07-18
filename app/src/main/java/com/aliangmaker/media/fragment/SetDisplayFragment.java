package com.aliangmaker.media.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.databinding.FragmentSetDisplayBinding;


public class SetDisplayFragment extends Fragment {
    private FragmentSetDisplayBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSetDisplayBinding.inflate(getLayoutInflater(), container, false);
        sharedPreferences = getActivity().getSharedPreferences("play_set", Context.MODE_PRIVATE);
        binding.sdSwSize.setChecked(sharedPreferences.getBoolean("small_tv", false));
        binding.sdSwLand.setChecked(sharedPreferences.getBoolean("un_horizon", false));
        binding.sdSwGone.setChecked(sharedPreferences.getBoolean("wipe", false));
        binding.sdSwStuff.setChecked(sharedPreferences.getBoolean("stuff",false));
        binding.sdSwInner.setChecked(sharedPreferences.getBoolean("inner_show",false));
        if (sharedPreferences.getBoolean("dp_hd1",false)) binding.title.setVisibility(View.GONE);
        boolean dark = sharedPreferences.getBoolean("dark", false);
        boolean bitmap = sharedPreferences.getBoolean("bitmap",true);
        binding.sdSwBitmap.setChecked(bitmap);
        binding.sdSwDark.setChecked(dark);
        binding.sdSbBitmapSize.setMax(600);
        binding.sdSbDark.setMax(255);
        if (dark) {
            binding.sdClSb.setVisibility(View.VISIBLE);
            binding.sdSbDark.setProgress(sharedPreferences.getInt("dark_pg", 125));
        }
        if (bitmap) {
            binding.sdCl1.setVisibility(View.VISIBLE);
            binding.sdSbBitmapSize.setProgress(sharedPreferences.getInt("size_pg",250));
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("显示设置");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.title.setOnLongClickListener(view -> {
            sharedPreferences.edit().putBoolean("dp_hd1",true).apply();
            binding.title.setVisibility(View.GONE);
            return true;
        });
        binding.sdTvBitmapSize.setText(sharedPreferences.getInt("size_pg",250) + "");
        binding.sdTvDark.setText(sharedPreferences.getInt("dark_pg", 125) + "");
        binding.sdSwSize.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPreferences.edit().putBoolean("small_tv", b).apply();
            Toast.makeText(getContext(), "重启生效", Toast.LENGTH_SHORT).show();
        });
        binding.sdSwLand.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("un_horizon", b).apply());
        binding.sdSwStuff.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferences.edit().putBoolean("stuff",isChecked).apply());
        binding.sdSwGone.setOnCheckedChangeListener((compoundButton, b) ->
        {
            Toast.makeText(getContext(), "重启生效", Toast.LENGTH_SHORT).show();
            sharedPreferences.edit().putBoolean("wipe", b).apply();
        });
        binding.sdSwDark.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPreferences.edit().putBoolean("dark", b).apply();
            if (b) {
                binding.sdClSb.setVisibility(View.VISIBLE);
                binding.sdSbDark.setProgress(sharedPreferences.getInt("dark_pg", 125));
            } else {
                binding.sdClSb.setVisibility(View.GONE);
            }
        });
        binding.sdSwBitmap.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPreferences.edit().putBoolean("bitmap",b).apply();
            if (b) {
                binding.sdCl1.setVisibility(View.VISIBLE);
                binding.sdSbBitmapSize.setProgress(sharedPreferences.getInt("size_pg",250));
            } else {
                binding.sdCl1.setVisibility(View.GONE);
            }
        });
        binding.sdSwInner.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("inner_show",b).apply());
        binding.sdNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "即将停止", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences1 = getContext().getSharedPreferences("display",Context.MODE_PRIVATE);
            sharedPreferences1.edit().putBoolean("init", false).commit();
            sharedPreferences1.edit().putFloat("dpi",1.00f).commit();
            Handler handler = new Handler();
            handler.postDelayed(() -> android.os.Process.killProcess(android.os.Process.myPid()), 500);
        });
        binding.sdSbBitmapSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    progress = i;
                    binding.sdTvBitmapSize.setText(i + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sharedPreferences.edit().putInt("size_pg", progress).apply();
            }
        });
        binding.sdSbDark.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    progress = i;
                    binding.sdTvDark.setText(i + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sharedPreferences.edit().putInt("dark_pg", progress).apply();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("设置");
    }

}