package com.example.mytasksapplication.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.Adapters.AllTasksAdapter;
import com.example.mytasksapplication.Adapters.DailyTasksAdapter;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class AllTasksActivity extends AppCompatActivity {

    private Model model;
    private ListView lstAllTasks;
    private TextView tvEmptyList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Today");

        lstAllTasks = findViewById(R.id.lstAllTasks);
        List<Task> tasks = new ArrayList<>();

        AllTasksAdapter adapter = new AllTasksAdapter(this, tasks);
        lstAllTasks.setAdapter(adapter);

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        tvEmptyList = findViewById(R.id.tvEmptyList);
        if (tasks.isEmpty()) {
            tvEmptyList.setText("There are no events");
            lstAllTasks.setEmptyView(tvEmptyList);
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_Add) {
                    startActivity(new Intent(AllTasksActivity.this, NewTaskActivity.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_Today) {
                    startActivity(new Intent(AllTasksActivity.this, MainActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
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
            intent.putExtra("REQUEST", "action_FollowRequest");
            startActivity(new Intent(AllTasksActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_users){
            startActivity(new Intent(AllTasksActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_following){
            intent.putExtra("FOLLOWING", "action_Following");
            startActivity(new Intent(AllTasksActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_profile){
            startActivity(new Intent(AllTasksActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_logout){
            startActivity(new Intent(AllTasksActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return false;
    }
}
