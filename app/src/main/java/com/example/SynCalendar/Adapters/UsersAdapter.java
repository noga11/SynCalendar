package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SynCalendar.Model;
import com.example.SynCalendar.PhotoHelper;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

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
        ShapeableImageView imageView = convertView.findViewById(R.id.imageView);

        if (tvUName == null) {
            Log.e("UsersAdapter", "TextView tvUName is null at position: " + position);
        } else {
            tvUName.setText(otherUser.getuName());
        }

        // Set profile picture
        if (imageView != null) {
            String profilePicString = otherUser.getProfilePicString();
            if (profilePicString != null) {
                Bitmap profilePic = PhotoHelper.stringToBitmap(profilePicString);
                if (profilePic != null) {
                    imageView.setImageBitmap(profilePic);
                } else {
                    imageView.setImageResource(R.drawable.images); // Set default image
                }
            } else {
                imageView.setImageResource(R.drawable.images); // Set default image
            }
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
        model.removeFollower(follower, new Model.FollowRequestCallback() {
            @Override
            public void onSuccess() {
                // Remove from followers list
                users.remove(position);
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("UsersAdapter", "Error removing follower", e);
                Toast.makeText(context, "Error removing follower", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFollowUnfollow(User otherUser, Button btnFollow) {
        model.handleFollowUnfollow(otherUser, new Model.FollowRequestCallback() {
            @Override
            public void onSuccess() {
                // Update button state after action
                boolean isNowFollowing = currentUser.getFollowing().containsKey(otherUser.getId());
                boolean hasNowRequest = otherUser.getRequests().containsKey(currentUser.getId());
                updateButtonState(btnFollow, isNowFollowing, hasNowRequest);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("UsersAdapter", "Error in follow/unfollow operation", e);
                Toast.makeText(context, "Error updating follow status", Toast.LENGTH_SHORT).show();
            }
        });
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
