package com.aliangmaker.meida;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;

public class ListVideoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_video_activity);

        mListView = findViewById(R.id.listvideo);
        mProgressBar = findViewById(R.id.progressBar);
        mVideoList = new ArrayList<>();
        mAdapter = new VideoAdapter();
        mListView.setAdapter(mAdapter);
        textSize = getIntent().getBooleanExtra("displayset",false);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                if(f instanceof BaseTitleFragment){
                    ((BaseTitleFragment)f).setTextViewText("MediaStore");
                }
            }

        }, false);

        // 初始化 CursorLoader 加载视频列表
        getSupportLoaderManager().initLoader(1, null, this);
    }
    private ListView mListView;
    private boolean textSize;
    private ArrayList<VideoItem> mVideoList;
    private VideoAdapter mAdapter;
    private ProgressBar mProgressBar;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // 创建 CursorLoader 加载视频信息
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Thumbnails.DATA};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        return new CursorLoader(
                this,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mVideoList.clear();
        // 加载完成后将视频信息和缩略图信息保存到列表中
        if (data != null && data.moveToFirst()) {
            do {
                String videoName = data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String thumbnailUri = data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                long videoId = data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String videoPath = data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)); // 获取视频路径

                mVideoList.add(new VideoItem(videoName, thumbnailUri, videoId, videoPath));
            } while (data.moveToNext());

            // 更新适配器的数据
            mAdapter.notifyDataSetChanged();
        }

        // 隐藏进度条
        mProgressBar.setVisibility(View.GONE);
        // 如果视频列表为空，跳转到 Activity A
        if (mVideoList.isEmpty()) {
            Intent intent = new Intent(ListVideoActivity.this,FileEmptyActivity.class);
            intent.putExtra("pac","com");
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // 重置数据时清空列表
        mVideoList.clear();

        // 更新适配器的数据
        mAdapter.notifyDataSetChanged();
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int cornerRadius) {
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
    int sure=0;
    // 自定义适配器 VideoAdapter
    private class VideoAdapter extends ArrayAdapter<VideoItem> {
        public VideoAdapter() {
            super(ListVideoActivity.this, R.layout.list_item, mVideoList);
        }

        @NonNull
        @Override

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.list_item, parent, false);
            }

            VideoItem videoItem = getItem(position);

            // 设置视频名称
            TextView textViewVideoName = view.findViewById(R.id.textViewlist);
            textViewVideoName.setText(videoItem.getVideoName());

            // 设置视频缩略图
            ImageView imageViewThumbnail = view.findViewById(R.id.imageView);
            ImageView aboutWhat = view.findViewById(R.id.aboutWhat);
            aboutWhat.setVisibility(View.GONE);
            // 使用MediaStore.Video.Thumbnails获取系统生成的视频缩略图
            Bitmap thumbnailBitmap = MediaStore.Video.Thumbnails.getThumbnail(
                    getContentResolver(),
                    videoItem.getId(),
                    MediaStore.Video.Thumbnails.MICRO_KIND,
                    null
            );

            if (thumbnailBitmap != null) {
                int cornerRadius = 20; // 圆角半径，根据需要调整大小
                Bitmap roundedThumbnailBitmap = getRoundedCornerBitmap(thumbnailBitmap, cornerRadius);
                imageViewThumbnail.setImageBitmap(roundedThumbnailBitmap);
            }
            if (textSize) {
                TextView textView = view.findViewById(R.id.textViewlist);
                textView.setTextSize(13);
                textView.setMaxLines(3);
            }
            view.setOnLongClickListener(v -> {

                // 处理长按视频项的操作
                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())) {
                    if(sure == 0){
                        Toast.makeText(ListVideoActivity.this, "再次长按以删除", Toast.LENGTH_SHORT).show();
                        sure = 1;
                    } else if (sure == 1) {
                        deleteVideoFile(videoItem.getVideoPath());
                        Toast.makeText(ListVideoActivity.this, "已尝试删除", Toast.LENGTH_SHORT).show();
                        sure = 0;
                    }

                } else
                    startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                return true;
            });
            view.setOnClickListener(v -> {
                // 在这里处理点击事件
                String videoName = videoItem.getVideoName();
                String videoPath = videoItem.getVideoPath();

                // 启动服务并传递数据
                Intent intent = new Intent(ListVideoActivity.this, SaveGetVideoProgressService.class);
                intent.putExtra("videoName", videoName);
                intent.putExtra("videoPath", videoPath);
                intent.putExtra("play", "play");
                intent.putExtra("activity", false);
                startService(intent);
            });

            return view;
        }
    }

    private void deleteVideoFile(String videoPath) {
        ContentResolver contentResolver = getContentResolver();

        // 查询MediaStore中匹配该视频路径的记录
        Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Video.Media.DATA + "=?";
        String[] selectionArgs = new String[]{videoPath};

        Cursor cursor = contentResolver.query(contentUri, null, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            // 获取视频文件的MediaStore ID
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));

            // 构建删除请求
            Uri deleteUri = ContentUris.withAppendedId(contentUri, id);

            // 删除视频文件
            int rowsDeleted = contentResolver.delete(deleteUri, null, null);

            cursor.close();
        }
    }

    private static class VideoItem {
        private String videoName;
        private String thumbnailUri;
        private long id;
        private String videoPath; // 添加视频路径信息

        public VideoItem(String videoName, String thumbnailUri, long id, String videoPath) {
            this.videoName = videoName;
            this.thumbnailUri = thumbnailUri;
            this.id = id;
            this.videoPath = videoPath; // 存储视频路径信息
        }

        public String getVideoName() {
            return videoName;
        }

        public long getId() {
            return id;
        }

        public String getVideoPath() {
            return videoPath;
        }
    }
}
