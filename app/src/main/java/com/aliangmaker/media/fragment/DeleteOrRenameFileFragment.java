package com.aliangmaker.media.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aliangmaker.media.R;
import com.aliangmaker.media.control.ConfirmationSliderSeekBar;
import com.aliangmaker.media.databinding.FragmentDeleteOrRenameFileBinding;

import java.io.File;

public class DeleteOrRenameFileFragment extends Fragment {

    private FragmentDeleteOrRenameFileBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (binding == null) binding = FragmentDeleteOrRenameFileBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.drTvThrow.setText(Html.fromHtml("滑动<font color='#FF0000'>删除</font>"));
        binding.drTvName.setText(getArguments().getString("name"));
        binding.drTvPath.setText(getArguments().getString("path"));
        binding.drBtnRename.setOnClickListener(v -> {

        });
        binding.drStcThrow.setOnConfirmationSliderSeekBarChangeListener(() -> {
            String filePath = (String) binding.drTvPath.getText();
            File file = new File(filePath); // 创建文件对象
            String info;
            if (file.exists()) { // 检查文件是否存在
                boolean isDeleted = file.delete(); // 删除文件
                if (isDeleted) {
                    info = "文件删除成功";
                } else {
                    info = "文件删除失败";
                }
            } else {
                info = "文件不存在";
            }
            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("更多");
        TitleFragment.setBackGone(false);
    }
    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("MStore");
    }
}