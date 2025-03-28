package com.example.mytasksapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.Adapters.DailyEventsAdapter;
import com.example.mytasksapplication.CustomCalendarView;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Event;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Model model;
    private DailyEventsAdapter adapter;
    private List<Event> events;
    private CustomCalendarView customCalendarView;
    private ListView lstDailyEvents;
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

        lstDailyEvents = findViewById(R.id.lstDailyEvents);
        // tasks = model.tempData();

        adapter = new DailyEventsAdapter(this, events);
        lstDailyEvents.setAdapter(adapter);

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        if (events.isEmpty()) {
            tvEmptyList.setText("You don't have any events, press the + button and add an event");
            lstDailyEvents.setEmptyView(tvEmptyList);
        }


        customCalendarView = findViewById(R.id.customCalendarView);
        customCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();
            updateEventsForSelectedDate(selectedDate);
        });

//        drawLines();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_Add) {
                Intent intent = new Intent(MainActivity.this, NewEventActivity.class);
                activityStartLauncher.launch(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_Events) {
                Intent intent = new Intent(MainActivity.this, AllEventsActivity.class);
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
        if (item.getItemId() == R.id.action_profile) {
            intent.putExtra("PROFILE", "action_profile");
            activityStartLauncher.launch(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            intent.putExtra("LOGOUT", "action_logout");
            activityStartLauncher.launch(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        customCalendarView.invalidate();
    }

    private void updateEventsForSelectedDate(Date selectedDate) {
        List<Event> filteredEvents = new ArrayList<>();
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(selectedDate);

        for (Event event : events) {
            Calendar eventCalendar = Calendar.getInstance();
            eventCalendar.setTime(event.getDate());

            if (selectedCalendar.get(Calendar.YEAR) == eventCalendar.get(Calendar.YEAR) &&
                    selectedCalendar.get(Calendar.MONTH) == eventCalendar.get(Calendar.MONTH) &&
                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == eventCalendar.get(Calendar.DAY_OF_MONTH)) {
                filteredEvents.add(event);
            }
        }

        adapter.clear();
        if (!filteredEvents.isEmpty()) {
            adapter.addAll(filteredEvents);
            lstDailyEvents.setEmptyView(null);
        } else {
            tvEmptyList.setText("You don't have any events");
            lstDailyEvents.setEmptyView(tvEmptyList);
        }

        adapter.notifyDataSetChanged();
    }


/*    private void drawLines() {
        Set<Long> taskDates = new HashSet<>();
        for (Task task : tasks) {
            taskDates.add(task.getDate().getTime());
        }
        customCalendarView.setTaskDates(taskDates);
    }*/
}
