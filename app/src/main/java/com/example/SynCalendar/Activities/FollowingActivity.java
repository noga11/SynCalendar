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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private TextView tvEmptyList, tvNoRequest, tvRequestsTitle, tvFollowersTitle;
    private EditText editTextSearch;
    private UsersAdapter usersAdapter;
    private RequestAdapter requestAdapter;
    private List<User> allUsers = new ArrayList<>();
    List<User> followRequests = new ArrayList<>();
    private ActivityResultLauncher<Intent> activityStartLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        model = Model.getInstance(this);
        source = getIntent().getStringExtra("SOURCE");

        activityStartLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Handle any result if needed
            }
        );

        tvEmptyList = findViewById(R.id.tvEmptyList);
        tvNoRequest = findViewById(R.id.tvNoRequest);
        tvRequestsTitle = findViewById(R.id.tvRequestsTitle);
        tvFollowersTitle = findViewById(R.id.tvFollowersTitle);
        lstUsers = findViewById(R.id.lstUsers);
        lstFollowRequest = findViewById(R.id.lstFollowRequest);

        editTextSearch = findViewById(R.id.editTextSearch);

        // Set initial visibility
        tvEmptyList.setVisibility(View.GONE);
        tvNoRequest.setVisibility(View.GONE);
        
        if (!"action_Followers".equals(source)) {
            tvRequestsTitle.setVisibility(View.GONE);
            lstFollowRequest.setVisibility(View.GONE);
            tvNoRequest.setVisibility(View.GONE);
            tvFollowersTitle.setVisibility(View.GONE);
        }

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
        Intent intent = new Intent(FollowingActivity.this, LoginActivity.class);
        if (item.getItemId() == R.id.action_followers) {
            intent = new Intent(FollowingActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_Followers");
            activityStartLauncher.launch(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_users){
            intent = new Intent(FollowingActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_FindUser");
            activityStartLauncher.launch(intent);
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_following){
            intent = new Intent(FollowingActivity.this, FollowingActivity.class);
            intent.putExtra("SOURCE", "action_Following");
            activityStartLauncher.launch(intent);
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_profile){
            intent.putExtra("PROFILE", "action_profile");
            activityStartLauncher.launch(intent);
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_logout){
            intent.putExtra("LOGOUT", "action_logout");
            activityStartLauncher.launch(intent);
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
                            usersAdapter.notifyDataSetChanged();
                            updateEmptyState();
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FollowingActivity", "Error fetching user: ", e);
                    }
                });
            }
            updateEmptyState();
        } else if ("action_FindUser".equals(source)) {
            setTitle("Search Users");
            updateEmptyState();
        } else if ("action_Followers".equals(source)) {
            setTitle("Followers");
            tvFollowersTitle.setText("Followers");
            tvRequestsTitle.setVisibility(View.VISIBLE);
            lstFollowRequest.setVisibility(View.VISIBLE);
            tvNoRequest.setVisibility(View.VISIBLE);
            tvFollowersTitle.setVisibility(View.VISIBLE);
            
            Map<String, String> followersMap = model.getCurrentUser().getFollowers();
            allUsers.clear();
            for (Map.Entry<String, String> entry : followersMap.entrySet()) {
                model.getUserById(entry.getKey(), new OnSuccessListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            allUsers.add(user);
                            usersAdapter.notifyDataSetChanged();
                            updateEmptyState();
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FollowingActivity", "Error fetching user: ", e);
                    }
                });
            }
            
            // Handle follow requests
            followRequests.clear();
            Map<String, String> pendingRequests = model.getCurrentUser().getRequests();
            if (pendingRequests.isEmpty()) {
                tvNoRequest.setVisibility(View.VISIBLE);
                lstFollowRequest.setVisibility(View.GONE);
            } else {
                tvNoRequest.setVisibility(View.GONE);
                lstFollowRequest.setVisibility(View.VISIBLE);
                for (String requestId : pendingRequests.keySet()) {
                    model.getUserById(requestId, new OnSuccessListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            if (user != null) {
                                followRequests.add(user);
                                requestAdapter.notifyDataSetChanged();
                                updateRequestEmptyState();
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
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (allUsers.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
            lstUsers.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lstUsers.setVisibility(View.VISIBLE);
        }
    }

    private void updateRequestEmptyState() {
        if (followRequests.isEmpty()) {
            tvNoRequest.setVisibility(View.VISIBLE);
            lstFollowRequest.setVisibility(View.GONE);
        } else {
            tvNoRequest.setVisibility(View.GONE);
            lstFollowRequest.setVisibility(View.VISIBLE);
        }
    }
}