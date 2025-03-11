/*
package com.example.mytasksapplication;

import java.util.ArrayList;

public class Group {
    private ArrayList<Event> events;
    private String name, id;

    public Group(String name, String id) {
        this.events = new ArrayList<>();
        this.name = name;
        this.id = id;
    }

    // ------------------------------------------------------- Getters and setters ----------------------------------------------------------------------
    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Add a event to the group
    public void addEvent(Event event) {
        if (event != null && !events.contains(event)) {
            events.add(event);
        }
    }

    // -------------------------------------------------- Event Functions ---------------------------------------------------------
    public void removeEvent(Event event) {
        if (event != null) {
            events.remove(event);
        }
    }

    public Event getEventById(String eventId) {
        for (Event event : events) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    public void updateEvent(Event updatedEvent) {
        Event event = getEventById(updatedEvent.getId());
        if (event != null) {
            int index = events.indexOf(event);
            events.set(index, updatedEvent);
        }
    }

    public boolean containsEvent(Event event) {
        return events.contains(event);
    }

    public void clearEvents() {
        events.clear();
    }
}*/
