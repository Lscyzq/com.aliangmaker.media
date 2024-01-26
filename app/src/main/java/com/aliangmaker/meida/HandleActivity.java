package com.aliangmaker.meida;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aliangmaker.meida.databinding.ActivityHandleBinding;

import java.util.ArrayList;
import java.util.List;

public class HandleActivity extends AppCompatActivity {
    private ActivityHandleBinding binding;
    private ListView listView;
    private List<Item> itemList;
    private CustomAdapter adapter;
    private String[] itemTexts;
    private boolean getSPSet(String item) {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HandleActivity.this);
        if (item.equals("2")) {
            boolean switchState1 = sharedPreferences.getBoolean("single_touch", false);
            return switchState1;
        }
        throw new IllegalArgumentException("Invalid item: " + item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHandleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("操作设置");
                }
            }
        }, false);

        listView = binding.listhandleset;
        itemList = new ArrayList<>();
        adapter = new CustomAdapter();

        // 加载数据到itemList中（假设有两个条目）
        itemList.add(new Item("播完返回"));
        itemList.add(new Item("右滑返回"));
        itemList.add(new Item("单指缩放"));
        itemList.add(new Item("静音开播"));

        // 设置ListView适配器
        listView.setAdapter(adapter);
    }

    private class CustomAdapter extends ArrayAdapter<Item> {

        public CustomAdapter() {
            super(HandleActivity.this, 0, itemList);
            // 初始化每个条目的文字
            itemTexts = new String[4];
            itemTexts[0] = "视频播放完毕将自动返回";
            itemTexts[1] = "视频播放界面可右滑返回";
            itemTexts[2] = "特殊手势可实现单指缩放";
            itemTexts[3] = "开始播放时会将手表静音";
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_set_item, parent, false);
            }

            Item currentItem = getItem(position);

            TextView textView = convertView.findViewById(R.id.danmakuSeekbar_name);
            TextView textView2 = convertView.findViewById(R.id.textIllustrate);
            ImageView imageView = convertView.findViewById(R.id.aboutWhatSet);

            imageView.setVisibility(View.GONE);
            if (position == 2 && !getSPSet("2")) {
                imageView.setVisibility(View.VISIBLE);
            }
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(HandleActivity.this, EmptyActivity.class);
                intent.putExtra("layout", "activity_single_touch");
                startActivity(intent);
            });
            final Switch switchView = convertView.findViewById(R.id.switch_item);

            // 设置文本
            textView.setText(currentItem.getName());
            textView2.setText(itemTexts[position]);
            // 获取SharedPreferences对象
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HandleActivity.this);

            // 获取开关状态，默认为false
            boolean switchState = sharedPreferences.getBoolean("switch_state_" + position, false);
            switchView.setChecked(switchState);

            // 设置开关点击监听器
            switchView.setOnClickListener(v -> {
                // 保存开关状态到SharedPreferences
                sharedPreferences.edit().putBoolean("switch_state_" + position, switchView.isChecked()).apply();
            });

            return convertView;
        }
    }

    private class Item {
        private String name;

        public Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}