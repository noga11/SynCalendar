package com.example.SynCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Event {
    private String title, details, address, id, topic;
    private ArrayList<String> usersId;
    private Date start, remTime;
    private boolean reminder;
    private int notificationId, duration;

    public Event(){}

    public Event(String title, String details, String address, String id, String topic, ArrayList<String> users, Date start, Date remTime, boolean reminder, int notificationId, int duration) {
        this.title = title;
        this.details = details;
        this.address = address;
        this.id = id;
        this.topic = topic;
        this.usersId = users;
        this.start = start;
        this.remTime = remTime;
        this.reminder = reminder;
        this.notificationId = notificationId;
        this.duration = duration;
    }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroup() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public ArrayList<String> getUsersId() { return usersId; }
    public void setUsersId(ArrayList<String> usersId) { this.usersId = usersId; }

    public Date getStart() { return start; }
    public void setStart(Date start) { this.start = start; }

    public Date getRemTime() { return remTime; }
    public void setRemTime(Date remTime) { this.remTime = remTime; }

    public boolean isReminder() { return reminder; }
    public void setReminder(boolean reminder) { this.reminder = reminder; }

    public Date getEnd() {
        if (start == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.MINUTE, duration);
        return calendar.getTime();
    }
}
