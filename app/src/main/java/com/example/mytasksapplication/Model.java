package com.example.mytasksapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
        currentUser =null;
    }

    //Tasks functions
    public Task getNoteByTitle(String title) {
        for (Task task : currentUser.getTasks()) {
            if (task.getTitle().equals(title)) {
                return task;
            }
        }
        return null;
    }

    public void addTask(String title, String details, String group, String[] shareWithUser, Time start, Time end,
                        Time remTime, Date date, Date remDate, boolean reminder, boolean important,
                        boolean started, int progress) {
        Task task = new Task(title, details, group, shareWithUser, start, end, remTime, date, remDate,
                            reminder, important,started, progress);
        currentUser.getTasks().add(task);
    }

    public void updateNote(String title, String details, String group, String[] shareWithUser, Time start, Time end,
                           Time remTime, Date date, Date remDate, boolean reminder, boolean important,
                           boolean started, int progress) {
        Task task = getNoteByTitle(title);
        if (task != null) {
            task.setTitle(title);
            task.setDetails(details);
            task.setGroup(group);
            task.setShareWithUser(shareWithUser);
            task.setStart(start);
            task.setEnd(end);
            task.setReminder(reminder);
            task.setRemTime(remTime);
            task.setRemDate(remDate);
            task.setImportant(important);
            task.setStarted(started);
            task.setProgress(progress);
        }
    }

    public void deleteTask(String title) {
        currentUser.getTasks().removeIf(task -> task.getTitle().equals(title));
    }
}
