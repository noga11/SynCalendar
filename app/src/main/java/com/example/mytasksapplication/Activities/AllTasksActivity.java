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

import com.example.mytasksapplication.Adapters.AllTasksAdapter;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.example.mytasksapplication.User;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

public class AllTasksActivity extends AppCompatActivity {

    private Model model;
    private User currentUser;
    private RecyclerView lstAllTasks;
    private TextView tvEmptyList;
    private ActivityResultLauncher<Intent> activityStartLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);
        model = Model.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Today");

        lstAllTasks = findViewById(R.id.lstAllTasks);
        tvEmptyList = findViewById(R.id.tvEmptyList);

        activityStartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { }
        );

        currentUser = model.getUser();
        List<Task> tasks = currentUser.getGroups();

        // Setup adapter
        AllTasksAdapter adapter = new AllTasksAdapter(this, tasks);
        lstAllTasks.setAdapter(adapter);

        // Handle empty list
        if (tasks.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
            lstAllTasks.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lstAllTasks.setVisibility(View.VISIBLE);
        }

        // Setup bottom navigation
        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent intent = new Intent();
            if (item.getItemId() == R.id.nav_Add) {
                intent.setClass(AllTasksActivity.this, NewTaskActivity.class);
                activityStartLauncher.launch(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_Today) {
                intent.setClass(AllTasksActivity.this, MainActivity.class);
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
        if (item.getItemId() == R.id.action_follow_request) {
            intent.putExtra("FOLLOW_REQUEST", "action_follow_request");
            intent.setClass(AllTasksActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_users) {
            intent.putExtra("USERS", "action_users");
            intent.setClass(AllTasksActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_following) {
            intent.putExtra("FOLLOWING", "action_following");
            intent.setClass(AllTasksActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            intent.putExtra("PROFILE", "action_profile");
            intent.setClass(AllTasksActivity.this, LoginActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            intent.putExtra("LOGOUT", "action_logout");
            intent.setClass(AllTasksActivity.this, LoginActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        }
        return false;
    }
}
