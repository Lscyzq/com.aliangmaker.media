package com.aliangmaker.media.event;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadActivity {
    Context context;
    public DownloadActivity(String path, Context context) {
        this.context = context;
        Request request = new Request.Builder()
                .url(path) // 替换为你要下载的文件的URL
                .build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 请求失败的处理
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 请求成功的处理
                Sink sink;
                BufferedSink bufferedSink = null;
                try {
                    File danmakuFile = context.getExternalFilesDir("danmaku");
                    sink = Okio.sink(danmakuFile);
                    byte[] decompressBytes = decompress(response.body().bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.write(decompressBytes);

                    bufferedSink.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }
                }
            }
        });
    }


    public static byte[] decompress(byte[] data) {
        byte[] output;
        Inflater decompresser = new Inflater(true);//这个true是关键
        decompresser.reset();
        decompresser.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[2048];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }

}
