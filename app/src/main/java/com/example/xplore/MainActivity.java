package com.example.xplore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    TextView txt_login;
    Button btn_get_started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            // User is logged in, go directly to LandingPage
            String email = preferences.getString("email", "");
            Intent intent = new Intent(this, LandingPage.class);
            intent.putExtra("email", email);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Continue with normal MainActivity setup for non-logged in users
        setContentView(R.layout.activity_main);

        txt_login = findViewById(R.id.registerLink);
        btn_get_started = findViewById(R.id.get_started);

        txt_login.setOnClickListener(view -> {
            Intent Login = new Intent(view.getContext(), login.class);
            startActivity(Login);
        });

        btn_get_started.setOnClickListener(view -> {
            Intent Signup = new Intent(view.getContext(), signup.class);
            startActivity(Signup);
        });
    }
}
