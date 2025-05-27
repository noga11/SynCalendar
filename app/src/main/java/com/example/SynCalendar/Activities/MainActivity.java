package com.example.SynCalendar.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SynCalendar.Adapters.DailyEventsAdapter;
import com.example.SynCalendar.CustomCalendar.CalendarAdapter;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.Event;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.recyclerview.widget.GridLayoutManager;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private Model model;
    private DailyEventsAdapter adapter;
    private List<Event> events;
    private ActivityResultLauncher<Intent> activityStartLauncher;

    private RecyclerView lstDailyEvents;
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

        // Initialize UI components directly in onCreate
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        lstDailyEvents = findViewById(R.id.lstDailyEvents);
        tvEmptyList = findViewById(R.id.tvEmptyList);

        selectedDate = new Date();

        // Initialize the events list and adapter
        events = new ArrayList<>();
        adapter = new DailyEventsAdapter(this, events);
        
        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lstDailyEvents.setLayoutManager(layoutManager);
        lstDailyEvents.setAdapter(adapter);

        // Add divider decoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lstDailyEvents.getContext(), layoutManager.getOrientation());
        lstDailyEvents.addItemDecoration(dividerItemDecoration);

        // Set up item click listener
        adapter.setOnItemClickListener((event, position) -> {
            Intent intent = new Intent(MainActivity.this, NewEventActivity.class);
            intent.putExtra("Event", event.getId());
            intent.putExtra("isEditing", true);
            activityStartLauncher.launch(intent);
        });

        // Add swipe-to-delete functionality
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Event deletedEvent = events.get(position);

                // Remove from RecyclerView
                events.remove(position);
                adapter.notifyItemRemoved(position);

                // Show Snackbar with Undo option
                Snackbar snackbar = Snackbar.make(viewHolder.itemView, "Event deleted", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", v -> {
                    events.add(position, deletedEvent);
                    adapter.notifyItemInserted(position);
                });

                // Delete from database only if NOT undone
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            model.deleteEvent(deletedEvent);
                        }
                    }
                });

                snackbar.show();
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(lstDailyEvents);

        setMonthView();

        // Fetch events for the current user
        refreshEvents();

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
                    // Refresh events when returning from NewEventActivity
                    if (result.getData() != null && result.getData().getComponent() != null &&
                        result.getData().getComponent().getClassName().contains("NewEventActivity")) {
                        refreshEvents();
                    }
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
            intent = new Intent(MainActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_Followers");
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_users) {
            intent = new Intent(MainActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_FindUser");
            activityStartLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_following) {
            intent = new Intent(MainActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_Following");
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

    private void refreshEvents() {
        if (model.getCurrentUser() == null) {
            Log.d(TAG, "No user logged in");
            return;
        }
        
        model.getEventsByUserId(model.getCurrentUser().getId(), 
            userEvents -> {
                Log.d(TAG, "Fetched " + userEvents.size() + " events");
                events.clear();
                events.addAll(userEvents);
                sortEventsByStartTime(events);
                
                // Update both the calendar view and daily events
                setMonthView();
                updateEventsForSelectedDate(selectedDate);
            }, 
            e -> {
                Log.e(TAG, "Failed to load events", e);
                Toast.makeText(MainActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void updateEventsForSelectedDate(Date selectedDate) {
        if (adapter == null || tvEmptyList == null || lstDailyEvents == null) {
            Log.e(TAG, "UI components not initialized");
            return;
        }

        List<Event> filteredEvents = new ArrayList<>();
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(selectedDate);
        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
        selectedCalendar.set(Calendar.MINUTE, 0);
        selectedCalendar.set(Calendar.SECOND, 0);
        selectedCalendar.set(Calendar.MILLISECOND, 0);

        // Debug log for selected date
        Log.d("EventDebug", "Selected date: " + selectedCalendar.getTime());
        for (Event event : events) {
            if (event.getStart() != null) {
                Log.d("EventDebug", "Event: " + event.getTitle() + " start: " + event.getStart());
                Calendar eventCalendar = Calendar.getInstance();
                eventCalendar.setTime(event.getStart());
                boolean sameDay = eventCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                                eventCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                                eventCalendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH);
                if (sameDay) {
                    filteredEvents.add(event);
                }
            }
        }

        // Sort filtered events by start time
        Collections.sort(filteredEvents, (event1, event2) -> {
            if (event1.getStart() == null && event2.getStart() == null) return 0;
            if (event1.getStart() == null) return 1;
            if (event2.getStart() == null) return -1;
            return event1.getStart().compareTo(event2.getStart());
        });

        runOnUiThread(() -> {
            if (!filteredEvents.isEmpty()) {
                adapter.updateData(filteredEvents);
                tvEmptyList.setVisibility(View.GONE);
                lstDailyEvents.setVisibility(View.VISIBLE);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvEmptyList.setText("No events for " + dateFormat.format(selectedDate));
                tvEmptyList.setVisibility(View.VISIBLE);
                lstDailyEvents.setVisibility(View.GONE);
            }
        });
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFormat.format(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        // Create a set of event dates
        Set<String> eventDates = new HashSet<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        Calendar eventCalendar = Calendar.getInstance();
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(selectedDate);

        // Log the total number of events being processed
        Log.d("MainActivity", "Processing " + events.size() + " events for calendar view");

        for (Event event : events) {
            if (event.getStart() != null) {
                eventCalendar.setTime(event.getStart());
                // Only add dates from the current month and year
                if (eventCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                    eventCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)) {
                    String day = dayFormat.format(eventCalendar.getTime());
                    eventDates.add(day);
                    Log.d("MainActivity", "Adding event date: " + day + " for event at " + event.getStart());
                }
            }
        }

        Log.d("MainActivity", "Final event dates set: " + eventDates);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, eventDates, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        
        // Set the selected day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        String selectedDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        calendarAdapter.setSelectedDay(selectedDay);
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
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayText));
            selectedDate = calendar.getTime();
            
            Log.d(TAG, "Selected new date: " + selectedDate);
            
            // Only update the daily events view, not the whole calendar
            updateEventsForSelectedDate(selectedDate);
            
            // Update calendar selection without refreshing events
            CalendarAdapter calendarAdapter = (CalendarAdapter) calendarRecyclerView.getAdapter();
            if (calendarAdapter != null) {
                calendarAdapter.setSelectedDay(dayText);
            }
        }
    }

    private void sortEventsByStartTime(List<Event> eventList) {
        Collections.sort(eventList, (event1, event2) -> {
            if (event1.getStart() == null && event2.getStart() == null) return 0;
            if (event1.getStart() == null) return 1;
            if (event2.getStart() == null) return -1;
            return event1.getStart().compareTo(event2.getStart());
        });
    }
}