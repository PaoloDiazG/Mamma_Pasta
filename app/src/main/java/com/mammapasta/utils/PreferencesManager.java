package com.mammapasta.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MAMMAPASTA_PREFS";

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean("isLoggedIn", loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public void setEmail(String email) {
        prefs.edit().putString("email", email).apply();
    }

    public String getEmail() {
        return prefs.getString("email", null);
    }
}
