package com.csr.masterapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.csr.masterapp.receiver.AlarmServiceBroadcastReciever;

/**
 * 定时执行场景
 */

public class SchedulingService extends IntentService {

    public SchedulingService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        final int id = intent.getExtras().getInt("id");
        Log.d("场景触发了", "onHandleIntent:-------------------------- id：" + id);

        AlarmServiceBroadcastReciever.completeWakefulIntent(intent);
    }

}
