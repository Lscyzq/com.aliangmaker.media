package com.aliangmaker.media.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.aliangmaker.media.R;
public class MoreHelpFragment extends Fragment {
    SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = getContext().getSharedPreferences("main", Context.MODE_PRIVATE);
        return inflater.inflate(R.layout.fragment_more_help, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        TitleFragment.setTitle("赞助我们");
        TitleFragment.setBackGone(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!sharedPreferences.getString("version","3.14.10").equals(getString(R.string.version))) {
            sharedPreferences.edit().putString("version",getString(R.string.version)).apply();
            TitleFragment.setTitle("MStore");
        } else TitleFragment.setTitle("凉腕播放器");
        TitleFragment.setBackGone(true);
    }
}