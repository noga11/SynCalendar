package com.example.SynCalendar.CustomCalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public CalendarAdapter(ArrayList<String> daysOfMonth, Set<String> eventDates, OnItemListener onItemListener)
    {
        this.daysOfMonth = daysOfMonth;
        this.eventDates = eventDates;
        this.onItemListener = onItemListener;
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
        
        if (eventDates.contains(dayText)) {
            holder.dayOfMonth.setPaintFlags(holder.dayOfMonth.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        } else {
            holder.dayOfMonth.setPaintFlags(holder.dayOfMonth.getPaintFlags() & (~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}
