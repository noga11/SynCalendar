package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Event;

import java.util.List;

public class AllEventsAdapter extends RecyclerView.Adapter<AllEventsAdapter.ViewHolder> {
    private Context context;
    private List<Event> allEvents;

    public AllEventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.allEvents = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = allEvents.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDetails.setText(event.getDetails());
        holder.tvDate.setText(event.getDate().toString());
        holder.tvStart.setText(event.getStart().toString());
        holder.tvEnd.setText(event.getEnd().toString());  // Fixed start->end bug
    }

    @Override
    public int getItemCount() {
        return allEvents.size();
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
