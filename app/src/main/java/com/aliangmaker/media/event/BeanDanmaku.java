package com.aliangmaker.media.event;

public class BeanDanmaku {
    int time,type,color;
    float size;
    String value;
    public BeanDanmaku(int time, int type, float size, int color) {
        this.time = time;
        this.type = type;
        this.size = size;
        this.color = color;
    }

    public void addString(String value) {
        this.value = value;
    }

    public int getTime() {
        return time;
    }

    public String getValue() {
        return value;
    }
    public int getColor() {
        return color;
    }
    public float getSize() {
        return size;
    }
    public int getType() {
        return type;
    }
}
