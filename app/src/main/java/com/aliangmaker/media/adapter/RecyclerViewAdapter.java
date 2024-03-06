package com.aliangmaker.media.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliangmaker.media.PlayVideoActivity;
import com.aliangmaker.media.R;
import com.aliangmaker.media.event.BiliVideoBean;
import com.aliangmaker.media.event.VideoBean;

import java.io.File;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
    Context context;
    private boolean fromMediaStore;
    public RecyclerViewAdapter(Context context, boolean mediaStore) {
        this.context = context;
        this.fromMediaStore = mediaStore;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.basic_video_list0, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) return 0;
        return 1;
    }
    private void initAdapter(List<String[]> video,List<Bitmap> bitmaps,RecyclerViewHolder holder,int position){
        holder.textView.setText(video.get(position)[0]);
        Bitmap bitmap = bitmaps.get(position);
        if (bitmap != null) holder.imageView.setImageBitmap(bitmap);
        holder.itemView.setOnClickListener(v -> {
            String path = video.get(position)[1];
            Intent intent = new Intent(context, PlayVideoActivity.class);
            intent.putExtra("name", video.get(position)[0]);
            intent.putExtra("path", path);
            if (!fromMediaStore) {
                File file = new File(path);
                String parent = file.getParent();
                intent.putExtra("danmaku", parent + "/danmaku.xml");
            }
            context.startActivity(intent);
        });
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (getItemViewType(position) == 0) holder.view.setVisibility(View.VISIBLE);
        if (fromMediaStore) initAdapter(VideoBean.getVideo(),VideoBean.getBitmaps(),holder,position);
        else initAdapter(BiliVideoBean.getVideo(),BiliVideoBean.getBitmaps(),holder,position);

    }

    @Override
    public int getItemCount() {
        if (fromMediaStore)  return VideoBean.getVideo().size();
        else return BiliVideoBean.getVideo().size();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView textView;
        ImageView imageView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.vl_v);
            textView = itemView.findViewById(R.id.bvlTv);
            if (itemView.getContext().getSharedPreferences("play_set",Context.MODE_PRIVATE).getBoolean("small_tv",false)) {
                textView.setTextSize(13);
                textView.setMaxLines(3);
            }
            imageView = itemView.findViewById(R.id.bvlIm);
        }
    }
}
