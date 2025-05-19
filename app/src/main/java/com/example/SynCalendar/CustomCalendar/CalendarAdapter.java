package com.example.SynCalendar.CustomCalendar;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SynCalendar.R;

import java.util.ArrayList;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final Set<String> eventDates;
    private final OnItemListener onItemListener;
    private String selectedDay = "";

    public CalendarAdapter(ArrayList<String> daysOfMonth, Set<String> eventDates, OnItemListener onItemListener)
    {
        this.daysOfMonth = daysOfMonth;
        this.eventDates = eventDates;
        this.onItemListener = onItemListener;
        Log.d("CalendarAdapter", "Event dates in constructor: " + eventDates);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        view.setLayoutParams(layoutParams);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        String dayText = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayText);

        // Only process non-empty days
        if (!dayText.isEmpty()) {
            // Set text style based on selection
            if (dayText.equals(selectedDay)) {
                holder.dayOfMonth.setTypeface(null, Typeface.BOLD);
            } else {
                holder.dayOfMonth.setTypeface(null, Typeface.NORMAL);
            }

            // Check for events and apply underline
            if (eventDates.contains(dayText)) {
                Log.d("CalendarAdapter", "Underlining day: " + dayText);
                holder.dayOfMonth.setPaintFlags(holder.dayOfMonth.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            } else {
                holder.dayOfMonth.setPaintFlags(holder.dayOfMonth.getPaintFlags() & (~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
            }
        } else {
            // Reset empty cells
            holder.dayOfMonth.setTypeface(null, Typeface.NORMAL);
            holder.dayOfMonth.setPaintFlags(0);
        }
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public void setSelectedDay(String day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    public interface OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}
