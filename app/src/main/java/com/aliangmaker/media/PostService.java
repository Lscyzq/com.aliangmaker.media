package com.aliangmaker.media;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("aaaaaaa", decrypt("http://aliangmaker.top/com.media/detail.json"));
        SharedPreferences sharedPreferences = getSharedPreferences("main", Context.MODE_PRIVATE);
        int count = sharedPreferences.getInt("count", 0);
        if (count >= 4) {
            String url = sharedPreferences.getString("server", decrypt("kwws=22doldqjpdnhu1wrs2")) + decrypt("frp1phgld2ghyhorshu");
            OkHttpClient okHttpClient = new OkHttpClient();
            String deviceName = "$" + Build.MODEL + "%" + count;
            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"),deviceName);
            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(url)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        sharedPreferences.edit().putInt("count", 1).apply();
                    }
                    response.close();
                }
            });
        } else sharedPreferences.edit().putInt("count", count+1).apply();
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // 加密方法
    private static String encrypt(String plainText) {
        StringBuilder encryptedText = new StringBuilder();
        int offset = 3; // 加密偏移量，可以根据需要更改
        for (char c : plainText.toCharArray()) {
            int encryptedChar = (c + offset) % 256; // 加密字符
            encryptedText.append((char) encryptedChar);
        }
        return encryptedText.toString();
    }

    private static String decrypt(String encryptedText) {
        StringBuilder decryptedText = new StringBuilder();
        int offset = 3;
        for (char c : encryptedText.toCharArray()) {
            int decryptedChar = (c - offset + 256) % 256;
            decryptedText.append((char) decryptedChar);
        }
        return decryptedText.toString();
    }
}