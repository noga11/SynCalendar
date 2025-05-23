package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private Model model;
    private User currentUser;

    public UsersAdapter(Context context, List<User> users) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
    }

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

        // Set button click behavior
        btnFollow.setOnClickListener(v -> {
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
        });

        // Set initial button state
        boolean isFollowing = currentUser.getFollowing().containsKey(otherUser.getId());
        boolean hasSentRequest = otherUser.getRequests().containsKey(currentUser.getId());
        updateButtonState(btnFollow, isFollowing, hasSentRequest);

        return convertView;
    }

    private void updateButtonState(Button button, boolean isFollowing, boolean hasSentRequest) {
        if (isFollowing) {
            button.setText("Following");
            button.setEnabled(true);
        } else if (hasSentRequest) {
            button.setText("Request Sent");
            button.setEnabled(true);
        } else {
            button.setText("Follow");
            button.setEnabled(true);
        }
        Log.d("UsersAdapter", "Button state updated - Following: " + isFollowing + ", Request Sent: " + hasSentRequest);
    }
}
