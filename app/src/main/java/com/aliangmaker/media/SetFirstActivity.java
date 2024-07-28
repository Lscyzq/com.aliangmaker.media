package com.aliangmaker.media;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.aliangmaker.media.databinding.ActivityFirstSetBinding;
import com.aliangmaker.media.event.Bean0;
import com.aliangmaker.media.event.ChangeTitleStatue;
import com.aliangmaker.media.fragment.*;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public  class SetFirstActivity extends AppCompatActivity {
    private static ActivityFirstSetBinding binding;
    private ChangeTitleStatue changeTitleStatue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirstSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TitleFragment.setTitle("隐私安全");
        changeTitleStatue = new ChangeTitleStatue(this, binding.fsLl);
        EventBus.getDefault().register(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.fs_fl, new SafeFragment()).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        changeTitleStatue.remove();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent0(Bean0 event0) {
        int value = event0.getValue();
        if (value == 1) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    .add(R.id.fs_fl, new AgreementFragment())
                    .commit();
        } else if (value == 2) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    .add(R.id.fs_fl, new SetInitFragment())
                    .commit();
        } else if (value == 0) {
            TitleFragment.setTitle("隐私安全");
        } else if (value ==3) {
            TitleFragment.setTitle("使用协议");
        }
    }
}