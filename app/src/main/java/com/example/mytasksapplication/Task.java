package com.example.mytasksapplication;

import java.sql.Date;
import java.sql.Time;

public class Task {
    private String title, details, group;
    private String[] shareWithUser;
    private Time start, end, remTime;
    private Date date, remDate;
    private boolean reminder, important, started;
    private int progress;

    public Task(String title, String details, String group, String[] shareWithUser, Time start, Time end,
                Time remTime, Date date, Date remDate, boolean reminder, boolean important,
                boolean started, int progress) {
        this.title = title;
        this.details = details;
        this.group = group;
        this.shareWithUser = shareWithUser;
        this.start = start;
        this.end = end;
        this.date = date;
        this.reminder = reminder;
        this.remTime = remTime;
        this.remDate = remDate;
        this.important = important;
        this.started = started;
        this.progress = progress;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String[] getShareWithUser() { return shareWithUser; }
    public void setShareWithUser(String[] shareWithUser) { this.shareWithUser = shareWithUser; }

    public Time getStart() { return start; }
    public void setStart(Time start) { this.start = start; }

    public Time getEnd() { return end; }
    public void setEnd(Time end) { this.end = end; }

    public Time getRemTime() { return remTime; }
    public void setRemTime(Time remTime) { this.remTime = remTime; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getRemDate() { return remDate; }
    public void setRemDate(Date remDate) { this.remDate = remDate; }

    public boolean isReminder() { return reminder; }
    public void setReminder(boolean reminder) { this.reminder = reminder; }

    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}
