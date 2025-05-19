package com.example.SynCalendar.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.SynCalendar.Event;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed received");
            
            // Get the current user from Model
            Model model = Model.getInstance(context);
            User currentUser = model.getCurrentUser();
            
            // Check if user is logged in
            if (currentUser == null) {
                Log.d(TAG, "No user logged in, skipping alarm restoration");
                return;
            }

            String currentUserId = currentUser.getId();
            if (currentUserId == null || currentUserId.isEmpty()) {
                Log.e(TAG, "Current user ID is null or empty");
                return;
            }

            Log.d(TAG, "Restoring alarms for user: " + currentUserId);

            // Query Firestore for all events with reminders
            FirebaseFirestore.getInstance()
                .collection("events")
                .whereArrayContains("usersId", currentUserId)
                .whereEqualTo("hasReminder", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int restoredCount = 0;
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null && event.getRemTime() != null) {
                            // Check if the reminder time is in the future
                            if (event.getRemTime().after(new Date())) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(event.getRemTime());
                                
                                // Set the alarm
                                Reminder.setAlarm(
                                    context,
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    event.getTitle()
                                );
                                
                                restoredCount++;
                                Log.d(TAG, "Restored alarm for event: " + event.getTitle());
                            }
                        }
                    }
                    Log.d(TAG, "Successfully restored " + restoredCount + " alarms");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error restoring alarms", e);
                    Toast.makeText(context, "Error restoring alarms", Toast.LENGTH_SHORT).show();
                });
        }
    }
}
