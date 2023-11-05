package com.aliangmaker.meida;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliangmaker.meida.databinding.ActivityAppAboutBinding;

public class AppAboutActivity extends AppCompatActivity implements UpdateChecker.UpdateListener,UpdateDetail.DetailListener{
    private ActivityAppAboutBinding binding;
    Intent intent;
    @Override
    public void onUpdateCheckCompleted(String latestVersion,String happyVersion,String noticeVersion) {
        if (!latestVersion.equals(getString(R.string.version))) {
            if (latestVersion.equals("netWorkOut")) {
                Toast.makeText(this, "请检查网络连接！", Toast.LENGTH_SHORT).show();
            } else if (latestVersion.equals("serverOut")) {
                Toast.makeText(this, "耶？服务器好像坏掉了，开始更新服务地址", Toast.LENGTH_SHORT).show();
                new ChangeServerTask(this).execute();
            } else {
                intent = new Intent(AppAboutActivity.this, EmptyActivity.class);
                intent.putExtra("layout", "activity_update");
                UpdateDetail updateDetail = new UpdateDetail(this, this);
                updateDetail.execute();
            }
        } else Toast.makeText(this, "已是最新版！", Toast.LENGTH_SHORT).show();
        binding.progressBar2.setVisibility(View.GONE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBar2.setVisibility(View.GONE);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("应用信息");
                }
            }
        }, false);
        TextView textView = binding.ystk1;
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(AppAboutActivity.this,EmptyActivity.class);
            intent.putExtra("layout","activity_privacy_policy");
            startActivity(intent);
        });
        binding.buttonUrl.setOnClickListener(v -> {
            UpdateChecker updateChecker = new UpdateChecker(AppAboutActivity.this, AppAboutActivity.this);
            updateChecker.execute();
            binding.progressBar2.setVisibility(View.VISIBLE);
        });
        binding.textView57.setOnClickListener(v -> {

                Intent intent = new Intent(AppAboutActivity.this,EmptyActivity.class);
                intent.putExtra("layout", "github");
                startActivity(intent);

        });
        binding.ystk.setOnClickListener(v -> {
            key = "notice";
            binding.progressBar2.setVisibility(View.VISIBLE);
            UpdateDetail updateDetail = new UpdateDetail(AppAboutActivity.this, AppAboutActivity.this);
            updateDetail.execute();
        });
    }
    String key = "";
    @Override
    public void onDetailCheckCompleted(String upLog, String happyUrl, String happyName, String noticeDetail) {
        if(!upLog.equals("netWorkOut")&&!(upLog.equals("serverOut"))) {
            if (!key.equals("notice")) {
                intent.putExtra("upLog", upLog);
                intent.putExtra("visible", "null");
                startActivity(intent);
            } else {
                Intent intent1 = new Intent(AppAboutActivity.this, EmptyActivity.class);
                intent1.putExtra("layout", "activity_notice");
                intent1.putExtra("notice", noticeDetail);
                startActivity(intent1);
            }
            key = "";
        }else if(upLog.equals("netWorkOut")){
            Toast.makeText(this, "请检查网络连接", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "耶？服务器好像坏掉了，开始更新服务地址", Toast.LENGTH_SHORT).show();
            new ChangeServerTask(this).execute();
        }
        binding.progressBar2.setVisibility(View.GONE);
    }
}