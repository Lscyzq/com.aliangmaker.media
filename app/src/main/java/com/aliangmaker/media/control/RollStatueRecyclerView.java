package com.aliangmaker.media.control;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aliangmaker.media.event.ChangeTitleStatue;

import org.greenrobot.eventbus.EventBus;

public class RollStatueRecyclerView extends RecyclerView {
    private boolean invisible = false;

    public RollStatueRecyclerView(@NonNull Context context) {
        super(context);
    }

    public RollStatueRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RollStatueRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if (!this.canScrollVertically(-1) && invisible) {
            invisible = false;
            EventBus.getDefault().post(new ChangeTitleStatue(false));
        }
        if (dy > 10 && !invisible) {
            invisible = true;
            EventBus.getDefault().post(new ChangeTitleStatue(true));
        } else if (dy < -16 && invisible) {
            invisible = false;
            EventBus.getDefault().post(new ChangeTitleStatue(false));
        }
    }
}
