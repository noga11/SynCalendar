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

public class AllTasksAdapter extends ArrayAdapter<Task> {
    private Context context;
    private List<Task> allTasks;

    public AllTasksAdapter(Context context, List<Task> tasks) {
        super(context, 0, tasks);
        this.context = context;
        this.allTasks = tasks;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_task, parent, false);
        }

        Task task = allTasks.get(position);

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(task.getTitle());

        TextView tvDetails = convertView.findViewById(R.id.tvDetails);
        tvDetails.setText(task.getDetails());

        TextView tvDate = convertView.findViewById(R.id.tvDate);
        tvDate.setText(task.getDate().toString());

        TextView tvStart = convertView.findViewById(R.id.tvStart);
        tvStart.setText(task.getStart().toString());

        TextView tvEnd = convertView.findViewById(R.id.tvEnd);
        tvEnd.setText(task.getStart().toString());

        return convertView;
    }
}
