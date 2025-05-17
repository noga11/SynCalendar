package com.example.SynCalendar.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.SynCalendar.Adapters.RequestAdapter;
import com.example.SynCalendar.Adapters.UsersAdapter;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class FollowingActivity extends AppCompatActivity {

    private Model model;
    private ListView lstUsers, lstFollowRequest;
    private String source;
    private TextView tvEmptyList, tvFollowRequest;
    private EditText editTextSearch;
    private UsersAdapter usersAdapter;
    private RequestAdapter requestAdapter;
    private List<User> allUsers = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        model = Model.getInstance(this);
        source = getIntent().getStringExtra("SOURCE");

        tvEmptyList = findViewById(R.id.tvEmptyList);
        tvFollowRequest = findViewById(R.id.tvFollowRequest);
        lstUsers = findViewById(R.id.lstUsers);
        lstFollowRequest = findViewById(R.id.lstFollowRequest);

        editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        List<User> users = new ArrayList<>();
        List<User> followRequests = new ArrayList<>();

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if ("action_Following".equals(source)) {
            setTitle("Following");
            if (users.isEmpty()) {
                tvEmptyList.setText("You dont follow anyone");
                lstUsers.setEmptyView(tvEmptyList);
            }

        } else if ("action_FindUser".equals(source)) {
            setTitle("Search Users");
            if (users.isEmpty()) {
                tvEmptyList.setText("There aren't any other users");
                lstUsers.setEmptyView(tvEmptyList);
            }

        } else if ("action_Followers".equals(source)) {
            setTitle("Followers");
            if (users.isEmpty()) {
                tvEmptyList.setText("You dont have any Following requests");
                lstUsers.setEmptyView(tvEmptyList);
            }
            if(model.getCurrentUser().getRequests().isEmpty()){
                tvFollowRequest.setVisibility(View.GONE);
                lstFollowRequest.setVisibility(View.GONE);
            }else{
                tvFollowRequest.setVisibility(View.VISIBLE);
                lstFollowRequest.setVisibility(View.VISIBLE);
            }
        }

        usersAdapter = new UsersAdapter(this, users);
        requestAdapter = new RequestAdapter(this, followRequests);
        lstUsers.setAdapter(usersAdapter);
        lstFollowRequest.setAdapter(requestAdapter);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_Add) {
                    startActivity(new Intent(FollowingActivity.this, NewEventActivity.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_Today) {
                    startActivity(new Intent(FollowingActivity.this, MainActivity.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_Events) {
                    startActivity(new Intent(FollowingActivity.this, AllEventsActivity.class));
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
        if (item.getItemId() == R.id.action_followers) {
            intent.putExtra("FOLLOWERS", "action_Followers");
            startActivity(new Intent(FollowingActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_users){
            startActivity(new Intent(FollowingActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_following){
            startActivity(new Intent(FollowingActivity.this, FollowingActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_profile){
            startActivity(new Intent(FollowingActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_logout){
            startActivity(new Intent(FollowingActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void filterEvents(String query) {
        List<User> filteredUsers = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getuName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        usersAdapter.clear();
        usersAdapter.addAll(filteredUsers);
        usersAdapter.notifyDataSetChanged();
    }

}