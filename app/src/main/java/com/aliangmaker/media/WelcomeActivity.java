package com.aliangmaker.media;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.aliangmaker.media.databinding.ActivityWelcomeBinding;
import com.aliangmaker.media.event.AsyncVideoList;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.welTvMain.setText(Html.fromHtml("<font color='#99CC00'>凉腕</font>播放器"));
        playAnim();
    }

    private void playAnim() {
        //灰度动画
        AlphaAnimation alphaAnim = new AlphaAnimation(0.5f, 1f);
        alphaAnim.setDuration(700);
        alphaAnim.setFillAfter(true);
        binding.getRoot().startAnimation(alphaAnim);
        SharedPreferences sharedPreferences = getSharedPreferences("main", MODE_PRIVATE);
        boolean stage = sharedPreferences.getBoolean("signed", false);
        if (stage) {
            binding.welTv3.setVisibility(View.VISIBLE);
            new AsyncVideoList(this,getSharedPreferences("play_set",MODE_PRIVATE).getBoolean("deep",false));
        }
        alphaAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (!stage) {
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                binding.welCo0.setVisibility(View.VISIBLE);
                                binding.welIm2.setVisibility(View.VISIBLE);
                                TranslateAnimation translateAnim = new TranslateAnimation(0f, 0f, 40, 0f);
                                translateAnim.setDuration(600);
                                translateAnim.setFillAfter(true);
                                binding.welTv2.startAnimation(translateAnim);
                                TranslateAnimation translateAnim2 = new TranslateAnimation(0f, 0f, Math.round((float) 40 * getResources().getDisplayMetrics().density), 0f);
                                translateAnim2.setDuration(600);
                                translateAnim2.setFillAfter(true);
                                binding.welCo0.startAnimation(translateAnim2);
                                TranslateAnimation translateAnim3 = new TranslateAnimation(0f, 40f, 0f, 0f);
                                translateAnim3.setDuration(1100);
                                translateAnim3.setFillAfter(true);
                                binding.welIm0.startAnimation(translateAnim3);

                                binding.welIm2.setOnClickListener(v -> finish());
                                binding.welCo0.setOnClickListener(v -> {startActivity(new Intent(WelcomeActivity.this, SetFirstActivity.class));finish();});
                            });
                        }
                    };
                    new Timer().schedule(timerTask, 400);
                } else {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        //平移动画
        TranslateAnimation translateAnim = new TranslateAnimation(0f, 0f, 10f, 0f);
        translateAnim.setDuration(500);
        translateAnim.setFillAfter(true);
        binding.welTvMain.startAnimation(translateAnim);
        TranslateAnimation translateAnim2 = new TranslateAnimation(0f, 0f, 50f, 40f);
        translateAnim2.setDuration(500);
        translateAnim2.setFillAfter(true);
        binding.welTv2.startAnimation(translateAnim2);
    }
}