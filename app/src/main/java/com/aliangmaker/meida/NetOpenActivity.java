package com.aliangmaker.meida;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliangmaker.meida.databinding.ActivityNetOpenBinding;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetOpenActivity extends AppCompatActivity implements UpdateDetail.DetailListener{
    private ActivityNetOpenBinding binding;
    private String url;
    @Override
    public void  onDetailCheckCompleted(String upLog, String happyUrl, String happyName, String noticeDetail) {
        if (!upLog.equals("netWorkOut")&&!upLog.equals("serverOut")) {
            url = resolveContentUriToFilePath(happyUrl);
            // Request 中封装了请求相关信息
            Request request = new Request.Builder()
                    .url(url)   // 设置请求地址
                    .get()                          // 使用 Get 方法
                    .build();

            // 同步 Get 请求
            new Thread(() -> {
                String result;
                Response response;
                try {
                    response = mOkHttpClient.newCall(request).execute();
                    result = String.valueOf(response.request().url());
                    intent.putExtra("videoPath", result);
                    intent.putExtra("videoName", happyName);
                    intent.putExtra("internet", true);
                    startService(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }else if(upLog.equals("netWorkOut")) {
            Toast.makeText(this, "请检查网络连接！", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "耶？服务器好像坏掉了，开始更新服务地址", Toast.LENGTH_SHORT).show();
            new ChangeServerTask(this).execute();
        }
        binding.buttonUrl.setClickable(true);
        binding.progressBar4.setVisibility(View.GONE);
    }
    private String resolveContentUriToFilePath(String result) {
        int index = result.indexOf("http");
        result = result.substring(index);
        return result;
    }
    Intent intent;
    OkHttpClient mOkHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNetOpenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mOkHttpClient = new OkHttpClient();
        binding.progressBar4.setVisibility(View.GONE);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("播放Url");
                }
            }
        }, false);
        binding.buttonDownload.setOnClickListener(view -> {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnected();
            String url = binding.editText.getText().toString().trim();
            if(isConnected) {
                if(url.startsWith("http://")||url.startsWith("https://")) {
                    if (!url.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(NetOpenActivity.this, "开始尝试下载", Toast.LENGTH_SHORT).show());
                        Request request = new Request.Builder()
                                .url(url)   // 设置请求地址
                                .get()                          // 使用 Get 方法
                                .build();

                        // 同步 Get 请求
                        new Thread(() -> {
                            String result;
                            Response response;

                            try {
                                response = mOkHttpClient.newCall(request).execute();
                                result = String.valueOf(response.request().url());
                                Log.e("url", result);
                                DownloadDanmakuTask task = new DownloadDanmakuTask(this, () -> runOnUiThread(() -> Toast.makeText(NetOpenActivity.this, "尝试下载结束", Toast.LENGTH_SHORT).show()));
                                task.execute(result, url);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } else Toast.makeText(NetOpenActivity.this, "还没有输入链接捏", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(this, "请输入正确的链接", Toast.LENGTH_SHORT).show();
            }else Toast.makeText(this, "请检查网络连接！", Toast.LENGTH_SHORT).show();
        });
        binding.buttonUrl.setOnClickListener(v -> {
            String url = binding.editText.getText().toString().trim();
            intent = new Intent(NetOpenActivity.this, SaveGetVideoProgressService.class);
            intent.putExtra("play", "play");
            intent.putExtra("activity", false);
            if (url.isEmpty()) {
                SharedPreferences sharedPreferences = getSharedPreferences("happy", MODE_PRIVATE);
                sharedPreferences.edit().putString("happyVersion",getIntent().getStringExtra("happyVersion")).apply();
                binding.progressBar4.setVisibility(View.VISIBLE);
                binding.buttonUrl.setClickable(false);
                // 在onCreate或其他适当的地方调用UpdateChecker，传入当前Activity作为Context，并传入this作为UpdateListener
                UpdateDetail updateDetail = new UpdateDetail(NetOpenActivity.this, NetOpenActivity.this);
                updateDetail.execute();
            } else {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = networkInfo != null && networkInfo.isConnected();
                if (isConnected) {
                    intent.putExtra("videoName", url);
                    intent.putExtra("videoPath", url);
                    intent.putExtra("internet", true);
                    startService(intent);
                }else Toast.makeText(NetOpenActivity.this, "请检查网络连接！", Toast.LENGTH_SHORT).show();
            }
        });
    }

}