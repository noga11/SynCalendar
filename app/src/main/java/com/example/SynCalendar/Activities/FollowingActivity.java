package com.example.SynCalendar.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FollowingActivity extends AppCompatActivity {

    private Model model;
    private ListView lstUsers, lstFollowRequest;
    private String source;
    private TextView tvEmptyList, tvFollowRequest;
    private EditText editTextSearch;
    private UsersAdapter usersAdapter;
    private RequestAdapter requestAdapter;
    private List<User> allUsers = new ArrayList<>();
    List<User> followRequests = new ArrayList<>();

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

        // Simulate fetching users
        fetchAllUsers();

        usersAdapter = new UsersAdapter(this, new ArrayList<>(allUsers));
        lstUsers.setAdapter(usersAdapter);
        requestAdapter = new RequestAdapter(this, followRequests);
        lstFollowRequest.setAdapter(requestAdapter);

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

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        if (query.isEmpty()) {
            // If the query is empty, reset to the original list
            usersAdapter.clear();
            usersAdapter.addAll(allUsers);
            usersAdapter.notifyDataSetChanged();
            return;
        }

        model.searchUsers(query, new OnSuccessListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users.isEmpty()) {
                    Log.d("FollowingActivity", "No users found for query: " + query);
                }
                usersAdapter.clear();
                usersAdapter.addAll(users);
                usersAdapter.notifyDataSetChanged();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the error, e.g., show a toast or log the error
                Log.e("FollowingActivity", "Error fetching users: ", e);
            }
        });
    }

    // Placeholder method to simulate fetching users
    private void fetchAllUsers() {
        if ("action_Following".equals(source)) {
            setTitle("Following");
            Map<String, String> followingMap = model.getCurrentUser().getFollowing();
            allUsers.clear();
            for (Map.Entry<String, String> entry : followingMap.entrySet()) {
                model.getUserById(entry.getKey(), new OnSuccessListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            allUsers.add(user);
                            usersAdapter.notifyDataSetChanged(); // Update adapter after adding each user
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FollowingActivity", "Error fetching user: ", e);
                    }
                });
            }
            if (allUsers.isEmpty()) {
                tvEmptyList.setText("You don't follow anyone");
                lstUsers.setEmptyView(tvEmptyList);
            }
        } else if ("action_FindUser".equals(source)) {
            setTitle("Search Users");
            if (allUsers.isEmpty()) {
                tvEmptyList.setText("There aren't any other users");
                lstUsers.setEmptyView(tvEmptyList);
            }
        } else if ("action_Followers".equals(source)) {
            setTitle("Followers");
            Map<String, String> followersMap = model.getCurrentUser().getFollowers();
            allUsers.clear();
            for (Map.Entry<String, String> entry : followersMap.entrySet()) {
                model.getUserById(entry.getKey(), new OnSuccessListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            allUsers.add(user);
                            usersAdapter.notifyDataSetChanged(); // Update adapter after adding each user
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FollowingActivity", "Error fetching user: ", e);
                    }
                });
            }
            if (allUsers.isEmpty()) {
                tvEmptyList.setText("You don't have any Following requests");
                lstUsers.setEmptyView(tvEmptyList);
            }
            // --- Fill followRequests with pending requests ---
            followRequests.clear();
            Map<String, String> pendingRequests = model.getCurrentUser().getRequests();
            if (pendingRequests.isEmpty()) {
                tvFollowRequest.setVisibility(View.GONE);
                lstFollowRequest.setVisibility(View.GONE);
            } else {
                tvFollowRequest.setVisibility(View.VISIBLE);
                lstFollowRequest.setVisibility(View.VISIBLE);
                for (String requestId : pendingRequests.keySet()) {
                    model.getUserById(requestId, new OnSuccessListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            if (user != null) {
                                followRequests.add(user);
                                requestAdapter.notifyDataSetChanged();
                            }
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FollowingActivity", "Error fetching request user: ", e);
                        }
                    });
                }
            }
        }
    }

}