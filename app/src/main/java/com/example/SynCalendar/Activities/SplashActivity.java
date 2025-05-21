package com.example.SynCalendar.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private Model model;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        model = Model.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        // First ensure Firebase Auth is signed out
        mAuth.signOut();
        Log.d(TAG, "Firebase Auth signed out");

        new Handler().postDelayed(() -> {
            // After sign out, check if user is still authenticated
            if (mAuth.getCurrentUser() != null) {
                Log.d(TAG, "User still authenticated after signOut, forcing logout");
                mAuth.signOut();
            }
            
            // Start with login activity
            Log.d(TAG, "Redirecting to login screen");
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
