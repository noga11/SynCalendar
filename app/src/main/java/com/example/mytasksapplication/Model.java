package com.example.mytasksapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Model {
    private static Model instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private Context context;
    private User currentUser;

    private ArrayList<User> allUsers = new ArrayList<>();

    public Model(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public static Model getInstance(Context context) {
        if (instance == null) instance = new Model(context);
        return instance;
    }

    // -------------------------------------- User Functions --------------------------------------

    private User findUserByUsername(String username) {
        for (User u : allUsers) {
            if (u.getuName().equals(username)) return u;
        }
        return null;
    }


    public void SetChipIconPictureFromUsername(String username, Chip chip) {
        User user = findUserByUsername(username);
        if (user == null || user.getProfilePicUrl() == null) {
            Log.e("Model", "User not found or profile picture URL is null");
            return;
        }

        String pictureUrl = user.getProfilePicUrl();
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(pictureUrl);

        storageRef.getBytes(1024 * 1024) // Download the image (limit size to 1MB)
                .addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Drawable drawable = new BitmapDrawable(chip.getContext().getResources(), bitmap);
                    chip.setChipIcon(drawable); // Set the profile picture
                })
                .addOnFailureListener(e -> Log.e("Model", "Failed to load profile picture", e));
    }


    public void raiseUserDataChange() {
        if (firebaseUser == null) {
            Log.e("Model", "No user is logged in.");
            return;
        }
        String userId = firebaseUser.getUid();

        // Set up the real-time listener for user data
        firestore.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("Model", "Error listening to user data", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Update the current user object with the new data
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            Log.d("Model", "User data has been updated.");
                        }
                    } else {
                        Log.e("Model", "No user data found.");
                    }
                });
    }

    public void createUser(String uName, String email, String password, Bitmap profilePic, Boolean privacy) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseUser = firebaseAuth.getCurrentUser();
                        Log.d("Model", "User created successfully: " + email);
                        uploadProfilePicture(profilePic, firebaseUser.getUid(), uName, email, password, privacy);
                    } else {
                        Log.e("Model", "User creation failed", task.getException());
                    }
                });
    }

    // Upload the profile picture to Firebase Storage
    private void uploadProfilePicture(Bitmap profilePic, String userId, String uName, String email, String password, Boolean privacy) {
        StorageReference storageRef = firebaseStorage.getReference();
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + userId + ".jpg");

        // Upload the picture
        profilePicRef.putBytes(BitmapUtils.bitmapToByteArray(profilePic))
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Model", "Profile picture uploaded successfully!");

                    // After uploading, get the download URL
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save user data to Firestore, including the profile picture URL
                        String profilePicUrl = uri.toString();
                        saveUserToFirestore(userId, uName, email, password, profilePicUrl, privacy);
                    }).addOnFailureListener(e -> {
                        Log.e("Model", "Error getting download URL", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Model", "Error uploading profile picture", e);
                });
    }

    // Utility method to convert Bitmap to byte array
    private static class BitmapUtils {
        public static byte[] bitmapToByteArray(Bitmap bitmap) {
            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private void saveUserToFirestore(String userId, String uName, String email, String password, String profilePicUrl, Boolean privacy) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.set(new User(uName, email, password, profilePicUrl, null, privacy))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Model", "User details saved to Firestore.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Model", "Error saving user to Firestore", e);
                });
    }

    public User login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseUser = firebaseAuth.getCurrentUser();
                        Log.d("Model", "User logged in successfully: " + firebaseUser.getEmail());
                        getUserFromFirebase(firebaseUser.getUid());
                    } else {
                        Log.e("Model", "Login failed", task.getException());
                    }
                });
        return currentUser;
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

    public User getUser() {
        if (currentUser != null) {
            return currentUser;
        }
        return null;
    }

    public void updateUser(String uName, String email, String password, Bitmap profilePic, ArrayList<Group> groups, Boolean privacy) {
        String userId = firebaseUser.getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        currentUser.setuName(uName);
        currentUser.setEmail(email);
        currentUser.setPassword(password);
        currentUser.setPrivacy(privacy);
        currentUser.setGroups(groups);


        // Prepare a map for Firestore updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("uName", uName);
        updates.put("email", email);
        updates.put("privacy", privacy);
        updates.put("groups", groups);

        // Handle profile picture update
        if (profilePic != null) {
            uploadProfilePicture(profilePic, userId, userRef, updates);
        } else {
            updateFirestoreUser(userRef, updates);
        }

        // Update Firebase Authentication email and password (requires re-authentication)
        reauthenticateAndUpdateAuthDetails(email, password);
    }

    private void uploadProfilePicture(Bitmap profilePic, String userId, DocumentReference userRef, Map<String, Object> updates) {
        StorageReference profilePicRef = firebaseStorage.getReference().child("profile_pictures/" + userId + ".jpg");

        profilePicRef.putBytes(BitmapUtils.bitmapToByteArray(profilePic))
                .addOnSuccessListener(taskSnapshot -> {
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("profilePicUrl", uri.toString());
                        updateFirestoreUser(userRef, updates);
                    });
                })
                .addOnFailureListener(e -> Log.e("Model", "Error uploading new profile picture", e));
    }

    private void updateFirestoreUser(DocumentReference userRef, Map<String, Object> updates) {
        userRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d("Model", "User profile updated successfully in Firestore"))
                .addOnFailureListener(e -> Log.e("Model", "Error updating user profile in Firestore", e));
    }

    private void reauthenticateAndUpdateAuthDetails(String newEmail, String newPassword) {
        if (firebaseUser == null || firebaseUser.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), "USER_CURRENT_PASSWORD"); // Replace with actual current password

        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Model", "Re-authentication successful.");

                    if (newEmail != null && !newEmail.equals(firebaseUser.getEmail())) {
                        firebaseUser.verifyBeforeUpdateEmail(newEmail)
                                .addOnSuccessListener(aVoid1 -> Log.d("Model", "Email update verification sent"))
                                .addOnFailureListener(e -> Log.e("Model", "Error sending email update verification", e));
                    }

                    if (newPassword != null && !newPassword.isEmpty()) {
                        firebaseUser.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> Log.d("Model", "Password updated successfully"))
                                .addOnFailureListener(e -> Log.e("Model", "Error updating password", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Model", "Re-authentication failed", e));
    }

    public void logout() {
        firebaseAuth.signOut();
        Log.d("Model", "User logged out");
        currentUser = null;
    }

    // -------------------------------------- Task Functions --------------------------------------
    public Task getTaskByIdAndUser(String id, User user) {
        for (Group group : user.getGroups()) {
            for (Task task : group.getTasks()) {
                if (task.getId().equals(id)) {
                    return task;
                }
            }
        }
        return null;
    }

    public void raiseTaskDataChange() {
        if (firebaseUser == null) {
            Log.e("Model", "No user is logged in.");
            return;
        }
        String userId = firebaseUser.getUid();

        // Set up the real-time listener for tasks
        firestore.collection("users").document(userId).collection("groups")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Model", "Error listening to task data", e);
                        return;
                    }

                    // Clear the current user's groups and reload the groups with their tasks
                    currentUser.setGroups(new ArrayList<>());
                    for (DocumentSnapshot groupDoc : snapshots) {
                        Group group = groupDoc.toObject(Group.class);
                        if (group != null) {
                            // Add tasks to the group
                            firestore.collection("users").document(userId).collection("groups")
                                    .document(group.getId()).collection("tasks")
                                    .get().addOnSuccessListener(taskSnapshots -> {
                                        for (DocumentSnapshot taskDoc : taskSnapshots) {
                                            Task task = taskDoc.toObject(Task.class);
                                            if (task != null) {
                                                group.addTask(task);
                                            }
                                        }
                                    });
                            currentUser.getGroups().add(group);
                        }
                    }
                    Log.d("Model", "Task data has been updated.");
                });
    }

    public void loadTasks() {
        // Ensure the user is logged in
        if (firebaseUser == null) {
            Log.e("Model", "No user is logged in.");
            return;
        }

        // Get the user ID
        String userId = firebaseUser.getUid();

        // Get the user's groups from Firestore
        firestore.collection("users").document(userId).collection("groups").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.setGroups(new ArrayList<>());
                        // Loop through the groups and load tasks for each group
                        for (DocumentSnapshot groupDoc : task.getResult()) {
                            Group group = groupDoc.toObject(Group.class);
                            if (group != null) {
                                firestore.collection("users").document(userId)
                                        .collection("groups").document(group.getId())
                                        .collection("tasks").get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                for (DocumentSnapshot taskDoc1 : task1.getResult()) {
                                                    Task taskData = taskDoc1.toObject(Task.class);
                                                    if (taskData != null) {
                                                        group.addTask(taskData);
                                                    }
                                                }
                                            }
                                        });
                                currentUser.getGroups().add(group);
                            }
                        }
                        Log.d("Model", "Tasks loaded successfully.");
                    } else {
                        Log.e("Model", "Error loading tasks from Firestore", task.getException());
                    }
                });
    }

    public void addTask(String title, String details, String groupId, String address, ArrayList<String> shareWithUsers,
                        Date start, Date end, Date remTime, Date date, Date remDate,
                        boolean reminder, boolean important, int colour, int notificationId) {
        // Create task object
        Task task = new Task(title, details, address, shareWithUsers, start, end, remTime, date, remDate,
                reminder, important, colour, notificationId);

        // Find the group to add the task to
        Group group = findGroupById(groupId);
        if (group != null) {
            group.addTask(task);
            saveTaskToFirestore(groupId, task);

            // Share task with other users
            if (shareWithUsers != null) {
                if (!shareWithUsers.contains(currentUser.getuName())) {
                    shareWithUsers.add(currentUser.getuName());
                }
                task.setShareWithUsers(shareWithUsers);
                for (String username : shareWithUsers) {
                    if (username.equals(currentUser.getuName())) continue;
                    User userToShareWith = findUserByUsername(username);
                    if (userToShareWith != null) {
                        Group sharedGroup = findGroupById(groupId, userToShareWith);
                        if (sharedGroup != null) {
                            sharedGroup.addTask(task);
                            saveTaskToFirestoreForOtherUser(userToShareWith.getuName(), sharedGroup.getId(), task);
                        }
                    }
                }
            }
        }
    }

    private Group findGroupById(String groupId) {
        for (Group group : currentUser.getGroups()) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    private Group findGroupById(String groupId, User user) {
        for (Group group : user.getGroups()) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    private void saveTaskToFirestore(String groupId, Task task) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DocumentReference taskRef = firestore.collection("users").document(userId)
                    .collection("groups").document(groupId).collection("tasks").document(task.getId());
            taskRef.set(task)
                    .addOnSuccessListener(aVoid -> Log.d("Model", "Task added to Firestore"))
                    .addOnFailureListener(e -> Log.e("Model", "Error adding task to Firestore", e));
        }
    }

    private void saveTaskToFirestoreForOtherUser(String username, String groupId, Task task) {
        User user = findUserByUsername(username);
        if (user != null) {
            DocumentReference taskRef = firestore.collection("users").document(user.getEmail())
                    .collection("groups").document(groupId).collection("tasks").document(task.getId());
            taskRef.set(task)
                    .addOnSuccessListener(aVoid -> Log.d("Model", "Task added to Firestore for user: " + username))
                    .addOnFailureListener(e -> Log.e("Model", "Error adding task to Firestore for user: " + username, e));
        }
    }

    public void deleteTask(String taskId, String groupId) {
        Task taskToDelete = getTaskByIdAndUser(taskId, currentUser);
        if (taskToDelete != null) {
            // Delete task from Firestore for the current user.
            if (firebaseUser != null) {
                String userId = firebaseUser.getUid();
                DocumentReference taskRef = firestore.collection("users").document(userId)
                        .collection("groups").document(groupId).collection("tasks").document(taskId);
                taskRef.delete()
                        .addOnSuccessListener(aVoid -> Log.d("Model", "Task deleted from Firestore"))
                        .addOnFailureListener(e -> Log.e("Model", "Error deleting task from Firestore", e));
            }

            Group group = findGroupById(groupId);
            if (group != null) {
                group.removeTask(taskToDelete);
            }

            // Remove task from shared users
            ArrayList<String> sharedUsers = taskToDelete.getShareWithUsers();
            if (sharedUsers != null && !sharedUsers.isEmpty()) {
                for (String username : sharedUsers) {
                    User userToShareWith = findUserByUsername(username);
                    if (userToShareWith != null) {
                        Group sharedGroup = findGroupById(groupId, userToShareWith);
                        if (sharedGroup != null) {
                            sharedGroup.removeTask(taskToDelete);
                            DocumentReference sharedTaskRef = firestore.collection("users").document(userToShareWith.getEmail())
                                    .collection("groups").document(groupId).collection("tasks").document(taskId);
                            sharedTaskRef.delete()
                                    .addOnSuccessListener(aVoid -> Log.d("Model", "Shared task deleted for user: " + username))
                                    .addOnFailureListener(e -> Log.e("Model", "Error deleting shared task for user: " + username, e));
                        }
                    }
                }
            }
        }
    }
}

