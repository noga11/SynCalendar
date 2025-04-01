package com.example.mytasksapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Model {
    private static final String TAG = "Model";
    private static Model instance;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private CollectionReference eventRef;
    private CollectionReference userRef;
    private Context context;
    private User currentUser;
    private ArrayList<Event> events = new ArrayList<>();

    public Model(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        eventRef = firestore.collection("Events");
        userRef = firestore.collection("Users");
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

    public void createUser(String displayName, String email, String password, boolean privacy, Bitmap profilePic) throws Exception {
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        currentUser = new User(displayName, email, profilePic, firebaseUser.getUid(), null, null, privacy);
                        DocumentReference userDoc = firestore.collection("users").document(firebaseUser.getUid());
                        userDoc.set(currentUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Model", "User details saved to Firestore.");
                                    //raise User changed
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Model", "Error saving user to Firestore", e);
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"create currentUser failed"+ e.getMessage(),Toast.LENGTH_SHORT);
                    }
                });
    }

    public User login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser= mAuth.getCurrentUser();
                        getUserFromFirebase(firebaseUser.getUid());
//                            raiseUserUpdate();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "login failed", Toast.LENGTH_SHORT).show();
                    }
                });
        return null;
    }
    private void getUserFromFirebase(String userId) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUser = documentSnapshot.toObject(User.class);
                Log.d("Model", "User data retrieved: " + currentUser.getuName());
            } else {
                Log.e("Model", "No such user in Firestore");
            }
        }).addOnFailureListener(e -> {
            Log.e("Model", "Error retrieving user data from Firestore", e);
        });
    }

    public void logout() {
        mAuth.signOut();
        Log.d("Model", "User logged out");
        currentUser = null;
//        raiseUserUpdate();
    }

    public void updateUser(String uName, String email, String password, boolean privacy, Bitmap profilePic) {
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

        if (email != null && !email.equals(firebaseUser.getEmail())) {
            firebaseUser.updateEmail(email) // need to change
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "User email updated in Firebase Authentication.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Failed to update email in Firebase Authentication.", e);
                        }
                    });
        }

        if (password != null && !password.isEmpty()) {
            firebaseUser.updatePassword(password)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "User password updated in Firebase Authentication.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Failed to update password in Firebase Authentication.", e);
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
        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
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


    // -------------------------------------- Event Functions --------------------------------------

    public void createEvent(Event event) {
        eventRef.add(event)
                .addOnSuccessListener(documentReference -> {
                    event.setId(documentReference.getId());
                    events.add(event);
                    raiseEventDataChange();
                }).addOnFailureListener(ex ->{
                    Log.e(TAG, "createEvent: failed ", ex );
                });
        /*DocumentReference eventRef = firestore.collection("events").document(event.getId());
        eventRef.set(event)
                .addOnSuccessListener(aVoid -> Log.d("Model", "Event created successfully."))
                .addOnFailureListener(e -> Log.e("Model", "Error creating event", e));*/
    }

    public void deleteEvent(String eventId) {
        DocumentReference eventRef = firestore.collection("events").document(eventId);
        eventRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("Model", "Event deleted successfully."))
                .addOnFailureListener(e -> Log.e("Model", "Error deleting event", e));
    }

    public void updateEvent(Event event) {
        DocumentReference eventRef = firestore.collection("events").document(event.getId());
        eventRef.set(event)
                .addOnSuccessListener(aVoid -> Log.d("Model", "Event updated successfully."))
                .addOnFailureListener(e -> Log.e("Model", "Error updating event", e));
    }

    public ArrayList<Event> getEventsByUserId(String userId) {
        firestore.collection("events")
                .whereArrayContains("users", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            events.add(event);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Model", "Error updating user in Firestore", e);
                    }
                });

        return events;
    }

    public void raiseEventDataChange() {
        firestore.collection("events")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("Model", "Error listening for event changes", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        Log.d("Model", "Events updated.");
                    }
                });
    }

    public ArrayList<String> getTopics() {
        ArrayList<String> topics = new ArrayList<>();
        for (Event event : events) {
            if (event.getTopic() != null && !topics.contains(event.getTopic())) {
                topics.add(event.getTopic());
            }
        }
        return topics;
    }


}
