package com.aliangmaker.media.control;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ScaleVideoConstraintLayout extends ConstraintLayout {
    public ScaleVideoConstraintLayout(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public ScaleVideoConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ScaleVideoConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    private FrameLayout frameLayout;
    private int Ordinary = 0, TowFingerZoom = 2, doubleClickZoom = 3, scaleZoom = 8, zoomInMode = Ordinary;
    private PointF firstPoint = new PointF(), secondPoint = new PointF();
    private long doubleClickTimeSpan = 320, lastClickTime = 0;
    boolean canScale = false, tapScale = false, noBorder = false;
    private SharedPreferences sharedPreferences;
    private Context context;
    int[] viewLocation = new int[2];

    public void initScale(FrameLayout frameLayout) {
        this.frameLayout = frameLayout;
        this.sharedPreferences = context.getSharedPreferences("play_set", Context.MODE_PRIVATE);
        tapScale = sharedPreferences.getBoolean("tap_scale", false);
        noBorder = sharedPreferences.getBoolean("no_border", false);
        canScale = true;
    }

    public void setScale(boolean canScale) {
        this.canScale = canScale;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canScale) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                firstPoint.set(event.getX(), event.getY());
                zoomInMode = Ordinary;
                if (tapScale && System.currentTimeMillis() - lastClickTime <= doubleClickTimeSpan) {
                    //双击缩放
                    if (frameLayout.getScaleX() == 1) {
                        frameLayout.setPivotX(firstPoint.x * frameLayout.getWidth() / getWidth());
                        frameLayout.setPivotY(firstPoint.y * frameLayout.getHeight() / getHeight());
                        frameLayout.animate().scaleX(doubleClickZoom).scaleY(doubleClickZoom).setDuration(100).start();
                    } else {
                        //放大状态，再次双击缩小
                        frameLayout.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    }
                    return true;
                } else if (tapScale) lastClickTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                secondPoint.set(event.getX(1), event.getY(1));
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    zoomInMode = TowFingerZoom;
                    if (!tapScale) {
                        frameLayout.getLocationInWindow(viewLocation);
                        float scale = (float) (frameLayout.getScaleX() + (float) 0.0045 * (-(Math.sqrt(Math.pow((firstPoint.x - secondPoint.x), 2) + Math.pow(firstPoint.y - secondPoint.y, 2))) + (Math.sqrt(Math.pow(event.getX() - event.getX(1), 2) + Math.pow(event.getY() - event.getY(1), 2)))));
                        if (scale >= 1 && scale < scaleZoom) {
                            frameLayout.setScaleX(scale);
                            frameLayout.setScaleY(scale);
                        }
                        secondPoint.set(event.getX(1), event.getY(1));
                    }
                } else if (zoomInMode != TowFingerZoom) {
                    frameLayout.getLocationInWindow(viewLocation);
                    frameLayout.setTranslationX(adjustX(event.getX() - firstPoint.x, viewLocation[0]));
                    frameLayout.setTranslationY(adjustY(event.getY() - firstPoint.y, viewLocation[1]));
                }
                firstPoint.set(event.getX(0), event.getY(0));
                return true;
        }
        return super.onTouchEvent(event);
    }

    private float adjustY(float y, int viewLocationY) {
        boolean a = viewLocationY >= 0, b = viewLocationY <= getHeight() - frameLayout.getHeight() * frameLayout.getScaleY();
        if (!noBorder && !(a && b)) {
            if (a) {
                if (y > 0) y = 0;
            } else if (b) {
                if (y < 0) y = 0;
            }
        }
        return frameLayout.getTranslationY() + y*1.35f;
    }

    private float adjustX(float x, int viewLocationX) {
        boolean a = viewLocationX >= 0, b = viewLocationX <= getWidth() - frameLayout.getWidth() * frameLayout.getScaleX();
        if (!noBorder && !(a && b)) {
            if (a) {
                if (x > 0) x = 0;
            } else if (b) {
                if (x < 0) x = 0;
            }
        }
        return frameLayout.getTranslationX() + x*1.35f;
    }
}
