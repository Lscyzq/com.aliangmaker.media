package com.aliangmaker.media.control;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.aliangmaker.media.event.ChangeTitleStatue;

import org.greenrobot.eventbus.EventBus;

public class RollStatueScrollView extends ScrollView {
    private boolean invisible = false;

    public RollStatueScrollView(Context context) {
        super(context);
    }

    public RollStatueScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RollStatueScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t > 20 && oldt > t && oldt - t > 14 && invisible) {
            EventBus.getDefault().post(new ChangeTitleStatue(false));
            invisible = false;
        } else if (t > 20 && t > oldt && t - oldt > 9 && !invisible) {
            EventBus.getDefault().post(new ChangeTitleStatue(true));
            invisible = true;
        } else if (t < 20 && invisible) {
            EventBus.getDefault().post(new ChangeTitleStatue(false));
            invisible = false;
        }
    }
}
