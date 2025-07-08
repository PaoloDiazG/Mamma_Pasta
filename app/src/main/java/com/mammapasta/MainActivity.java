package com.mammapasta;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.mammapasta.home.HomeActivity;
import com.mammapasta.login.LoginActivity;
import com.mammapasta.utils.PreferencesManager;

public class MainActivity extends AppCompatActivity {

    PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager = new PreferencesManager(this);

        if (preferencesManager.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
