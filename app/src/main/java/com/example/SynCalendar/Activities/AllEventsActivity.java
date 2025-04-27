package com.example.SynCalendar.Activities;

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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SynCalendar.Adapters.AllEventsAdapter;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.Event;
import com.example.SynCalendar.SwipeToDeleteCallback;
import com.example.SynCalendar.User;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class AllEventsActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnClickListener {

    private Model model;
    private User currentUser;
    private RecyclerView lstAllEvents;
    private TextView tvEmptyList;
    private ActivityResultLauncher<Intent> activityStartLauncher;
    private ArrayList<Event> events, filterdEvents;
    private ArrayAdapter<String> spinnerAdapter;
    private AutoCompleteTextView spinnerGroup;
    private ArrayList<String> groups;
    // temporary to push

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

        currentUser = model.getCurrentUser();
        events = model.getEventsByUserId(currentUser.getId());
        filterdEvents= model.getEventsByUserId(currentUser.getId());

        // Setup RecyclerView
        lstAllEvents = findViewById(R.id.lstAllEvents);
        lstAllEvents.setLayoutManager(new LinearLayoutManager(this));
        lstAllEvents.setOnClickListener(this);

        List<Event> events = model.getEventsByUserId(model.getCurrentUser().getId());
        AllEventsAdapter adapter = new AllEventsAdapter(this, filterdEvents);
        lstAllEvents.setAdapter(adapter);

        // Attach swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(adapter, events, this));
        itemTouchHelper.attachToRecyclerView(lstAllEvents);

        if (events.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
            lstAllEvents.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lstAllEvents.setVisibility(View.VISIBLE);
        }

        groups = model.getGroups();
        spinnerGroup = findViewById(R.id.spinnerGroup);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups);
        spinnerGroup.setAdapter(spinnerAdapter);

        spinnerGroup.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGroup = groups.get(position);
            filterdEvents.clear();

            if ("Add New Group".equals(selectedGroup)) {
                showAddGroupDialog();
                spinnerGroup.dismissDropDown();
            } else {
                for (Event event : events) {
                    if (event.getGroup().equals(selectedGroup)) {
                        filterdEvents.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        spinnerGroup.setOnLongClickListener(this);

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
        groups.add(groups.size() - 1, newGroup);

        // Notify adapter of the change
        spinnerAdapter.notifyDataSetChanged();

        // Set the new group as the selected item
        spinnerGroup.setText(newGroup, false);
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

    @Override
    public boolean onLongClick(View view) {
        int position = lstAllEvents.getChildAdapterPosition(view); // Get the clicked position
        if (position == RecyclerView.NO_POSITION || position >= groups.size()) {
            return false; // Invalid position, ignore
        }

        String selectedGroup = groups.get(position);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_delete_group, null);

        // Initialize dialog components
        TextView textViewNo = dialogView.findViewById(R.id.textView4);
        TextView textViewYes = dialogView.findViewById(R.id.textView5);

        // Create and show the AlertDialog
        AlertDialog groupDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        groupDialog.show();

        // No button
        textViewNo.setOnClickListener(v -> groupDialog.dismiss());

        // Yes button
        textViewYes.setOnClickListener(v -> {
            ArrayList<Event> eventsToRemove = new ArrayList<>();
            for (Event event : events) {
                if (event.getGroup().equals(selectedGroup)) {
                    eventsToRemove.add(event);
                }
            }
            events.removeAll(eventsToRemove);

            // Remove group from the list
            groups.remove(selectedGroup);
            spinnerAdapter.notifyDataSetChanged();
            groupDialog.dismiss();
        });

        return true; // Indicate that the event was handled
    }

    @Override
    public void onClick(View view) {
        int position = lstAllEvents.getChildAdapterPosition(view);
        if (position != RecyclerView.NO_POSITION) {
            Event event = filterdEvents.get(position);
            Intent intent = new Intent(AllEventsActivity.this, NewEventActivity.class);
            intent.putExtra("Event", event.getId());
            activityStartLauncher.launch(intent);
        }
    }
}
