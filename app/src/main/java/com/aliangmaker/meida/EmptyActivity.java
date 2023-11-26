package com.aliangmaker.meida;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aliangmaker.meida.databinding.ActivityErrorBackBinding;
import com.aliangmaker.meida.databinding.ActivitySingleTouchBinding;
import com.aliangmaker.meida.databinding.ActivityVersionUpBinding;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;

public class EmptyActivity extends AppCompatActivity {
    private ActivitySingleTouchBinding binding;
    private ActivityVersionUpBinding bindingVersion;

    private void setSPSet(String item) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EmptyActivity.this);
        if (item.equals("0")) {
            sharedPreferences.edit().putBoolean("single_touch",true).apply();
        }
    }
    String layout;
    private void setTitleText (String text){
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText(text);

                }
            }
        }, false);
    }
    private void installUpdate() {
        // 设置APK文件的路径
        File apk = new File("/sdcard/Download/凉腕播放器.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(this, "com.aliangmaker.media.fileprovider", apk);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        layout = getIntent().getStringExtra("layout");
        if (layout.equals("activity_privacy_policy")) {
            setContentView(R.layout.activity_privacy_policy);
            setTitleText("隐私条款");
        } else if (layout.equals("activity_up_log")) {
            setContentView(R.layout.activity_up_log);
            setTitleText("更新日志");
        } else if (layout.equals("activity_aliang_unique")) {
            setContentView(R.layout.activity_aliang_unique);
            setTitleText("作者的话");
        } else if (layout.equals("activity_allowance")) {
            setContentView(R.layout.activity_allowance);
            setTitleText("赞助我们");
        } else if (layout.equals("activity_error_back")) {
            setContentView(R.layout.activity_error_back);
            setTitleText("建议反馈");
        } else if (layout.equals("activity_single_touch")) {
            binding = ActivitySingleTouchBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            setSPSet("0");
            setTitleText("单指说明");
        } else if (layout.equals("activity_update")) {
            SharedPreferences sharedPreference = getSharedPreferences("stage",MODE_PRIVATE);
            String value = sharedPreference.getString("server","http://aliangmaker.top/");
            bindingVersion = ActivityVersionUpBinding.inflate(getLayoutInflater());
            setContentView(bindingVersion.getRoot());
            setTitleText("更新指引");
            bindingVersion.textView42.setText(getIntent().getStringExtra("upLog")+"\n建议你直接点击下方按钮更新（安装包保存在/sdcard/Download/），或者访问通过"+value+"com.media/aliang-media.apk访问下载（长按以复制），也可以通过洋葱商店进行更新以体验最新版（其他商店审核需要时间）");
            bindingVersion.textView42.setOnLongClickListener(v -> {
                CharSequence text = value+"com.media/aliang-media.apk";
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", text);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(EmptyActivity.this, "已复制链接！", Toast.LENGTH_SHORT).show();
                return true;
            });
            bindingVersion.updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(EmptyActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
                    OkHttpClient mOkHttpClient = new OkHttpClient();
                    String url = value+"com.media/aliang-media.apk";
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
                            DownloadDanmakuTask task = new DownloadDanmakuTask(EmptyActivity.this, () -> runOnUiThread(() -> {
                                installUpdate();
                                Toast.makeText(EmptyActivity.this, "下载完成，即将安装", Toast.LENGTH_SHORT).show();
                            }));
                            task.execute(result,"凉腕播放器.apk");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();

                }
            });
            if (getIntent().getStringExtra("visible").equals("null")) {
                bindingVersion.checkBox.setVisibility(View.GONE);
            } else {
                bindingVersion.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("ifCheck", MODE_PRIVATE);
                    if (isChecked) {
                        sharedPreferences.edit().putString("Version", getIntent().getStringExtra("versionCheck")).apply();
                    } else
                        sharedPreferences.edit().putString("Version", getString(R.string.version)).apply();
                });
            }
        } else if (layout.equals("github")) {
            ActivityErrorBackBinding bindingGithub;
            bindingGithub = ActivityErrorBackBinding.inflate(getLayoutInflater());
            setContentView(bindingGithub.getRoot());
            bindingGithub.imageView12.setImageResource(R.drawable.github);
            bindingGithub.textView27.setText("https://lscyzq.github.io/access/com.aliangmaker.media.txt");
            bindingGithub.textView27.setOnLongClickListener(v -> {
                CharSequence text = "https://lscyzq.github.io/access/com.aliangmaker.media.txt";
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", text);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(EmptyActivity.this, "已复制链接！", Toast.LENGTH_SHORT).show();
                return true;
            });
            bindingGithub.textView28.setText("请通过其他设备扫描上方二维码打开或者长按二维码下方链接进行复制");
            bindingGithub.textView33.setVisibility(View.GONE);
            bindingGithub.textView32.setVisibility(View.GONE);
            setTitleText("接入指引");
        }else if (layout.equals("activity_notice")) {
            bindingVersion = ActivityVersionUpBinding.inflate(getLayoutInflater());
            setContentView(bindingVersion.getRoot());
            setTitleText("重要公告");
            bindingVersion.updateButton.setVisibility(View.GONE);
            bindingVersion.detailTv.setVisibility(View.GONE);
            bindingVersion.checkBox.setVisibility(View.GONE);
            bindingVersion.imageView14.setVisibility(View.GONE);
            bindingVersion.textView42.setText(getIntent().getStringExtra("notice"));
        }
    }
}