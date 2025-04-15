package com.example.xplore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class signup extends AppCompatActivity {
    EditText nameField, emailField, phoneField, passwordField;
    Button signupButton;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI components
        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        passwordField = findViewById(R.id.passwordField);
        signupButton = findViewById(R.id.signupButton);

        // Initialize database handler
        dbHandler = new DBHandler(this);

        // Handle signup button click
        signupButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Validate input fields
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(signup.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(signup.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash the password securely using bcrypt
            String hashedPassword = PasswordHasher.hashPassword(password);

            // Insert user into the database
            boolean isInserted = dbHandler.insertUser(name, email, phone, hashedPassword);
            if (isInserted) {
                Toast.makeText(signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(signup.this, login.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(signup.this, "Signup failed. User may already exist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
