package com.aliangmaker.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.aliangmaker.media.databinding.FragmentMoreInfoBinding;

public class MoreInfoFragment extends Fragment {

    private FragmentMoreInfoBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoreInfoBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("应用信息");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setTitle("凉腕播放器");
        TitleFragment.setBackGone(true);
    }
}