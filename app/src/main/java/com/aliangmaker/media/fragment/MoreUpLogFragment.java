package com.aliangmaker.media.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.R;

public class MoreUpLogFragment extends Fragment {
    SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = getContext().getSharedPreferences("main", Context.MODE_PRIVATE);
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
        if (!sharedPreferences.getString("version","3.14.10").equals(getString(R.string.version))) {
            TitleFragment.setTitle("赞助我们");
        } else {
            TitleFragment.setTitle("凉腕播放器");
            TitleFragment.setBackGone(true);
        }
    }
}