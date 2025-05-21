package com.example.SynCalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

import android.content.SharedPreferences;

public class Model {
    private static final String TAG = "Model";
    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private static Model instance;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private CollectionReference eventRef;
    private CollectionReference userRef;
    private Context context;
    private User currentUser;
    private ArrayList<Event> events = new ArrayList<>();
    private ArrayList<String> groups = new ArrayList<>();

    private static final String PREFS_NAME = "SynCalendarPrefs";
    private static final String KEY_USER_ID = "userId";
    private SharedPreferences sharedPreferences;

    public Model(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        eventRef = firestore.collection(EVENTS_COLLECTION);
        userRef = firestore.collection(USERS_COLLECTION);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        checkUserLoginState();
    }

    public static Model getInstance(Context context) {
        if (instance == null) instance = new Model(context);
        return instance;
    }

    // -------------------------------------- User Functions --------------------------------------

    public User getCurrentUser(){
        if (currentUser != null) {
            return currentUser;
        }
        return null;
    }

    public void createUser(String displayName, String email, String password, boolean privacy, Bitmap profilePic, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        currentUser = new User(displayName, email, profilePic, firebaseUser.getUid(), null, null, privacy, password);
                        DocumentReference userDoc = firestore.collection(USERS_COLLECTION).document(firebaseUser.getUid());
                        // Log the currentUser object before saving to Firestore
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

    public void login(String email, String password, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        getUserFromFirebase(firebaseUser.getUid(), user -> {
                            currentUser = user;
                            sharedPreferences.edit().putString(KEY_USER_ID, firebaseUser.getUid()).apply();
                            Log.d(TAG, "User logged in and saved to shared preferences: " + currentUser.getuName());
                            onSuccess.onSuccess(currentUser);
                        }, onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    private void getUserFromFirebase(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                            Log.d("Model", "User data retrieved: " + currentUser.getuName());
                            onSuccess.onSuccess(currentUser);
                        } else {
                            Log.e("Model", "No such user in Firestore");
                            onFailure.onFailure(new Exception("User not found in database"));
                        }
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
                            onSuccess.onSuccess(user);
                        } else {
                            onSuccess.onSuccess(null); // User not found
                        }
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void logout() {
        mAuth.signOut();
        Log.d("Model", "User logged out");
        currentUser = null;
        sharedPreferences.edit().remove(KEY_USER_ID).apply();
        //      raiseUserUpdate();
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
        currentUser.setEmail(email);
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
        userRef.whereGreaterThanOrEqualTo("uName", query)
                .whereLessThanOrEqualTo("uName", query + '\uf8ff')
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<User> users = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            User user = document.toObject(User.class);
                            users.add(user);
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

    public interface GroupsCallback {
        void onGroupsLoaded(ArrayList<String> groups);
    }

    public void getGroups(GroupsCallback callback) {
        // Use a HashSet to ensure uniqueness of all groups
        Set<String> uniqueGroups = new HashSet<>();
        
        // Add default "All" group
        uniqueGroups.add("All");
        
        // Get all events from Firestore and extract their groups
        firestore.collection(EVENTS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Event event = document.toObject(Event.class);
                        if (event != null && event.getGroup() != null && !event.getGroup().isEmpty()) {
                            uniqueGroups.add(event.getGroup());
                        }
                    }
                    
                    // Clear and rebuild the groups list
                    groups.clear();
                    groups.addAll(uniqueGroups);
                    
                    // Make sure "All" is the first item
                    if (groups.remove("All")) {
                        groups.add(0, "All");
                    }
                    
                    // Add "Add New Group" at the end if not present
                    if (!groups.contains("Add New Group")) {
                        groups.add("Add New Group");
                    }
                    
                    Log.d(TAG, "Retrieved groups from Firestore: " + groups);
                    
                    // Call the callback with the updated groups
                    callback.onGroupsLoaded(new ArrayList<>(groups));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting groups from Firestore", e);
                    callback.onGroupsLoaded(new ArrayList<>(groups)); // Return current groups on error
                });
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

    private void checkUserLoginState() {
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId != null) {
            getUserFromFirebase(userId, user -> {
                currentUser = user;
                Log.d(TAG, "User logged in from shared preferences: " + currentUser.getuName());
            }, e -> Log.e(TAG, "Failed to retrieve user from shared preferences", e));
        }
    }

}

