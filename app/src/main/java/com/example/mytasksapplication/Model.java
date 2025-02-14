package com.example.mytasksapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Model {
    private static Model instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private Context context;
    private User currentUser;

    private ArrayList<User> allUsers = new ArrayList<>();

    public Model(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public static Model getInstance(Context context) {
        if (instance == null) instance = new Model(context);
        return instance;
    }

    // User Functions
    private User findUserByUsername(String username) {
        for (User u : allUsers) {
            if (u.getuName().equals(username)) return u;
        }
        return null;
    }

    public void createUser(String uName, String email, String password, Bitmap profilePic, Boolean privacy) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseUser = firebaseAuth.getCurrentUser();
                        Log.d("Model", "User created successfully: " + currentUser.getEmail());
                        currentUser = new User(uName, email, password, null, profilePic, privacy);
//                        allUsers.add(currentUser);
                        saveUserToFirestore(firebaseUser.getUid(), uName, email, profilePic, privacy);

                    } else {
                        Log.e("Model", "User creation failed", task.getException());
                    }
                });
    }

    private void saveUserToFirestore(String userId, String uName, String email, Bitmap profilePic, Boolean privacy) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.set(new User(uName, email, null, null, profilePic, privacy))
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
                        Log.d("Model", "User logged in successfully: " + currentUser.getEmail());
                        getUserFromFirestore(firebaseUser.getUid());
                    } else {
                        Log.e("Model", "Login failed", task.getException());
                    }
                });
        return currentUser;
    }

    private void getUserFromFirestore(String userId) {
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

    // Tasks Functions
    public Task getTaskByIdAndUser(String id, User user) {
        for (Task task : user.getTasks()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    public void addTask(String title, String details, String group, String adress, ArrayList<String> shareWithUsers,
                        Date start, Date end, Date remTime, Date date, Date remDate,
                        boolean reminder, boolean important, int colour) {
        // Create task object
        Task task = new Task(title, details, group, adress, shareWithUsers, start, end, remTime, date, remDate,
                reminder, important, colour);
        currentUser.getTasks().add(task);

        // Save task to Firestore
        saveTaskToFirestore(task);

        // Share task with other users
        if (shareWithUsers != null) {
            shareWithUsers.add(currentUser.getuName());
            for (String username : shareWithUsers) {
                // update shareWithUsers
                shareWithUsers.remove(username);
                task.setShareWithUsers(shareWithUsers);
                User userToShareWith = findUserByUsername(username);
                userToShareWith.getTasks().add(task);
                // actually sharing
                if (userToShareWith != null) {
                    userToShareWith.getTasks().add(task);
                    saveTaskToFirestoreForUser(userToShareWith.getuName(), task);
                }
                shareWithUsers.add(username);
            }
        }
    }

    // Save task to Firestore
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
    private void saveTaskToFirestoreForUser(String username, Task task) {
        User user = findUserByUsername(username);
        if (user != null) {
            DocumentReference taskRef = firestore.collection("users").document(user.getEmail()).collection("tasks").document(task.getId());
            taskRef.set(task)
                    .addOnSuccessListener(aVoid -> Log.d("Model", "Task added to Firestore for user: " + username))
                    .addOnFailureListener(e -> Log.e("Model", "Error adding task to Firestore for user: " + username, e));
        }
    }

    public void updateTask(String title, String details, String group, String adress, ArrayList<String> shareWithUsers,
                           Date start, Date end, Date remTime, Date date, Date remDate,
                           boolean reminder, boolean important, int colour) {
        // Update the task for the current user
        Task task = getTaskByIdAndUser(title, currentUser);
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
        }

        // Update the task for the users it's shared with
        if (shareWithUsers != null) {
            for (String username : shareWithUsers) {
                User userToShareWith = findUserByUsername(username);
                if (userToShareWith != null) {
                    Task sharedTask = getTaskByIdAndUser(title, userToShareWith);
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
                    }
                }
            }
        }
    }

    public void deleteTask(String title) {
        Task taskToDelete = getTaskByIdAndUser(title, currentUser);

        // Remove currentUser from other users' shared list
        ArrayList<String> shareWithUsers = taskToDelete.getShareWithUsers();
        if (shareWithUsers != null) {
            for (String username : shareWithUsers) {
                User userToShareWith = findUserByUsername(username);
                if (userToShareWith != null) {
                    Task sharedTask = getTaskByIdAndUser(title, userToShareWith);
                    if (sharedTask != null) {
                        ArrayList<String> eachShare = sharedTask.getShareWithUsers();
                        eachShare.remove(currentUser);
                        sharedTask.setShareWithUsers(eachShare);
                    }
                }
            }
        }

        // Remove task for the current user
        currentUser.getTasks().remove(taskToDelete);
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
