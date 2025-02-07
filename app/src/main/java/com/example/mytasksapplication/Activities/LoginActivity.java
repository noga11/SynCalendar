package com.example.mytasksapplication.Activities;

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

import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvScreenTitle, tvLoginSignUp, tvQuestion, tvPublicOrPrivate;
    private TextInputLayout tilEmail;
    private ImageButton imgbtnPicture;
    private Button btnEnter;
    private TextInputEditText tietUsername, tietEmail, tietPassword;
    private RadioButton rbtnPublic, rbtnPrivate;
    private boolean LOrSChecked = true, privacy;
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
        tietUsername = findViewById(R.id.tietUsername);
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
                tvPublicOrPrivate.setVisibility(View.GONE);
                rbtnPublic.setVisibility(View.GONE);
                rbtnPrivate.setVisibility(View.GONE);
                LOrSChecked = false;
            }
            else{ // Sign Up screen
                tvScreenTitle.setText("Sign Up");
                tvLoginSignUp.setText("Login");
                tvQuestion.setText("Already have an account?");
                tilEmail.setVisibility(View.VISIBLE);
                tvPublicOrPrivate.setVisibility(View.VISIBLE);
                rbtnPublic.setVisibility(View.VISIBLE);
                rbtnPrivate.setVisibility(View.VISIBLE);
                LOrSChecked = true;
            }
        });

        activityStartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle result if needed
                }
        );
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
        if (view == rbtnPublic){
            privacy = false;
        }
        else if (view == rbtnPrivate){
            privacy = true;
        }
        if (view == btnEnter){
            if (LOrSChecked){ //Login screen
                if (model.login(tietUsername.toString(), tietPassword.toString())!=null){
                    activityStartLauncher.launch(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
            else{ // Sign Up screen
                String username = tietUsername.getText().toString();
                String email = tietEmail.getText().toString();
                String password = tietPassword.getText().toString();
                Bitmap userProfilePic = ((BitmapDrawable) imgbtnPicture.getDrawable()).getBitmap();
                Log.d("LoginActivity", "Username: " + username + ", Email: " + email + ", Password: " + password); // Check values
                try {
                    model.createUser(username, email, password, userProfilePic, privacy);
                    activityStartLauncher.launch(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "username already exists, choose a different one;", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}