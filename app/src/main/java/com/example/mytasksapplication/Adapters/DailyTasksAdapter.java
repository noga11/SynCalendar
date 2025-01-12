package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;

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

        TextView tvStart = convertView.findViewById(R.id.tvStart);
        tvStart.setText((CharSequence) task.getStart());

        TextView tvEnd = convertView.findViewById(R.id.tvEnd);
        tvEnd.setText((CharSequence) task.getStart());

        return convertView;
    }
}
