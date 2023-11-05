package com.aliangmaker.meida;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliangmaker.meida.databinding.ActivityFileEmptyBinding;

public class FileEmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.aliangmaker.meida.databinding.ActivityFileEmptyBinding binding = ActivityFileEmptyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getIntent().getBooleanExtra("what", false)) {
            binding.textView20.setVisibility(View.GONE);
        }
        if (getIntent().getStringExtra("pac").equals("com")){
            getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                    super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                    if(f instanceof BaseTitleFragment){
                        ((BaseTitleFragment)f).setTextViewText("文件说明");
                    }
                }
            }, false);
            binding.textView19.setText("MediaStore是系统媒体管理器，相比普通的视频文件查找方式，获取MediaStore能够大大缩短加载时间。当设备上的媒体文件发生改变时（例如添加、删除、重命名文件），MediaStore会自动进行更新。如果你已经尝试以上方法列表依然为空，你可以通过重启你的手表以重置MediaStore。");
        } else if (getIntent().getStringExtra("pac").equals("bili")) {
            getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                    super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                    if(f instanceof BaseTitleFragment){
                        ((BaseTitleFragment)f).setTextViewText("文件说明");
                    }
                }
            }, false);
            binding.textView19.setText("腕上哔哩是由第三方开发的哔哩哔哩手表端，当您从腕上哔哩app下载视频文件时，会在这里进行显示。");
        } else if (getIntent().getStringExtra("pac").equals("url")) {
            getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                    super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                    if(f instanceof BaseTitleFragment){
                        ((BaseTitleFragment)f).setTextViewText("彩蛋更新");
                    }
                }
            }, false);
            binding.imageView2.setImageResource(R.drawable.up);
            binding.textView19.setText("彩蛋更新啦~你可以在“播放Url”中保持编辑框为空时点击“播放URL”进行播放。");
        }
    }
}