package com.example.xplore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class EmergencyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleEmergencyAction();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:7300426784")); // Replace with your number
        startActivity(callIntent);

    }

    private void handleEmergencyAction() {
        // Get the type of emergency from the intent
        String emergencyType = getIntent().getStringExtra("emergency_type");

        if (emergencyType != null) {
            if (emergencyType.equals("police")) {
                dialPhoneNumber("100");  // Police number
            } else if (emergencyType.equals("ambulance")) {
                dialPhoneNumber("102");  // Ambulance number
            }
        }
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
