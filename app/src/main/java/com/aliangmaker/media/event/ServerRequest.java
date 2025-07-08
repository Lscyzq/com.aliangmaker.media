package com.aliangmaker.media.event;

import android.app.Activity;
import android.widget.Toast;

import com.aliangmaker.media.MainActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerRequest {
    private Activity context;
    public ServerRequest(Activity context) {
        this.context = context;
    }
    OkHttpClient okHttpClient = new OkHttpClient();
    String urlUpdate = decrypt("p||xB77itqivouismz6|wx7kwu6umlqi7}xli|m6r{wv");
    String urlUrl = decrypt("p||xB77itqivouismz6|wx7kwu6umlqi7lm|iqt6r{wv");
    public void getVersion(versionCallBack serverCallBack) {
        okHttpClient.newCall(new Request.Builder()
                .url(urlUpdate)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!(context instanceof MainActivity)) {
                    context.runOnUiThread(() -> Toast.makeText(context, "请检查网络连接", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        serverCallBack.getVersionSuccess(jsonObject.getString("lasted_version"), jsonObject.getString("happy_version"), jsonObject.getString("notice_version"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else serverCallBack.getVersionFail();
                response.close();
            }
        });
    }

    public void getUrl (urlCallBack serverCallBack) {
        okHttpClient.newCall(new Request.Builder()
                .url(urlUrl)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> Toast.makeText(context, "请检查网络连接", Toast.LENGTH_SHORT).show());
                serverCallBack.getUrlFail();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        serverCallBack.getUrlSuccess(jsonObject.getString("up_log"), jsonObject.getString("notice_detail"), jsonObject.getString("happy_name"), jsonObject.getString("happy_url"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else serverCallBack.getUrlFail();
                response.close();
            }
        });
    }
    private static String decrypt(String encryptedText) {
        StringBuilder decryptedText = new StringBuilder();
        int offset = 8;
        for (char c : encryptedText.toCharArray()) {
            int decryptedChar = (c - offset + 256) % 256;
            decryptedText.append((char) decryptedChar);
        }
        return decryptedText.toString();
    }
    public interface versionCallBack {
        void getVersionSuccess(String lastedVersion, String happyVersion, String noticeVersion);

        void getVersionFail();
    }

    public interface urlCallBack {
        void getUrlSuccess(String upLog, String noticeDetail, String happyName, String happyUrl);

        void getUrlFail();
    }
}