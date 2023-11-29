package com.aliangmaker.meida;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aliangmaker.meida.databinding.ActivityPlaySetBinding;

public class PlaySetActivity extends AppCompatActivity {
    ActivityPlaySetBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaySetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if (f instanceof BaseTitleFragment) {
                    ((BaseTitleFragment) f).setTextViewText("播放设置");
                }
            }
        }, false);
        SharedPreferences sharedPreferences = getSharedPreferences("play_set", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("hard_play", true)) {
            binding.pr2.setChecked(true);
            binding.pr1.setChecked(false);
        } else {
            binding.pr2.setChecked(false);
            binding.pr1.setChecked(true);
        }
        if (sharedPreferences.getString("view","null").equals("surface")) {
            binding.rbxzs.setChecked(true);
            binding.rbxzt.setChecked(false);
        }else if(!sharedPreferences.getString("view","null").equals("null")){
            binding.rbxzs.setChecked(false);
            binding.rbxzt.setChecked(true);
        }

        if (sharedPreferences.getBoolean("audio",true)) {
            binding.rbOpenSL.setChecked(false);
            binding.rbAudioTrack.setChecked(true);
        }else {
            binding.rbOpenSL.setChecked(true);
            binding.rbAudioTrack.setChecked(false);
        }

        binding.rg2xz.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rbxzs) sharedPreferences.edit().putString("view","surface").apply();
            else if (i == R.id.rbxzt) {
                sharedPreferences.edit().putString("view","texture").apply();
            }
        });
        binding.rgypsc.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rbOpenSL) sharedPreferences.edit().putBoolean("audio",false).apply();
            else if (i == R.id.rbAudioTrack) {
                sharedPreferences.edit().putBoolean("audio",true).apply();
            }
        });
        binding.rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.pr2)   sharedPreferences.edit().putBoolean("hard_play", true).apply();
            else   sharedPreferences.edit().putBoolean("hard_play", false).apply();
        });
        if (sharedPreferences.getBoolean("sharp", false)) {
            binding.sharpSwitch.setChecked(true);
        }else binding.sharpSwitch.setChecked(false);
        binding.sharpSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean("sharp", binding.sharpSwitch.isChecked()).apply());

        if (sharedPreferences.getBoolean("jump_play", true)) {
            binding.jumpSwitch.setChecked(true);
        }else binding.jumpSwitch.setChecked(false);
        binding.jumpSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean("jump_play", binding.jumpSwitch.isChecked()).apply());
    }
}