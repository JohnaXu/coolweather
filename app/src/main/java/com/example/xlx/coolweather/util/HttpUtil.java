package com.example.xlx.coolweather.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
//import java.nio.Buffer;

//import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2016/6/3.
 */
public class HttpUtil {

    private static final String TAG = "HttpUtil";
    public static void sendHttpRequest(final String address,
                                       final HttpCallbackListener listener){
        Log.d(TAG, "address: " + address);
        new Thread(new Runnable(){
            @Override
        public void run(){
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Connection", "close");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Accept-Encoding" , "");
//                    Log.d(TAG, "connection.getInputStream(): " );
                    int code = connection.getResponseCode();
                    Log.d(TAG, code+"");
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line ;
                    Log.d(TAG, "the error is here:");
                    while((line = reader.readLine()) != null){
                        Log.d(TAG, line);
                        response.append(line);
                    }
                    Log.d(TAG, "connection.response: " + response );
                    if(listener != null){
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e ){
                    Log.d(TAG,"Connection Error is: " + e);
                    e.printStackTrace();
                    if (listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if(connection != null){
                        connection.disconnect();

                    }
                }
            }
        }).start();

    }
}
