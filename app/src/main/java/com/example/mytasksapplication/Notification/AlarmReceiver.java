package com.example.mytasksapplication.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context,"IN THE RECEIVER!",Toast.LENGTH_LONG).show();
        NotificationMsg notificationMsg= new NotificationMsg(context);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("events");
        databaseReference.get().addOnCompleteListener(event -> {
            if (event.isSuccessful() && event.getResult().exists()) {
                String events = event.getResult().child("events").getValue(String.class);
                notificationMsg.sendNotification("Event Reminder: " + events);
            }
        });
    }
}