package com.example.mytasksapplication;

public class User {
    private int id;
    private String uName, email, password;
    private Task[] tasks;

    public User(int id, String uName, String email, String password, Task[] tasks) {
        this.id = id;
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.tasks = tasks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getuName() { return uName; }
    public void setuName(String uName) { this.uName = uName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Task[] getTasks() { return tasks; }
    public void setTasks(Task[] tasks) { this.tasks = tasks; }

}
