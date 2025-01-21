package com.example.mytasksapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.Adapters.DailyTasksAdapter;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Model model;
    private DailyTasksAdapter adapter;
    private List<Task> tasks;
    private CalendarView calendarView;
    private ListView lstDailyTasks;
    private TextView tvEmptyList;

    private ActivityResultLauncher<Intent> activityStartLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = Model.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Calander");

        lstDailyTasks = findViewById(R.id.lstDailyTasks);
        tasks = model.tempData();

        adapter = new DailyTasksAdapter(this, tasks);
        lstDailyTasks.setAdapter(adapter);

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        tvEmptyList = findViewById(R.id.tvEmptyList);
        if (tasks.isEmpty()) {
            tvEmptyList.setText("There are no events");
            lstDailyTasks.setEmptyView(tvEmptyList);
        }

        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            Date selectedDate = new Date(year, month, day);
            updateTasksForSelectedDate(selectedDate);
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_Add) {
                    Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                    activityStartLauncher.launch(intent);
                    return true;
                } else if (item.getItemId() == R.id.nav_Tasks) {
                    Intent intent = new Intent(MainActivity.this, AllTasksActivity.class);
                    activityStartLauncher.launch(intent);
                    return true;
                }
                return false;
            }
        });

        activityStartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                }
        );
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
            activityStartLauncher.launch(new Intent(MainActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_users){
            intent.putExtra("USERS", "action_users");
            activityStartLauncher.launch(new Intent(MainActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_following){
            intent.putExtra("FOLLOWING", "action_following");
            activityStartLauncher.launch(new Intent(MainActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_profile){
            intent.putExtra("PROFILE", "action_profile");
            activityStartLauncher.launch(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_logout){
            intent.putExtra("LOGOUT", "action_logout");
            activityStartLauncher.launch(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        }
        return false;
    }

    private void updateTasksForSelectedDate(Date selectedDate) {
        List<Task> filteredTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDate().equals(selectedDate)) {
                filteredTasks.add(task);
            }
        }

        adapter.clear();
        adapter.addAll(filteredTasks);
        adapter.notifyDataSetChanged();

        if (filteredTasks.isEmpty()) {
            tvEmptyList.setText("You don't have any events");
            lstDailyTasks.setEmptyView(tvEmptyList);
        }
    }
}
