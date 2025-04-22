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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        }

        model = Model.getInstance(context);
        currentUser = model.getCurrentUser();
        User otherUser = users.get(position);

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        tvUName.setText(otherUser.getuName());

        Button btnFollow = convertView.findViewById(R.id.btnAction);

        boolean isFollowing = otherUser.getFollowers().contains(currentUser.getId());
        boolean hasSentRequest = otherUser.getRequests().contains(currentUser.getId());

        // Set button text based on current status
        if (isFollowing) {
            btnFollow.setText("Following");
        } else if (hasSentRequest) {
            btnFollow.setText("Request Sent");
        } else {
            btnFollow.setText("Follow");
        }

        // Set button click behavior
        btnFollow.setOnClickListener(v -> {
            if (isFollowing) {
                otherUser.getFollowers().remove(currentUser.getId());
                btnFollow.setText("Follow");
            } else if (hasSentRequest) {
                otherUser.getRequests().remove(currentUser.getId());
                btnFollow.setText("Follow");
            } else {
                if (!otherUser.getPrivacy()) {
                    otherUser.getFollowers().add(currentUser.getId());
                    btnFollow.setText("Following");
                } else {
                    otherUser.getRequests().add(currentUser.getId());
                    btnFollow.setText("Request Sent");
                }
            }

            notifyDataSetChanged();
        });

        return convertView;
    }
}
