package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;

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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        }

        model = Model.getInstance(context);
        currentUser = model.getCurrentUser();
        User otherUser = users.get(position);

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        tvUName.setText(otherUser.getuName());

        Button btnFollow = convertView.findViewById(R.id.btnAction);

        // Check if currentUser is following the otherUser or has sent a request
        Map<String, String> followers = otherUser.getFollowers();
        Map<String, String> requests = otherUser.getRequests();

        boolean isFollowing = followers.containsKey(currentUser.getId());
        boolean hasSentRequest = requests.containsKey(currentUser.getId());

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
                followers.remove(currentUser.getId());
                btnFollow.setText("Follow");
            } else if (hasSentRequest) {
                requests.remove(currentUser.getId());
                btnFollow.setText("Follow");
            } else {
                if (!otherUser.getPrivacy()) {
                    followers.put(currentUser.getId(), currentUser.getuName());
                    btnFollow.setText("Following");
                } else {
                    requests.put(currentUser.getId(), currentUser.getuName());
                    btnFollow.setText("Request Sent");
                }
            }

            // Notify the adapter that the data has changed
            notifyDataSetChanged();
        });

        return convertView;
    }
}
