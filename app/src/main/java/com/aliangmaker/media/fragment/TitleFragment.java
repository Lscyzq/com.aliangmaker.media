package com.aliangmaker.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.databinding.FragmentTitleBinding;

public class TitleFragment extends Fragment {
    private static FragmentTitleBinding binding;
    private static ConstraintLayout.LayoutParams layoutParams;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTitleBinding.inflate(getLayoutInflater());
        binding.ttBtnBack.setOnClickListener(v -> getActivity().onBackPressed());
        return binding.getRoot();
    }
    public static void setTitle(String title) {
        binding.ttTvMain.setText(title);
    }

    public static void setBackGone(boolean b) {
        if(b) {
            binding.ttIm0.setVisibility(View.GONE);
            binding.ttBtnBack.setVisibility(View.GONE);
        }
        else {
            binding.ttIm0.setVisibility(View.VISIBLE);
            binding.ttBtnBack.setVisibility(View.VISIBLE);
        }
    }
}