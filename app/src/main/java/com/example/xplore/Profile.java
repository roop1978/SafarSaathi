package com.example.xplore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    DBHandler dbHandler;
    String email;
    ImageButton home, search;
    ImageView pfp;
    TextView address,phone,emailTV,person_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        email = getIntent().getStringExtra("email");

        dbHandler = new DBHandler(this);
        String email = getIntent().getStringExtra("email");

        home = (ImageButton) findViewById(R.id.homeButton);
        pfp = (ImageView) findViewById(R.id.pfp);
        search = (ImageButton) findViewById(R.id.searchButton);
        address = (TextView) findViewById(R.id.person_address);
        phone = (TextView) findViewById(R.id.person_phone);
        emailTV = (TextView) findViewById(R.id.person_email);
        person_name = (TextView) findViewById(R.id.person_name);

        pfp.setImageResource(R.drawable.ic_launcher_foreground);
        ArrayList<String> details = dbHandler.giveDetails(email);

        person_name.setText(details.get(0));
        emailTV.setText(email);
        phone.setText(details.get(2));
        address.setText(details.get(3));

        getPfp(email);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, LandingPage.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        pfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, SearchPlaces.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

    private void getPfp(String email) {
        ArrayList<String> details = dbHandler.giveDetails(email);
        try{
            String image_string = details.get(5);
            if (image_string != null) {
                byte[] decodedString = Base64.decode(image_string, Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                pfp.setImageBitmap(bitmap);
            }
        }
        catch(Exception e){
            pfp.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                bitmap = getResizedBitmap(bitmap, 200, 200);
                bitmap = cropToSquare(bitmap);
                String image_string = encodeBitmapToBase64(bitmap);
                dbHandler.updatePfp(email, image_string);
                pfp.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > 1) {
            finalWidth = (int) ((float)maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float)maxWidth / ratioBitmap);
        }
        return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    }

    public Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newDimension = Math.min(width, height);

        int cropX = (width - newDimension) / 2;
        int cropY = (height - newDimension) / 2;

        return Bitmap.createBitmap(bitmap, cropX, cropY, newDimension, newDimension);
    }

    public String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);  // Use PNG for lossless conversion
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}