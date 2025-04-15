package com.example.xplore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class login extends AppCompatActivity {

    EditText emailField, passwordField;
    Button loginButton;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);

        // Initialize database handler
        dbHandler = new DBHandler(this);

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Validate input fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(login.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(login.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate credentials against the database
            String storedHash = dbHandler.getPasswordHash(email);
            if (storedHash != null && PasswordHasher.verifyPassword(password, storedHash)) {
                // Save login session
                saveLoginSession(email);

                Toast.makeText(login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                // Create intent with proper flags to clear activity stack
                Intent intent = new Intent(login.this, LandingPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("email", email); // Pass user session data securely
                startActivity(intent);
                finish(); // Important: close the login activity
            } else {
                Toast.makeText(login.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Save login session to prevent needing to login again
    private void saveLoginSession(String email) {
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_logged_in", true);
        editor.putString("email", email);
        editor.apply();
    }
}
