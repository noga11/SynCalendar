package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class UsersAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private Model model;
    private User currentUser;
    private String source;
    private RequestAdapter requestAdapter;
    private List<User> requestsList;

    public UsersAdapter(Context context, List<User> users, String source, RequestAdapter requestAdapter, List<User> requestsList) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
        this.source = source;
        this.requestAdapter = requestAdapter;
        this.requestsList = requestsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_following, parent, false);
            Log.d("UsersAdapter", "Inflated new view for position: " + position);
        }

        model = Model.getInstance(context);
        currentUser = model.getCurrentUser();
        final User otherUser = users.get(position);

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        if (tvUName == null) {
            Log.e("UsersAdapter", "TextView tvUName is null at position: " + position);
        } else {
            tvUName.setText(otherUser.getuName());
        }

        Button btnFollow = convertView.findViewById(R.id.btnAction);
        if (btnFollow == null) {
            Log.e("UsersAdapter", "Button btnAction is null at position: " + position);
            return convertView;
        }

        // Set initial button state based on the source
        if ("action_Followers".equals(source)) {
            btnFollow.setText("Remove");
        } else {
            boolean isFollowing = currentUser.getFollowing().containsKey(otherUser.getId());
            boolean hasSentRequest = otherUser.getRequests().containsKey(currentUser.getId());
            updateButtonState(btnFollow, isFollowing, hasSentRequest);
        }

        // Set button click behavior
        btnFollow.setOnClickListener(v -> {
            if ("action_Followers".equals(source)) {
                // Handle removing follower
                handleRemoveFollower(otherUser, position);
            } else {
                // Handle regular follow/unfollow
                handleFollowUnfollow(otherUser, btnFollow);
            }
        });

        return convertView;
    }

    private void handleRemoveFollower(User follower, int position) {
        // Remove from followers list
        currentUser.removeFollower(follower.getId());
        // Remove from their following list
        follower.removeFollowing(currentUser.getId());
        // Add to their requests
        currentUser.addPendingRequest(follower.getId(), follower.getuName());

        // Update Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Update current user
        db.collection("users")
            .document(currentUser.getId())
            .set(currentUser)
            .addOnSuccessListener(aVoid -> {
                Log.d("UsersAdapter", "Current user updated successfully");
                // Remove from followers list
                users.remove(position);
                notifyDataSetChanged();

                // Add to requests list if not already there
                if (!requestsList.contains(follower)) {
                    requestsList.add(follower);
                    requestAdapter.notifyDataSetChanged();
                }
                
                Toast.makeText(context, "Follower removed", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e("UsersAdapter", "Error updating current user", e);
                Toast.makeText(context, "Error removing follower", Toast.LENGTH_SHORT).show();
            });

        // Update follower
        db.collection("users")
            .document(follower.getId())
            .set(follower)
            .addOnFailureListener(e -> 
                Log.e("UsersAdapter", "Error updating follower", e));
    }

    private void handleFollowUnfollow(User otherUser, Button btnFollow) {
        boolean isCurrentlyFollowing = currentUser.getFollowing().containsKey(otherUser.getId());
        boolean hasCurrentRequest = otherUser.getRequests().containsKey(currentUser.getId());

        if (isCurrentlyFollowing) {
            // Unfollow
            currentUser.removeFollowing(otherUser.getId());
            otherUser.removeFollower(currentUser.getId());
            Log.d("UsersAdapter", "Unfollowed user: " + otherUser.getuName());
        } else if (hasCurrentRequest) {
            // Cancel request
            otherUser.removePendingRequest(currentUser.getId());
            Log.d("UsersAdapter", "Cancelled request to: " + otherUser.getuName());
        } else {
            // Follow or send request
            if (!otherUser.getPrivacy()) {
                // Direct follow for public accounts
                currentUser.addFollowing(otherUser.getId(), otherUser.getuName());
                otherUser.addFollower(currentUser.getId(), currentUser.getuName());
                Log.d("UsersAdapter", "Following user: " + otherUser.getuName());
            } else {
                // Send request for private accounts
                otherUser.addPendingRequest(currentUser.getId(), currentUser.getuName());
                Log.d("UsersAdapter", "Sent request to: " + otherUser.getuName());
            }
        }

        // Update button state after action
        boolean isNowFollowing = currentUser.getFollowing().containsKey(otherUser.getId());
        boolean hasNowRequest = otherUser.getRequests().containsKey(currentUser.getId());
        updateButtonState(btnFollow, isNowFollowing, hasNowRequest);

        // Update both users in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("users")
            .document(otherUser.getId())
            .set(otherUser)
            .addOnSuccessListener(aVoid -> Log.d("UsersAdapter", "Other user updated successfully"))
            .addOnFailureListener(e -> Log.e("UsersAdapter", "Error updating other user", e));

        db.collection("users")
            .document(currentUser.getId())
            .set(currentUser)
            .addOnSuccessListener(aVoid -> Log.d("UsersAdapter", "Current user updated successfully"))
            .addOnFailureListener(e -> Log.e("UsersAdapter", "Error updating current user", e));
    }

    private void updateButtonState(Button button, boolean isFollowing, boolean hasSentRequest) {
        if ("action_Followers".equals(source)) {
            button.setText("Remove");
        } else if (isFollowing) {
            button.setText("Following");
        } else if (hasSentRequest) {
            button.setText("Request Sent");
        } else {
            button.setText("Follow");
        }
        button.setEnabled(true);
        Log.d("UsersAdapter", "Button state updated - Following: " + isFollowing + ", Request Sent: " + hasSentRequest);
    }
}
