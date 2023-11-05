package com.aliangmaker.meida;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateDetail extends AsyncTask<Void, Void, UpdateDetail.UpdateResult> {
    private WeakReference<Context> contextRef;
    private DetailListener listener;
    public UpdateDetail(Context context, DetailListener listener) {
        this.contextRef = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected UpdateResult doInBackground(Void... voids) {
        UpdateResult updateResult = new UpdateResult();
        HttpURLConnection urlConnection = null;
        try {
            SharedPreferences sharedPreferences = contextRef.get().getSharedPreferences("stage",Context.MODE_PRIVATE);
            URL url = new URL(sharedPreferences.getString("server","http://aliangmaker.top/")+"com.media/detail.json");
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
                String upLog = jsonObject.getString("up_log");
                String happyUrl = jsonObject.getString("happy_url");
                String happyName = jsonObject.getString("happy_name");
                String noticeDetail = jsonObject.getString("notice_detail");
                updateResult.setSuccess(true);
                updateResult.setLatestVersion(upLog,happyUrl,happyName,noticeDetail);
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
            upLog = updateResult.getUpLog();
            happyUrl = updateResult.getHappyUrl();
            happyName = updateResult.getHappyName();
            noticeDetail = updateResult.getNoticeDetail();
            // 在这里主动调用listener的回调方法，将获取的最新版本号传递给MainActivity
        }else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnected();
            if(!isConnected)upLog = "netWorkOut";
            else upLog = "serverOut";
        }
        listener.onDetailCheckCompleted(upLog,happyUrl,happyName,noticeDetail);
    }
    public interface DetailListener {
        void onDetailCheckCompleted(String upLog,String happyUrl,String happyName,String noticeDetail);
    }
   String upLog,happyUrl,happyName,noticeDetail;
    public static class UpdateResult {
        private boolean success;
        private String upLog,happyUrl,happyName,noticeDetail;
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getHappyUrl() {
            return happyUrl;
        }

        public String getHappyName() {
            return happyName;
        }
        public String getNoticeDetail(){return noticeDetail;}
        public String getUpLog() {
            return upLog;
        }
        public void setLatestVersion(String upLog,String happyUrl,String happyName,String noticeDetail) {
            this.upLog = upLog;
            this.happyUrl = happyUrl;
            this.happyName = happyName;
            this.noticeDetail = noticeDetail;
        }
    }
}