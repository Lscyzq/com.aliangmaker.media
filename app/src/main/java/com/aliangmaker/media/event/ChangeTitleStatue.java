package com.aliangmaker.media.event;

import android.content.Context;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ChangeTitleStatue {
    private LinearLayout linearLayout;
    private Context context;
    private boolean value;
    private boolean isGone;
    public ChangeTitleStatue(Context context,LinearLayout linearLayout) {
        EventBus.getDefault().register(this);
        this.linearLayout = linearLayout;
        this.context = context;
    }
    public ChangeTitleStatue(Boolean value) {
        this.value = value;
    }
    public boolean getValue(){return this.value;}
    public void remove() {
        EventBus.getDefault().unregister(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMyEvent(ChangeTitleStatue changeTitleStatue) {
        TranslateAnimation translateAnim;
        if (changeTitleStatue.getValue() && !isGone) {
            isGone = true;
            translateAnim = new TranslateAnimation(0f, 0f,0f, -dpToPx(25));
            translateAnim.setDuration(600);
            translateAnim.setFillAfter(true);
            linearLayout.startAnimation(translateAnim);
        } else if (isGone){
            isGone = false;
            translateAnim = new TranslateAnimation(0f, 0f, -dpToPx(25), 0f);
            translateAnim.setDuration(600);
            translateAnim.setFillAfter(true);
            linearLayout.startAnimation(translateAnim);
        }
    }

    private int dpToPx(int dp) {
        return Math.round((float) 40 * context.getResources().getDisplayMetrics().density);
    }
}
