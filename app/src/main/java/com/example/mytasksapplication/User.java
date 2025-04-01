package com.example.mytasksapplication;

import android.graphics.Bitmap;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String uName, email, id;
    private HashMap<String, FollowStatus> followStatus;
    private ArrayList<String> followers;
    private Boolean privacy;
    private Bitmap profilePic;

    public User(String uName, String email, Bitmap profilePic, String id/*, ArrayList<Group> groups*/, HashMap<String, FollowStatus> followStatus, ArrayList<String> followers, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.profilePic = profilePic;
        this.id = id;
        this.followStatus = (followStatus != null) ? followStatus : new HashMap<>();
        this.followers = (followers != null) ? followers : new ArrayList<>();
        this.privacy = privacy;
    }

    public User(FirebaseUser firebaseUser){
        this.uName = firebaseUser.getDisplayName();
        this.email = firebaseUser.getEmail();
    }

    public List<String> getPendingFollowRequests() {
        List<String> pendingRequests = new ArrayList<>();
        for (Map.Entry<String, FollowStatus> entry : followStatus.entrySet()) {
            if (entry.getValue() == FollowStatus.REQUEST) {
                pendingRequests.add(entry.getKey());
            }
        }
        return pendingRequests;
    }

    public List<String> getPendingFollowing() {
        List<String> pendingRequests = new ArrayList<>();
        for (Map.Entry<String, FollowStatus> entry : followStatus.entrySet()) {
            if (entry.getValue() == FollowStatus.FOLLOW) {
                pendingRequests.add(entry.getKey());
            }
        }
        return pendingRequests;
    }

    public FollowStatus getUserFollowStatus(String otherUserId) { return followStatus.getOrDefault(otherUserId, null); }
    public void setUserFollowStatus(String otherUserId, FollowStatus status) { followStatus.put(otherUserId, status); }

    public HashMap<String, FollowStatus> getFollowStatusMap() { return followStatus; }
    public void setFollowStatusMap(HashMap<String, FollowStatus> followStatus) { this.followStatus = followStatus; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ArrayList<String> getFollowers() { return followers; }
    public void setFollowers(ArrayList<String> followers) { this.followers = followers; }
    public void addFollower(String otherUserId) { this.followers.add(otherUserId); }

    public Boolean getPrivacy() { return privacy; }
    public void setPrivacy(Boolean privacy) { this.privacy = privacy; }

    public String getuName() { return uName; }
    public void setuName(String uName) { this.uName = uName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public enum FollowStatus {
        FOLLOW, UNFOLLOW, REQUEST
    }

    @Exclude
    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }

    public String getBitmapStr(){
        return PhotoHelper.getEncodedString(profilePic);
    }

    public  void setBitmapStr(String bmpStr){
        profilePic = PhotoHelper.getBitmapFromEncodedString(bmpStr);
    }
}
