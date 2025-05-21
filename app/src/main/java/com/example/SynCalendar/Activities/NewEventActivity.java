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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.SynCalendar.Event;
import com.example.SynCalendar.GeminiManager;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.Notification.NotificationMsg;
import com.example.SynCalendar.Notification.Reminder;
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
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NewEventActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_CODE = 100;
    private Model model;
    private User currentUser;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private NotificationMsg notificationMsg;
    private String[] otherUsers = {"user1", "user2", "user3", "user4"};// need to connect to the model (temporary)
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<String> groups;

    private EditText etTitle, etDetails, auetShare, etAddress;

    private TextView tvStartTime, tvEndTime, tvDate, tvReminderDate, tvReminderTime;
    private Button btbAddEvent;
    private Switch swchReminder;
    private ChipGroup chipGroup;
    private AutoCompleteTextView spinnerGroup;
    private ImageButton btnMic;
    private GeminiManager geminiManager;

    private final ActivityResultLauncher<Intent> speechRecognizerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> speechResults = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (speechResults != null && !speechResults.isEmpty()) {
                        String spokenText = speechResults.get(0);

                        // Send spoken text to Gemini for processing
                        geminiManager.sendMessage(spokenText, new GeminiManager.GeminiCallback() {
                            @Override
                            public void onSuccessful(String response) {
                                try {
                                    // Parse the JSON response
                                    JSONObject eventJson = new JSONObject(response);

                                    // Update UI fields with parsed data
                                    runOnUiThread(() -> {
                                        try {
                                            if (eventJson.has("title")) {
                                                etTitle.setText(eventJson.getString("title"));
                                            }
                                            if (eventJson.has("details")) {
                                                etDetails.setText(eventJson.getString("details"));
                                            }
                                            if (eventJson.has("address")) {
                                                EditText etAddress = findViewById(R.id.etAdress);
                                                if (etAddress != null) {
                                                    etAddress.setText(eventJson.getString("address"));
                                                }
                                            }
                                            if (eventJson.has("topic")) {
                                                spinnerGroup.setText(eventJson.getString("topic"), false);
                                            }
                                            if (eventJson.has("start")) {
                                                String startDateTime = eventJson.getString("start");
                                                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                Date startDate = isoFormat.parse(startDateTime);

                                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                                                tvDate.setText(dateFormat.format(startDate));
                                                tvStartTime.setText(timeFormat.format(startDate));

                                                // Set end time based on duration if available
                                                if (eventJson.has("duration")) {
                                                    Calendar endCal = Calendar.getInstance();
                                                    endCal.setTime(startDate);
                                                    endCal.add(Calendar.MINUTE, eventJson.getInt("duration"));
                                                    tvEndTime.setText(timeFormat.format(endCal.getTime()));
                                                }
                                            }
                                            if (eventJson.has("reminder") && eventJson.getBoolean("reminder")) {
                                                swchReminder.setChecked(true);
                                                if (eventJson.has("remTime")) {
                                                    String remDateTime = eventJson.getString("remTime");
                                                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                    Date remDate = isoFormat.parse(remDateTime);

                                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                                                    tvReminderDate.setText(dateFormat.format(remDate));
                                                    tvReminderTime.setText(timeFormat.format(remDate));
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e("NewEventActivity", "Error parsing date/time", e);
                                            Toast.makeText(NewEventActivity.this, "Error parsing event details", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (JSONException e) {
                                    Log.e("NewEventActivity", "Error parsing JSON response", e);
                                    Toast.makeText(NewEventActivity.this, "Error processing speech input", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Throwable ex) {
                                Log.e("NewEventActivity", "Gemini API error", ex);
                                runOnUiThread(() -> {
                                    Toast.makeText(NewEventActivity.this, "Error processing speech input", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        // Initialize model and check if user is logged in
        model = Model.getInstance(this);
        notificationMsg = new NotificationMsg(this);
        currentUser = model.getCurrentUser();

        // Initialize GeminiManager with system prompt
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.system_prompt);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String systemPrompt = stringBuilder.toString();
            geminiManager = GeminiManager.getInstance(systemPrompt);
        } catch (IOException e) {
            Log.e("NewEventActivity", "Error reading system prompt", e);
            Toast.makeText(this, "Error initializing speech recognition", Toast.LENGTH_SHORT).show();
        }

        etAddress = findViewById(R.id.etAdress);
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
        btnMic = findViewById(R.id.btnMic);

        // Check if we're editing an existing event
        String eventId = getIntent().getStringExtra("Event");

        // Load all events first, then initialize groups
        model.getEventsByUserId(currentUser.getId(), events -> {
            // Now that events are loaded, get groups and setup spinner
            setupGroupSpinner();

            // If we're editing an event, load its details
            if (eventId != null) {
                // Change button text to "Update"
                btbAddEvent.setText("Update Event");
                setTitle("Edit Event");

                // Find the event in the loaded events
                for (Event event : events) {
                    if (event.getId().equals(eventId)) {
                        // Populate fields with event data
                        populateEventData(event);
                        break;
                    }
                }
            }
        }, e -> Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show());

        tvReminderDate.setVisibility(View.GONE);
        tvReminderTime.setVisibility(View.GONE);

        btbAddEvent.setOnClickListener(this);

        tvDate.setOnClickListener(view -> showDatePicker(1));
        tvReminderDate.setOnClickListener(view -> showDatePicker(2));
        tvStartTime.setOnClickListener(view -> showTimePicker(1));
        tvEndTime.setOnClickListener(view -> showTimePicker(2));
        tvReminderTime.setOnClickListener(view -> showTimePicker(3));

        // Initialize current date and time
        setupInitialDateTime();

        btnMic.setOnClickListener(v -> startSpeechToText());
    }

    private void setupGroupSpinner() {
        // Get groups from model
        groups = model.getGroups();

        // Ensure 'Add New Group' option is present
        if (!groups.contains("Add New Group")) {
            groups.add("Add New Group");
        }

        // Setup the adapter with dropdown layout
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups);
        spinnerGroup.setAdapter(spinnerAdapter);
        
        // Set dropdown width to match parent
        spinnerGroup.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        
        // Enable dropdown on click
        spinnerGroup.setOnClickListener(v -> spinnerGroup.showDropDown());

        spinnerGroup.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGroup = groups.get(position);
            if ("Add New Group".equals(selectedGroup)) {
                showAddGroupDialog();
                spinnerGroup.dismissDropDown();
            } else {
                spinnerGroup.setText(selectedGroup, false);
            }
        });

        // Set the default selected group to 'All'
        spinnerGroup.setText("All", false);
    }

    private void setupInitialDateTime() {
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

        // Set the default reminder date and time to current date and time
        tvReminderDate.setText(currentDate);
        tvReminderTime.setText(currentTime);
    }

    private void populateEventData(Event event) {
        etTitle.setText(event.getTitle());
        etDetails.setText(event.getDetails());
        
        // Set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        tvDate.setText(dateFormat.format(event.getStart()));
        
        // Set start time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        tvStartTime.setText(timeFormat.format(event.getStart()));
        
        // Calculate and set end time based on duration
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(event.getStart());
        endCal.add(Calendar.MINUTE, event.getDuration());
        tvEndTime.setText(timeFormat.format(endCal.getTime()));
        
        // Set group
        spinnerGroup.setText(event.getGroup(), false);
        
        // Set reminder if exists
        if (event.isReminder() && event.getRemTime() != null) {
            swchReminder.setChecked(true);
            tvReminderDate.setVisibility(View.VISIBLE);
            tvReminderTime.setVisibility(View.VISIBLE);
            tvReminderDate.setText(dateFormat.format(event.getRemTime()));
            tvReminderTime.setText(timeFormat.format(event.getRemTime()));
        }
        
        // Set address if exists
        if (event.getAddress() != null) {
            etAddress.setText(event.getAddress());
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btbAddEvent) {
            String eventTitle = etTitle.getText().toString().trim();
            if (eventTitle.isEmpty()) {
                Toast.makeText(this, "Event title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get all the event details
            String details = etDetails.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            String group = spinnerGroup.getText().toString().trim();
            
            // Get userIds from chips
            ArrayList<String> usersId = new ArrayList<>();
            usersId.add(currentUser.getId()); // Add current user

            // Add all selected users from chips
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View chipView = chipGroup.getChildAt(i);
                if (chipView instanceof Chip) {
                    Chip chip = (Chip) chipView;
                    String userId = (String) chip.getTag();
                    String username = chip.getText().toString();
                    
                    if (userId != null && !userId.isEmpty()) {
                        if (!usersId.contains(userId)) {
                            usersId.add(userId);
                            Log.d("NewEventActivity", "Added user ID: " + userId + " for user: " + username);
                        } else {
                            Log.d("NewEventActivity", "Skipped duplicate user ID: " + userId + " for user: " + username);
                        }
                    } else {
                        Log.w("NewEventActivity", "Chip for user '" + username + "' has no associated userId!");
                        // Try to recover the userId from mutuals if possible
                        for (Map.Entry<String, String> entry : currentUser.getMutuals().entrySet()) {
                            if (entry.getValue().equals(username)) {
                                String recoveredUserId = entry.getKey();
                                if (!usersId.contains(recoveredUserId)) {
                                    usersId.add(recoveredUserId);
                                    Log.d("NewEventActivity", "Recovered and added user ID: " + recoveredUserId + " for user: " + username);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (usersId.size() <= 1) {
                Log.w("NewEventActivity", "Event has only the current user!");
            }
            
            Log.d("NewEventActivity", "Final user IDs for event: " + usersId.toString());

            // Parse start date and time
            Date startDate = null;
            try {
                String dateStr = tvDate.getText().toString().trim() + " " + tvStartTime.getText().toString().trim();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                startDate = sdf.parse(dateStr);
            } catch (Exception e) {
                startDate = new Date(); // fallback to current time
            }

            // Calculate duration
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

            // Handle reminder
            Date reminderTime = null;
            if (swchReminder.isChecked()) {
                try {
                    String dateStr = tvReminderDate.getText().toString().trim() + " " + tvReminderTime.getText().toString().trim();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    reminderTime = sdf.parse(dateStr);

                    // Schedule notification if reminder time is in the future
                    if (reminderTime.getTime() > System.currentTimeMillis()) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(reminderTime);
                        Reminder.setAlarm(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), eventTitle);
                        Toast.makeText(this, "Reminder set for " + tvReminderDate.getText() + " " + tvReminderTime.getText(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Reminder time must be in the future", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Log.e("NewEventActivity", "Error parsing reminder date/time", e);
                    Toast.makeText(this, "Error setting reminder time", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Check if we're editing an existing event
            String eventId = getIntent().getStringExtra("Event");
            if (eventId != null) {
                // Update existing event
                model.updateEvent(eventId, eventTitle, details, address, group, usersId,
                                startDate, reminderTime, swchReminder.isChecked(), 
                                duration); // notificationId removed as it will be set by the system
                Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Create new event
                Event event = new Event(
                    eventTitle,
                    details,
                    address,
                    null, // id will be set by Firestore
                    group,
                    usersId,
                    startDate,
                    reminderTime,
                    swchReminder.isChecked(),
                    duration
                );
                model.createEvent(event);
                Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK, getIntent());  // Set the result before finishing
            finish(); // Return to previous screen
        }
    }

    private boolean isValidUsername(String username) {
        // Check if the username exists in the current user's mutuals
        if (currentUser.getMutuals() != null) {
            return currentUser.getMutuals().containsValue(username);
        }
        return false;
    }

    private void addUserChip(String username) {
        // Create a new chip
        Chip chip = new Chip(this);
        chip.setText(username);

        // Find the userId from mutuals map
        String userId = null;
        for (Map.Entry<String, String> entry : currentUser.getMutuals().entrySet()) {
            if (entry.getValue().equals(username)) {
                userId = entry.getKey();
                chip.setTag(userId); // Store the userId in the chip's tag
                break;
            }
        }

        if (userId != null) {
            model.getUserById(userId,
                    user -> {
                        if (user != null) {
                            // Set profile picture if available
                            Bitmap profileBitmap = user.getProfilePic();
                            if (profileBitmap != null) {
                                Drawable drawable = new BitmapDrawable(getResources(), profileBitmap);
                                chip.setChipIcon(drawable);
                                chip.setChipIconSize(48f);
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
            Toast.makeText(this, "User not found in your mutuals", Toast.LENGTH_SHORT).show();
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
        // Get updated groups and refresh spinner
        setupGroupSpinner();
        spinnerGroup.setText(newGroup, false);
    }

    private Date parseReminderDateTime() {
        try {
            String dateStr = tvReminderDate.getText().toString().trim() + " " + tvReminderTime.getText().toString().trim();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            Log.e("NewEventActivity", "Error parsing reminder date/time", e);
            return null;
        }
    }

    private void startSpeechToText() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_CODE);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the event title...");

        try {
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error starting speech recognition",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechToText();
            } else {
                Toast.makeText(this, "Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}