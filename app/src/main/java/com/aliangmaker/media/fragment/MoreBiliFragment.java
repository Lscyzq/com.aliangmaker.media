package com.aliangmaker.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aliangmaker.media.adapter.RecyclerViewAdapter;
import com.aliangmaker.media.databinding.FragmentMoreBiliBinding;
import com.aliangmaker.media.event.AsyncBiliList;
import com.aliangmaker.media.event.BiliVideoBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MoreBiliFragment extends Fragment {
    private FragmentMoreBiliBinding binding;
    private boolean isAsynced = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (binding == null) binding = FragmentMoreBiliBinding.inflate(getLayoutInflater(),container,false);
        EventBus.getDefault().register(this);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        new AsyncBiliList();
        if (BiliVideoBean.getStatue()) {
            binding.pb.setVisibility(View.GONE);
            if (BiliVideoBean.getVideo().isEmpty()) {
                binding.sv.setVisibility(View.VISIBLE);
            } else {
            isAsynced = true;
            setAdapter();
            }
        }
        TitleFragment.setTitle("腕上哔哩");
        TitleFragment.setBackGone(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBiliVideoBean(BiliVideoBean biliVideoBean) {
        if (!isAsynced && biliVideoBean.getStatue()) {
            binding.pb.setVisibility(View.GONE);
            if (biliVideoBean.getVideo().isEmpty()) {
                binding.sv.setVisibility(View.VISIBLE);
            } else setAdapter();
        }
    }

    private void setAdapter() {
        binding.biliRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.biliRv.setAdapter(new RecyclerViewAdapter(getActivity(), false));
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        TitleFragment.setBackGone(true);
        TitleFragment.setTitle("凉腕播放器");
    }
}