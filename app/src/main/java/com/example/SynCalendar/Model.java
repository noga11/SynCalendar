package com.example.SynCalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.SynCalendar.Notification.Reminder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Calendar;
import java.util.Map;

public class Model {
    private static final String TAG = "Model";
    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private static Model instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CollectionReference eventRef;
    private CollectionReference userRef;
    private Context context;
    private User currentUser;
    private ArrayList<Event> events = new ArrayList<>();
    private ArrayList<String> groups = new ArrayList<>();

    public Model(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        eventRef = firestore.collection(EVENTS_COLLECTION);
        userRef = firestore.collection(USERS_COLLECTION);
        
        // Initialize collections
        initializeCollections();
        
        // Initialize groups with default values
        groups = new ArrayList<>();
        groups.add("All");
        groups.add("Add New Group");
        
        // Load initial groups
        loadGroups();

        checkUserLoginState();
    }

    private void initializeCollections() {
        // Create events collection if it doesn't exist
        eventRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d(TAG, "Events collection exists");
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Creating events collection");
            // Create an empty document to initialize the collection
            eventRef.document("_init").set(new HashMap<String, Object>())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Events collection created"))
                .addOnFailureListener(error -> Log.e(TAG, "Error creating events collection", error));
        });

        // Create users collection if it doesn't exist
        userRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d(TAG, "Users collection exists");
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Creating users collection");
            // Create an empty document to initialize the collection
            userRef.document("_init").set(new HashMap<String, Object>())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Users collection created"))
                .addOnFailureListener(error -> Log.e(TAG, "Error creating users collection", error));
        });
    }

    public static Model getInstance(Context context) {
        if (instance == null) instance = new Model(context);
        return instance;
    }

    // -------------------------------------- User Functions --------------------------------------

    public boolean isUserLoggedIn() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return firebaseUser != null;
    }

    private void checkUserLoginState() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            getUserById(firebaseUser.getUid(), user -> {
                if (user != null) {
                    currentUser = user;
                    Log.d(TAG, "User logged in from Firebase Auth: " + currentUser.getuName());
                } else {
                    Log.e(TAG, "User document not found in Firestore");
                    // Handle the case where user exists in Auth but not in Firestore
                    logout(); // Log out the user since their Firestore data is missing
                }
            }, e -> {
                Log.e(TAG, "Failed to retrieve user from Firebase", e);
                // Handle the error case
                logout(); // Log out the user on error
            });
        }
    }

    public User getCurrentUser(){
        if (currentUser != null) {
            return currentUser;
        }
        return null;
    }

    public void login(String email, String password, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        getUserById(firebaseUser.getUid(), user -> {
                            currentUser = user;
                            Log.d(TAG, "User logged in: " + currentUser.getuName());
                            onSuccess.onSuccess(currentUser);
                        }, onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void logout() {
        mAuth.signOut();
        Log.d("Model", "User logged out");
        currentUser = null;
        events.clear();
        groups.clear();
        // Clear the singleton instance to force a fresh start
        instance = null;
    }

    public void createUser(String displayName, String email, String password, boolean privacy, Bitmap profilePic, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        currentUser = new User(displayName, email, profilePic, firebaseUser.getUid(), null, null, privacy, password);
                        DocumentReference userDoc = firestore.collection(USERS_COLLECTION).document(firebaseUser.getUid());
                        Log.d("Model", "Creating user with details: " + currentUser.toString());
                        userDoc.set(currentUser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Model", "User details saved to Firestore.");
                                        onSuccess.onSuccess(currentUser);
                                    }
                                })
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void getUserById(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        DocumentReference userDocRef = firestore.collection(USERS_COLLECTION).document(userId);
        userDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Ensure maps are initialized
                                if (user.getFollowing() == null) {
                                    user.setFollowing(new HashMap<>());
                                }
                                if (user.getFollowers() == null) {
                                    user.setFollowers(new HashMap<>());
                                }
                                if (user.getRequests() == null) {
                                    user.setPendingRequests(new HashMap<>());
                                }
                                
                                // Set the ID since it might not be set in the Firestore document
                                user.setId(documentSnapshot.getId());
                                
                                Log.d("Model", "Retrieved user: " + user.getuName());
                            }
                            onSuccess.onSuccess(user);
                        } else {
                            onSuccess.onSuccess(null); // User not found
                        }
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void updateUser(String uName, String email, boolean privacy, Bitmap profilePic) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();  // Get the current FirebaseUser

        // Update display name in Firebase Authentication
        if (uName != null && !uName.isEmpty()) {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(uName)
                    .build();

            firebaseUser.updateProfile(profileUpdate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "User display name updated in Firebase Authentication.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Failed to update display name in Firebase Authentication.", e);
                        }
                    });
        }

        // Update currentUser (not in the firebase)
        currentUser.setPrivacy(privacy);
        currentUser.setuName(uName);
        currentUser.setPrivacy(privacy);
        currentUser.setProfilePic(profilePic);

        // Update the user document in Firestore
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.getUid());
        userRef.set(currentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Model", "User details updated in Firestore.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Model", "Error updating user in Firestore", e);
                    }
                });
    }

    public void searchUsers(String query, OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure) {
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<User> users = new ArrayList<>();
                        String lowercaseQuery = query.toLowerCase().trim();
                        
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            User user = document.toObject(User.class);
                            if (user != null && !user.getId().equals(currentUser.getId())) {
                                String username = user.getuName().toLowerCase();
                                if (lowercaseQuery.isEmpty() || username.contains(lowercaseQuery)) {
                                    // Check if the current user is following this user
                                    if (currentUser.getFollowing().containsKey(user.getId())) {
                                        // Ensure the follower relationship is properly set
                                        user.getFollowers().put(currentUser.getId(), currentUser.getuName());
                                    }
                                    // Check if there's a pending request
                                    if (user.getRequests().containsKey(currentUser.getId())) {
                                        // Ensure the request is properly set
                                        user.getRequests().put(currentUser.getId(), currentUser.getuName());
                                    }
                                    users.add(user);
                                }
                            }
                        }
                        onSuccess.onSuccess(users);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // -------------------------------------- Event Functions --------------------------------------

    public void createEvent(Event event) {
        eventRef.add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        event.setId(documentReference.getId());
                        events.add(event);
                        raiseEventDataChange();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {
                        Log.e(TAG, "createEvent: failed ", ex);
                    }
                });
    }

    public void deleteEvent(Event event) {
        // delete event for current user
        String eventId = event.getId();
        event.getUsersId().remove(currentUser.getId());
        updateEvent(eventId, event.getTitle(), event.getDetails(), event.getAddress(),
                   event.getGroup(), event.getUsersId(), event.getStart(), 
                   event.getRemTime(), event.isReminder(), event.getDuration());

        //delete event for everyone
        if(event.getUsersId().isEmpty()) {
            DocumentReference eventRef = firestore.collection(EVENTS_COLLECTION).document(eventId);
            eventRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "Event deleted successfully.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Error deleting event", e);
                        }
                    });
        }
    }

    public void updateEvent(String eventId, String title, String details, String address, 
                          String group, ArrayList<String> usersId, Date start, 
                          Date remTime, boolean reminder, int duration) {
        // Create an Event object with the updated values
        Event eventToUpdate = new Event(title, details, address, eventId, group, usersId,
                                      start, remTime, reminder, duration);

        // Update directly in Firestore
        DocumentReference eventRef = firestore.collection(EVENTS_COLLECTION).document(eventId);
        eventRef.set(eventToUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Event updated successfully in Firestore");
                        // Update local events list if it exists in there
                        for (int i = 0; i < events.size(); i++) {
                            if (events.get(i).getId().equals(eventId)) {
                                events.set(i, eventToUpdate);
                                break;
                            }
                        }
                        raiseEventDataChange();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error updating event", e);
                        Toast.makeText(context, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getEventsByUserId(String userId, OnSuccessListener<List<Event>> onSuccess, OnFailureListener onFailure) {
        firestore.collection(EVENTS_COLLECTION)
                .whereArrayContains("usersId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> userEvents = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        userEvents.add(event);
                    }
                    onSuccess.onSuccess(userEvents);
                })
                .addOnFailureListener(onFailure);
    }

    public void raiseEventDataChange() {
        firestore.collection(EVENTS_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("Model", "Error listening for event changes", e);
                            return;
                        }
                        if (querySnapshot != null) {
                            // Clear and rebuild the events list
                            events.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Event event = document.toObject(Event.class);
                                if (event != null) {
                                    event.setId(document.getId());
                                    events.add(event);
                                }
                            }
                            Log.d("Model", "Events list updated with " + events.size() + " events");
                        }
                    }
                });
    }

    // -------------------------------------- Group Functions --------------------------------------

    public interface GroupsCallback {
        void onGroupsLoaded(ArrayList<String> groups);
    }

    public void getGroups(GroupsCallback callback) {
        // First return current groups immediately
        callback.onGroupsLoaded(new ArrayList<>(groups));
        
        // Then refresh groups in background
        loadGroups();
    }

    public ArrayList<String> getGroups() {
        return new ArrayList<>(groups);
    }

    public void deleteGroup(String groupToDelete) {
        if (groups.contains(groupToDelete) && !groupToDelete.equals("All") && !groupToDelete.equals("Add New Group")) {
            groups.remove(groupToDelete);
            Log.d(TAG, "Deleted group: " + groupToDelete);
            
            // Get all events in this group and update them
            firestore.collection(EVENTS_COLLECTION)
                    .whereEqualTo("group", groupToDelete)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Event event = document.toObject(Event.class);
                            if (event != null) {
                                event.setId(document.getId());
                                event.setGroup("All"); // Set to default group
                                // Update in Firestore
                                firestore.collection(EVENTS_COLLECTION)
                                        .document(event.getId())
                                        .set(event)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Event updated after group deletion"))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error updating event after group deletion", e));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error querying events for group deletion", e));
        }
    }

    public void addGroup(String newGroup) {
        if (!groups.contains(newGroup) && !newGroup.equals("All") && !newGroup.equals("Add New Group")) {
            // Remove "Add New Group" if it exists
            groups.remove("Add New Group");
            // Add the new group
            groups.add(newGroup);
            // Add "Add New Group" back at the end
            groups.add("Add New Group");
            Log.d(TAG, "Added new group: " + newGroup);
        }
    }

    // Add new method to load groups
    private void loadGroups() {
        firestore.collection(EVENTS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> uniqueGroups = new HashSet<>();
                    uniqueGroups.add("All");  // Always include default group
                    
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Event event = document.toObject(Event.class);
                        if (event != null && event.getGroup() != null && !event.getGroup().isEmpty()) {
                            uniqueGroups.add(event.getGroup());
                        }
                    }
                    
                    // Update groups list
                    groups.clear();
                    groups.add("All");  // Ensure "All" is first
                    uniqueGroups.remove("All");  // Remove to avoid duplication
                    groups.addAll(uniqueGroups);
                    groups.add("Add New Group");  // Ensure "Add New Group" is last
                    
                    Log.d(TAG, "Groups loaded successfully: " + groups);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading groups", e);
                    // Ensure we at least have default groups
                    if (groups.isEmpty()) {
                        groups.add("All");
                        groups.add("Add New Group");
                    }
                });
    }

    // -------------------------------------- Follow Functions --------------------------------------

    public interface FollowRequestCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void acceptFollowRequest(User requester, FollowRequestCallback callback) {
        Map<String, String> requests = currentUser.getRequests();

        if (requests.containsKey(requester.getId())) {
            // Accept the request
            currentUser.approveFollowRequest(requester.getId());
            
            // Update requester's following list
            requester.addFollowing(currentUser.getId(), currentUser.getuName());

            // Update Firestore for both users
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.getId())
                .set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Current user updated successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating current user", e);
                    callback.onFailure(e);
                });

            firestore.collection(USERS_COLLECTION)
                .document(requester.getId())
                .set(requester)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating requester", e);
                    callback.onFailure(e);
                });
        }
    }

    public void rejectFollowRequest(User requester, FollowRequestCallback callback) {
        Map<String, String> requests = currentUser.getRequests();
        
        if (requests.containsKey(requester.getId())) {
            // Reject the request
            currentUser.denyFollowRequest(requester.getId());

            // Update Firestore
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.getId())
                .set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Request rejected successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rejecting request", e);
                    callback.onFailure(e);
                });
        }
    }

    public void removeFollower(User follower, FollowRequestCallback callback) {
        // Remove from followers list
        currentUser.removeFollower(follower.getId());
        // Remove from their following list
        follower.removeFollowing(currentUser.getId());
        // Add to their requests
        currentUser.addPendingRequest(follower.getId(), follower.getuName());

        // Update Firestore
        userRef.document(currentUser.getId())
            .set(currentUser)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Current user updated successfully after removing follower");
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating current user after removing follower", e);
                callback.onFailure(e);
            });

        // Update follower
        userRef.document(follower.getId())
            .set(follower)
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating follower", e);
                callback.onFailure(e);
            });
    }

    public void handleFollowUnfollow(User otherUser, FollowRequestCallback callback) {
        boolean isCurrentlyFollowing = currentUser.getFollowing().containsKey(otherUser.getId());
        boolean hasCurrentRequest = otherUser.getRequests().containsKey(currentUser.getId());

        if (isCurrentlyFollowing) {
            // Unfollow
            currentUser.removeFollowing(otherUser.getId());
            otherUser.removeFollower(currentUser.getId());
            Log.d(TAG, "Unfollowed user: " + otherUser.getuName());
        } else if (hasCurrentRequest) {
            // Cancel request
            otherUser.removePendingRequest(currentUser.getId());
            Log.d(TAG, "Cancelled request to: " + otherUser.getuName());
        } else {
            // Follow or send request
            if (!otherUser.getPrivacy()) {
                // Direct follow for public accounts
                currentUser.addFollowing(otherUser.getId(), otherUser.getuName());
                otherUser.addFollower(currentUser.getId(), currentUser.getuName());
                Log.d(TAG, "Following user: " + otherUser.getuName());
            } else {
                // Send request for private accounts
                otherUser.addPendingRequest(currentUser.getId(), currentUser.getuName());
                Log.d(TAG, "Sent request to: " + otherUser.getuName());
            }
        }

        // Update both users in Firestore
        userRef.document(otherUser.getId())
            .set(otherUser)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Other user updated successfully");
                userRef.document(currentUser.getId())
                    .set(currentUser)
                    .addOnSuccessListener(aVoid2 -> {
                        Log.d(TAG, "Current user updated successfully");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating current user", e);
                        callback.onFailure(e);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating other user", e);
                callback.onFailure(e);
            });
    }

}

