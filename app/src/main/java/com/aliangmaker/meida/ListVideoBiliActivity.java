package com.aliangmaker.meida;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ListVideoBiliActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<String> mVideoList;
    private ArrayList<String> mVideoPathList;
    private ArrayAdapter<String> mAdapter;
    private ProgressBar mProgressBar;
    private boolean textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_video_activity);

        mListView = findViewById(R.id.listvideo);
        mProgressBar = findViewById(R.id.progressBar);
        textSize = getIntent().getBooleanExtra("displayset",false);
        mVideoList = new ArrayList<>();
        mVideoPathList = new ArrayList<>();

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("腕上哔哩");
                }
            }
        }, false);
        new ListVideoTask().execute("/storage/emulated/0/Android/media/cn.luern0313.wristbilibili/download/");
    }

    private class ListVideoTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            String directoryPath = params[0];
            File directory = new File(directoryPath);
            listVideosRecursive(directory);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            if (mVideoList.isEmpty()) {
                Intent intent = new Intent(ListVideoBiliActivity.this,FileEmptyActivity.class);
                intent.putExtra("pac","bili");
                startActivity(intent);
                finish();
            }
            mAdapter=new ArrayAdapter<String>(ListVideoBiliActivity.this,R.layout.list_item_video,R.id.textViewlist,mVideoList){
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View view;
                    if (convertView == null) {
                        view = getLayoutInflater().inflate(R.layout.list_item_video, parent, false);
                    } else {
                        view = convertView;
                    }
                    view=super.getView(position,convertView,parent);

                    String videoPath=mVideoPathList.get(position);
                    File videoFile=new File(videoPath);
                    File parentDir=new File(videoFile.getParent());
                    String coverPath=parentDir.getAbsolutePath()+"/cover.png";
                    Bitmap bitmap=BitmapFactory.decodeFile(coverPath);

                    if(bitmap!=null){
                        //添加小圆角
                        int cornerRadius=20;
                        Bitmap roundedThumbnailBitmap=getRoundedCornerBitmap(bitmap,cornerRadius);

                        //设置缩略图到ImageView
                        ImageView imageViewThumbnail=view.findViewById(R.id.imageView);
                        imageViewThumbnail.setImageBitmap(roundedThumbnailBitmap);
                    }

                    // Add your click listener
                    view.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            String videoName=mVideoList.get(position);
                            String videoPath=mVideoPathList.get(position);

                            // 获取视频文件路径
                            Intent intent=new Intent(ListVideoBiliActivity.this,SaveGetVideoProgressService.class);
                            intent.putExtra("videoName", videoName);
                            intent.putExtra("videoPath", videoPath);
                            // 将视频文件路径作为额外数据传递
                            intent.putExtra("play", "play");
                            intent.putExtra("activity", true);
                            startService(intent);
                        }
                    });

                    // Add your long click listener
                    view.setOnLongClickListener(new View.OnLongClickListener(){
                        private String getParentFolderPath(String videoPath) {
                            File videoFile = new File(videoPath);
                            String parentPath = videoFile.getParent(); // 获取上一层文件夹路径
                            File parentDir = new File(parentPath);
                            String grandParentPath = parentDir.getParent(); // 获取上两层文件夹路径
                            return grandParentPath;
                        }
                        private void deleteFolder(String folderPath) {
                            File folder = new File(folderPath);
                            if (folder.exists()) {
                                File[] files = folder.listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if (file.isDirectory()) {
                                            deleteFolder(file.getPath()); // 递归删除子文件夹
                                        } else {
                                            file.delete(); // 删除子文件
                                        }
                                    }
                                }
                                folder.delete(); // 删除空文件夹
                            }
                        }
                        @Override
                        public boolean onLongClick(View v){

                            if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())) {
                                if(sure == 0){
                                    Toast.makeText(ListVideoBiliActivity.this, "再次长按以删除", Toast.LENGTH_SHORT).show();
                                    sure = 1;
                                } else if (sure == 1) {
                                    String videoPath = mVideoPathList.get(position);
                                    String grandParentFolderPath = getParentFolderPath(videoPath);
                                    deleteFolder(grandParentFolderPath);
                                    mVideoList.remove(position);
                                    mVideoPathList.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                    Toast.makeText(ListVideoBiliActivity.this, "已尝试删除", Toast.LENGTH_SHORT).show();
                                    sure = 0;
                                }

                            } else
                                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                            // 创建一个AlertDialog.Builder实例

                            return true;
                        }
                    });
                    if (textSize) {
                        TextView textView = view.findViewById(R.id.textViewlist);
                        textView.setTextSize(13);
                        textView.setMaxLines(3);
                    }
                    return view;
                }
            };

            mListView.setAdapter(mAdapter);
        }
    }
    int sure = 0;
    private void listVideosRecursive(File directory) {
        File[] files = directory.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".mp4"));

        if (files != null) {
            for (File file : files) {
                mVideoList.add(getVideoTitle(file.getParentFile()));
                mVideoPathList.add(file.getAbsolutePath());
            }
        }

        File[] subDirectories = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        if (subDirectories != null) {
            for (File subDirectory : subDirectories) {
                listVideosRecursive(subDirectory);
            }
        }
    }
    private String getVideoTitle(File parentDir){
        try{
            File infoFile = new File(parentDir, "info.json");
            if(infoFile.exists()){
                FileInputStream fis = new FileInputStream(infoFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine())!=null){
                    sb.append(line);
                }
                br.close();
                fis.close();
                JSONObject jsonObject = new JSONObject(sb.toString());

                String fullTitle = jsonObject.getString("video_title");

                // Split the title at "-" and get the second part
                if(fullTitle.contains("-")) {
                    String[] parts = fullTitle.split("-", 2);
                    return parts[1].trim(); // Return second part of the split string
                } else {
                    // If "-" not found in title, return the full title
                    return fullTitle;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return "";
    }
    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int cornerRadius){
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = cornerRadius;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}