package com.example.mytasksapplication.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Boot Receiver", Toast.LENGTH_SHORT).show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                int hour = task.getResult().child("hour").getValue(Integer.class);
                int minutes = task.getResult().child("minutes").getValue(Integer.class);
                Toast.makeText(context, "Setting alarm to " + hour + ":" + minutes, Toast.LENGTH_LONG).show();
                Reminder.setAlarm(context, hour, minutes);
            }
        });
    }
}
