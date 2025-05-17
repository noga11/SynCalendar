package com.example.SynCalendar;

import android.graphics.Bitmap;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String uName, email, id;
    private Boolean privacy;
    private String profilePicString;
    // userID -> username
    private HashMap<String, String> pendingRequests;     // users requesting to follow this user
    private ArrayList<String> following;       // users this user follows
    private ArrayList<String> followers;       // users who follow this user
    private HashMap<String, String> mutuals;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String uName, String email, Bitmap profilePic, String id,
                ArrayList<String> following, ArrayList<String> followers, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.profilePicString = PhotoHelper.bitmapToString(profilePic);
        this.id = id;
        this.following = following != null ? following : new ArrayList<>();
        this.followers = followers != null ? followers : new ArrayList<>();
        this.privacy = privacy;
    }

    public User(FirebaseUser firebaseUser) {
        this.uName = firebaseUser.getDisplayName();
        this.email = firebaseUser.getEmail();
        this.pendingRequests = new HashMap<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
    }

    // --- Follow Management ---

    public Map<String, String> getRequests() { return pendingRequests; }
    public void setPendingRequests(HashMap<String, String> pendingRequests) { this.pendingRequests = pendingRequests; }
    public void addPendingRequest(String userId, String username) { pendingRequests.put(userId, username); }
    public void removePendingRequest(String userId) { pendingRequests.remove(userId); }

    public ArrayList<String> getFollowing() { return following; }
    public void setFollowing(ArrayList<String> following) { this.following = following; }
    public void addFollowing(String userId, String username) { following.add(userId); }
    public void removeFollowing(String userId) { following.remove(userId); }

    public ArrayList<String> getFollowers() { return followers; }
    public void setFollowers(ArrayList<String> followers) { this.followers = followers; }
    public void addFollower(String userId, String username) { followers.add(userId); }
    public void removeFollower(String userId) { followers.remove(userId); }

    // --- Other Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Boolean getPrivacy() { return privacy; }
    public void setPrivacy(Boolean privacy) { this.privacy = privacy; }

    public String getuName() { return uName; }
    public void setuName(String uName) { this.uName = uName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public HashMap<String, String> getMutuals() { return mutuals; }
    public void setMutuals(HashMap<String, String> mutuals) { this.mutuals = mutuals;}
    public void updateMutuals() {
        if (mutuals == null) {
            mutuals = new HashMap<>();
        } else {
            mutuals.clear();
        }

        for (String userId : following) {
            mutuals.put(userId, userId);
        }
    }
    @Exclude
    public Bitmap getProfilePic() {
        return null;// PhotoHelper.stringToBitmap(profilePicString);
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePicString = PhotoHelper.bitmapToString(profilePic);
    }

    public String getProfilePicString() {
        return profilePicString;
    }

    public void setProfilePicString(String profilePicString) {
        this.profilePicString = profilePicString;
    }

    // Approves a follow request: move from pendingRequests to followers
    public void approveFollowRequest(String userId) {
        if (pendingRequests.containsKey(userId)) {
            String username = pendingRequests.get(userId);
            pendingRequests.remove(userId);
            followers.add(userId);
        }
    }

    // Denies a follow request: just remove from pendingRequests
    public void denyFollowRequest(String userId) {
        pendingRequests.remove(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "uName='" + uName + '\'' +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
