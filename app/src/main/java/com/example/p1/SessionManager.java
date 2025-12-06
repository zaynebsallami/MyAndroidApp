package com.example.p1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "CashBuddySession";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_BUDGET = "monthly_budget";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        try {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = prefs.edit();
            Log.d(TAG, "SessionManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SessionManager: " + e.getMessage());
        }
    }

    public void saveUser(String email) {
        try {
            editor.putString(KEY_EMAIL, email);
            editor.apply();
            Log.d(TAG, "User saved: " + email);
        } catch (Exception e) {
            Log.e(TAG, "Error saving user: " + e.getMessage());
        }
    }

    public String getUser() {
        try {
            String user = prefs.getString(KEY_EMAIL, null);
            Log.d(TAG, "Getting user: " + user);
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user: " + e.getMessage());
            return null;
        }
    }

    public void saveBudget(double budget) {
        try {
            editor.putFloat(KEY_BUDGET, (float) budget);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving budget: " + e.getMessage());
        }
    }

    public double getBudget() {
        try {
            return prefs.getFloat(KEY_BUDGET, 1000f);
        } catch (Exception e) {
            Log.e(TAG, "Error getting budget: " + e.getMessage());
            return 1000f;
        }
    }

    public void logout() {
        try {
            editor.clear();
            editor.apply();
            Log.d(TAG, "User logged out");
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage());
        }
    }
}