package com.example.xlx.coolweather.util;

/**
 * Created by Administrator on 2016/6/3.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
