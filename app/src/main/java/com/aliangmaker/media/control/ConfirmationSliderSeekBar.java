package com.aliangmaker.media.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ConfirmationSliderSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {


    public ConfirmationSliderSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMax(156);
        setProgress(20);
    }

    public ConfirmationSliderSeekBar(@NonNull Context context) {
        super(context);
        setMax(156);
        setProgress(20);
    }

    public ConfirmationSliderSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMax(156);
        setProgress(20);
    }
    float oldPro;
    @SuppressLint("ClickableViewAccessibility")
    public void setOnConfirmationSliderSeekBarChangeListener(onCheckedListener listener) {
        setOnTouchListener((view, motionEvent) -> {
            if (!isEnabled()) {
                return false;
            }
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    oldPro = motionEvent.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    if (getProgress() >= 136) {
                        listener.onChecked();
                    } else
                        setProgress(20);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int curProgress = getProgress();
                    if (curProgress <= 20 && motionEvent.getX() - oldPro <= 0) {
                        setProgress(20);
                    } else if (curProgress >= 136) {
                        setProgress(136);
                    } else
                        setProgress((int) (motionEvent.getX() * getMax() / getWidth()));
                    break;
            }
            return true;
        });
    }

    public interface onCheckedListener {
        void onChecked();
    }
}