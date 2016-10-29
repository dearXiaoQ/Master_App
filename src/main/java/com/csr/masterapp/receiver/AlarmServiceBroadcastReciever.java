package com.csr.masterapp.receiver;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.csr.masterapp.entities.Alarm;
import com.csr.masterapp.service.SchedulingService;

import java.util.Calendar;

/**
 *
 */
public class AlarmServiceBroadcastReciever extends WakefulBroadcastReceiver {

    Alarm alarm;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());

        int id = intent.getIntExtra("id",-1);
        try {
            Intent service = new Intent(context, SchedulingService.class);
            service.putExtra("id", id);
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, service);
            setResultCode(Activity.RESULT_OK);
        } catch (Exception e) {
            Log.wtf("WTF", e);
        }
    }


    public void setAlarm(Context context, Alarm alarm) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        Intent intent = new Intent(context, AlarmServiceBroadcastReciever.class);
        intent.setAction("zeusro.action.alert");
        intent.putExtra("alarm", alarm);
        intent.putExtra("id",alarm.getId());
        Log.d("创建场景", "onHandleIntent:-------------------------- id：" + alarm.getId());
        alarmIntent = PendingIntent.getBroadcast(context, alarm.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = alarm.getAlarmTime();
        Calendar now = (Calendar) calendar.clone();
        now.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        now.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
        now.set(Calendar.SECOND, calendar.getInstance().get(Calendar.SECOND));
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (now.getTimeInMillis() > calendar.getTimeInMillis()) {
            CancelAlarm(context,alarm.getId());
            Log.d("场景添加出错", "setAlarm: ");
            return;
        }
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        //可用状态
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    /**
     * Cancels the alarm.
     *
     * @param context
     */
    public void CancelAlarm(Context context, int id) {
        if (alarmIntent == null) {
            Intent intent = new Intent(context, AlarmServiceBroadcastReciever.class);
            alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            Log.d("场景取消了", "CancelAlarm: " + id + "-------------");
            alarmMgr.cancel(alarmIntent);
        }
        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
