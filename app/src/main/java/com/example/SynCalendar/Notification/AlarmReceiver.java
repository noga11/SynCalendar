package com.example.SynCalendar.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationMsg notificationMsg = new NotificationMsg(context);
        String eventTitle = intent.getStringExtra("eventTitle");
        if (eventTitle != null) {
            notificationMsg.sendNotification("Event Reminder: " + eventTitle);
        }
    }
}