package com.example.mytasksapplication;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class User {
    private String uName, email, password;
    private Bitmap profilePic;
    private ArrayList<Task> tasks;
    private Boolean privacy;

    public User(String uName, String email, String password, ArrayList<Task> tasks, Bitmap profilePic, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.tasks = tasks;
        this.profilePic = profilePic;
        this.privacy = privacy;
    }

    public Boolean getPrivacy() { return privacy; }
    public void setPrivacy(Boolean privacy) { this.privacy = privacy; }

    public Bitmap getProfilePic() {
        return profilePic;
    }
    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
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
