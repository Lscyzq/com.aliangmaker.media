package com.aliangmaker.meida;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.aliangmaker.meida.databinding.FragmentBaseTitleBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class BaseTitleFragment extends Fragment {

    TextView textView;
    private class UpdateTimeTask extends TimerTask {
        @Override
        public void run() {
            getActivity().runOnUiThread(() -> {
                // 获取当前时间，仅包含小时和分钟
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String currentTime = sdf.format(new Date());

                // 更新TextView中的时间显示
                textViewTime.setText(currentTime);
            });
        }
    }
    private FragmentBaseTitleBinding binding;
    public static BaseTitleFragment newInstance() {
        return new BaseTitleFragment();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 在活动销毁时停止定时器
        if (timer != null) {
            timer.cancel();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBaseTitleBinding.inflate(getLayoutInflater());
        ImageView btnBack = binding.backBase;
        textViewTime = binding.textViewTimeBase;
        textView = binding.textViewName;
        timer = new Timer();
        timer.schedule(new UpdateTimeTask(), 0, 1000);
        btnBack.setOnClickListener(v -> getActivity().onBackPressed());
        binding.textViewName.setOnClickListener(v -> getActivity().onBackPressed());
        return binding.getRoot();
    }
    public void setTextViewText(String value) {
        textView.setText(value);
    }

    private Timer timer;
    private TextView textViewTime;
}
