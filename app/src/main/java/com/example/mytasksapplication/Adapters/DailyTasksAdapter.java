package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;

import java.text.SimpleDateFormat;
import java.util.List;

public class DailyTasksAdapter extends ArrayAdapter<Task> {

    private Context context;
    private List<Task> dailyTasks;

    public DailyTasksAdapter(Context context, List<Task> tasks) {
        super(context, 0, tasks);
        this.context = context;
        this.dailyTasks = tasks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_task, parent, false);
        }

        Task task = dailyTasks.get(position);

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(task.getTitle());

        TextView tvSpacer = convertView.findViewById(R.id.tvSpacer);
        tvTitle.setText(" ");

        TextView tvStart = convertView.findViewById(R.id.tvStart);
        tvStart.setText(formatTime(task.getStart()));

        TextView tvEnd = convertView.findViewById(R.id.tvEnd);
        tvEnd.setText(formatTime(task.getEnd()));

        return convertView;
    }

    // Helper method to format the Time object (Hour and Minute only)
    private String formatTime(java.sql.Time time) {
        if (time != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // Format to display only Hour and Minute
            return sdf.format(new java.util.Date(time.getTime()));
        }
        return "";
    }
}
