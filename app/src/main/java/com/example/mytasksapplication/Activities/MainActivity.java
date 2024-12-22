package com.example.mytasksapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private Model model;
    private ListView lstDailyTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Today's Tasks");

        // Initialize the NavigationBarView (formerly BottomNavigationView)
        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the listener for item selection using the new method
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Use Intent to switch activities based on selected item
                switch (item.getItemId()) {
                    case R.id.nav_Today:
                        // Navigate to MainActivity (already in the correct activity)
                        // No need to start MainActivity again
                        return true;
                    case R.id.nav_Add:
                        // Navigate to NewTaskActivity (Add Task)
                        startActivity(new Intent(MainActivity.this, NewTaskActivity.class));
                        return true;
                    case R.id.nav_Tasks:
                        // Navigate to AllTasksActivity (All Tasks)
                        startActivity(new Intent(MainActivity.this, AllTasksActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    // This method will be called to inflate the options menu in the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle action bar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_follow_request:
                // Handle the settings item click
                // For example, navigate to SettingsActivity (if it exists)
                startActivity(new Intent(MainActivity.this, FollowingActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
