package com.aliangmaker.meida;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliangmaker.meida.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("设置");
                }
            }
        }, false);

        String[] data = {"显示设置","操作设置","弹幕设置","播放设置"};
        // Create an ArrayAdapter to convert the array into views
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data) {

            @NonNull
            @Override

            public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(R.layout.list_item, parent, false);
                }

                TextView textView = view.findViewById(R.id.textViewlist);
                ImageView imageView = view.findViewById(R.id.imageView);
                ImageView aboutWhat = view.findViewById(R.id.aboutWhat);
                aboutWhat.setVisibility(View.GONE);
                String itemText = getItem(position);
                textView.setText(itemText);

                if (position == 1) {
                    imageView.setImageResource(R.drawable.displayset);
                } else if (position == 0) {
                    imageView.setImageResource(R.drawable.handleset);
                } else if (position == 2) {
                    imageView.setImageResource(R.drawable.danmakuic);
                } else if (position == 3) {
                    imageView.setImageResource(R.drawable.play_set_ic);
                }
                return view;
            }
        };
        // Set the adapter for the ListView
        binding.listset.setAdapter(adapter);
        // Set an OnItemClickListener for the ListView
        binding.listset.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 点击了"显示设置"条目
                        startActivity(new Intent(SettingsActivity.this,DisplayActivity.class));
                        break;
                    case 1:
                        // 点击了"操作设置"条目
                        startActivity(new Intent(SettingsActivity.this,HandleActivity.class));
                        break;
                    case 2:
                        //点击了"弹幕设置"条目
                        startActivity(new Intent(SettingsActivity.this,DanmakuSetActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(SettingsActivity.this,PlaySetActivity.class));
                        break;
                }
            }
        });
    }
}