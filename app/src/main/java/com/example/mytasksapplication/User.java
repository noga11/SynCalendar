package com.example.mytasksapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String uName, email, password, profilePicUrl, id;
    private ArrayList<Group> groups;
    private HashMap<String, FollowStatus> followStatus;
    private ArrayList<String> followers;
    private Boolean privacy;

    public User(String uName, String email, String password, String profilePicUrl, String id, ArrayList<Group> groups, HashMap<String, FollowStatus> followStatus, ArrayList<String> followers, Boolean privacy) {
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.profilePicUrl = profilePicUrl;
        this.id = id;
        this.groups = groups;
        this.followStatus = (followStatus != null) ? followStatus : new HashMap<>();
        this.followers = (followers != null) ? followers : new ArrayList<>();
        this.privacy = privacy;
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

    public enum FollowStatus {
        FOLLOW, UNFOLLOW, REQUEST
    }
}
