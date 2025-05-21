package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SynCalendar.Activities.NewEventActivity;
import com.example.SynCalendar.R;
import com.example.SynCalendar.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyEventsAdapter extends RecyclerView.Adapter<DailyEventsAdapter.ViewHolder> {
    private Context context;
    private List<Event> dailyEvents;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Event event, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public DailyEventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.dailyEvents = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = dailyEvents.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvStart.setText(formatTime(event.getStart()));

        // Calculate end time based on duration
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(event.getStart());
        endCal.add(Calendar.MINUTE, event.getDuration());
        holder.tvEnd.setText(formatTime(endCal.getTime()));

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(event, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dailyEvents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStart, tvEnd;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStart = itemView.findViewById(R.id.tvStart);
            tvEnd = itemView.findViewById(R.id.tvEnd);
        }
    }

    private String formatTime(Date date) {
        if (date != null) {
            return timeFormat.format(date);
        }
        return "";
    }

    public void removeItem(int position) {
        dailyEvents.remove(position);
        notifyItemRemoved(position);
    }

    public void updateData(List<Event> newEvents) {
        dailyEvents.clear();
        dailyEvents.addAll(newEvents);
        notifyDataSetChanged();
    }
}
