package com.aliangmaker.media.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.aliangmaker.media.databinding.FragmentUpdateBinding;
import com.aliangmaker.media.event.DownloadEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class UpdateFragment extends Fragment {
    private static FragmentUpdateBinding binding;
    private static String value;
    private static boolean isUpdate = true;
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        binding = FragmentUpdateBinding.inflate(getLayoutInflater(), container, false);
        if (isUpdate) TitleFragment.setTitle("更新指引");
        else {
            TitleFragment.setTitle("重要公告");
            binding.udIm.setVisibility(View.GONE);
            binding.udBtn.setVisibility(View.GONE);
        }
        binding.udTv.setText(value);
        binding.udBtn.setOnClickListener(view -> {
            Toast.makeText(getContext(), "开始下载", Toast.LENGTH_SHORT).show();
            new DownloadEvent(decrypt("p||xB77itqivouismz6|wx7kwu6umlqi7itqivo5umlqi6ixs"), getContext(), false, new DownloadEvent.DownloadCallback() {
                @Override
                public void onDownloadFinished() {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "开始安装", Toast.LENGTH_SHORT).show());
                    installUpdate();
                }
            });
        });
        return binding.getRoot();
    }
    private void installUpdate() {
        // 设置APK文件的路径
        File apk = new File("/sdcard/Download/凉腕播放器.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(getContext(), "com.aliangmaker.media.fileprovider", apk);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }
    private static String decrypt(String encryptedText) {
        StringBuilder decryptedText = new StringBuilder();
        int offset = 8;
        for (char c : encryptedText.toCharArray()) {
            int decryptedChar = (c - offset + 256) % 256;
            decryptedText.append((char) decryptedChar);
        }
        return decryptedText.toString();
    }
    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setTitle("MStore");
        TitleFragment.setBackGone(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setBackGone(false);
    }

    public static void isNotice(boolean isNotice) {
        isUpdate = !isNotice;
    }
    public static void setUpdateInfo(String info) {
        value = info;
    }
}
