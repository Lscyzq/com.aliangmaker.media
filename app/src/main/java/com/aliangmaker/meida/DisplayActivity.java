package com.aliangmaker.meida;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliangmaker.meida.databinding.ActivityHandleBinding;

import java.util.ArrayList;
import java.util.List;

public class DisplayActivity extends AppCompatActivity {
    private ActivityHandleBinding binding;
    private ListView listView;
    private List<Item> itemList;
    private CustomAdapter adapter;
    private String[] itemTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHandleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listView = binding.listhandleset;
        itemList = new ArrayList<>();
        adapter = new CustomAdapter();
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("显示设置");
                }
            }
        }, false);
        // 加载数据到itemList中（假设有两个条目）
        itemList.add(new Item("列表小字"));
        itemList.add(new Item("默认横屏"));
        itemList.add(new Item("夜间模式"));
        itemList.add(new Item("音量隐藏"));
        // 设置ListView适配器
        listView.setAdapter(adapter);
    }

    private class CustomAdapter extends ArrayAdapter<Item> {

        public CustomAdapter() {
            super(DisplayActivity.this, 0, itemList);
            // 初始化每个条目的文字
            itemTexts = new String[4];
            itemTexts[0] = "减小列表字号以显示更多内容";
            itemTexts[1] = "视频默认横屏播放";
            itemTexts[2] = "降低屏幕亮度";
            itemTexts[3] = "隐藏播放界面音量按钮";
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
            final Switch switchView = convertView.findViewById(R.id.switch_item);

            // 设置文本
            textView.setText(currentItem.getName());
            textView2.setText(itemTexts[position]);
            // 获取SharedPreferences对象
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DisplayActivity.this);

            // 获取开关状态，默认为false
            boolean switchState = sharedPreferences.getBoolean("switch_state_display" + position, false);
            switchView.setChecked(switchState);

            // 设置开关点击监听器
            switchView.setOnClickListener(v -> {
                // 保存开关状态到SharedPreferences
                sharedPreferences.edit().putBoolean("switch_state_display" + position, switchView.isChecked()).apply();

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