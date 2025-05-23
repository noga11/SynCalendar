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
import java.util.HashMap;
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

        // Initialize adapters first
        allUsers = new ArrayList<>();
        followRequests = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, allUsers);
        lstUsers.setAdapter(usersAdapter);
        requestAdapter = new RequestAdapter(this, followRequests);
        lstFollowRequest.setAdapter(requestAdapter);

        // Set initial visibility
        tvEmptyList.setVisibility(View.GONE);
        tvNoRequest.setVisibility(View.GONE);
        
        if (!"action_Followers".equals(source)) {
            tvRequestsTitle.setVisibility(View.GONE);
            lstFollowRequest.setVisibility(View.GONE);
            tvNoRequest.setVisibility(View.GONE);
            tvFollowersTitle.setVisibility(View.GONE);
        }

        // Now fetch users after adapter initialization
        fetchAllUsers();

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
        if ("action_Following".equals(source)) {
            // For Following mode, only filter the existing following list
            List<User> filteredUsers = new ArrayList<>();
            String lowercaseQuery = query.toLowerCase().trim();
            
            for (User user : allUsers) {
                if (user.getuName().toLowerCase().contains(lowercaseQuery)) {
                    filteredUsers.add(user);
                }
            }
            
            usersAdapter.clear();
            usersAdapter.addAll(filteredUsers);
            usersAdapter.notifyDataSetChanged();
            
            if (filteredUsers.isEmpty()) {
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
                if (!query.isEmpty()) {
                    tvEmptyList.setText("No following users found matching '" + query + "'");
                } else {
                    tvEmptyList.setText("You are not following anyone");
                }
            } else {
                tvEmptyList.setVisibility(View.GONE);
                lstUsers.setVisibility(View.VISIBLE);
            }
        } else {
            // For other modes, use the normal search
            model.searchUsers(query, users -> {
                allUsers.clear();
                allUsers.addAll(users);
                usersAdapter.clear();
                usersAdapter.addAll(users);
                usersAdapter.notifyDataSetChanged();
                
                if (users.isEmpty()) {
                    tvEmptyList.setVisibility(View.VISIBLE);
                    lstUsers.setVisibility(View.GONE);
                    if (!query.isEmpty()) {
                        tvEmptyList.setText("No users found matching '" + query + "'");
                    } else {
                        tvEmptyList.setText("No users found");
                    }
                } else {
                    tvEmptyList.setVisibility(View.GONE);
                    lstUsers.setVisibility(View.VISIBLE);
                }
            }, e -> {
                Log.e("FollowingActivity", "Error searching users: ", e);
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
                tvEmptyList.setText("Error searching users");
            });
        }
    }

    private void fetchAllUsers() {
        if ("action_Following".equals(source)) {
            setTitle("Following");
            allUsers.clear();
            usersAdapter.clear();
            
            User currentUser = model.getCurrentUser();
            if (currentUser == null) {
                Log.e("FollowingActivity", "Current user is null");
                tvEmptyList.setText("Error loading user data");
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
                return;
            }

            Map<String, String> followingMap = currentUser.getFollowing();
            if (followingMap == null) {
                followingMap = new HashMap<>();
            }
            
            Log.d("FollowingActivity", "Current user: " + currentUser.getuName());
            Log.d("FollowingActivity", "Following map: " + followingMap.toString());
            Log.d("FollowingActivity", "Following count: " + followingMap.size());
            
            if (followingMap.isEmpty()) {
                Log.d("FollowingActivity", "Following map is empty");
                tvEmptyList.setText("You are not following anyone");
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
                return;
            }

            // Show loading state
            tvEmptyList.setText("Loading...");
            tvEmptyList.setVisibility(View.VISIBLE);
            lstUsers.setVisibility(View.GONE);

            // Create a final copy of the map for the async operations
            final Map<String, String> finalFollowingMap = followingMap;
            
            // Keep track of loaded users
            final int[] loadedCount = {0};
            final int totalToLoad = followingMap.size();

            for (Map.Entry<String, String> entry : followingMap.entrySet()) {
                String userId = entry.getKey();
                Log.d("FollowingActivity", "Loading user with ID: " + userId);
                
                model.getUserById(userId, user -> {
                    loadedCount[0]++;
                    
                    if (user != null) {
                        // Ensure the user is still in our following list
                        if (finalFollowingMap.containsKey(user.getId())) {
                            Log.d("FollowingActivity", "Successfully loaded following user: " + user.getuName());
                            allUsers.add(user);
                            
                            // Update UI immediately when a user is loaded
                            runOnUiThread(() -> {
                                usersAdapter.notifyDataSetChanged();
                                if (lstUsers.getVisibility() != View.VISIBLE) {
                                    tvEmptyList.setVisibility(View.GONE);
                                    lstUsers.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else {
                        Log.e("FollowingActivity", "Failed to load user with ID: " + userId);
                    }

                    // Final check after all users are loaded
                    if (loadedCount[0] >= totalToLoad) {
                        runOnUiThread(() -> {
                            if (!allUsers.isEmpty()) {
                                usersAdapter.notifyDataSetChanged();
                                tvEmptyList.setVisibility(View.GONE);
                                lstUsers.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptyList.setText("You are not following anyone");
                                tvEmptyList.setVisibility(View.VISIBLE);
                                lstUsers.setVisibility(View.GONE);
                            }
                        });
                    }
                }, e -> {
                    Log.e("FollowingActivity", "Error loading user with ID: " + userId, e);
                    loadedCount[0]++;
                    
                    // Final check after all users are loaded
                    if (loadedCount[0] >= totalToLoad) {
                        runOnUiThread(() -> {
                            if (!allUsers.isEmpty()) {
                                usersAdapter.notifyDataSetChanged();
                                tvEmptyList.setVisibility(View.GONE);
                                lstUsers.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptyList.setText("You are not following anyone");
                                tvEmptyList.setVisibility(View.VISIBLE);
                                lstUsers.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        } else if ("action_FindUser".equals(source)) {
            setTitle("Search Users");
            filterEvents("");
        } else if ("action_Followers".equals(source)) {
            setTitle("Followers");
            tvFollowersTitle.setText("Followers");
            tvRequestsTitle.setVisibility(View.VISIBLE);
            lstFollowRequest.setVisibility(View.VISIBLE);
            tvNoRequest.setVisibility(View.VISIBLE);
            tvFollowersTitle.setVisibility(View.VISIBLE);
            
            Map<String, String> followersMap = model.getCurrentUser().getFollowers();
            allUsers.clear();
            usersAdapter.clear();
            
            if (followersMap.isEmpty()) {
                tvEmptyList.setText("No followers yet");
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
            } else {
                for (Map.Entry<String, String> entry : followersMap.entrySet()) {
                    model.getUserById(entry.getKey(), new OnSuccessListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            if (user != null) {
                                allUsers.add(user);
                                usersAdapter.clear();
                                usersAdapter.addAll(allUsers);
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
        }
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (allUsers.isEmpty()) {
            if ("action_Following".equals(source)) {
                tvEmptyList.setText("You are not following anyone");
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
            } else if ("action_Followers".equals(source)) {
                tvEmptyList.setText("No followers yet");
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
            } else {
                tvEmptyList.setText("No users found");
                tvEmptyList.setVisibility(View.VISIBLE);
                lstUsers.setVisibility(View.GONE);
            }
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lstUsers.setVisibility(View.VISIBLE);
            usersAdapter.clear();
            usersAdapter.addAll(allUsers);
            usersAdapter.notifyDataSetChanged();
        }
    }

    private void updateRequestEmptyState() {
        if (followRequests.isEmpty()) {
            tvNoRequest.setVisibility(View.VISIBLE);
            lstFollowRequest.setVisibility(View.GONE);
        } else {
            tvNoRequest.setVisibility(View.GONE);
            lstFollowRequest.setVisibility(View.VISIBLE);
            requestAdapter.notifyDataSetChanged();
        }
    }
}