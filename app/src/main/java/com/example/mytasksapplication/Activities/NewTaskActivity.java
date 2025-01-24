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

/*
    // Inside your activity, use ColorPickerDialog to get a color from the user
ColorPickerDialog.newBuilder()
        .setDialogId(0)
    .setAllowCustom(true)
    .setShowAlphaSlider(true)
    .setColor(Color.BLACK)  // Default color
    .setPresets(new int[] {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE})
            .setListener(new ColorPickerDialog.OnColorSelectedListener() {
        @Override
        public void onColorSelected(int color) {
            // Update the line color in the custom calendar view
            CustomCalendarView customCalendarView = findViewById(R.id.calendarView);
            customCalendarView.setLineColor(color);
        }
    }).build().show(getSupportFragmentManager(), "color_picker");
*/

}