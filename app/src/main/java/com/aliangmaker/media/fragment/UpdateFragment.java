package com.aliangmaker.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.aliangmaker.media.databinding.FragmentUpdateBinding;
import org.jetbrains.annotations.NotNull;

public class UpdateFragment extends Fragment {
    private FragmentUpdateBinding binding;
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        binding = FragmentUpdateBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }
}
