package com.example.mytasksapplication;

import android.graphics.Bitmap;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String uName, email, id;
    private Boolean privacy;
    private Bitmap profilePic;
        // userID -> username
    private HashMap<String, String> pendingRequests;     // users requesting to follow this user
    private HashMap<String, String> following;       // users this user follows
    private HashMap<String, String> followers;       // users who follow this user
    private HashMap<String, String> mutuals;

    public User(String uName, String email, Bitmap profilePic, String id,
                HashMap<String, String> pendingRequests, HashMap<String, String> following,
                HashMap<String, String> followers, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.profilePic = profilePic;
        this.id = id;
        this.pendingRequests = (pendingRequests != null) ? pendingRequests : new HashMap<>();
        this.following = (following != null) ? following : new HashMap<>();
        this.followers = (followers != null) ? followers : new HashMap<>();
        this.privacy = privacy;
    }

    public User(FirebaseUser firebaseUser) {
        this.uName = firebaseUser.getDisplayName();
        this.email = firebaseUser.getEmail();
        this.pendingRequests = new HashMap<>();
        this.following = new HashMap<>();
        this.followers = new HashMap<>();
    }

    // --- Follow Management ---

    public Map<String, String> getRequests() { return pendingRequests; }
    public void setPendingRequests(HashMap<String, String> pendingRequests) { this.pendingRequests = pendingRequests; }
    public void addPendingRequest(String userId, String username) { pendingRequests.put(userId, username); }
    public void removePendingRequest(String userId) { pendingRequests.remove(userId); }

    public Map<String, String> getFollowing() { return following; }
    public void setFollowing(HashMap<String, String> following) { this.following = following; }
    public void addFollowing(String userId, String username) { following.put(userId, username); }
    public void removeFollowing(String userId) { following.remove(userId); }

    public Map<String, String> getFollowers() { return followers; }
    public void setFollowers(HashMap<String, String> followers) { this.followers = followers; }
    public void addFollower(String userId, String username) { followers.put(userId, username); }
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

        for (Map.Entry<String, String> entry : following.entrySet()) {
            String userId = entry.getKey();
            String username = entry.getValue();

            if (followers.containsKey(userId)) {
                mutuals.put(userId, username);
            }
        }
    }

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
        if (pendingRequests.containsKey(userId)) {
            String username = pendingRequests.get(userId);
            pendingRequests.remove(userId);
            followers.put(userId, username);
        }
    }

    // Denies a follow request: just remove from pendingRequests
    public void denyFollowRequest(String userId) {
        pendingRequests.remove(userId);
    }
}
