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

public class RequestAdapter   extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private Model model;
    private User currentUser;

    public RequestAdapter(Context context, List<User> users) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        }

        model = Model.getInstance(context);
        currentUser = model.getUser();
        User otherUser = users.get(position);

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        tvUName.setText(otherUser.getuName());

        Button taskButton = convertView.findViewById(R.id.btnAction);
        taskButton.setText("Accept Request");

        taskButton.setOnClickListener(v -> {
            if (currentUser.getUserFollowStatus(otherUser.getId()) == User.FollowStatus.REQUEST){
                otherUser.setUserFollowStatus(otherUser.getId(), User.FollowStatus.FOLLOW);
                currentUser.addFollower(currentUser.getId());
                taskButton.setText("Following");
            }
            else {

            }
            notifyDataSetChanged();
        });

        return convertView;
    }
}
