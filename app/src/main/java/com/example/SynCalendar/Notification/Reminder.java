package com.example.SynCalendar.Notification;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

class Reminder {

    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    public static void setAlarm(Context context, int startHour, int startMinutes) {
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMinutes);
        calendar.set(Calendar.SECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        } else {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
    }
}
