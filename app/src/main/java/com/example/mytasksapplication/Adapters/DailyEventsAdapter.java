package com.example.mytasksapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyEventsAdapter extends ArrayAdapter<Event> {

    private Context context;
    private List<Event> dailyEvents;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault()); // Format for time display

    public DailyEventsAdapter(Context context, List<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.dailyEvents = events;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_event, parent, false);
        }

        Event event = dailyEvents.get(position);

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(event.getTitle());

        TextView tvSpacer = convertView.findViewById(R.id.tvSpacer);
        tvSpacer.setText(" ");

        TextView tvStart = convertView.findViewById(R.id.tvStart);
        tvStart.setText(formatTime(event.getStart()));

        TextView tvEnd = convertView.findViewById(R.id.tvEnd);
        tvEnd.setText(formatTime(event.getEnd()));

        return convertView;
    }

    // Helper method to format Date object to display Hour and Minute only
    private String formatTime(Date date) {
        if (date != null) {
            return timeFormat.format(date);
        }
        return "";
    }
}
