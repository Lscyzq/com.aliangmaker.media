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
    boolean canControl;
    @SuppressLint("ClickableViewAccessibility")
    public void setOnConfirmationSliderSeekBarChangeListener(onCheckedListener listener) {
        setOnTouchListener((view, motionEvent) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (!isEnabled()) {
                return false;
            }
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (getProgressForX(motionEvent.getX()) < 40) canControl = true;
                    else canControl = false;
                    break;
                case MotionEvent.ACTION_UP:
                    if (getProgress() >= 136) {
                        listener.onChecked();
                    } else
                        setProgress(20);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int curProgress = getProgress();
                    if (curProgress >= 20 && curProgress < 136 && canControl) {
                        setProgress(adjust((int) (motionEvent.getX() * getMax() / getWidth())));
                    }
                    break;
            }
            return true;
        });
    }

    private int getProgressForX(float x) {
        return (int) (x * getMax() / getWidth());
    }

    private int adjust(int newPro) {
        if (newPro <= 20) return 20;
        else if (newPro >= 136) return 136;
        return newPro;
    }
    public interface onCheckedListener {
        void onChecked();
    }
}