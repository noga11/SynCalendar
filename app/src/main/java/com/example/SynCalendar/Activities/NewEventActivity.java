package com.example.SynCalendar.Activities;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.SynCalendar.Event;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.Notification.NotificationMsg;
import com.example.SynCalendar.R;

import com.example.SynCalendar.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class NewEventActivity extends AppCompatActivity implements View.OnClickListener {

    private Model model;
    private User currentUser;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private NotificationMsg notificationMsg;
    private String[] otherUsers = {"user1", "user2", "user3", "user4"};// need to connect to the model (temporary)
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<String> groups;

    private EditText etTitle, etDetails, auetShare;
    private TextView tvStartTime, tvEndTime, tvDate, tvReminderDate, tvReminderTime;
    private Button btbAddEvent;
    private Switch swchReminder;
    private ChipGroup chipGroup;
    private AutoCompleteTextView spinnerGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        // need to create edit screen

        model = Model.getInstance(this);
        notificationMsg = new NotificationMsg(this);
        currentUser = model.getCurrentUser();

        etTitle = findViewById(R.id.etTitle);
        etDetails = findViewById(R.id.etDetails);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvDate = findViewById(R.id.tvDate);
        tvReminderDate = findViewById(R.id.tvReminderDate);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        auetShare = findViewById(R.id.auetShare);
        chipGroup = findViewById(R.id.cgUsers);
        swchReminder = findViewById(R.id.swchReminder);
        btbAddEvent = findViewById(R.id.btbAddEvent);
        spinnerGroup = findViewById(R.id.spinnerGroup);

        groups = model.getGroups();

        // Ensure 'Add New Group' option is present
        if (!groups.contains("Add New Group")) {
            groups.add("Add New Group");
        }

        // Setup the adapter
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups);
        spinnerGroup.setAdapter(spinnerAdapter);

        tvReminderDate.setVisibility(View.GONE);
        tvReminderTime.setVisibility(View.GONE);

        btbAddEvent.setOnClickListener(this);

        tvDate.setOnClickListener(view -> showDatePicker(1));
        tvReminderDate.setOnClickListener(view -> showDatePicker(2));
        tvStartTime.setOnClickListener(view -> showTimePicker(1));
        tvEndTime.setOnClickListener(view -> showTimePicker(2));
        tvReminderTime.setOnClickListener(view -> showTimePicker(3));

        spinnerGroup.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGroup = groups.get(position);

            if ("Add New Group".equals(selectedGroup)) {
                showAddGroupDialog();
                spinnerGroup.dismissDropDown();
            }
        });

        swchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                    swchReminder.setChecked(false);
                    return;
                }
                tvReminderDate.setVisibility(View.VISIBLE);
                tvReminderTime.setVisibility(View.VISIBLE);
            } else {
                tvReminderDate.setVisibility(View.GONE);
                tvReminderTime.setVisibility(View.GONE);
                notificationMsg.cancelNotification();
                Toast.makeText(this, "Reminder canceled", Toast.LENGTH_SHORT).show();
            }
        });

        auetShare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // You can add functionality to filter usernames or show suggestions based on what the user types
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String typedUsername = editable.toString().trim();
                if (!typedUsername.isEmpty() && isValidUsername(typedUsername)) {
                    addUserChip(typedUsername);
                    auetShare.setText("");  // Clear the input field
                }
            }
        });

        // Initialize current date and time
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);

        // Set start time and date to current time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String currentDate = dateFormat.format(calendar.getTime());
        tvDate.setText(currentDate);

        String currentTime = String.format("%02d:%02d", selectedHour, selectedMinute);
        tvStartTime.setText(currentTime);

        // Set end time to be one hour after start time
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String endTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        tvEndTime.setText(endTime);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btbAddEvent) {
            String eventTitle = etTitle.getText().toString().trim();
            if (eventTitle.isEmpty()) {
                Toast.makeText(this, "Event title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (swchReminder.isChecked()) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                calendar.set(Calendar.SECOND, 0);

                long reminderTimeMillis = calendar.getTimeInMillis();
                if (reminderTimeMillis > System.currentTimeMillis()) {
                    notificationMsg.sendNotification("New Event: " + eventTitle);
                    Toast.makeText(this, "Reminder set", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Reminder time must be in the future", Toast.LENGTH_SHORT).show();
                }
            }

            // --- ADDED: Create and save the event ---
            ArrayList<String> usersId = new ArrayList<>();
            usersId.add(currentUser.getId());

            String address = "";
            EditText etAddress = findViewById(R.id.etAdress);
            if (etAddress != null) address = etAddress.getText().toString().trim();

            String group = spinnerGroup.getText().toString().trim();
            Date startDate = new Date();
            try {
                String dateStr = tvDate.getText().toString().trim() + " " + tvStartTime.getText().toString().trim();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                startDate = sdf.parse(dateStr);
            } catch (Exception e) { /* fallback to now */ }

            int duration = 60; // default 1 hour
            try {
                String startStr = tvStartTime.getText().toString().trim();
                String endStr = tvEndTime.getText().toString().trim();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Date start = sdf.parse(startStr);
                Date end = sdf.parse(endStr);
                if (start != null && end != null) {
                    long diff = end.getTime() - start.getTime();
                    duration = (int) (diff / (60 * 1000));
                    if (duration <= 0) duration = 60;
                }
            } catch (Exception e) { /* fallback to 60 */ }

            Event event = new Event(
                eventTitle,
                etDetails.getText().toString().trim(),
                address,
                null, // id, will be set by Firestore
                group,
                usersId,
                null, // repeat, set as needed
                null, // status, set as needed
                startDate,
                null, // remTime, set as needed
                swchReminder.isChecked(),
                false, // important, set as needed
                0, // colour, set as needed
                0, // notificationId, set as needed
                duration
            );
            model.createEvent(event);
            // --- END ADDED ---

            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
            finish(); // Return to previous screen
        }
    }

    private boolean isValidUsername(String username) {
        for (String user : otherUsers) {
            if (user.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    private void addUserChip(String username) {
        Chip chip = new Chip(this);
        chip.setText(username);

        String userId = null;
        for (Map.Entry<String, String> entry : currentUser.getMutuals().entrySet()) {
            if (entry.getValue().equals(username)) {
                userId = entry.getKey();
                break;
            }
        }

        if (userId != null) {
            model.getUserById(userId,
                    user -> {
                        if (user != null) {
                            Bitmap profileBitmap = user.getProfilePic();
                            if (profileBitmap != null) {
                                Drawable drawable = new BitmapDrawable(getResources(), profileBitmap);
                                chip.setChipIcon(drawable);
                                chip.setChipIconSize(48f); // Optional: adjust as needed
                            }

                            chip.setCloseIconVisible(true);
                            chip.setCloseIconResource(R.drawable.baseline_close_24);

                            chip.setOnCloseIconClickListener(v -> {
                                chipGroup.removeView(chip);
                                Toast.makeText(this, username + " removed", Toast.LENGTH_SHORT).show();
                            });

                            chipGroup.addView(chip);
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    },
                    e -> {
                        Log.e("NewEventActivity", "Error getting user data", e);
                        Toast.makeText(this, "Error getting user data", Toast.LENGTH_SHORT).show();
                    }
            );
        } else {
            Toast.makeText(this, "User not found in your network", Toast.LENGTH_SHORT).show();
        }
    }


    private void showDatePicker(int option) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            selectedYear = calendar.get(Calendar.YEAR);
            selectedMonth = calendar.get(Calendar.MONTH);
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String selectedDate = sdf.format(calendar.getTime());

            if (option == 1) {
                tvDate.setText(selectedDate);
            } else if (option == 2) {
                tvReminderDate.setText(selectedDate);
            }
        });
    }

    private void showTimePicker(int option) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .build();

        timePicker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");

        timePicker.addOnPositiveButtonClickListener(selection -> {
            selectedHour = timePicker.getHour();
            selectedMinute = timePicker.getMinute();

            String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
            if (option == 1) {
                tvStartTime.setText(formattedTime);
            } else if (option == 2) {
                tvEndTime.setText(formattedTime);
            } else if (option == 3) {
                tvReminderTime.setText(formattedTime);
            }
        });
    }

    private void showAddGroupDialog() {
        // Create an input field inside the dialog
        EditText input = new EditText(this);
        input.setHint("Enter new group name");

        // Create and show the dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("New Group")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newGroup = input.getText().toString().trim();

                    if (!newGroup.isEmpty() && !groups.contains(newGroup)) {
                        addNewGroup(newGroup);
                    } else {
                        Toast.makeText(this, "Group already exists or invalid name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addNewGroup(String newGroup) {
        model.addGroup(newGroup);
        groups = model.getGroups();
        spinnerAdapter.clear();
        spinnerAdapter.addAll(groups);
        spinnerAdapter.notifyDataSetChanged();
        spinnerGroup.setText(newGroup, false);
    }

}