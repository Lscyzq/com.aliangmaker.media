package com.aliangmaker.meida;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;

public class DownloadDanmakuTask extends AsyncTask<String, Void, String> {
    
    private Context context;
    private DownloadDanmakuListener listener;
    public DownloadDanmakuTask(Context context, DownloadDanmakuListener listener) {
        this.context = context;
        this.listener = listener;
    }
    String name;
    @Override
    protected String doInBackground(String... params) {
        String urlStr = params[0];
        name = params[1];
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(urlStr).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Request failed
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Sink sink;
                BufferedSink bufferedSink = null;
                try {
                    File danmakuFile = createDanmakuFile();
                    sink = Okio.sink(danmakuFile);
                    byte[] decompressBytes = decompress(response.body().bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
//                    Log.d("length", String.valueOf(decompressBytes.length));
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.write(decompressBytes);//将解压后数据写入文件（sink）中
                    listener.onDanmakuDownloaded();
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
        return null;
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
    private File createDanmakuFile() throws IOException {
        String danmakuFileName;
        File danmakuDir;
        if(name.equals("danmaku.xml")) {
            danmakuFileName = "danmaku.xml";
            // 获取外部存储的应用私有目录
            danmakuDir = context.getExternalFilesDir(null);

            // 如果目录不存在，创建它
            if (danmakuDir != null && !danmakuDir.exists()) {
                danmakuDir.mkdirs();
            }
        } else if (name.equals("凉腕播放器.apk")) {
            danmakuFileName = name;
            danmakuDir = new File("/sdcard/Download");

            // 如果目录不存在，创建它
            if (danmakuDir != null && !danmakuDir.exists()) {
                danmakuDir.mkdirs();
            }
        } else{
            int lastIndex = name.lastIndexOf('/');
            String result = name.substring(lastIndex + 1);
            if (result.contains(".")) {
                result = result.substring(0, result.lastIndexOf(".")) + ".mp4";
            } else {
                result += ".mp4";
            }
            danmakuFileName = result;
            // 获取外部存储的应用私有目录
            danmakuDir = new File("/sdcard/Movies");

            // 如果目录不存在，创建它
            if (danmakuDir != null && !danmakuDir.exists()) {
                danmakuDir.mkdirs();
            }
        }
        return new File(danmakuDir, danmakuFileName);
    }
    public interface DownloadDanmakuListener {
        void onDanmakuDownloaded();
    }

    @Override
    protected void onPostExecute(String filePath) {
    }
}


