package com.example.SynCalendar.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SynCalendar.Adapters.DailyEventsAdapter;
import com.example.SynCalendar.CustomCalendar.CalendarAdapter;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.Event;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.recyclerview.widget.GridLayoutManager;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private Model model;
    private DailyEventsAdapter adapter;
    private List<Event> events;
    private ActivityResultLauncher<Intent> activityStartLauncher;

    private ListView lstDailyEvents;
    private TextView tvEmptyList;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private Date selectedDate;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize model and check if user is logged in
        model = Model.getInstance(this);
        if (model.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize UI components directly in onCreate
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        lstDailyEvents = findViewById(R.id.lstDailyEvents);
        tvEmptyList = findViewById(R.id.tvEmptyList);

        selectedDate = new Date();
        setMonthView();

        // Initialize the events list and adapter
        events = new ArrayList<>();
        adapter = new DailyEventsAdapter(this, events);
        lstDailyEvents.setAdapter(adapter);

        // Fetch events for the current user
        events = model.getEventsByUserId(model.getCurrentUser().getId());
        updateEventsForSelectedDate(selectedDate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Calendar");

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);
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
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        if (item.getItemId() == R.id.action_followers) {
            intent.putExtra("FOLLOW_REQUEST", "action_follow_request");
            intent.setClass(MainActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_users) {
            intent.putExtra("USERS", "action_users");
            intent.setClass(MainActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_following) {
            intent.putExtra("FOLLOWING", "action_following");
            intent.setClass(MainActivity.this, FollowingActivity.class);
            activityStartLauncher.launch(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_profile) {
            intent.putExtra("PROFILE", "action_profile");
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            intent.putExtra("LOGOUT", "action_logout");
            activityStartLauncher.launch(intent);
            finish();
            return true;
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
//        customCalendarView.invalidate();
    }

    private void updateEventsForSelectedDate(Date selectedDate) {
        if (adapter == null || tvEmptyList == null) {
            return; // Ensure adapter and tvEmptyList are not null
        }

        List<Event> filteredEvents = new ArrayList<>();
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(selectedDate);
        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
        selectedCalendar.set(Calendar.MINUTE, 0);
        selectedCalendar.set(Calendar.SECOND, 0);
        selectedCalendar.set(Calendar.MILLISECOND, 0);

        for (Event event : events) {
            Calendar eventCalendar = Calendar.getInstance();
            eventCalendar.setTime(event.getStart());
            eventCalendar.set(Calendar.HOUR_OF_DAY, 0);
            eventCalendar.set(Calendar.MINUTE, 0);
            eventCalendar.set(Calendar.SECOND, 0);
            eventCalendar.set(Calendar.MILLISECOND, 0);

            Log.d("MainActivity", "Selected date: " + selectedCalendar.getTime() + ", Event date: " + eventCalendar.getTime());

            if (selectedCalendar.equals(eventCalendar)) {
                filteredEvents.add(event);
            }
        }

        Log.d("MainActivity", "Filtered events count: " + filteredEvents.size());

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

    private void setMonthView()
    {
        monthYearText.setText(monthYearFormat.format(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(Date date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        // Get the first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Get the number of days in the month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for(int i = 1; i <= 42; i++) {
            if(i < firstDayOfWeek || i > daysInMonth + firstDayOfWeek - 1) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - firstDayOfWeek + 1));
            }
        }
        return daysInMonthArray;
    }

    public void previousMonthAction(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        calendar.add(Calendar.MONTH, -1);
        selectedDate = calendar.getTime();
        setMonthView();
    }

    public void nextMonthAction(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        calendar.add(Calendar.MONTH, 1);
        selectedDate = calendar.getTime();
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if(!dayText.equals("")) {
            // Create a new date object for the selected day
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayText));
            Date clickedDate = calendar.getTime();
            
            // Update the events list for the selected date
            updateEventsForSelectedDate(clickedDate);
            
            String message = "Selected Date " + dayText + " " + monthYearFormat.format(selectedDate);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}