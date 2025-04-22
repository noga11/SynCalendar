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

public class RequestAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private Model model;
    private User currentUser;

    public RequestAdapter(Context context, List<User> users) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        }

        model = Model.getInstance(context);
        currentUser = model.getCurrentUser();
        User requester = users.get(position); // This user sent the request

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        tvUName.setText(requester.getuName());

        Button btnAccept = convertView.findViewById(R.id.btnAction);
        btnAccept.setText("Accept Request");

        btnAccept.setOnClickListener(v -> {
            if (currentUser.getRequests().contains(requester.getId())) {
                currentUser.approveFollowRequest(requester.getId());
                btnAccept.setText("Following");
            }
            notifyDataSetChanged();
        });

        return convertView;
    }
}
