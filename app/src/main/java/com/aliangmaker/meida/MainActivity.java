package com.aliangmaker.meida;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements UpdateChecker.UpdateListener,UpdateDetail.DetailListener{

    private boolean getSPSet(String item) {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        if (item.equals("0")) {
            boolean switchState1 = sharedPreferences.getBoolean("switch_state_display0", false);
            return switchState1;
        } else if (item.equals("1")) {
            boolean mediaVisible = sharedPreferences.getBoolean("MediaVisible", false);
            return mediaVisible;
        } else if (item.equals("2")) {
            boolean biliVisible = sharedPreferences.getBoolean("BiliVisible", false);
            return biliVisible;
        }
        throw new IllegalArgumentException("Invalid item: " + item);
    }

    private String getSPSetSring() {
        SharedPreferences sharedPreferences = getSharedPreferences("ifCheck", MODE_PRIVATE);
        String stage = sharedPreferences.getString("Version", getString(R.string.version));
        return stage;
    }
    private void setSPSet(String item) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (item.equals("0")) {
            sharedPreferences.edit().putBoolean("MediaVisible",true).apply();
        } else if (item.equals("1")) {
            sharedPreferences.edit().putBoolean("BiliVisible",true).apply();
        }
    }
    private Timer timer;
    private TextView textViewTime;



    private class UpdateTimeTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(() -> {
                // 获取当前时间，仅包含小时和分钟
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String currentTime = sdf.format(new Date());

                // 更新TextView中的时间显示
                textViewTime.setText(currentTime);
            });
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在活动销毁时停止定时器
        if (timer != null) {
            timer.cancel();
        }
    }
    Intent intent;
    String key ="";
    @Override
    public void onDetailCheckCompleted(String upLog, String happyUrl, String happyName, String noticeDetail) {
        if (key.equals("update")){
            intent.putExtra("upLog", upLog);
            startActivity(intent);
        }else {
            Intent intent1 = new Intent(MainActivity.this, EmptyActivity.class);
            intent1.putExtra("layout", "activity_notice");
            intent1.putExtra("notice", noticeDetail);
            startActivity(intent1);
        }

    }
    @Override
    public void onUpdateCheckCompleted(String latestVersion,String happyVersion,String noticeVersion) {
        if (!latestVersion.equals("netWorkOut")&&!latestVersion.equals("serverOut")) {
            if (!happyVersion.equals(sharedPreferences.getString("happyVersion","1.0"))) {
                sharedPreferences.edit().putBoolean("happyUpdate", true).apply();
                sharedPreferences.edit().putString("lastHappyVersion", happyVersion).apply();
            } else {
                sharedPreferences.edit().putBoolean("happyUpdate", false).apply();
            }
            if (!noticeVersion.equals(sharedPreferences.getString("notice","0.0"))) {
                sharedPreferences.edit().putString("notice", noticeVersion).apply();
                UpdateDetail updateDetail = new UpdateDetail(this, this);
                updateDetail.execute();
            }
        }else if(latestVersion.equals("serverOut")){
            new ChangeServerTask(MainActivity.this).execute();
        }
        if (!latestVersion.equals(R.string.version)&&!latestVersion.equals(getSPSetSring())) {
            if (!latestVersion.equals("netWorkOut")&&!latestVersion.equals("serverOut")) {
                intent = new Intent(MainActivity.this, EmptyActivity.class);
                intent.putExtra("versionCheck", latestVersion);
                intent.putExtra("layout", "activity_update");
                intent.putExtra("visible", "1");
                key = "update";
                UpdateDetail updateDetail = new UpdateDetail(this, this);
                updateDetail.execute();
            }
        }
    }
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
        PermissionUtil.checkPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE,2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.listvideo);
        textViewTime = findViewById(R.id.textViewTime1);
        sharedPreferences = getSharedPreferences("happy", MODE_PRIVATE);
        // 在onCreate或其他适当的地方调用UpdateChecker，传入当前Activity作为Context，并传入this作为UpdateListener

            UpdateChecker updateChecker = new UpdateChecker(this, this);
            updateChecker.execute();

        // 启动定时器，每隔1秒更新时间
        timer = new Timer();
        timer.schedule(new UpdateTimeTask(), 0, 1000);
        String[] data = {"MediaStore","腕上哔哩缓存","播放Url","设置","关于"};
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
                ImageView imageViewWhat = view.findViewById(R.id.aboutWhat);
                String itemText = getItem(position);
                textView.setText(itemText);

                if (position == 0) {
                    imageView.setImageResource(R.drawable.file);
                    if (getSPSet("1")) {
                        imageViewWhat.setVisibility(View.GONE);
                    }
                } else if (position == 3) {
                    imageView.setImageResource(R.drawable.set);
                    imageViewWhat.setVisibility(View.GONE);
                } else if (position == 1){
                    if (getSPSet("2")) {
                        imageViewWhat.setVisibility(View.GONE);
                    }
                    imageView.setImageResource(R.drawable.bili);
                } else if (position == 4) {
                    imageView.setImageResource(R.drawable.check);
                    imageViewWhat.setVisibility(View.GONE);
                } else if (position == 2) {
                    imageView.setImageResource(R.drawable.net);
                    if (sharedPreferences.getBoolean("happyUpdate", false)) {
                        imageViewWhat.setImageResource(R.drawable.up);
                    } else imageViewWhat.setVisibility(View.GONE);
                }

                imageViewWhat.setOnClickListener(v -> {
                    switch (position) {
                        case 0:
                            if (!getSPSet("1")) {
                                setSPSet("0");
                            }
                            Intent intent = new Intent(MainActivity.this,FileEmptyActivity.class);
                            intent.putExtra("pac", "com");
                            intent.putExtra("what",true);
                            startActivity(intent);
                            break;
                        case 1:
                            if (!getSPSet("2")) {
                                setSPSet("1");
                            }
                            Intent intent1 = new Intent(MainActivity.this,FileEmptyActivity.class);
                            intent1.putExtra("pac", "bili");
                            intent1.putExtra("what",true);
                            startActivity(intent1);
                            break;
                        case 2:
                            sharedPreferences.edit().putString("happyVersion",sharedPreferences.getString("lastHappyVersion","1.0")).apply();
                            Intent intent2 = new Intent(MainActivity.this,FileEmptyActivity.class);
                            intent2.putExtra("pac", "url");
                            intent2.putExtra("what",true);
                            startActivity(intent2);
                            break;
                        default:
                            break;
                    }
                });
                return view;
            }
        };
        // Set the adapter for the ListView
        listView.setAdapter(adapter);


        // Set an OnItemClickListener for the ListView
        listView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    // 点击了"此手表"条目
                    // 在这里添加处理"此手表"条目点击事件的代码
                    Intent intent1 = new Intent(MainActivity.this, ListVideoActivity.class);
                    intent1.putExtra("displayset",getSPSet("0"));
                    startActivity(intent1);
                    break;
                case 3:
                    // 点击了"设置"条目
                    // 在这里添加处理"设置"条目点击事件的代码
                    startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                    break;
                case 1:
                    //点击了“腕上哔哩”
                    Intent intent2 = new Intent(MainActivity.this, ListVideoBiliActivity.class);
                    intent2.putExtra("displayset", getSPSet("0"));
                    startActivity(intent2);
                    break;
                case 2:
                    Intent intent = new Intent(MainActivity.this,NetOpenActivity.class);
                    intent.putExtra("happyVersion", sharedPreferences.getString("lastHappyVersion","1.0"));
                    startActivity(intent);
                    break;
                case 4:
                    // 点击了"设置"条目
                    // 在这里添加处理"设置"条目点击事件的代码
                    startActivity(new Intent(MainActivity.this,AboutActivity.class));
                    break;
                default:
                    break;
            }
        });

    }
}
