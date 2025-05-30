package com.example.SynCalendar.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SynCalendar.Adapters.AllEventsAdapter;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.Event;
import com.example.SynCalendar.User;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public class AllEventsActivity extends AppCompatActivity implements View.OnLongClickListener {

    private Model model;
    private User currentUser;
    private RecyclerView lstAllEvents;
    private TextView tvEmptyList;
    private ActivityResultLauncher<Intent> activityStartLauncher;
    private ArrayList<Event> events, filterdEvents;
    private ArrayAdapter<String> spinnerAdapter;
    private AutoCompleteTextView spinnerGroup;
    private ArrayList<String> groups;
    private AllEventsAdapter adapter;
    private SearchView searchView;

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
        searchView = findViewById(R.id.searchView);

        activityStartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshEventsList(); // Refresh events list after any successful result
                    }
                }
        );

        currentUser = model.getCurrentUser();
        events = new ArrayList<>();
        filterdEvents = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lstAllEvents.setLayoutManager(layoutManager);
        
        // Add divider decoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lstAllEvents.getContext(), layoutManager.getOrientation());
        lstAllEvents.addItemDecoration(dividerItemDecoration);
        
        adapter = new AllEventsAdapter(this, filterdEvents);
        adapter.setOnItemClickListener((event, position) -> {
            Intent intent = new Intent(AllEventsActivity.this, NewEventActivity.class);
            intent.putExtra("Event", event.getId());
            intent.putExtra("isEditing", true);
            activityStartLauncher.launch(intent);
        });
        
        lstAllEvents.setAdapter(adapter);

        model.getEventsByUserId(currentUser.getId(), new com.google.android.gms.tasks.OnSuccessListener<java.util.List<Event>>() {
            @Override
            public void onSuccess(java.util.List<Event> userEvents) {
                events.clear();
                events.addAll(userEvents);
                sortEventsByStartTime(events);  // Sort the events
                filterdEvents.clear();
                filterdEvents.addAll(events);
                adapter.notifyDataSetChanged();
                if (userEvents.isEmpty()) {
                    tvEmptyList.setVisibility(View.VISIBLE);
                    lstAllEvents.setVisibility(View.GONE);
                } else {
                    tvEmptyList.setVisibility(View.GONE);
                    lstAllEvents.setVisibility(View.VISIBLE);
                }
            }
        }, new com.google.android.gms.tasks.OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                Toast.makeText(AllEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });

        // Attach swipe-to-delete functionality directly in the activity
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Drag & drop not needed
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Event deletedEvent = filterdEvents.get(position);
                String deletedEventGroup = deletedEvent.getGroup();

                // Remove from RecyclerView
                events.remove(deletedEvent);
                filterdEvents.remove(position);
                adapter.notifyItemRemoved(position);

                // Show Snackbar with Undo option
                Snackbar snackbar = Snackbar.make(viewHolder.itemView, "Event deleted", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", v -> {
                    events.add(deletedEvent);
                    filterdEvents.add(position, deletedEvent);
                    adapter.notifyItemInserted(position);
                });

                // Delete from database only if NOT undone
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            model.deleteEvent(deletedEvent); // Use the deleteEvent method
                            
                            // Check if any events still use this group
                            boolean groupStillInUse = false;
                            for (Event e : events) {
                                if (e.getGroup().equals(deletedEventGroup)) {
                                    groupStillInUse = true;
                                    break;
                                }
                            }
                            
                            // If group is no longer in use, refresh groups
                            if (!groupStillInUse && !deletedEventGroup.equals("All")) {
                                refreshGroups();
                            }
                        }
                    }
                });

                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(lstAllEvents);

        groups = model.getGroups();

        // Remove 'Add New Group' option
        groups.remove("Add New Group");

        // Setup the adapter
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        spinnerGroup.setAdapter(spinnerAdapter);

        // Set the default selected group to 'All' in the spinnerGroup
        spinnerGroup.setText("All", false);

        spinnerGroup.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGroup = groups.get(position);

            filterdEvents.clear();
            if ("All".equals(selectedGroup)) {
                filterdEvents.addAll(events);
            } else {
                for (Event event : events) {
                    if (event.getGroup().equals(selectedGroup)) {
                        filterdEvents.add(event);
                    }
                }
            }
            adapter.notifyDataSetChanged();
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
                finish();
                return true;
            }
            return false;
        });

        setupGroupSpinner();

        // Setup SearchView
        setupSearchView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(AllEventsActivity.this, LoginActivity.class);
        if (item.getItemId() == R.id.action_followers) {
            intent = new Intent(AllEventsActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_Followers");
            activityStartLauncher.launch(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_users) {
            intent = new Intent(AllEventsActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_FindUser");
            activityStartLauncher.launch(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_following) {
            intent = new Intent(AllEventsActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_Following");
            activityStartLauncher.launch(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
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

    private void addNewGroup(String newGroup) {
        // Add the new group using the Model
        model.addGroup(newGroup);
        
        // Refresh groups from model
        refreshGroups();
        
        // Update adapter and spinner
        spinnerAdapter.notifyDataSetChanged();
        
        // Set the new group as selected
        spinnerGroup.setText(newGroup, false);
        
        // Filter events to show only the new group's events
        filterEventsByGroup(newGroup);
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

    private void setupGroupSpinner() {
        refreshGroups();
        
        // Set up listener for group changes in Firestore
        model.getEventsByUserId(currentUser.getId(), events -> {
            refreshGroups(); // Refresh groups whenever events change
        }, e -> Log.e("AllEventsActivity", "Error listening for event changes", e));
    }

    private void refreshGroups() {
        // Get groups from model with callback
        model.getGroups(new Model.GroupsCallback() {
            @Override
            public void onGroupsLoaded(ArrayList<String> loadedGroups) {
                runOnUiThread(() -> {
                    groups.clear();
                    groups.addAll(loadedGroups);
                    
                    // Remove 'Add New Group' option if it exists
                    if (groups.contains("Add New Group")) {
                        groups.remove("Add New Group");
                    }

                    // If adapter doesn't exist, create it
                    if (spinnerAdapter == null) {
                        spinnerAdapter = new ArrayAdapter<>(AllEventsActivity.this, 
                            android.R.layout.simple_dropdown_item_1line, groups);
                        spinnerGroup.setAdapter(spinnerAdapter);
                        
                        // Set dropdown width to match parent
                        spinnerGroup.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                        
                        // Enable dropdown on click
                        spinnerGroup.setOnClickListener(v -> spinnerGroup.showDropDown());

                        spinnerGroup.setOnItemClickListener((parent, view, position, id) -> {
                            String selectedGroup = groups.get(position);
                            spinnerGroup.setText(selectedGroup, false);
                            filterEventsByGroup(selectedGroup);
                        });
                    } else {
                        // Just notify the adapter of the data change
                        spinnerAdapter.notifyDataSetChanged();
                    }

                    // Maintain current selection if possible, otherwise default to "All"
                    String currentSelection = spinnerGroup.getText().toString();
                    if (!groups.contains(currentSelection)) {
                        spinnerGroup.setText("All", false);
                        filterEventsByGroup("All");
                    }
                });
            }
        });
    }

    private void filterEventsByGroup(String selectedGroup) {
        // Clear search query when changing groups, but prevent the listener from triggering
        if (searchView != null) {
            searchView.setOnQueryTextListener(null);  // Remove listener temporarily
            searchView.setQuery("", false);
            searchView.clearFocus();
            setupSearchView();  // Restore the listener
        }

        filterdEvents.clear();
        if ("All".equals(selectedGroup)) {
            filterdEvents.addAll(events);
        } else {
            for (Event event : events) {
                if (event.getGroup() != null && event.getGroup().equals(selectedGroup)) {
                    filterdEvents.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() != R.id.spinnerGroup) {
            return false;
        }

        String selectedGroup = spinnerGroup.getText().toString();
        // Don't allow deletion of special groups
        if ("All".equals(selectedGroup) || "Add New Group".equals(selectedGroup)) {
            Toast.makeText(this, "Cannot delete this group", Toast.LENGTH_SHORT).show();
            return true;
        }

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
            // Use model to delete group
            model.deleteGroup(selectedGroup);
            
            // Refresh the groups spinner
            refreshGroups();
            
            // Set spinner back to "All"
            spinnerGroup.setText("All", false);
            filterEventsByGroup("All");

            groupDialog.dismiss();
        });

        return true;
    }

    private void sortEventsByStartTime(List<Event> eventList) {
        Collections.sort(eventList, (event1, event2) -> {
            if (event1.getStart() == null && event2.getStart() == null) return 0;
            if (event1.getStart() == null) return 1;
            if (event2.getStart() == null) return -1;
            return event1.getStart().compareTo(event2.getStart());
        });
    }

    private void refreshEventsList() {
        model.getEventsByUserId(currentUser.getId(), userEvents -> {
            runOnUiThread(() -> {
                events.clear();
                events.addAll(userEvents);
                sortEventsByStartTime(events);
                
                // Maintain current group filter
                String currentGroup = spinnerGroup.getText().toString();
                filterdEvents.clear();
                if ("All".equals(currentGroup)) {
                    filterdEvents.addAll(events);
                } else {
                    for (Event event : events) {
                        if (event.getGroup().equals(currentGroup)) {
                            filterdEvents.add(event);
                        }
                    }
                }
                
                adapter.notifyDataSetChanged();
                updateEmptyState();
                
                // Refresh groups to capture any new groups added
                refreshGroups();
            });
        }, e -> {
            runOnUiThread(() -> {
                Toast.makeText(AllEventsActivity.this, "Failed to refresh events", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateEmptyState() {
        if (filterdEvents.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
            lstAllEvents.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lstAllEvents.setVisibility(View.VISIBLE);
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });
    }

    private void filterEvents(String query) {
        ArrayList<Event> currentGroupEvents = new ArrayList<>();
        String currentGroup = spinnerGroup.getText().toString();

        // First, filter by group
        if ("All".equals(currentGroup)) {
            currentGroupEvents.addAll(events);
        } else {
            for (Event event : events) {
                if (event.getGroup() != null && event.getGroup().equals(currentGroup)) {
                    currentGroupEvents.add(event);
                }
            }
        }

        // If query is empty, show all events for current group
        if (query == null || query.isEmpty()) {
            filterdEvents.clear();
            filterdEvents.addAll(currentGroupEvents);
            adapter.notifyDataSetChanged();
            updateEmptyState();
            return;
        }

        // Then filter by search query
        filterdEvents.clear();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (Event event : currentGroupEvents) {
            boolean matchesTitle = event.getTitle() != null && 
                                 event.getTitle().toLowerCase().contains(lowerCaseQuery);
            boolean matchesDetails = event.getDetails() != null && 
                                   event.getDetails().toLowerCase().contains(lowerCaseQuery);
            
            if (matchesTitle || matchesDetails) {
                filterdEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
}
