package com.aliangmaker.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.aliangmaker.media.R;

public class MoreUpLogFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more_up_log, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("更新日志");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setTitle("凉腕播放器");
        TitleFragment.setBackGone(true);
    }
}