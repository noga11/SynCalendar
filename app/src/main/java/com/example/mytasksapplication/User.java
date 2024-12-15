package com.example.mytasksapplication;

import java.util.ArrayList;

public class User {
    private String uName, email, password;
    private ArrayList<Task> tasks;

    public User(String uName, String email, String password, ArrayList<Task> tasks) {
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.tasks = tasks;
    }

    public String getuName() { return uName; }
    public void setuName(String uName) { this.uName = uName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public ArrayList<Task> getTasks() { return tasks; }
    public void setTasks(ArrayList<Task> tasks) { this.tasks = tasks; }

}
