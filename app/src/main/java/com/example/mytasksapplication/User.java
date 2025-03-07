package com.example.mytasksapplication;

import java.util.ArrayList;

public class User {
    private String uName, email, password, profilePicUrl, id;
    private ArrayList<Group> groups;
    private ArrayList<String> following, followers;
    private Boolean privacy;

    public User(String uName, String email, String password, String profilePicUrl, String id, ArrayList<Group> groups, ArrayList<String> following, ArrayList<String> followers, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.profilePicUrl = profilePicUrl;
        this.id = id;
        this.groups = groups;
        this.following = following;
        this.followers = followers;
        this.privacy = privacy;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ArrayList<String> getFollowing() { return following; }
    public void setFollowing(ArrayList<String> following) { this.following = following; }

    public ArrayList<String> getFollowers() { return followers; }
    public void setFollowers(ArrayList<String> followers) { this.followers = followers; }

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
