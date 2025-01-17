package com.example.mytasksapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.Adapters.DailyTasksAdapter;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    private Model model;
    private DailyTasksAdapter adapter;
    private List<Task> tasks;
    private CalendarView calendarView;
    private ListView lstDailyTasks;
    private TextView tvEmptyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = Model.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Calander");

        lstDailyTasks = findViewById(R.id.lstDailyTasks);
        List<Task> tasks = model.tempData();

        DailyTasksAdapter adapter = new DailyTasksAdapter(this, tasks);
        lstDailyTasks.setAdapter(adapter);

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        tvEmptyList = findViewById(R.id.tvEmptyList);
        if (tasks.isEmpty()) {
            tvEmptyList.setText("There are no events");
            lstDailyTasks.setEmptyView(tvEmptyList);
        }

        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + day;
            updateTasksForSelectedDate(selectedDate);
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_Add) {
                    startActivity(new Intent(MainActivity.this, NewTaskActivity.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_Tasks) {
                    startActivity(new Intent(MainActivity.this, AllTasksActivity.class));
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
            startActivity(new Intent(MainActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_users){
            startActivity(new Intent(MainActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_following){
            startActivity(new Intent(MainActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_profile){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_logout){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void updateTasksForSelectedDate(String selectedDate) {
        List<Task> filteredTasks = new ArrayList<>();
        LocalDate selectedLocalDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-M-d"));

        for (Task task : tasks) {
            LocalDate taskDate = task.getDate().toLocalDate();
            if (taskDate.equals(selectedLocalDate)) {
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
