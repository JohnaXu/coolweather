package com.example.xlx.coolweather.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.xlx.coolweather.service.AutoUpdateService;

/**
 * Created by Administrator on 2016/6/5.
 */
public class AutoUpdateReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
