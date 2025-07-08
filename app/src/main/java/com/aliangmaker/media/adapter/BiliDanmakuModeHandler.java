package com.aliangmaker.media.adapter;

import static com.bytedance.danmaku.render.engine.utils.ConstantsKt.LAYER_TYPE_BOTTOM_CENTER;
import static com.bytedance.danmaku.render.engine.utils.ConstantsKt.LAYER_TYPE_SCROLL;
import static com.bytedance.danmaku.render.engine.utils.ConstantsKt.LAYER_TYPE_TOP_CENTER;

import android.content.Context;
import android.util.TypedValue;

import com.aliangmaker.media.event.BeanDanmaku;
import com.bytedance.danmaku.render.engine.data.DanmakuData;
import com.bytedance.danmaku.render.engine.render.draw.text.TextData;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BiliDanmakuModeHandler extends DefaultHandler {
    List<DanmakuData> danmakuData = new ArrayList<>();
    List<BeanDanmaku> beanDanmakus = new ArrayList<>();
    Integer[] danmakuTypeList;
    DanmakuHandlerCallback danmakuHandlerCallback;
    Context context;
    Float size;
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    boolean isDanmaku = false;
    BeanDanmaku beanDanmaku;
    private int spToPx(int spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        String tagName = qName.toLowerCase(Locale.getDefault()).trim();
        if (tagName.equals("d")) {
            // <d p="23.826000213623,1,25,16777215,1422201084,0,057075e9,757076900">我从未见过如此厚颜无耻之猴</d>
            // 0:时间(弹幕出现时间)
            // 1:类型(1从左至右滚动弹幕|6从右至左滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕)
            // 2:字号
            // 3:颜色
            // 4:时间戳 ?
            // 5:弹幕池id
            // 6:用户hash
            // 7:弹幕id
            isDanmaku = true;
            String[] values = attributes.getValue("p").split(",");
            if (values.length > 0) {
                beanDanmaku = new BeanDanmaku((int) (Float.parseFloat(values[0]) * 1000),
                        danmakuTypeList[Integer.parseInt(values[1])], spToPx(Integer.parseInt(values[2])) * size, Integer.parseInt(values[3]) | 0xFF000000);
                beanDanmakus.add(beanDanmaku);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (isDanmaku) beanDanmaku.addString(new String(ch,start,length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        isDanmaku = false;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        sortDanmakus();
        for (BeanDanmaku beanDanmaku : beanDanmakus) {
            TextData data = new TextData();
            data.setText(beanDanmaku.getValue());
            data.setTextColor(beanDanmaku.getColor());
            data.setTextSize(beanDanmaku.getSize());
            data.setLayerType(beanDanmaku.getType());
            data.setShowAtTime(beanDanmaku.getTime());
            danmakuData.add(data);
        }
        danmakuHandlerCallback.DanmakuHandlerPrepared(danmakuData);
    }


    private void sortDanmakus() {
        Collections.sort(beanDanmakus, (beanDanmaku, t1) -> Integer.compare(beanDanmaku.getTime(),t1.getTime()));
    }

    public BiliDanmakuModeHandler(Boolean middleDanmakuDisplayable, Context context, Float size, DanmakuHandlerCallback danmakuHandlerCallback) {
        this.danmakuHandlerCallback = danmakuHandlerCallback;
        this.context = context;
        if (middleDanmakuDisplayable) danmakuTypeList = new Integer[]{LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL
                , LAYER_TYPE_BOTTOM_CENTER, LAYER_TYPE_TOP_CENTER, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL};
        else danmakuTypeList = new Integer[]{LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL
                , LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL, LAYER_TYPE_SCROLL};
        this.size = size;
    }

    public interface DanmakuHandlerCallback {
        void DanmakuHandlerPrepared(List<DanmakuData> danmakuData);
    }
}
