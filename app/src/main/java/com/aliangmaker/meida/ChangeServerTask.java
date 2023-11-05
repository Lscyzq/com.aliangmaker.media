package com.aliangmaker.meida;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChangeServerTask extends AsyncTask<Void, Void, String> {
    private Context mContext;

    public ChangeServerTask(Context context) {
        mContext = context;
    }
    Boolean isOK = true;
    @Override
    protected String doInBackground(Void... voids) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("https://lscyzq.github.io/com.aliangmaker.media/server.json");
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
                String server = jsonObject.getString("server");
                SharedPreferences sharedPreferences = mContext.getSharedPreferences("stage",Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("server",server).apply();
            }else isOK = false;
        } catch (IOException | JSONException e) {
            isOK = false;
            e.printStackTrace();
            // 设置请求异常的标志
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String a) {
        if (!(mContext instanceof MainActivity) && isOK) {
            Toast.makeText(mContext, "服务器地址已更新，如若再次失败，请等待几分钟再试", Toast.LENGTH_SHORT).show();
        }else if (!(mContext instanceof MainActivity) && !isOK) {
            Toast.makeText(mContext, "服务器地址更新失败，请进群反馈", Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(a);
    }
}