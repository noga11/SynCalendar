package com.example.SynCalendar.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;
import com.example.SynCalendar.PhotoHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvScreenTitle, tvLoginSignUp, tvQuestion, tvPublicOrPrivate;
    private TextInputLayout tilEmail;
    private ImageButton imgbtnPicture;
    private Button btnEnter;
    private TextInputEditText tietUsername, tietEmail, tietPassword;
    private RadioButton rbtnPublic, rbtnPrivate;
    private boolean LOrSChecked = true, privacy = true;
    private String source;
    private User currentUser;
    private Model model;
    private ActivityResultLauncher<Intent> activityStartLauncher;

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
        model = Model.getInstance(this);
        source = getIntent().getStringExtra("PROFILE");

        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvLoginSignUp = findViewById(R.id.tvLoginSignUp);
        tvQuestion = findViewById(R.id.tvQuestion);
        tilEmail = findViewById(R.id.tilEmail);
        tvPublicOrPrivate = findViewById(R.id.tvPublicOrPrivate);
        rbtnPublic = findViewById(R.id.rbtnPublic);
        rbtnPrivate = findViewById(R.id.rbtnPrivate);
        imgbtnPicture = findViewById(R.id.imgbtnPicture);
        btnEnter = findViewById(R.id.btnEnter);
        tietUsername = findViewById(R.id.tietUsername);
        tietEmail = findViewById(R.id.tietEmail);
        tietPassword = findViewById(R.id.tietPassword);

        btnEnter.setOnClickListener(this);
        rbtnPublic.setOnClickListener(this);
        rbtnPrivate.setOnClickListener(this);

        if ("action_profile".equals(source)) {
            // User Settings screen
            tvScreenTitle.setText("Settings");
            tvLoginSignUp.setVisibility(View.GONE);
            tvQuestion.setVisibility(View.GONE);
            tietUsername.setVisibility(View.VISIBLE);
            tvPublicOrPrivate.setVisibility(View.VISIBLE);
            rbtnPublic.setVisibility(View.VISIBLE);
            rbtnPrivate.setVisibility(View.VISIBLE);
            imgbtnPicture.setVisibility(View.VISIBLE);

            // Load current user data
            currentUser = model.getCurrentUser();
            if (currentUser != null) {
                Log.d("LoginActivity", "Current user email: " + currentUser.getEmail());
                tietUsername.setText(currentUser.getuName());
                tietEmail.setText(currentUser.getEmail());
                tietPassword.setText(currentUser.getPassword()); // Display masked password

                // Make email and password fields read-only
                tietEmail.setFocusable(false);
                tietEmail.setClickable(false);
                tietPassword.setFocusable(false);
                tietPassword.setClickable(false);

                // Display profile picture if available
                if (currentUser.getProfilePicString() != null) {
                    Bitmap profilePic = PhotoHelper.stringToBitmap(currentUser.getProfilePicString());
                    if (profilePic != null) {
                        imgbtnPicture.setImageBitmap(profilePic);
                    }
                }

                // Set the current privacy setting
                if (currentUser.getPrivacy()) {
                    rbtnPrivate.setChecked(true);
                } else {
                    rbtnPublic.setChecked(true);
                }

                tietPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            // Allow changing profile picture
            imgbtnPicture.setOnClickListener(v -> setUserPicture(v));
        } else {
            // Default to Login screen
            tvScreenTitle.setText("Login");
            tvLoginSignUp.setText("Sign up");
            tvQuestion.setText("Don't have an account?");
            tietUsername.setVisibility(View.GONE);
            tvPublicOrPrivate.setVisibility(View.GONE);
            rbtnPublic.setVisibility(View.GONE);
            rbtnPrivate.setVisibility(View.GONE);
            imgbtnPicture.setVisibility(View.GONE);
            LOrSChecked = false;
        }

        tvLoginSignUp.setOnClickListener(v -> {
            if (LOrSChecked) { // Login screen
                tvScreenTitle.setText("Login");
                tvLoginSignUp.setText("Sign up");
                tvQuestion.setText("Don't have an account?");
                tietUsername.setVisibility(View.GONE);
                tvPublicOrPrivate.setVisibility(View.GONE);
                rbtnPublic.setVisibility(View.GONE);
                rbtnPrivate.setVisibility(View.GONE);
                imgbtnPicture.setVisibility(View.GONE);
                LOrSChecked = false;
            } else { // Sign Up screen
                tvScreenTitle.setText("Sign Up");
                tvLoginSignUp.setText("Login");
                tvQuestion.setText("Already have an account?");
                tietUsername.setVisibility(View.VISIBLE);
                tvPublicOrPrivate.setVisibility(View.VISIBLE);
                rbtnPublic.setVisibility(View.VISIBLE);
                rbtnPrivate.setVisibility(View.VISIBLE);
                imgbtnPicture.setVisibility(View.VISIBLE);
                LOrSChecked = true;
            }
        });

        activityStartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle result if needed
                }
        );

        // Set private as default
        rbtnPrivate.setChecked(true);
        rbtnPublic.setChecked(false);
    }

    private ActivityResultLauncher picLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview()
            , new ActivityResultCallback<Bitmap>() {
                @Override
                public void onActivityResult(Bitmap o) {
                    imgbtnPicture.setImageBitmap(o);
                }
            });

    public void setUserPicture(View view) {
        picLauncher.launch(null);
    }

    @Override
    public void onClick(View view) {
        if (view == rbtnPublic) {
            privacy = false;
        } else if (view == rbtnPrivate) {
            privacy = true;
        }

        if (view == btnEnter) {
            String username = tietUsername.getText().toString().trim();
            String email = tietEmail.getText().toString().trim();
            String password = tietPassword.getText().toString().trim();
            Bitmap userProfilePic = null;
            if (imgbtnPicture.getDrawable() != null) {
                userProfilePic = ((BitmapDrawable) imgbtnPicture.getDrawable()).getBitmap();
            }

            if ("action_profile".equals(source)) {
                // Log current user state before update
                Log.d("LoginActivity", "Before update: " + (model.getCurrentUser() != null ? model.getCurrentUser().getuName() : "null"));

                // Ensure `currentUser` is available
                if (model.getCurrentUser() != null) {
                    model.updateUser(username, email, privacy, userProfilePic);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    // Log current user state after update
                    Log.d("LoginActivity", "After update: " + (model.getCurrentUser() != null ? model.getCurrentUser().getuName() : "null"));

                    finish(); // Return to the previous screen
                    return;
                } else {
                    Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
                }
            } else if (!LOrSChecked) { // Login screen
                String loginEmail = email;
                if (loginEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                model.login(loginEmail, password,
                    user -> {
                        // Success callback
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        activityStartLauncher.launch(intent);
                        finish();
                    },
                    e -> {
                        // Failure callback
                        Toast.makeText(LoginActivity.this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
                    }
                );
            } else { // Sign Up screen
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                model.createUser(username, email, password, privacy, userProfilePic,
                    user -> {
                        // Success callback
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        activityStartLauncher.launch(intent);
                        finish();
                    },
                    e -> {
                        // Failure callback
                        Toast.makeText(LoginActivity.this, "Error creating account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                );
            }
        }
    }
}