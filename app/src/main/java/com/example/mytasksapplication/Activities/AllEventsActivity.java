package com.example.mytasksapplication.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytasksapplication.Adapters.AllEventsAdapter;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Event;
import com.example.mytasksapplication.User;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class AllEventsActivity extends AppCompatActivity implements View.OnLongClickListener {

    private Model model;
    private User currentUser;
    private RecyclerView lstAllEvents;
    private TextView tvEmptyList;
    private ActivityResultLauncher<Intent> activityStartLauncher;
    private ArrayList<Event> events;
    private ArrayAdapter<String> spinnerAdapter;
    private AutoCompleteTextView spinnerTopic;
    private ArrayList<String> topics;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events);
        model = Model.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Today");

        lstAllEvents = findViewById(R.id.lstAllEvents);
        tvEmptyList = findViewById(R.id.tvEmptyList);

        activityStartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { }
        );

        List<Event> events = new ArrayList<Event>();
        currentUser = model.getCurrentUser();
        events = model.getEventsByUserId(currentUser.getId());

        // Setup adapter
        AllEventsAdapter adapter = new AllEventsAdapter(this, events);
        lstAllEvents.setAdapter(adapter);

        if (events.isEmpty()) { // Handle empty list
            tvEmptyList.setVisibility(View.VISIBLE);
            lstAllEvents.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lstAllEvents.setVisibility(View.VISIBLE);
        }

        topics = model.getTopics();
        spinnerTopic = findViewById(R.id.spinnerTopic);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, topics);
        spinnerTopic.setAdapter(spinnerAdapter);

        spinnerTopic.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGroup = topics.get(position);

            // set normal add click filter

            if ("Add New Group".equals(selectedGroup)) {
                showAddGroupDialog();
            }
            spinnerTopic.dismissDropDown(); // Hide the dropdown after selection
        });

        spinnerTopic.setOnLongClickListener(this);

        // Setup bottom navigation
        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent intent = new Intent();
            if (item.getItemId() == R.id.nav_Add) {
                intent.setClass(AllEventsActivity.this, NewEventActivity.class);
                activityStartLauncher.launch(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_Today) {
                intent.setClass(AllEventsActivity.this, MainActivity.class);
                activityStartLauncher.launch(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        if (item.getItemId() == R.id.action_followers) {
            intent.putExtra("FOLLOW_REQUEST", "action_follow_request");
            intent.setClass(AllEventsActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_users) {
            intent.putExtra("USERS", "action_users");
            intent.setClass(AllEventsActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_following) {
            intent.putExtra("FOLLOWING", "action_following");
            intent.setClass(AllEventsActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            intent.putExtra("PROFILE", "action_profile");
            intent.setClass(AllEventsActivity.this, LoginActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            intent.putExtra("LOGOUT", "action_logout");
            intent.setClass(AllEventsActivity.this, LoginActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        }
        return false;
    }

    private void addNewGroup(String newGroup) {
        // Add the new group before "Add New Group" option
        topics.add(topics.size() - 1, newGroup);

        // Notify adapter of the change
        spinnerAdapter.notifyDataSetChanged();

        // Set the new group as the selected item
        spinnerTopic.setText(newGroup, false);
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

                    if (!newGroup.isEmpty() && !topics.contains(newGroup)) {
                        addNewGroup(newGroup);
                    } else {
                        Toast.makeText(this, "Group already exists or invalid name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onLongClick(View view) {
        int position = lstAllEvents.getChildAdapterPosition(view); // Get the clicked position
        if (position == RecyclerView.NO_POSITION || position >= topics.size()) {
            return false; // Invalid position, ignore
        }

        String selectedTopic = topics.get(position);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_delete_topic, null);

        // Initialize dialog components
        TextView textViewNo = dialogView.findViewById(R.id.textView4);
        TextView textViewYes = dialogView.findViewById(R.id.textView5);

        // Create and show the AlertDialog
        AlertDialog topicDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        topicDialog.show();

        // No button
        textViewNo.setOnClickListener(v -> topicDialog.dismiss());

        // Yes button
        textViewYes.setOnClickListener(v -> {
            ArrayList<Event> eventsToRemove = new ArrayList<>();
            for (Event event : events) {
                if (event.getTopic().equals(selectedTopic)) {
                    eventsToRemove.add(event);
                }
            }
            events.removeAll(eventsToRemove);

            // Remove topic from the list
            topics.remove(selectedTopic);
            spinnerAdapter.notifyDataSetChanged();
            topicDialog.dismiss();
        });

        return true; // Indicate that the event was handled
    }

}
