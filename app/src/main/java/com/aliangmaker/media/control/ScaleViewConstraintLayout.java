package com.aliangmaker.media.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ScaleViewConstraintLayout extends ConstraintLayout {
    public ScaleViewConstraintLayout(@NonNull Context context) {
        super(context);
    }

    public ScaleViewConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleViewConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.animate().scaleX(0.93f).scaleY(0.93f).setDuration(150).start();
                return super.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                return super.onTouchEvent(event);
            default:
                return super.onTouchEvent(event);
        }
    }
}
