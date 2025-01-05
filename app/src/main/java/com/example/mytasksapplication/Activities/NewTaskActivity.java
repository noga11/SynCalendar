package com.example.mytasksapplication.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;

public class NewTaskActivity extends AppCompatActivity {

    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
    }
}