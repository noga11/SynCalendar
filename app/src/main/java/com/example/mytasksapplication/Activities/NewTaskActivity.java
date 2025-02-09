package com.example.mytasksapplication.Activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;

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
    private EditText etTitle, etDetails;
    private TextView tvStartTime, tvEndTime, tvDate, tvReminderDate, tvReminderTime;

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

        tvDate.setOnClickListener(view -> showDatePicker(1));
        tvReminderDate.setOnClickListener(view -> showDatePicker(2));
        tvStartTime.setOnClickListener(view -> showTimePicker(1));
        tvEndTime.setOnClickListener(view -> showTimePicker(2));
        tvReminderTime.setOnClickListener(view -> showTimePicker(3));
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
