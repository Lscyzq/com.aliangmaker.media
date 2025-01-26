package com.aliangmaker.media.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliangmaker.media.R;
import com.aliangmaker.media.databinding.FragmentDeleteOrRenameFileBinding;

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
        Log.e("ddddd", getArguments().getString("name"));
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