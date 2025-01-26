package com.aliangmaker.media.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.PlayVideoActivity;
import com.aliangmaker.media.databinding.FragmentMoreUrlBinding;
import com.aliangmaker.media.event.ServerRequest;

public class MoreUrlFragment extends Fragment {
    private FragmentMoreUrlBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoreUrlBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("播放URL");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EditText editText = binding.editText;
        binding.buttonUrl.setOnClickListener(view -> {
            binding.pb.setVisibility(View.VISIBLE);
            String text = editText.getText().toString().trim();
            if (text.equals("我爱阿凉")) {
                new ServerRequest(getActivity()).getUrl(new ServerRequest.urlCallBack() {
                    @Override
                    public void getUrlSuccess(String upLog, String noticeDetail, String happyName, String happyUrl) {
                        Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
                        intent.putExtra("name", happyName);
                        intent.putExtra("path", happyUrl);
                        binding.pb.setVisibility(View.INVISIBLE);
                        startActivity(intent);
                    }
                    @Override
                    public void getUrlFail() {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "服务器请求失败", Toast.LENGTH_SHORT).show();
                            binding.pb.setVisibility(View.INVISIBLE);
                        });
                    }
                });
            } else if (text.startsWith("http")){
                Intent intent = new Intent(getContext(), PlayVideoActivity.class);
                intent.setData(Uri.parse(text));
                binding.pb.setVisibility(View.INVISIBLE);
                startActivity(intent);
            } else {
                binding.pb.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "请输入正确的链接", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("凉腕播放器");
    }
}