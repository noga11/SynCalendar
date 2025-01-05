package com.example.mytasksapplication.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.Adapters.DailyTasksAdapter;
import com.example.mytasksapplication.Adapters.UsersAdapter;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.example.mytasksapplication.User;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class FollowingActivity extends AppCompatActivity {

    private Model model;
    private ListView lstUsers;
    private String source;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        String source = getIntent().getStringExtra("source");

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Following");

        // Initialize the ListView
        lstUsers = findViewById(R.id.lstUsers);
        List<User> users = new ArrayList<>();

        // Set the custom adapter to the ListView
        UsersAdapter adapter = new UsersAdapter(this, users, source);
        lstUsers.setAdapter(adapter);

        // Initialize the NavigationBarView (formerly BottomNavigationView)
        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the listener for item selection
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_Add) {
                    startActivity(new Intent(FollowingActivity.this, NewTaskActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_Today) {
                    startActivity(new Intent(FollowingActivity.this, MainActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_Tasks) {
                    startActivity(new Intent(FollowingActivity.this, AllTasksActivity.class));
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
        if (item.getItemId() == R.id.action_follow_request) {
            startActivity(new Intent(FollowingActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_users){
            startActivity(new Intent(FollowingActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_following){
            startActivity(new Intent(FollowingActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_profile){
            startActivity(new Intent(FollowingActivity.this, LoginActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_logout){
            startActivity(new Intent(FollowingActivity.this, LoginActivity.class));
            return true;
        }
        return false;
    }
}