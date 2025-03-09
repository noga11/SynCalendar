package com.example.mytasksapplication.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytasksapplication.Adapters.AllEventsAdapter;
import com.example.mytasksapplication.Group;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Event;
import com.example.mytasksapplication.User;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class AllEventsActivity extends AppCompatActivity {

    private Model model;
    private User currentUser;
    private RecyclerView lstAllEvents;
    private TextView tvEmptyList;
    private ActivityResultLauncher<Intent> activityStartLauncher;

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
        currentUser = model.getUser();
        for (Group group : currentUser.getGroups()){
            for (Event task : group.getEvents()){
                events.add(task);
            }
        }

        // Setup adapter
        AllEventsAdapter adapter = new AllEventsAdapter(this, events);
        lstAllEvents.setAdapter(adapter);

        // Handle empty list
        if (events.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
            lstAllEvents.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lstAllEvents.setVisibility(View.VISIBLE);
        }

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
}
