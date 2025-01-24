package com.example.mytasksapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomCalendarView extends CalendarView {

    private Set<Long> taskDates = new HashSet<>();
    private int lineColor = Color.BLACK;
    private int cellWidth = 0;  // Initialize cellWidth

    public CustomCalendarView(Context context) {
        super(context);
    }

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calculate the cell width once the layout is complete
        cellWidth = w / 7;  // 7 days in a week
    }

    public void setTaskDates(Set<Long> taskDates) {
        this.taskDates = taskDates;
        invalidate();
    }

    public void setLineColor(int color) {
        this.lineColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(lineColor);
        paint.setStrokeWidth(5);

        // Loop through all the task dates and draw lines under them
        for (Long taskDate : taskDates) {
            float x = getDateX(taskDate);  // Get X position for each task date
            float y = getHeight() - 10;   // Y position just above the bottom of the CalendarView
            canvas.drawLine(x, y, x + cellWidth, y, paint);  // Draw the line
        }
    }

    private float getDateX(Long date) {
        // Convert Long (timestamp) to a Date object
        Date selectedDate = new Date(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        // Get the day of the week (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;  // Adjust for zero-based index

        // Calculate column index based on dayOfWeek (0 for Sunday, 1 for Monday, etc.)
        return dayOfWeek * cellWidth;  // Each column is spaced by cellWidth pixels
    }
}
