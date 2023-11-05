package com.aliangmaker.meida;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliangmaker.meida.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.aliangmaker.meida.databinding.ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("关于");
                }
            }
        }, false);

        String[] data = {"应用信息","更新日志","作者的话","赞助我们","官方群聊\n（反馈）"};
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
                    imageView.setImageResource(R.drawable.up);
                } else if (position == 0) {
                    imageView.setImageResource(R.drawable.about);
                } else if (position == 3) {
                    imageView.setImageResource(R.drawable.hand);
                } else if (position == 4) {
                    imageView.setImageResource(R.drawable.errorback);
                } else if (position == 2) {
                    imageView.setImageResource(R.drawable.auther);
                }
                return view;
            }
        };
        // Set the adapter for the ListView
        binding.listabout.setAdapter(adapter);
        // Set an OnItemClickListener for the ListView
        binding.listabout.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    // 点击了"应用信息"条目
                    startActivity(new Intent(AboutActivity.this,AppAboutActivity.class));
                    break;
                case 1:
                    Intent intent = new Intent(AboutActivity.this,EmptyActivity.class);
                    intent.putExtra("layout","activity_up_log");
                    startActivity(intent);
                    break;
                case 2:
                    Intent intent1 = new Intent(AboutActivity.this,EmptyActivity.class);
                    intent1.putExtra("layout","activity_aliang_unique");
                    startActivity(intent1);
                    break;
                case 3:
                    Intent intent2 = new Intent(AboutActivity.this,EmptyActivity.class);
                    intent2.putExtra("layout","activity_allowance");
                    startActivity(intent2);
                    break;
                case 4:
                    Intent intent3 = new Intent(AboutActivity.this,EmptyActivity.class);
                    intent3.putExtra("layout","activity_error_back");
                    startActivity(intent3);
                    break;
            }
        });
    }
}