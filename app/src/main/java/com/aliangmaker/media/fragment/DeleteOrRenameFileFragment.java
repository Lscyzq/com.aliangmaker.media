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
import com.aliangmaker.media.adapter.RecyclerViewAdapter;
import com.aliangmaker.media.adapter.ViewPageAdapter;
import com.aliangmaker.media.control.ConfirmationSliderSeekBar;
import com.aliangmaker.media.databinding.FragmentDeleteOrRenameFileBinding;
import com.aliangmaker.media.event.ChangeTitleStatue;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class DeleteOrRenameFileFragment extends Fragment {

    private FragmentDeleteOrRenameFileBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (binding == null) binding = FragmentDeleteOrRenameFileBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
    String info;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.drTvThrow.setText(Html.fromHtml("右滑<font color='#FF0000'>删除</font>"));
        binding.drTvName.setText(getArguments().getString("name"));
        binding.drTvPath.setText(getArguments().getString("path"));
        String filePath = binding.drTvPath.getText().toString();
        File file = new File(filePath);
        String fileName = file.getName();
        binding.drEtRename.setText(fileName);
        //binding.drEtRename.setSelection(0, fileName.lastIndexOf("."));
        binding.drBtnRename.setOnClickListener(v -> {
            String newName = binding.drEtRename.getText().toString();
            String newPath = filePath.substring(0, filePath.lastIndexOf("/") + 1) + newName;
            File newFile = new File(newPath);
            if (newName.equals(fileName)) {
                info = "请输入内容";
            } else if(newFile.exists()) {
                info = "存在同名文件";
            } else if (!isValidFileName(newName)) {
                info = "命名不合法";
            } else if (file.renameTo(newFile)) {
                info = "重命名成功";
                ViewPageAdapter.recyclerViewAdapter.renameItem(getArguments().getInt("pos"), newName, newPath);
                getActivity().onBackPressed();
            } else {
                info = "重命名失败";
            }
            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
        });
        binding.drStcThrow.setOnConfirmationSliderSeekBarChangeListener(() -> {
            if (file.exists()) {
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    info = "删除成功";
                    ViewPageAdapter.recyclerViewAdapter.removeItem(getArguments().getInt("pos"));
                } else {
                    info = "删除失败";
                }
            } else {
                info = "文件不存在";
            }
            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        });
    }
    private static boolean isValidFileName(String fileName) {
        String illegalChars = "/\\:*?\"<>|";
        for (char c : illegalChars.toCharArray()) {
            if (fileName.contains(String.valueOf(c))) {
                return false;
            }
        }
        if (!(fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mkv") || fileName.endsWith(".mov") || fileName.endsWith(".wmv"))){
            return false;
        }
        return true;
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
        EventBus.getDefault().post(new ChangeTitleStatue(false));
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("MStore");
    }
}