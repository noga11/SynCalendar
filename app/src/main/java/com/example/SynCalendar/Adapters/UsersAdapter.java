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
        User otherUser = users.get(position);

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
            // Check if currentUser is following the otherUser or has sent a request
            Map<String, String> followers = followers = new HashMap<>();
            followers = otherUser.getFollowers();


            Map<String, String> requests = requests = new HashMap<>();
            requests = otherUser.getRequests();

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
            Map<String, String> finalFollowers = followers;
            Map<String, String> finalRequests = requests;
            btnFollow.setOnClickListener(v -> {
                if (isFollowing) {
                    finalFollowers.remove(currentUser.getId());
                    btnFollow.setText("Follow");
                } else if (hasSentRequest) {
                    finalRequests.remove(currentUser.getId());
                    btnFollow.setText("Follow");
                } else {
                    if (!otherUser.getPrivacy()) {
                        finalFollowers.put(currentUser.getId(), currentUser.getuName());
                        btnFollow.setText("Following");
                    } else {
                        finalRequests.put(currentUser.getId(), currentUser.getuName());
                        btnFollow.setText("Request Sent");
                    }
                }

                // Notify the adapter that the data has changed
                notifyDataSetChanged();
            });
        }

        return convertView;
    }
}
