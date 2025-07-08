package com.aliangmaker.media.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import static android.opengl.ETC1.getWidth;
import static java.lang.Math.abs;

public class SteppedSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    Context context;

    public SteppedSeekBar(Context context) {
        super(context);
        this.context = context;
    }

    public SteppedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SteppedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }
    float touchPro;
    float firstPro;
    float toPro;
    boolean isDrag;
    @SuppressLint("ClickableViewAccessibility")
    public void setOnSteppedSeekBarChangeListener(OnSeekBarChangeListener listener) {
        setOnTouchListener((view, motionEvent) -> {
            if (!isEnabled()) {
                return false;
            }
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDrag = false;
                    listener.onStartTrackingTouch(this);
                    touchPro = motionEvent.getX();
                    firstPro = getProgress();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float currentPro = motionEvent.getX();
                    if (abs(currentPro - touchPro) > 1) {
                        isDrag = true;
                        float destPro = (float) (getProgressForX(currentPro - touchPro) * 0.75 + firstPro);
                        toPro = destPro > 0 ? destPro : 0;
                        setProgress((int) toPro);
                        listener.onProgressChanging((int) toPro);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isDrag) {
                        toPro = getProgressForX(motionEvent.getX());
                        setProgress((int) toPro);
                    }
                    listener.onStopTrackingTouch(this, (long) toPro);
                    break;
            }
            return true;
        });
    }
    private int getProgressForX(float x) {
        return (int) (x * getMax() / getWidth());
    }

    public interface OnSeekBarChangeListener {
        void onStartTrackingTouch(SeekBar seekBar);

        void onProgressChanging(long progress);
        void onStopTrackingTouch(SeekBar seekBar, long progress);
    }
}