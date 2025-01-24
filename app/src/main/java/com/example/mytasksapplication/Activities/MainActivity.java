package com.example.mytasksapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.Adapters.DailyTasksAdapter;
import com.example.mytasksapplication.CustomCalendarView;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Model model;
    private DailyTasksAdapter adapter;
    private List<Task> tasks;
    private CustomCalendarView customCalendarView;
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
        setTitle("Calendar");

        lstDailyTasks = findViewById(R.id.lstDailyTasks);
        tasks = model.tempData();

        adapter = new DailyTasksAdapter(this, tasks);
        lstDailyTasks.setAdapter(adapter);

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //temporary comment
        tvEmptyList = findViewById(R.id.tvEmptyList);
        if (tasks.isEmpty()) {
            tvEmptyList.setText("There are no events");
            lstDailyTasks.setEmptyView(tvEmptyList);
        }

        // Initialize custom calendar view
        customCalendarView = findViewById(R.id.customCalendarView);
        customCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Use Calendar to create Date (fix deprecated constructor usage)
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth); // Month is 0-based
            Date selectedDate = calendar.getTime();
            updateTasksForSelectedDate(selectedDate);
        });
        drawLines();

        bottomNavigationView.setOnItemSelectedListener(item -> {
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
        });

        activityStartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle result if needed
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

    // Draw lines based on task dates
    private void drawLines() {
        Set<Long> taskDates = new HashSet<>();
        for (Task task : tasks) {
            taskDates.add(task.getDate().getTime()); // Add the timestamp of the task
        }

        // Update custom calendar view with task dates
        customCalendarView.setTaskDates(taskDates); // Pass task dates to the custom calendar view
    }
}
