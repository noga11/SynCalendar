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

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("medicines");
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String tasks = task.getResult().child("tasks").getValue(String.class);
                notificationMsg.sendNotification("Event Reminder: " + tasks);
            }
        });
    }
}