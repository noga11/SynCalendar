package com.example.mytasksapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class DailyTasksAdapter {

    private Context context;
    private List<Task> dailyTasks;

    public DailyTasksAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.dailyTasks = tasks;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_task, parent, false);
        }

        Task task = dailyTasks.get(position);

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(task.getTitle());

        TextView tvTime = convertView.findViewById(R.id.tvTime);
        tvTime.setText((CharSequence) task.getStart());

        return convertView;
    }
}
