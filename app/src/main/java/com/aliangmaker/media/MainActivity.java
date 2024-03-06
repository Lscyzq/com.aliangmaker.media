package com.aliangmaker.media;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.aliangmaker.media.adapter.ViewPageAdapter;
import com.aliangmaker.media.databinding.ActivityMainBinding;
import com.aliangmaker.media.event.ChangeTitleStatue;
import com.aliangmaker.media.event.ServerRequest;
import com.aliangmaker.media.fragment.MoreHelpFragment;
import com.aliangmaker.media.fragment.MoreUpLogFragment;
import com.aliangmaker.media.fragment.TitleFragment;

import com.aliangmaker.media.fragment.UpdateFragment;
import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ChangeTitleStatue changeTitleStatue;
    private ViewPageAdapter viewPageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TitleFragment.setBackGone(true);
        changeTitleStatue = new ChangeTitleStatue(this, binding.mainLl);
        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        binding.mainVp.setAdapter(viewPageAdapter);
        SharedPreferences sharedPreferences = getSharedPreferences("main", MODE_PRIVATE);
        new ServerRequest(this).getVersion(new ServerRequest.versionCallBack() {
            @Override
            public void getVersionSuccess(String lastedVersion, String happyVersion, String noticeVersion) {
                if (Integer.getInteger(lastedVersion.replaceAll("\\.", "")) > Integer.getInteger(getString(R.string.version).replaceAll("\\.", ""))) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fl,new UpdateFragment()).addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out).commit();
                }
            }

            @Override
            public void getVersionFail() {

            }
        });
        if (!sharedPreferences.getString("version","3.14.10").equals(getString(R.string.version))) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fl,new MoreHelpFragment()).addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fl,new MoreUpLogFragment()).addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out).commit();
        }
        if (getSharedPreferences("play_set",MODE_PRIVATE).getBoolean("wipe", false)) {
            binding.mainIm0.setVisibility(View.GONE);
            binding.mainIm1.setVisibility(View.GONE);
            binding.mainIm2.setVisibility(View.GONE);
        }
        binding.mainVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                EventBus.getDefault().post(new ChangeTitleStatue(false));
                if (position == 0) {
                    binding.mainIm0.setImageResource(R.drawable.ba_vp_select);
                    binding.mainIm1.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm2.setImageResource(R.drawable.ba_vp_inselect);
                    if (sharedPreferences.getString("version", "3.14.10").equals(getString(R.string.version))) {
                        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        TitleFragment.setTitle("MStore");
                        TitleFragment.setBackGone(true);
                    }
                } else if (position == 1) {
                    getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    binding.mainIm0.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm1.setImageResource(R.drawable.ba_vp_select);
                    binding.mainIm2.setImageResource(R.drawable.ba_vp_inselect);
                    TitleFragment.setBackGone(true);
                    TitleFragment.setTitle("设置");
                } else if (position == 2) {
                    getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    TitleFragment.setTitle("凉腕播放器");
                    binding.mainIm0.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm1.setImageResource(R.drawable.ba_vp_inselect);
                    binding.mainIm2.setImageResource(R.drawable.ba_vp_select);
                    TitleFragment.setBackGone(true);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        changeTitleStatue.remove();
        viewPageAdapter.remove();
    }
}