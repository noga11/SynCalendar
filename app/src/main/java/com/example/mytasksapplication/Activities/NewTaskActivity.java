package com.example.mytasksapplication.Activities;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewTaskActivity extends AppCompatActivity {

    private Model model;
    private EditText etTitle, etDetails, auetShare;
    private TextView tvStartTime, tvEndTime, tvDate, tvReminderDate, tvReminderTime;
    private Switch swchReminder;
    private ChipGroup chipGroup;
    private String[] otherUsers = {"user1", "user2", "user3", "user4"};// need to connect to the model (temporary)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        model = Model.getInstance(this);

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

        tvReminderDate.setVisibility(View.GONE);
        tvReminderTime.setVisibility(View.GONE);

        tvDate.setOnClickListener(view -> showDatePicker(1));
        tvReminderDate.setOnClickListener(view -> showDatePicker(2));
        tvStartTime.setOnClickListener(view -> showTimePicker(1));
        tvEndTime.setOnClickListener(view -> showTimePicker(2));
        tvReminderTime.setOnClickListener(view -> showTimePicker(3));

        swchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                        swchReminder.setChecked(false); // Turn switch off until permission is granted
                        return;
                    }
                }
                tvReminderDate.setVisibility(View.VISIBLE);
                tvReminderTime.setVisibility(View.VISIBLE);
            } else {
                tvReminderDate.setVisibility(View.GONE);
                tvReminderTime.setVisibility(View.GONE);
            }
        });


        auetShare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
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

        model.SetChipIconPictureFromUsername(username, chip);
        chip.setCloseIconVisible(true);
        chip.setCloseIconResource(android.R.drawable.ic_menu_delete);  // Set the 'X' icon

        // Remove the chip when the user presses 'X'
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            Toast.makeText(this, username + " removed", Toast.LENGTH_SHORT).show();
        });

        chipGroup.addView(chip);
    }

    private void showDatePicker(int option) {
        // Create an instance of MaterialDatePicker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        // Show the date picker dialog
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");

        // Handle date selection
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert the selected date from milliseconds to a readable date format
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            String selectedDate = sdf.format(new Date(selection));

            // Set the selected date in the tvDate TextView
            if (option==1)
                tvDate.setText(selectedDate);
            else if (option==2)
                tvReminderDate.setText(selectedDate);
        });
    }

    private void showTimePicker(int option) {
        // Create the MaterialTimePicker instance
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)  // Set 24-hour format, you can change to CLOCK_12H if you prefer
                .setHour(12)  // Default hour value
                .setMinute(0)  // Default minute value
                .setTitleText("Select Start Time")
                .build();

        // Show the time picker
        timePicker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");

        // Handle time selection
        timePicker.addOnPositiveButtonClickListener(selection -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Format the time as per your need
            String formattedTime = String.format("%02d:%02d", hour, minute);
            if (option==1)
                tvStartTime.setText(formattedTime);
            else if (option==2)
                tvEndTime.setText(formattedTime);
            else if (option==3)
                tvReminderTime.setText(formattedTime);
        });
    }
}
