package com.example.mytasksapplication;

import java.util.ArrayList;

public class Group {
    private ArrayList<Task> tasks;
    private String name, id;

    public Group(String name, String id) {
        this.tasks = new ArrayList<>();
        this.name = name;
        this.id = id;
    }

    // ------------------------------------------------------- Getters and setters ----------------------------------------------------------------------
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
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

    // Add a task to the group
    public void addTask(Task task) {
        if (task != null && !tasks.contains(task)) {
            tasks.add(task);
        }
    }

    // -------------------------------------------------- Task Functions ---------------------------------------------------------
    public void removeTask(Task task) {
        if (task != null) {
            tasks.remove(task);
        }
    }

    public Task getTaskById(String taskId) {
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    public void updateTask(Task updatedTask) {
        Task task = getTaskById(updatedTask.getId());
        if (task != null) {
            int index = tasks.indexOf(task);
            tasks.set(index, updatedTask);
        }
    }

    public boolean containsTask(Task task) {
        return tasks.contains(task);
    }

    public void clearTasks() {
        tasks.clear();
    }
}