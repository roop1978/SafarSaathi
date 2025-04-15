package com.example.xplore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SearchPlaces extends AppCompatActivity {
    private static final String TAG = "SearchPlaces";

    // UI Components
    private Button find, clear;
    private EditText filterText;
    private ListView emergencyList;
    private ImageButton home, profile;

    // Data variables
    private double latitude, longitude;
    private ArrayList<String> list_values = new ArrayList<>();
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_places);

        // Initialize UI components
        filterText = findViewById(R.id.filter_text);
        emergencyList = findViewById(R.id.emergency_list);
        find = findViewById(R.id.find_button);
        clear = findViewById(R.id.clear_button);
        home = findViewById(R.id.homeButton);
        profile = findViewById(R.id.profileButton);

        // Get data from intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        // Set up navigation
        setupNavigation();

        // Set up button actions
        setupButtonActions();

        // Load emergency contacts for user's location
        loadEmergencyContacts();
    }

    private void setupNavigation() {
        // Home button now navigates to places nearby (LandingPage)
        home.setOnClickListener(v -> {
            Intent homeIntent = new Intent(SearchPlaces.this, LandingPage.class);
            homeIntent.putExtra("email", email);
            startActivity(homeIntent);
        });

        // Profile button navigation
        profile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(SearchPlaces.this, Profile.class);
            profileIntent.putExtra("email", email);
            startActivity(profileIntent);
        });
    }

    private void setupButtonActions() {
        // Filter button functionality
        find.setOnClickListener(v -> {
            if (filterText.getText().toString().isEmpty()) {
                Toast.makeText(SearchPlaces.this, "Please enter a filter term", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<String> filteredValues = filterValues(list_values, filterText.getText().toString());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchPlaces.this,
                    android.R.layout.simple_list_item_1, filteredValues);
            emergencyList.setAdapter(adapter);
        });

        // Clear button functionality
        clear.setOnClickListener(v -> {
            filterText.setText("");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchPlaces.this,
                    android.R.layout.simple_list_item_1, list_values);
            emergencyList.setAdapter(adapter);
        });

        // List item click handler for phone dialing
        emergencyList.setOnItemClickListener((adapterView, view, i, l) -> {
            String number = (String) adapterView.getItemAtPosition(i);
            String[] parts = number.split(" : ");
            if (parts.length == 2) {
                String phone = parts[1].replaceAll("\\s", "");
                Toast.makeText(SearchPlaces.this, "Calling " + phone, Toast.LENGTH_SHORT).show();
                dialPhoneNumber(phone);
            } else {
                Toast.makeText(SearchPlaces.this, "Invalid format", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEmergencyContacts() {
        // Get state information from the DBHandler using email
        DBHandler dbHandler = new DBHandler(this);
        String address = dbHandler.getAddress(email);
        String state = extractStateFromAddress(address);

        if (state.isEmpty()) {
            // Try to get state from coordinates
            state = "Karnataka"; // Default to Karnataka if we can't determine state
        }

        // Call the API with the state information
        postData(state);
    }

    private String extractStateFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "";
        }

        // Try to extract state from stored address
        String[] parts = address.split(", ");
        for (String part : parts) {
            // This is a simple attempt - might need refinement based on actual address format
            if (part.equals("Karnataka") || part.equals("Maharashtra") || part.equals("Tamil Nadu") ||
                    part.equals("Delhi") || part.equals("Uttar Pradesh")) {
                return part;
            }
        }
        return "";
    }

    private void postData(String location) {
        Log.d(TAG, "Sending location: " + location);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://xplore-vkzl.onrender.com/";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("location", location);
            Log.d(TAG, "Request body: " + jsonBody.toString());
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Log.d(TAG, "Success response: " + response.toString());
                    try {
                        list_values.clear(); // Avoid duplicates
                        Iterator<String> keys = response.keys();
                        while (keys.hasNext()) {
                            String agency = keys.next();
                            String phone = response.getString(agency);
                            String item = agency + " : " + phone;
                            list_values.add(item);
                            Log.d(TAG, "Added item: " + item);
                        }

                        emergencyList.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchPlaces.this,
                                android.R.layout.simple_list_item_1, list_values);
                        emergencyList.setAdapter(adapter);

                        // Apply filter if entered
                        if (filterText.getText().length() > 0) {
                            find.performClick();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e(TAG, "Error response: " + error.toString());
                    Toast.makeText(SearchPlaces.this, "Error: Unable to retrieve emergency services",
                            Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Filter emergency service contacts by search text
     */
    private ArrayList<String> filterValues(ArrayList<String> values, String searchText) {
        ArrayList<String> filteredValues = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().contains(searchText.toLowerCase())) {
                filteredValues.add(value);
            }
        }
        return filteredValues;
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}