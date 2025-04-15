package com.example.xplore;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "xplore.db";
    private static final int DB_VERSION = 4;

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE USERS (id integer primary key autoincrement, name text, email text, phone text, password text, image blob, address text)";
        db.execSQL(query);
    }

    public boolean insertUser(String name, String email, String phone, String hashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", hashedPassword);
        long result = db.insert("USERS", null, values);
        db.close();
        return result != -1; // Return true if insertion is successful
    }

    public boolean validateUser(Context context, String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE email = ? AND password = ?", new String[]{email, password});
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS USERS");
        onCreate(db);
    }

    @SuppressLint("Range")
    public ArrayList<String> giveDetails(String email) {
        SQLiteDatabase db1 = this.getReadableDatabase();
        Cursor cursor1 = db1.rawQuery("SELECT * FROM USERS WHERE email = ?", new String[]{email});
        ArrayList<String> details = new ArrayList<>();
        if (cursor1.getCount() > 0) {
            cursor1.moveToFirst();
            // Add details to the list
            details.add(cursor1.getString(cursor1.getColumnIndex("name")));
            details.add(cursor1.getString(cursor1.getColumnIndex("email")));
            details.add(cursor1.getString(cursor1.getColumnIndex("phone")));
            details.add(cursor1.getString(cursor1.getColumnIndex("address")));
            try {
                details.add(cursor1.getString(cursor1.getColumnIndex("image")));
            } catch (Exception ignored) {}
        }
        cursor1.close();
        db1.close(); // Close the database
        return details;
    }

    public void updatePfp(String email, String imageString) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image", imageString);
        db.update("USERS", values, "email = ?", new String[]{email});
        db.close();
    }

    public void setAddress(String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("address", address);
        db.update("USERS", values, "email = ?", new String[]{email});
        db.close();
    }

    public String getPasswordHash(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM USERS WHERE email=?", new String[]{email});
        String password = null;
        if (cursor.moveToFirst()) {
            password = cursor.getString(0); // Return the hashed password
        }
        cursor.close();
        db.close();
        return password; // Return null if no user is found
    }

    public String getAddress(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE email = ?", new String[]{email});
        String address = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            @SuppressLint("Range") String retrieved = cursor.getString(cursor.getColumnIndex("address"));
            address = retrieved;
        }
        cursor.close();
        db.close();
        return address;
    }
}
