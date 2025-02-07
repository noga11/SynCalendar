package com.example.mytasksapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Model {
    private static Model instance;
    private Context context;
    private User currentUser;
    private SharedPreferences sp;

    private ArrayList<User> allUsers = new ArrayList<>();

    public Model(Context context) {
        this.context = context;
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

    public void createUser(String uName, String email, String password, Bitmap profilePic, Boolean privacy) throws Exception {
        if (findUserByUsername(uName) != null) throw new Exception("Username already exists");
        User newUser = new User(uName, email, password, null, profilePic, privacy);
        allUsers.add(newUser);
        currentUser = newUser;
        Log.d("Model", "User created successfully: " + uName); // Add a log to confirm
    }

    public User login(String email, String password) {
        for (User u : allUsers) {
            if (u.getuName().equals(email) && u.getPassword().equals(password)) {
                currentUser = u;
                return currentUser;
            }
        }
        return null;
    }

    public void logout() {
        currentUser = null;
    }

    // Tasks Functions
    public Task getTaskByTitleAndUser(String title, User user) {
        for (Task task : user.getTasks()) {
            if (task.getTitle().equals(title)) {
                return task;
            }
        }
        return null;
    }

    public void addTask(String title, String details, ArrayList<String> group, ArrayList<String> shareWithUsers,
                        Date start, Date end, Date remTime, Date date, Date remDate,
                        boolean reminder, boolean important, boolean started, int progress, int colour) {
        Task task = new Task(title, details, group, shareWithUsers, start, end, remTime, date, remDate,
                reminder, important, started, progress, colour);
        currentUser.getTasks().add(task);

        // Share task with others
        if (shareWithUsers != null) {
            shareWithUsers.add(currentUser.getuName());
            for (String username : shareWithUsers) {
                User userToShareWith = findUserByUsername(username);
                shareWithUsers.remove(username);
                task = new Task(title, details, group, shareWithUsers, start, end, remTime, date, remDate,
                        reminder, important, started, progress, colour);
                userToShareWith.getTasks().add(task);
                shareWithUsers.add(username);
            }
        }
    }

    public void updateTask(String title, String details, ArrayList<String> group, ArrayList<String> shareWithUsers,
                           Date start, Date end, Date remTime, Date date, Date remDate,
                           boolean reminder, boolean important, boolean started, int progress) {
        // Update the task for the current user
        Task task = getTaskByTitleAndUser(title, currentUser);
        if (task != null) {
            task.setTitle(title);
            task.setDetails(details);
            task.setGroup(group);
            task.setShareWithUsers(shareWithUsers);
            task.setStart(start);
            task.setEnd(end);
            task.setReminder(reminder);
            task.setRemTime(remTime);
            task.setRemDate(remDate);
            task.setImportant(important);
            task.setStarted(started);
            task.setProgress(progress);
        }

        // Update the task for the users it's shared with
        if (shareWithUsers != null) {
            for (String username : shareWithUsers) {
                User userToShareWith = findUserByUsername(username);
                if (userToShareWith != null) {
                    Task sharedTask = getTaskByTitleAndUser(title, userToShareWith);
                    if (sharedTask != null) {
                        sharedTask.setTitle(title);
                        sharedTask.setDetails(details);
                        sharedTask.setGroup(group);
                        sharedTask.setShareWithUsers(shareWithUsers);
                        sharedTask.setStart(start);
                        sharedTask.setEnd(end);
                        sharedTask.setReminder(reminder);
                        sharedTask.setRemTime(remTime);
                        sharedTask.setRemDate(remDate);
                        sharedTask.setImportant(important);
                        sharedTask.setStarted(started);
                        sharedTask.setProgress(progress);
                    }
                }
            }
        }
    }

    public void deleteTask(String title) {
        Task taskToDelete = getTaskByTitleAndUser(title, currentUser);

        // Remove currentUser from other users' shared list
        ArrayList<String> shareWithUsers = taskToDelete.getShareWithUsers();
        if (shareWithUsers != null) {
            for (String username : shareWithUsers) {
                User userToShareWith = findUserByUsername(username);
                if (userToShareWith != null) {
                    Task sharedTask = getTaskByTitleAndUser(title, userToShareWith);
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

        tasks.add(new Task("Task 1", "Details for Task 1", new ArrayList<>(), new ArrayList<>(),
                start1, end1, remTime1, start1, remDate1,
                true, true, false, 50, 0xFF00FF00));

        calendar.set(2025, Calendar.FEBRUARY, 10, 11, 0);
        Date start2 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 10, 12, 0);
        Date end2 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 10, 10, 30);
        Date remTime2 = calendar.getTime();
        calendar.set(2025, Calendar.FEBRUARY, 9);
        Date remDate2 = calendar.getTime();

        tasks.add(new Task("Task 2", "Details for Task 2", new ArrayList<>(), new ArrayList<>(),
                start2, end2, remTime2, start2, remDate2,
                false, false, false, 20, 0xFFFF0000));

        return tasks;
    }
}
