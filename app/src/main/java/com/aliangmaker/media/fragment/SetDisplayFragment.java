package com.aliangmaker.media.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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
        binding.sdSwLand.setChecked(sharedPreferences.getBoolean("horizon", false));
        binding.sdSwGone.setChecked(sharedPreferences.getBoolean("wipe", false));
        if (sharedPreferences.getBoolean("dp_hd",false)) binding.title.setVisibility(View.GONE);
        boolean dark = sharedPreferences.getBoolean("dark", false);
        binding.sdSwDark.setChecked(dark);
        if (dark) {
            binding.sdSbDark.setMax(255);
            binding.sdClSb.setVisibility(View.VISIBLE);
            binding.sdSbDark.setProgress(sharedPreferences.getInt("dark_pg", 125));
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
            sharedPreferences.edit().putBoolean("dp_hd",true).apply();
            binding.title.setVisibility(View.GONE);
            return true;
        });
        binding.sdTvDark.setText(sharedPreferences.getInt("dark_pg", 125) + "");
        binding.sdSwSize.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("small_tv", b).apply());
        binding.sdSwLand.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean("horizon", b).apply());
        binding.sdSwGone.setOnCheckedChangeListener((compoundButton, b) ->
        {
            Toast.makeText(getContext(), "重启生效", Toast.LENGTH_SHORT).show();
            sharedPreferences.edit().putBoolean("wipe", b).apply();
        });
        binding.sdSwDark.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                sharedPreferences.edit().putBoolean("dark", true).apply();
                binding.sdClSb.setVisibility(View.VISIBLE);
                binding.sdSbDark.setProgress(sharedPreferences.getInt("dark_pg", 125));
                TranslateAnimation translateAnim = new TranslateAnimation(0f, 0f, -70, 0f);
                translateAnim.setDuration(350);
                translateAnim.setFillAfter(true);
                binding.sdClSb.startAnimation(translateAnim);
            } else {
                sharedPreferences.edit().putBoolean("dark", false).apply();
                TranslateAnimation translateAnim = new TranslateAnimation(0f, 0f, 0, -70f);
                translateAnim.setDuration(300);
                translateAnim.setFillAfter(false);
                binding.sdClSb.startAnimation(translateAnim);
                translateAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getActivity().runOnUiThread(() -> binding.sdClSb.setVisibility(View.GONE));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

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