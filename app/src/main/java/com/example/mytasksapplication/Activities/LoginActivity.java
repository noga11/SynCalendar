package com.example.mytasksapplication.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytasksapplication.R;
import com.example.mytasksapplication.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextView tvScreenTitle, tvLoginSignUp, tvQuestion;
    private TextInputLayout tilEmail;
    private TextInputEditText tietUsername, tietEmail,tietPassword;
    private boolean LOrSChecked = true;
    private String source;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvLoginSignUp = findViewById(R.id.tvLoginSignUp);
        tvQuestion = findViewById(R.id.tvQuestion);
        tilEmail = findViewById(R.id.tilEmail);
        tietUsername = findViewById(R.id.tietUsername);

        if ("action_profile".equals(source)){
            tvScreenTitle.setText("Your Profile");
            tvLoginSignUp.setVisibility(View.GONE);
            tvQuestion.setVisibility(View.GONE);
        }

        tvLoginSignUp.setOnClickListener(v -> {
            if (LOrSChecked){ //Login screen
                tvScreenTitle.setText("Login");
                tvLoginSignUp.setText("Sign up");
                tvQuestion.setText("Don't have an account?");
                tilEmail.setVisibility(View.GONE);
                LOrSChecked = false;
            }
            else{ // Sign Up screen
                tvScreenTitle.setText("Sign Up");
                tvLoginSignUp.setText("Login");
                tvQuestion.setText("Already Have an account?");
                tilEmail.setVisibility(View.VISIBLE);
                LOrSChecked = true;
            }
        });
    }
}