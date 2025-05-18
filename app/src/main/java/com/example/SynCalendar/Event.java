package com.example.SynCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Event {
    private String title, details, address, id, topic;
    private ArrayList<String> usersId;
    //add users display name or use collection of users
    private Repeat repeat;
    private Status status;
    private Date start, remTime;
    private boolean reminder, important;
    private int colour, notificationId, duration;

    public Event(){}
    public Event(String title, String details, String address, String id, String topic, ArrayList<String> users, Repeat repeat, Status status, Date start, Date remTime, boolean reminder, boolean important, int colour, int notificationId, int duration) {
        this.title = title;
        this.details = details;
        this.address = address;
        this.id = id;
        this.topic = topic;
        this.usersId = users;
        this.repeat = repeat;
        this.status = status;
        this.start = start;
        this.remTime = remTime;
        this.reminder = reminder;
        this.important = important;
        this.colour = colour;
        this.notificationId = notificationId;
        this.duration = duration;
    }

    public Repeat getRepeat() { return repeat; }
    public void setRepeat(Repeat repeat) { this.repeat = repeat; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

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

    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }

    public int getColour() { return colour; }
    public void setColour(int colour) { this.colour = colour; }

    public enum Status {
        FREE, BUSY
    }

    public enum Repeat {
        DAY, WEEK, MONTH, YEAR, CUSTOM
    }

    public ArrayList<Date> getNextOccurrences(int numberOfOccurrences) {
        ArrayList<Date> occurrences = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.start);

        for (int i = 0; i < numberOfOccurrences; i++) {
            switch (this.repeat) {
                case DAY:
                    calendar.add(Calendar.DATE, 1);
                    break;
                case WEEK:
                    calendar.add(Calendar.DATE, 7);
                    break;
                case MONTH:
                    calendar.add(Calendar.MONTH, 1);
                    break;
                case YEAR:
                    calendar.add(Calendar.YEAR, 1);
                    break;
                case CUSTOM:
                    // Handle custom repeat (if applicable)
                    break;
            }
            occurrences.add(calendar.getTime());
        }
        return occurrences;
    }

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
