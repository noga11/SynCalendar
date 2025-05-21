package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SynCalendar.R;
import com.example.SynCalendar.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllEventsAdapter extends RecyclerView.Adapter<AllEventsAdapter.ViewHolder> {
    private Context context;
    private List<Event> allEvents;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Event event, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public AllEventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.allEvents = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_all_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = allEvents.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDetails.setText(event.getDetails());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.tvDate.setText(dateFormat.format(event.getStart()));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvStart.setText(timeFormat.format(event.getStart()));

        long endTimeInMillis = event.getStart().getTime() + (event.getDuration() * 60 * 1000); // Convert minutes to milliseconds
        Date endDate = new Date(endTimeInMillis);
        holder.tvEnd.setText(timeFormat.format(endDate));

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(event, position);
            }
        });
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

    public void removeItem(int position) {
        allEvents.remove(position);
        notifyItemRemoved(position);
    }

    public void updateData(List<Event> newEvents) {
        allEvents.clear();
        allEvents.addAll(newEvents);
        notifyDataSetChanged();
    }
}
