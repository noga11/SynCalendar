package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.User;

import java.util.List;

public class UsersAdapter   extends ArrayAdapter<User> {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        }

        model = Model.getInstance(context);
        currentUser = model.getCurrentUser();
        User otherUser = users.get(position);

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        tvUName.setText(otherUser.getuName());

        Button taskButton = convertView.findViewById(R.id.btnAction);

        // Get the follow status of the other user
        User.FollowStatus followStatus = currentUser.getUserFollowStatus(otherUser.getId());

        // Set button text based on follow status
        if (followStatus == User.FollowStatus.FOLLOW) {
            taskButton.setText("Following");
        } else if (followStatus == User.FollowStatus.REQUEST) {
            taskButton.setText("Request Sent");
        } else {
            taskButton.setText("Follow");
        }

        // Set button click behavior
        taskButton.setOnClickListener(v -> {
            if (followStatus == User.FollowStatus.FOLLOW) {
                // Unfollow logic
                currentUser.setUserFollowStatus(otherUser.getId(), User.FollowStatus.UNFOLLOW);
                taskButton.setText("Follow");
            } else if (followStatus == User.FollowStatus.REQUEST) {
                // Cancel request
                currentUser.setUserFollowStatus(otherUser.getId(), User.FollowStatus.UNFOLLOW);
                taskButton.setText("Follow");
            } else {
                // Follow logic
                if (!otherUser.getPrivacy()) {
                    currentUser.setUserFollowStatus(otherUser.getId(), User.FollowStatus.FOLLOW);
                    otherUser.addFollower(currentUser.getId());
                    taskButton.setText("Following");
                } else {
                    currentUser.setUserFollowStatus(otherUser.getId(), User.FollowStatus.REQUEST);
                    taskButton.setText("Request Sent");
                }
            }
            notifyDataSetChanged();
        });

        return convertView;
    }
}