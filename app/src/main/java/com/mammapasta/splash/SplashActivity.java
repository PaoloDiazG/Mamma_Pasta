package com.mammapasta.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mammapasta.R;
import com.mammapasta.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView txtMensaje;
    private String[] mensajes = {
            "¡Horneando la masa!",
            "Agregando salsa de tomate...",
            "Preparando los ingredientes..."
    };

    private Handler handler = new Handler();
    private int currentMessageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.imgLogo);
        progressBar = findViewById(R.id.progressBar);
        txtMensaje = findViewById(R.id.txtMensaje);

        txtMensaje.setText(mensajes[0]);

        // Cambiar mensaje cada segundo
        handler.postDelayed(messageRunnable, 1000);

        // Ir al Login después de 3 segundos
        handler.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 3000);
    }

    private Runnable messageRunnable = new Runnable() {
        @Override
        public void run() {
            currentMessageIndex++;
            if (currentMessageIndex < mensajes.length) {
                txtMensaje.setText(mensajes[currentMessageIndex]);
                handler.postDelayed(this, 1000);
            }
        }
    };
}

