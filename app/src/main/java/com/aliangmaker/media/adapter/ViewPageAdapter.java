package com.aliangmaker.media.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliangmaker.media.R;
import com.aliangmaker.media.control.RollStatueScrollView;
import com.aliangmaker.media.event.ChangeTitleStatue;
import com.aliangmaker.media.event.VideoBean;
import com.aliangmaker.media.fragment.MoreHelpFragment;
import com.aliangmaker.media.fragment.MoreInfoFragment;
import com.aliangmaker.media.fragment.MoreUpLogFragment;
import com.aliangmaker.media.fragment.MoreUrlFragment;
import com.aliangmaker.media.fragment.SetDanmakuFragment;
import com.aliangmaker.media.fragment.SetDisplayFragment;
import com.aliangmaker.media.fragment.SetHandleFragment;
import com.aliangmaker.media.fragment.SetVideoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ViewPageAdapter extends RecyclerView.Adapter<ViewPageAdapter.ViewPageHolder> implements View.OnClickListener {
    private static RecyclerViewAdapter recyclerViewAdapter;
    static boolean setAdapter = false;
    FragmentManager fragmentManager;

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new ChangeTitleStatue(false));
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null).setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out);
        int id = v.getId();
        if (id == R.id.set_cl_danmaku) {
            fragmentTransaction.add(R.id.bvp_cl1, new SetDanmakuFragment()).commit();
        } else if (id == R.id.set_cl_display) {
            fragmentTransaction.add(R.id.bvp_cl1, new SetDisplayFragment()).commit();
        } else if (id == R.id.set_cl_handle) {
            fragmentTransaction.add(R.id.bvp_cl1, new SetHandleFragment()).commit();
        } else if (id == R.id.set_cl_video) {
            fragmentTransaction.add(R.id.bvp_cl1, new SetVideoFragment()).commit();
        } else if (id == R.id.more_cl_help) {
            fragmentTransaction.add(R.id.bvp_cl2, new MoreHelpFragment()).commit();
        } else if (id == R.id.more_cl_infor) {
            fragmentTransaction.add(R.id.bvp_cl2, new MoreInfoFragment()).commit();
        } else if (id == R.id.more_cl_uplog) {
            fragmentTransaction.add(R.id.bvp_cl2, new MoreUpLogFragment()).commit();
        } else if (id == R.id.more_cl_url) {
            fragmentTransaction.add(R.id.bvp_cl2, new MoreUrlFragment()).commit();
        }
    }

    public ViewPageAdapter(FragmentManager fragmentTransaction) {
        this.fragmentManager = fragmentTransaction;
    }

    @NonNull
    @Override
    public ViewPageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewPageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.basic_view_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPageHolder holder, int position) {
        holder.basic.removeAllViews();
        holder.basic.addView(holder.constraintLayoutList.get(position));
        if (position == 1) {
            holder.video.setOnClickListener(this);
            holder.handle.setOnClickListener(this);
            holder.display.setOnClickListener(this);
            holder.danmaku.setOnClickListener(this);
        } else if (position == 2) {
            holder.upLog.setOnClickListener(this);
            holder.info.setOnClickListener(this);
            holder.help.setOnClickListener(this);
            holder.url.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }


    static class ViewPageHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        ProgressBar progressBar;
        LinearLayout basic;
        List<ConstraintLayout> constraintLayoutList = new ArrayList<>();
        ConstraintLayout danmaku, display, handle, video, help, info, url, upLog;
        RollStatueScrollView rollStatueScrollView;
        public ViewPageHolder(@NonNull View itemView) {
            super(itemView);
            EventBus.getDefault().register(this);
            recyclerView = itemView.findViewById(R.id.bvp_rv);
            progressBar = itemView.findViewById(R.id.vp_pb);
            help = itemView.findViewById(R.id.more_cl_help);
            info = itemView.findViewById(R.id.more_cl_infor);
            url = itemView.findViewById(R.id.more_cl_url);
            upLog = itemView.findViewById(R.id.more_cl_uplog);
            danmaku = itemView.findViewById(R.id.set_cl_danmaku);
            display = itemView.findViewById(R.id.set_cl_display);
            handle = itemView.findViewById(R.id.set_cl_handle);
            video = itemView.findViewById(R.id.set_cl_video);
            basic = itemView.findViewById(R.id.bvp_ll);
            rollStatueScrollView = itemView.findViewById(R.id.sv);
            constraintLayoutList.add(itemView.findViewById(R.id.bvp_cl0));
            constraintLayoutList.add(itemView.findViewById(R.id.bvp_cl1));
            constraintLayoutList.add(itemView.findViewById(R.id.bvp_cl2));
            if (VideoBean.getStatue() && VideoBean.getVideo().isEmpty()) {
                progressBar.setVisibility(View.GONE);
                rollStatueScrollView.setVisibility(View.VISIBLE);
            } else if (VideoBean.getStatue()) {
                progressBar.setVisibility(View.GONE);
                setAdapter();
                setAdapter = true;
            }
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onVideoEvent(VideoBean videoEvent) {
            if (videoEvent.getStatue() && !setAdapter) {
                progressBar.setVisibility(View.GONE);
                if (videoEvent.getVideo().isEmpty()) {
                    rollStatueScrollView.setVisibility(View.VISIBLE);
                } else setAdapter();
            }
        }

        private void setAdapter() {
            recyclerViewAdapter = new RecyclerViewAdapter(itemView.getContext());
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

    }

    public void remove() {
        EventBus.getDefault().unregister(ViewPageHolder.class);
    }
}
