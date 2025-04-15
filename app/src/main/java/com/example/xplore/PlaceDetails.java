package com.example.xplore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import java.util.Locale;

public class PlaceDetails extends AppCompatActivity {
    private static final String TAG = "PlaceDetails";
    private SpeechRecognizer speechRecognizer;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    // UI Components
    private TextView placeName, placeCategory, placeLocation, placeDistance, placePhone;
    private Button callButton;
    private ListView reviewsList;
    private ImageButton home, search, profile;

    // Data variables
    private String email;
    private long phoneNumber;
    private double latitude, longitude;
    private String distance;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Initialize UI components
        placeName = findViewById(R.id.place_name);
        placeLocation = findViewById(R.id.place_location);
        placeCategory = findViewById(R.id.place_category);
        placeDistance = findViewById(R.id.place_distance);
        placePhone = findViewById(R.id.place_phone);
        callButton = findViewById(R.id.call_button);
        reviewsList = findViewById(R.id.reviews_list);
        home = findViewById(R.id.homeButton);
        search = findViewById(R.id.searchButton);
        profile = findViewById(R.id.profileButton);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> recognizedWords = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (recognizedWords != null) {
                    String command = recognizedWords.get(0).toLowerCase();
                    if (command.contains("police")) {
                        callEmergency("police");
                    } else if (command.contains("ambulance")) {
                        callEmergency("ambulance");
                    }
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {}
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onError(int error) {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // Voice command button click
        Button voiceButton = findViewById(R.id.voice_command_button);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your emergency command...");

                try {
                    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(PlaceDetails.this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Get data from intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        String fsq_id = intent.getStringExtra("fsq_id");
        distance = intent.getStringExtra("distance");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        if (distance != null) {
            placeDistance.setText("Distance: " + Double.parseDouble(distance) / 1000 + " km");
        }

        // Setup navigation
        setupNavigation();

        // Populate place details and reviews
        populateDetails(fsq_id);
        populateReviews(fsq_id);

        String emergencyType = intent.getStringExtra("emergency_type");
        if (emergencyType != null) {
            if (emergencyType.equals("police")) {
                dialPhoneNumber("100");
            } else if (emergencyType.equals("ambulance")) {
                dialPhoneNumber("102");
            }
        }
    }

    private void setupNavigation() {
        // Home button navigates to places nearby (LandingPage)
        home.setOnClickListener(v -> {
            Intent homeIntent = new Intent(PlaceDetails.this, LandingPage.class);
            homeIntent.putExtra("email", email);
            startActivity(homeIntent);
        });

        // Search button navigates to emergency contacts (SearchPlaces)
        search.setOnClickListener(v -> {
            Intent searchIntent = new Intent(PlaceDetails.this, SearchPlaces.class);
            searchIntent.putExtra("email", email);
            searchIntent.putExtra("latitude", latitude);
            searchIntent.putExtra("longitude", longitude);
            startActivity(searchIntent);
        });

        // Profile button navigation
        profile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(PlaceDetails.this, Profile.class);
            profileIntent.putExtra("email", email);
            startActivity(profileIntent);
        });
    }

    private void populateDetails(String fsqId) {
        if (fsqId == null || fsqId.isEmpty()) {
            Toast.makeText(this, "Invalid place ID", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.foursquare.com/v3/places/" + fsqId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String name = response.getString("name");
                        placeName.setText(name);

                        // Set font size based on name length
                        if(name.split(" ").length > 5) {
                            placeName.setTextSize(25);
                        } else {
                            placeName.setTextSize(30);
                        }

                        // Get location and address
                        JSONObject location = response.getJSONObject("location");
                        String formattedAddress = location.getString("formatted_address");
                        placeLocation.setText(formattedAddress);

                        // Get category
                        JSONArray categoriesArray = response.getJSONArray("categories");
                        if (categoriesArray.length() > 0) {
                            placeCategory.setText(categoriesArray.getJSONObject(0).getString("name"));
                        }
                        placePhone.setText("Phone: 08202923188");
                        placePhone.setVisibility(View.VISIBLE);
                        callButton.setEnabled(true);

                        callButton.setOnClickListener(v -> dialPhoneNumber("08202923188"));

                        Log.d(TAG, "Full Foursquare response: " + response.toString());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing place details", e);
                        Toast.makeText(PlaceDetails.this, "Error loading place details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "API error: " + error.toString());
                    Toast.makeText(PlaceDetails.this, "Error loading place details", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "fsq3ZWuj3scJYRSxdUEIJzOGg8YPEkyyKDeIwD2fsfnrQz4=");
                return headers;
            }
        };

        queue.add(request);
    }

    private void populateReviews(String fsq_id) {
        if (fsq_id == null || fsq_id.isEmpty()) {
            ArrayList<String> noReviews = new ArrayList<>();
            noReviews.add("No reviews available for this place.");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(PlaceDetails.this, android.R.layout.simple_list_item_1, noReviews);
            reviewsList.setAdapter(adapter);
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.foursquare.com/v3/places/" + fsq_id + "/tips?limit=50";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ArrayList<String> reviewList = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject tip = response.getJSONObject(i);
                            String text = tip.getString("text");
                            reviewList.add(text);
                        }

                        if (reviewList.isEmpty()) {
                            reviewList.add("No reviews available for this place.");
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(PlaceDetails.this, android.R.layout.simple_list_item_1, reviewList);
                        reviewsList.setAdapter(adapter);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing reviews", e);
                        setDefaultReviews();
                    }
                },
                error -> {
                    Log.e(TAG, "API error: " + error.toString());
                    setDefaultReviews();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "fsq3ZWuj3scJYRSxdUEIJzOGg8YPEkyyKDeIwD2fsfnrQz4=");
                return headers;
            }
        };

        queue.add(request);
    }

    private void setDefaultReviews() {
        ArrayList<String> noReviews = new ArrayList<>();
        noReviews.add("No reviews available for this place.");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(PlaceDetails.this, android.R.layout.simple_list_item_1, noReviews);
        reviewsList.setAdapter(adapter);
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void callEmergency(String emergencyType) {
        String phoneNumber = "";

        if (emergencyType.equals("police")) {
            phoneNumber = "100"; // Police number
        } else if (emergencyType.equals("ambulance")) {
            phoneNumber = "102"; // Ambulance number
        }

        if (!phoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber)); // Using tel: to trigger the phone dialer
            startActivity(intent); // This will launch the dialer with the number pre-filled
        } else {
            Toast.makeText(this, "Emergency number not available", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
