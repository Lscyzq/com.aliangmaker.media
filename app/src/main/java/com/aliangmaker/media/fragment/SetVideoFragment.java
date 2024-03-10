package com.aliangmaker.media.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.R;
import com.aliangmaker.media.databinding.FragmentSetVideoBinding;

public class SetVideoFragment extends Fragment {

    private FragmentSetVideoBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSetVideoBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }
    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("设置");
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("播放设置");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("play_set", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("hard_play", true)) binding.pr2.setChecked(true);
        else binding.pr1.setChecked(true);

        if (sharedPreferences.getBoolean("view",true)) binding.rbxzs.setChecked(true);
        else binding.rbxzt.setChecked(true);

        if (sharedPreferences.getBoolean("audio",true)) binding.rbAudioTrack.setChecked(true);
        else binding.rbOpenSL.setChecked(true);
        binding.svSbAudio.setMax(100);
        binding.svSbAudio.setProgress((int) sharedPreferences.getFloat("volume",100));
        binding.svTvAudio.setText("音量" + binding.svSbAudio.getProgress() + "%");
        binding.svSbAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float progress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                binding.svTvAudio.setText("音量" + i + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sharedPreferences.edit().putFloat("volume", progress).apply();
            }
        });
        binding.rg2xz.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rbxzs) sharedPreferences.edit().putBoolean("view",true).apply();
            else if (i == R.id.rbxzt) {
                sharedPreferences.edit().putBoolean("view",false).apply();
            }
        });
        binding.rgypsc.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rbOpenSL) sharedPreferences.edit().putBoolean("audio",false).apply();
            else if (i == R.id.rbAudioTrack) {
                sharedPreferences.edit().putBoolean("audio",true).apply();
            }
        });
        binding.rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.pr2) sharedPreferences.edit().putBoolean("hard_play", true).apply();
            else sharedPreferences.edit().putBoolean("hard_play", false).apply();
        });
        if (sharedPreferences.getBoolean("sharp", false)) binding.sharpSwitch.setChecked(true);
        binding.sharpSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean("sharp", binding.sharpSwitch.isChecked()).apply());
        if (sharedPreferences.getBoolean("jump_play", true)) binding.jumpSwitch.setChecked(true);
        binding.jumpSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean("jump_play", binding.jumpSwitch.isChecked()).apply());
    }
}