package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.User;

import java.util.List;

public class UsersAdapter   extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private String source;

    public UsersAdapter(Context context, List<User> users, String source) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
        this.source = source;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        }

        User user = users.get(position);

        // Set up the title text
        TextView tvUName = convertView.findViewById(R.id.tvUName);
        tvUName.setText(user.getuName());

        Button taskButton = convertView.findViewById(R.id.btnAction);

        // Set button behavior based on the source
        if ("action_Following".equals(source)) {
            taskButton.setText("Following");
            /*taskButton.setOnClickListener(v -> {
                // Handle button click
            });*/
        } else if ("action_FindUser".equals(source)) {
            taskButton.setText("Follow");
        } else if ("action_FollowRequest".equals(source)) {
            taskButton.setText("Accept");
        } else if("action_Followers".equals((source))){
            taskButton.setText("Unfollow");
        }

        return convertView;
    }
}
