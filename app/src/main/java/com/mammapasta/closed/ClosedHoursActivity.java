package com.mammapasta.closed;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.mammapasta.R;

public class ClosedHoursActivity extends AppCompatActivity {

    Button btnSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closed_hours);

        btnSalir = findViewById(R.id.btnSalir);

        btnSalir.setOnClickListener(v -> {
            finishAffinity(); // Cierra todo
            System.exit(0);   // Mata el proceso (lo más extremo)
        });

        // Bloquear botón atrás
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // No hace nada
            }
        });
    }
}
