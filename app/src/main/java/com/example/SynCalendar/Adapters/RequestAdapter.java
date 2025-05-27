package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SynCalendar.Activities.FollowingActivity;
import com.example.SynCalendar.Model;
import com.example.SynCalendar.PhotoHelper;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class RequestAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private Model model;
    private User currentUser;
    private ArrayAdapter<User> followersAdapter;
    private List<User> followersList;

    public RequestAdapter(Context context, List<User> users, ArrayAdapter<User> followersAdapter, List<User> followersList) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
        this.followersAdapter = followersAdapter;
        this.followersList = followersList;
    }

    public void setFollowersAdapter(ArrayAdapter<User> adapter) {
        this.followersAdapter = adapter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        }

        model = Model.getInstance(context);
        currentUser = model.getCurrentUser();
        User requester = users.get(position); // This user sent the request

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        ShapeableImageView imageView = convertView.findViewById(R.id.imageView);
        Button btnAccept = convertView.findViewById(R.id.btnAccept);
        Button btnReject = convertView.findViewById(R.id.btnReject);

        tvUName.setText(requester.getuName());

        // Set profile picture
        if (imageView != null) {
            String profilePicString = requester.getProfilePicString();
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

        // Handle accept button click
        btnAccept.setOnClickListener(v -> {
            model.acceptFollowRequest(requester, new Model.FollowRequestCallback() {
                @Override
                public void onSuccess() {
                    // Remove from requests list
                    users.remove(position);
                    notifyDataSetChanged();

                    // Refresh the followers list by calling fetchAllUsers on the activity
                    if (context instanceof FollowingActivity) {
                        ((FollowingActivity) context).fetchAllUsers();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Error accepting request", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Handle reject button click
        btnReject.setOnClickListener(v -> {
            model.rejectFollowRequest(requester, new Model.FollowRequestCallback() {
                @Override
                public void onSuccess() {
                    // Remove from the adapter's list
                    users.remove(position);
                    notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Error rejecting request", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return convertView;
    }
}
