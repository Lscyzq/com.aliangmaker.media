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

}