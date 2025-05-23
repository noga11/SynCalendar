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
        } else {
            // Check current relationship status
            boolean isFollowing = currentUser.getFollowing().containsKey(otherUser.getId());
            boolean hasSentRequest = otherUser.getRequests().containsKey(currentUser.getId());

            // Set button text based on current status
            updateButtonState(btnFollow, isFollowing, hasSentRequest);

            // Set button click behavior
            btnFollow.setOnClickListener(v -> {
                if (isFollowing) {
                    // Unfollow
                    currentUser.removeFollowing(otherUser.getId());
                    otherUser.removeFollower(currentUser.getId());
                    updateButtonState(btnFollow, false, false);
                } else if (hasSentRequest) {
                    // Cancel request
                    otherUser.removePendingRequest(currentUser.getId());
                    updateButtonState(btnFollow, false, false);
                } else {
                    // Follow or send request
                    if (!otherUser.getPrivacy()) {
                        // Direct follow for public accounts
                        currentUser.addFollowing(otherUser.getId(), otherUser.getuName());
                        otherUser.addFollower(currentUser.getId(), currentUser.getuName());
                        updateButtonState(btnFollow, true, false);
                    } else {
                        // Send request for private accounts
                        otherUser.addPendingRequest(currentUser.getId(), currentUser.getuName());
                        updateButtonState(btnFollow, false, true);
                    }
                }

                // Update both users in Firestore
                DocumentReference otherUserRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(otherUser.getId());
                otherUserRef.set(otherUser);

                DocumentReference currentUserRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.getId());
                currentUserRef.set(currentUser);
            });
        }

        return convertView;
    }

    private void updateButtonState(Button button, boolean isFollowing, boolean hasSentRequest) {
        if (isFollowing) {
            button.setText("Following");
        } else if (hasSentRequest) {
            button.setText("Request Sent");
        } else {
            button.setText("Follow");
        }
    }
}
