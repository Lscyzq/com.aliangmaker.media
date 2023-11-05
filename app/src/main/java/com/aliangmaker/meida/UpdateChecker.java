package com.aliangmaker.meida;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker extends AsyncTask<Void, Void, UpdateChecker.UpdateResult> {
    private WeakReference<Context> contextRef;
    private UpdateListener listener;
    public UpdateChecker(Context context, UpdateListener listener) {
        this.contextRef = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected UpdateResult doInBackground(Void... voids) {
        UpdateResult updateResult = new UpdateResult();
        HttpURLConnection urlConnection = null;
        try {
            SharedPreferences sharedPreferences = contextRef.get().getSharedPreferences("stage",Context.MODE_PRIVATE);
            URL url = new URL(sharedPreferences.getString("server","http://aliangmaker.top/")+"com.media/update.json");
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 如果请求成功，继续处理响应数据
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();

                // 解析JSON获取最新版本号
                JSONObject jsonObject = new JSONObject(response.toString());
                String latestVersion = jsonObject.getString("lasted_version");
                String happyVersion = jsonObject.getString("happy_version");
                String noticeVersion = jsonObject.getString("notice_version");
                updateResult.setSuccess(true);
                updateResult.setLatestVersion(latestVersion,happyVersion,noticeVersion);
            } else {
                // 如果请求失败，设置相应的标志
                updateResult.setSuccess(false);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            // 设置请求异常的标志
            updateResult.setSuccess(false);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return updateResult;
    }

    @Override
    protected void onPostExecute(UpdateResult updateResult) {
        Context context = contextRef.get();
        if (context == null) {
            return;
        }

        if (updateResult.isSuccess()) {
            // 请求成功，继续处理更新逻辑
            latestVersion = updateResult.getLatestVersion();
            happyVersion = updateResult.getHappyVersion();
            noticeVersion = updateResult.getNoticeVersion();
            // 在这里主动调用listener的回调方法，将获取的最新版本号传递给MainActivity
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnected();
            if(!isConnected)latestVersion = "netWorkOut";
            else latestVersion = "serverOut";
        }
        listener.onUpdateCheckCompleted(latestVersion,happyVersion,noticeVersion);
    }
    public interface UpdateListener {
        void onUpdateCheckCompleted(String latestVersion,String happyVersion,String noticeVersion);
    }
    String latestVersion,happyVersion,noticeVersion;
    public static class UpdateResult {
        private boolean success;
        private String latestVersion,happyVersion,noticeVersion;
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public String getHappyVersion() {
            return happyVersion;
        }

        public String getNoticeVersion() {
            return noticeVersion;
        }
        public void setLatestVersion(String latestVersion,String happyVersion,String noticeVersion) {
            this.latestVersion = latestVersion;
            this.noticeVersion = noticeVersion;
            this.happyVersion = happyVersion;
        }
    }
}