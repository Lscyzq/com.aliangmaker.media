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
    private PointF firstPoint = new PointF(), secondPoint = new PointF(), initFirstPoint = new PointF(), initSecondPoint = new PointF();
    private long  lastClickTime = 0;
    boolean canTrans = true, canScale = false, tapScale = false, noBorder = false, canTap = false, tapED = false;
    private SharedPreferences sharedPreferences;
    private Context context;
    int[] viewLocation = new int[2];

    public void initScale(FrameLayout frameLayout, float scale) {
        this.frameLayout = frameLayout;
        this.sharedPreferences = context.getSharedPreferences("play_set", Context.MODE_PRIVATE);
        tapScale = sharedPreferences.getBoolean("tap_scale", false);
        noBorder = sharedPreferences.getBoolean("no_border", false);
        frameLayout.setScaleY(scale);
        frameLayout.setScaleX(scale);
        canScale = true;
    }

    public void canScale(boolean canScale) {
        this.canScale = canScale;
    }
    public void canTrans(boolean canTrans) {this.canTrans = canTrans;}

    public float getScale() {
        return frameLayout.getScaleX();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canScale) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                initFirstPoint.set(event.getX(), event.getY());
                firstPoint.set(event.getX(), event.getY());
                zoomInMode = Ordinary;
                if (tapScale && System.currentTimeMillis() - lastClickTime <= 320) {
                    canTap = true;
                } else if (tapScale) lastClickTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                initSecondPoint.set(event.getX(1),event.getY(1));
                secondPoint.set(event.getX(1), event.getY(1));
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    zoomInMode = TowFingerZoom;
                    if (!tapScale) {
                        frameLayout.getLocationInWindow(viewLocation);
                        float scale = (float) (frameLayout.getScaleX() + (float) 0.0055 * (-distance(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y) + distance(event.getX(), event.getY(), event.getX(1), event.getY(1))));
                        if (!canTrans) { //二级屏锁
                            if (scale >= 0.7 && scale < scaleZoom) {
                                frameLayout.setScaleX(scale);
                                frameLayout.setScaleY(scale);
                            }
                            frameLayout.getLocationInWindow(viewLocation);
                            frameLayout.setTranslationX(adjustX((event.getX() - firstPoint.x + event.getX(1) - secondPoint.x) / 2, viewLocation[0]));
                            frameLayout.setTranslationY(adjustY((event.getY() - firstPoint.y + event.getY(1) - secondPoint.y) / 2, viewLocation[1]));
                        } else if (scale >= 0.7 && scale < scaleZoom) {
                            frameLayout.setScaleX(scale);
                            frameLayout.setScaleY(scale);
                        }
                        secondPoint.set(event.getX(1), event.getY(1));
                    }
                } else if (canTap) {
                    float scale = event.getY() - firstPoint.y;
                    if (!tapED && Math.abs(scale) > 1) tapED = true;
                    scale = frameLayout.getScaleX() - (float) 0.009 * scale;
                    if (scale >= 0.7 && scale < scaleZoom) {
                        frameLayout.setScaleY(scale);
                        frameLayout.setScaleX(scale);
                    }
                } else if (canTrans && zoomInMode != TowFingerZoom) {
                    frameLayout.getLocationInWindow(viewLocation);
                    frameLayout.setTranslationX(adjustX(event.getX() - firstPoint.x, viewLocation[0]));
                    frameLayout.setTranslationY(adjustY(event.getY() - firstPoint.y, viewLocation[1]));
                }
                firstPoint.set(event.getX(0), event.getY(0));
                return true;
            case MotionEvent.ACTION_UP:
                if (canTap && !tapED) {
                    //双击缩放
                    if (frameLayout.getScaleX() == 1) {
                        frameLayout.setPivotX(firstPoint.x * frameLayout.getWidth() / getWidth());
                        frameLayout.setPivotY(firstPoint.y * frameLayout.getHeight() / getHeight());
                        frameLayout.animate().scaleX(doubleClickZoom).scaleY(doubleClickZoom).setDuration(100).start();
                    } else {
                        //放大状态，再次双击缩小
                        frameLayout.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    }
                }
                if (frameLayout.getScaleX() < 1) {
                    frameLayout.setScaleX(1);
                    frameLayout.setScaleY(1);
                    frameLayout.setTranslationX(0);
                    frameLayout.setTranslationY(0);
                }
                tapED = false;
                canTap = false;
                break;
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
        return frameLayout.getTranslationY() + y * 1.6f;
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
        return frameLayout.getTranslationX() + x * 1.6f;
    }

    private double distance(float firstX, float firstY, float secondX, float secondY) {
        return  Math.sqrt(Math.pow((firstX - secondX), 2) + Math.pow(firstY - secondY, 2));
    }
}
