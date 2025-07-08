package com.aliangmaker.media.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.aliangmaker.media.event.ChangeTitleStatue;

import org.greenrobot.eventbus.EventBus;

public class RollStatueScrollView extends ScrollView {
    private boolean invisible = false;
    private Scroller mScroller;
    public RollStatueScrollView(Context context) {
        super(context);
        mScroller = new Scroller(context);
    }

    public RollStatueScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    public RollStatueScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
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
    //调用此方法滚动到目标位置  duration滚动时间
    public void smoothScrollToSlow(int fx, int fy, int duration) {
        int dx = fx - getScrollX();
        int dy = fy - getScrollY();
        smoothScrollBySlow(dx, dy, duration);
    }
    @Override
    public void computeScroll() {

        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset()) {

            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }

    public void smoothScrollBySlow(int dx, int dy,int duration) {

        //设置mScroller的滚动偏移量
        mScroller.startScroll(getScrollX(), getScrollY(), dx, dy,duration);//scrollView使用的方法（因为可以触摸拖动）
        // mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, duration);  //普通view使用的方法
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

}
