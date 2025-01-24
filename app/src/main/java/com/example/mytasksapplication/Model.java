package com.example.mytasksapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

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

    //User Functions
    private User findUserByUsername(String username) {
        for (User u : allUsers) {
            if (u.getuName().equals(username)) return u;
        }
        return null;
    }

    public void createUser(String uName, String email, String password, ArrayList<Task> tasks) throws Exception {
        if (findUserByUsername(uName) != null) throw new Exception("Username already exists");
        User newUser = new User(uName, email, password, tasks);
        allUsers.add(newUser);
        currentUser = newUser;
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

    //Tasks functions
    public Task getTaskByTitleAndUser(String title, User user) {
        for (Task task : user.getTasks()) {
            if (task.getTitle().equals(title)) {
                return task;
            }
        }
        return null;
    }

    public void addTask(String title, String details, ArrayList<String> group, ArrayList<String> shareWithUsers, Time start, Time end,
                        Time remTime, Date date, Date remDate, boolean reminder, boolean important,
                        boolean started, int progress, int colour) {
        Task task = new Task(title, details, group, shareWithUsers, start, end, remTime, date, remDate,
                reminder, important, started, progress, colour);
        currentUser.getTasks().add(task);

        // add task for others in shareWithUser
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

    public void updateTask(String title, String details, ArrayList<String> group, ArrayList<String> shareWithUsers, Time start, Time end,
                           Time remTime, Date date, Date remDate, boolean reminder, boolean important,
                           boolean started, int progress) {
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

        // delete currentUser from others shareWithUsers
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

        // delete task for current user
        currentUser.getTasks().remove(taskToDelete);
    }

    public ArrayList<Task> tempData() {
        ArrayList<Task> tasks = new ArrayList<>();

        tasks.add(new Task(
                "Task 1",
                "Details for Task 1",
                new ArrayList<>(),
                new ArrayList<>(),
                Time.valueOf("09:00:00"),
                Time.valueOf("10:00:00"),
                Time.valueOf("08:30:00"),
                Date.valueOf("2025-01-05"),
                Date.valueOf("2025-01-04"),
                true,
                true,
                false,
                50,
                0xFF00FF00 // Green color (hexadecimal representation of a color)
        ));

        tasks.add(new Task(
                "Task 2",
                "Details for Task 2",
                new ArrayList<>(),
                new ArrayList<>(),
                Time.valueOf("11:00:00"),
                Time.valueOf("12:00:00"),
                Time.valueOf("10:30:00"),
                Date.valueOf("2025-01-10"),
                Date.valueOf("2025-01-09"),
                false,
                false,
                false,
                20,
                0xFFFF0000 // Red color (hexadecimal representation of a color)
        ));

        tasks.add(new Task(
                "Task 3",
                "Details for Task 3",
                new ArrayList<>(),
                new ArrayList<>(),
                Time.valueOf("13:00:00"),
                Time.valueOf("14:00:00"),
                Time.valueOf("12:30:00"),
                Date.valueOf("2025-01-15"),
                Date.valueOf("2025-01-14"),
                true,
                false,
                true,
                75,
                0xFF0000FF // Blue color (hexadecimal representation of a color)
        ));

        tasks.add(new Task(
                "Task 4",
                "Details for Task 4",
                new ArrayList<>(),
                new ArrayList<>(),
                Time.valueOf("15:00:00"),
                Time.valueOf("16:00:00"),
                Time.valueOf("14:30:00"),
                Date.valueOf("2025-01-20"),
                Date.valueOf("2025-01-19"),
                false,
                true,
                true,
                100,
                0xFFFFFF00 // Yellow color (hexadecimal representation of a color)
        ));

        tasks.add(new Task(
                "Task 5",
                "Details for Task 5",
                new ArrayList<>(),
                new ArrayList<>(),
                Time.valueOf("17:00:00"),
                Time.valueOf("18:00:00"),
                Time.valueOf("16:30:00"),
                Date.valueOf("2025-01-25"),
                Date.valueOf("2025-01-24"),
                true,
                true,
                false,
                10,
                0xFF00FFFF // Cyan color (hexadecimal representation of a color)
        ));

        return tasks;
    }

}
