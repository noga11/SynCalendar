package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.example.mytasksapplication.User;

import java.util.List;

public class GroupAdapter  extends ArrayAdapter<String> {
    private Context context;
    private List<String> groups;

    public GroupAdapter(Context context, List<String> groups) {
        super(context, 0, groups);
        this.context = context;
        this.groups = groups;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_task, parent, false);
        }

        String group = groups.get(position);

        // Set up the title text
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(group);

        return convertView;
    }
}
