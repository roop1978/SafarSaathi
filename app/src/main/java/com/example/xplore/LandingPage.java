package com.example.xplore;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LandingPage extends AppCompatActivity {
    private static final String TAG = "LandingPage";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;

    // UI Components
    private ListView places_list;
    private Spinner selector;
    private ImageButton searchButton, profile;

    // Location and data variables
    private double latitude, longitude;
    private HashMap<String, String> place_map = new HashMap<>();
    private String email;
    private boolean doubleBackToExitPressedOnce = false;
    private CancellationTokenSource cancellationTokenSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        // Initialize UI components
        places_list = findViewById(R.id.places_list);
        selector = findViewById(R.id.selector);
        profile = findViewById(R.id.profileButton);
        searchButton = findViewById(R.id.searchButton);

        // Get user data
        email = getIntent().getStringExtra("email");

        // Set up navigation buttons
        setupNavigation();

        // Set up back press handling
        setupBackPressHandler();

        // Set up category spinner
        setupCategorySpinner();

        // Check location permissions and get location
        checkLocationPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
    }

    private void setupNavigation() {
        // Search button now navigates to the emergency contacts screen (SearchPlaces)
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPage.this, SearchPlaces.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("email", email);
            startActivity(intent);
        });

        // Profile button navigation
        profile.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPage.this, Profile.class);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }

    private void setupCategorySpinner() {
        // Set up spinner for category selection
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.place_categories));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selector.setAdapter(adapter);

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String query = adapterView.getItemAtPosition(i).toString();
                if (query.equals("All")) {
                    getPlacesNearby(latitude, longitude, null);
                } else {
                    getPlacesNearby(latitude, longitude, query);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                getPlacesNearby(latitude, longitude, null);
            }
        });

        // Set up item click listener for places list
        places_list.setOnItemClickListener((adapterView, view, i, l) -> {
            String placeName = adapterView.getItemAtPosition(i).toString();
            Intent detailsIntent = new Intent(LandingPage.this, PlaceDetails.class);
            detailsIntent.putExtra("fsq_id", place_map.get(placeName));
            detailsIntent.putExtra("distance", place_map.get(placeName + "_distance"));
            detailsIntent.putExtra("email", email);
            detailsIntent.putExtra("latitude", latitude);
            detailsIntent.putExtra("longitude", longitude);
            startActivity(detailsIntent);
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity();
                    return;
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(LandingPage.this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1000);
            }
        });
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Cancel any existing requests
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }

        // Create new cancellation token
        cancellationTokenSource = new CancellationTokenSource();

        // Get current location for better accuracy
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        // Log actual coordinates for debugging
                        Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);

                        // Fetch places nearby with default category
                        getPlacesNearby(latitude, longitude, null);
                    } else {
                        Log.e(TAG, "Location is null");
                        Toast.makeText(this, "Could not get location", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Location retrieval failed", e);
                    Toast.makeText(this, "Location service error", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Fetches places using Foursquare API.
     *
     * @param latitude Latitude of the user's location.
     * @param longitude Longitude of the user's location.
     * @param query Optional query string for filtering places.
     */
    private void getPlacesNearby(double latitude, double longitude, @Nullable String query) {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "Location data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        // Build URL dynamically based on query
        String baseUrl = "https://api.foursquare.com/v3/places/search?";
        String url = baseUrl + "ll=" + latitude + "%2C" + longitude +
                "&radius=5000&limit=50&sort=POPULARITY";
        if (query != null && !query.isEmpty() && !query.equals("All")) {
            url += "&query=" + query; // Add query parameter if provided
        }

        Log.d(TAG, "Foursquare URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        HashMap<String, String> place_map_temp = new HashMap<>();
                        ArrayList<String> list_elements = new ArrayList<>();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            list_elements.add(place.getString("name"));
                            place_map_temp.put(place.getString("name"), place.getString("fsq_id"));
                            place_map_temp.put(place.getString("name") + "_distance", place.getString("distance"));
                        }

                        // Update ListView with fetched places
                        if (list_elements.isEmpty()) {
                            Toast.makeText(this, "No places found", Toast.LENGTH_SHORT).show();
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, list_elements);
                        places_list.setAdapter(adapter);
                        place_map = place_map_temp;
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "API error", error);
                    Toast.makeText(this, "Failed to fetch places. Please try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", BuildConfig.FOURSQUARE_API_KEY);
                return headers;
            }
        };

        queue.add(request);
    }

    // Helper method to get state/admin area from coordinates (for potential use)
    private String getStateFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String state_val = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                state_val = address.getAdminArea();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return state_val;
    }
}