package com.example.mytasksapplication;

import java.util.ArrayList;

public class User {
    private String uName, email, password, profilePicUrl;
    private ArrayList<Group> groups;
    private Boolean privacy;

    public User(String uName, String email, String password, String profilePicUrl, ArrayList<Group> groups, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.groups = groups;
        this.profilePicUrl = profilePicUrl;
        this.privacy = privacy;
    }

    public Boolean getPrivacy() { return privacy; }
    public void setPrivacy(Boolean privacy) { this.privacy = privacy; }

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }

    public String getuName() { return uName; }
    public void setuName(String uName) { this.uName = uName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public ArrayList<Group> getGroups() { return groups; }
    public void setGroups(ArrayList<Group> groups) { this.groups = groups; }

}
