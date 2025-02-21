package com.example.mytasksapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.material.chip.Chip;
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
                        getUser(firebaseUser.getUid());
                    } else {
                        Log.e("Model", "Login failed", task.getException());
                    }
                });
        return currentUser;
    }

    private void getUser(String userId) {
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
        firebaseAuth.signOut();
        Log.d("Model", "User logged out");
        currentUser = null;
    }

    // -------------------------------------- Task Functions --------------------------------------

    public Task getTaskByIdAndUser(String id, User user) {
        for (Task task : user.getTasks()) {
            if (task.getId().equals(id)) {
                return task;
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
        firestore.collection("users").document(userId).collection("tasks")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Model", "Error listening to task data", e);
                        return;
                    }

                    // Clear the current task list and reload the tasks
                    currentUser.setTasks(new ArrayList<>());
                    for (DocumentSnapshot document : snapshots) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            currentUser.getTasks().add(task);
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

        // Get the user's tasks from Firestore
        firestore.collection("users").document(userId).collection("tasks").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.setTasks(new ArrayList<>());
                        // Loop through the task documents and convert them to Task objects
                        for (DocumentSnapshot document : task.getResult()) {
                            Task taskData = document.toObject(Task.class);
                            if (taskData != null) {
                                currentUser.getTasks().add(taskData);
                            }
                        }
                        Log.d("Model", "Tasks loaded successfully.");
                    } else {
                        Log.e("Model", "Error loading tasks from Firestore", task.getException());
                    }
                });
    }

    public void addTask(String title, String details, String group, String adress, ArrayList<String> shareWithUsers,
                        Date start, Date end, Date remTime, Date date, Date remDate,
                        boolean reminder, boolean important, int colour) {
        // Create task object
        Task task = new Task(title, details, group, adress, shareWithUsers, start, end, remTime, date, remDate,
                reminder, important, colour);

        if (currentUser.getTasks() == null) {
            currentUser.setTasks(new ArrayList<>());
        }
        currentUser.getTasks().add(task);

        // Save task to Firestore
        saveTaskToFirestore(task);

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
                    if (userToShareWith.getTasks() == null) {
                        userToShareWith.setTasks(new ArrayList<>());
                    }
                    userToShareWith.getTasks().add(task);
                    // actually sharing
                    saveTaskToFirestoreForOtherUser(userToShareWith.getuName(), task);
                }
            }
        }
    }

    // Save task to Firestore for currentUser
    private void saveTaskToFirestore(Task task) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DocumentReference taskRef = firestore.collection("users").document(userId).collection("tasks").document(task.getId());
            taskRef.set(task)
                    .addOnSuccessListener(aVoid -> Log.d("Model", "Task added to Firestore"))
                    .addOnFailureListener(e -> Log.e("Model", "Error adding task to Firestore", e));
        }
    }

    // Save task to Firestore for another user
    private void saveTaskToFirestoreForOtherUser(String username, Task task) {
        User user = findUserByUsername(username);
        if (user != null) {
            DocumentReference taskRef = firestore.collection("users").document(user.getEmail()).collection("tasks").document(task.getId());
            taskRef.set(task)
                    .addOnSuccessListener(aVoid -> Log.d("Model", "Task added to Firestore for user: " + username))
                    .addOnFailureListener(e -> Log.e("Model", "Error adding task to Firestore for user: " + username, e));
        }
    }

    // Changed method signature to use taskId for proper task identification
    public void updateTask(String taskId, String title, String details, String group, String adress, ArrayList<String> shareWithUsers,
                           Date start, Date end, Date remTime, Date date, Date remDate,
                           boolean reminder, boolean important, int colour) {
        // Update the task for the current user
        Task task = getTaskByIdAndUser(taskId, currentUser);
        if (task != null) {
            task.setTitle(title);
            task.setDetails(details);
            task.setGroup(group);
            task.setAdress(adress);
            task.setShareWithUsers(shareWithUsers);
            task.setStart(start);
            task.setEnd(end);
            task.setReminder(reminder);
            task.setRemTime(remTime);
            task.setRemDate(remDate);
            task.setImportant(important);
            task.setColour(colour);
            updateTaskForCurrentUser(task);
        }

        // Update the task for the users it's shared with
        if (shareWithUsers != null) {
            for (String username : shareWithUsers) {
                if (username.equals(currentUser.getuName())) continue;
                User userToShareWith = findUserByUsername(username);
                if (userToShareWith != null) {
                    Task sharedTask = getTaskByIdAndUser(taskId, userToShareWith);
                    if (sharedTask != null) {
                        sharedTask.setTitle(title);
                        sharedTask.setDetails(details);
                        sharedTask.setGroup(group);
                        sharedTask.setAdress(adress);
                        sharedTask.setShareWithUsers(shareWithUsers);
                        sharedTask.setStart(start);
                        sharedTask.setEnd(end);
                        sharedTask.setReminder(reminder);
                        sharedTask.setRemTime(remTime);
                        sharedTask.setRemDate(remDate);
                        sharedTask.setImportant(important);
                        sharedTask.setColour(colour);
                        updateTaskForSharingUsers(userToShareWith, sharedTask);
                    }
                }
            }
        }
    }

    private void updateTaskForCurrentUser(Task task) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DocumentReference taskRef = firestore.collection("users").document(userId)
                    .collection("tasks").document(task.getId());
            taskRef.set(task)
                    .addOnSuccessListener(aVoid -> Log.d("Model", "Task updated in Firestore"))
                    .addOnFailureListener(e -> Log.e("Model", "Error updating task in Firestore", e));
        }
    }
    private void updateTaskForSharingUsers(User otherUser, Task task) {
        DocumentReference taskRef = firestore.collection("users")
                .document(otherUser.getEmail()) // Consider using a unique id like uid instead of email.
                .collection("tasks")
                .document(task.getId());
        taskRef.set(task)
                .addOnSuccessListener(aVoid ->
                        Log.d("Model", "Shared task updated for user: " + otherUser.getuName()))
                .addOnFailureListener(e ->
                        Log.e("Model", "Error updating shared task for user: " + otherUser.getuName(), e));
    }

    public void deleteTask(String taskId) {
        Task taskToDelete = getTaskByIdAndUser(taskId, currentUser);
        if (taskToDelete != null) {
            // Delete task from Firestore for the current user.
            if (firebaseUser != null) {
                String userId = firebaseUser.getUid();
                DocumentReference taskRef = firestore.collection("users").document(userId)
                        .collection("tasks").document(taskId);
                taskRef.delete()
                        .addOnSuccessListener(aVoid -> Log.d("Model", "Task deleted from Firestore"))
                        .addOnFailureListener(e -> Log.e("Model", "Error deleting task from Firestore", e));
            }
            currentUser.getTasks().remove(taskToDelete);

            // Remove the task from any shared users.
            ArrayList<String> sharedUsers = taskToDelete.getShareWithUsers();
            if (sharedUsers != null && !sharedUsers.isEmpty()) {
                for (String username : sharedUsers) {
                    User userToShareWith = findUserByUsername(username);
                    if (userToShareWith != null) {
                        Task sharedTask = getTaskByIdAndUser(taskId, userToShareWith);
                        if (sharedTask != null) {
                            userToShareWith.getTasks().remove(sharedTask);
                            DocumentReference sharedTaskRef = firestore.collection("users").document(userToShareWith.getEmail())
                                    .collection("tasks").document(taskId);
                            sharedTaskRef.delete()
                                    .addOnSuccessListener(aVoid -> Log.d("Model", "Shared task deleted for user: " + username))
                                    .addOnFailureListener(e -> Log.e("Model", "Error deleting shared task for user: " + username, e));
                        }
                    }
                }
            }
        }
    }

    public ArrayList<Task> tempData() {
        ArrayList<Task> tasks = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.set(2025, Calendar.FEBRUARY, 5, 9, 0);
        Date start1 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 5, 10, 0);
        Date end1 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 5, 8, 30);
        Date remTime1 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 4);
        Date remDate1 = calendar.getTime();

        tasks.add(new Task("Task 1", "Details for Task 1", "Work", "1234 Address St", new ArrayList<>(),
                start1, end1, remTime1, start1, remDate1,
                true, true, 0xFF00FF00));

        calendar.set(2025, Calendar.FEBRUARY, 10, 11, 0);
        Date start2 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 10, 12, 0);
        Date end2 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 10, 10, 30);
        Date remTime2 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 9);
        Date remDate2 = calendar.getTime();

        tasks.add(new Task("Task 2", "Details for Task 2", "Home", "5678 Another Rd", new ArrayList<>(),
                start2, end2, remTime2, start2, remDate2,
                false, false, 0xFFFF0000));

        return tasks;
    }
}
