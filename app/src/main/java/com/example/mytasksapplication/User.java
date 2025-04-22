package com.example.mytasksapplication;

import android.graphics.Bitmap;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uName, email, id;
    private ArrayList<String> pendingRequests; // users requesting to follow this user
    private ArrayList<String> following;       // users this user follows
    private ArrayList<String> followers;       // users who follow this user
    private Boolean privacy;
    private Bitmap profilePic;

    public User(String uName, String email, Bitmap profilePic, String id,
                ArrayList<String> pendingRequests, ArrayList<String> following,
                ArrayList<String> followers, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.profilePic = profilePic;
        this.id = id;
        this.pendingRequests = (pendingRequests != null) ? pendingRequests : new ArrayList<>();
        this.following = (following != null) ? following : new ArrayList<>();
        this.followers = (followers != null) ? followers : new ArrayList<>();
        this.privacy = privacy;
    }

    public User(FirebaseUser firebaseUser) {
        this.uName = firebaseUser.getDisplayName();
        this.email = firebaseUser.getEmail();
        this.pendingRequests = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
    }

    // --- Follow Management ---
    public List<String> getRequests() {
        return pendingRequests;
    }

    public void addPendingRequest(String userId) {
        if (!pendingRequests.contains(userId)) pendingRequests.add(userId);
    }

    public void removePendingRequest(String userId) {
        pendingRequests.remove(userId);
    }

    public List<String> getFollowing() {
        return following;
    }

    public void addFollowing(String userId) {
        if (!following.contains(userId)) following.add(userId);
    }

    public void removeFollowing(String userId) {
        following.remove(userId);
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void addFollower(String userId) {
        if (!followers.contains(userId)) followers.add(userId);
    }

    public void removeFollower(String userId) {
        followers.remove(userId);
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Boolean getPrivacy() { return privacy; }
    public void setPrivacy(Boolean privacy) { this.privacy = privacy; }

    public String getuName() { return uName; }
    public void setuName(String uName) { this.uName = uName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Exclude
    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }

    public String getBitmapStr() {
        return PhotoHelper.getEncodedString(profilePic);
    }

    public void setBitmapStr(String bmpStr) {
        this.profilePic = PhotoHelper.getBitmapFromEncodedString(bmpStr);
    }

    // Approves a follow request: move from pendingRequests to followers
    public void approveFollowRequest(String userId) {
        if (pendingRequests.contains(userId)) {
            pendingRequests.remove(userId);
            if (!followers.contains(userId)) {
                followers.add(userId);
            }
        }
    }

    // Denies a follow request: just remove from pendingRequests
    public void denyFollowRequest(String userId) {
        pendingRequests.remove(userId);
    }

}
