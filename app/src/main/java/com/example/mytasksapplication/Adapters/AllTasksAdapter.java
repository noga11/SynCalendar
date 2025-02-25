package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;

import java.util.List;

public class AllTasksAdapter extends RecyclerView.Adapter<AllTasksAdapter.ViewHolder> {
    private Context context;
    private List<Task> allTasks;

    public AllTasksAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.allTasks = tasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = allTasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDetails.setText(task.getDetails());
        holder.tvDate.setText(task.getDate().toString());
        holder.tvStart.setText(task.getStart().toString());
        holder.tvEnd.setText(task.getEnd().toString());  // Fixed start->end bug
    }

    @Override
    public int getItemCount() {
        return allTasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvDate, tvStart, tvEnd;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStart = itemView.findViewById(R.id.tvStart);
            tvEnd = itemView.findViewById(R.id.tvEnd);
        }
    }
}
