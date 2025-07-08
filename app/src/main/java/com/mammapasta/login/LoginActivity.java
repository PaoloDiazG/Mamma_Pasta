package com.mammapasta.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.mammapasta.R;
import com.mammapasta.closed.ClosedHoursActivity;
import com.mammapasta.closed.TimeUtils;
import com.mammapasta.home.HomeActivity;
import com.mammapasta.utils.PreferencesManager;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView txtRegister;
    ImageView loadingPizza;

    FirebaseAuth auth;
    PreferencesManager preferencesManager;

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_TIME_MS = 60000; // 1 minuto
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TimeUtils.isWithinClosedHours()) {
            startActivity(new Intent(this, ClosedHoursActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        loadingPizza = findViewById(R.id.loadingPizza);

        txtRegister.setPaintFlags(txtRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        auth = FirebaseAuth.getInstance();
        preferencesManager = new PreferencesManager(this);

        if (preferencesManager.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> loginUser());
        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        verificarBloqueoPersistente();

        // Manejo moderno del botón "atrás"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarDialogoSalida();
            }
        });
    }

    // Método para verificar la conexión a Internet
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Verificar si hay conexión a internet
        if (!isConnectedToInternet()) {
            Toast.makeText(this, "Verifica tu conexión a Internet", Toast.LENGTH_LONG).show();
            return; // No continuar si no hay conexión
        }

        if (email.isEmpty()) {
            edtEmail.setError("Ingrese su correo electrónico");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Formato de correo inválido");
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Ingrese su contraseña");
            return;
        }

        long bloqueadoHasta = preferencesManager.getBlockUntil();
        if (System.currentTimeMillis() < bloqueadoHasta) {
            iniciarTemporizadorBloqueo(bloqueadoHasta - System.currentTimeMillis());
            Toast.makeText(this, "Demasiados intentos. Espera antes de volver a intentar.", Toast.LENGTH_SHORT).show();
            return;
        }

        startPizzaAnimation(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    startPizzaAnimation(false);
                    if (task.isSuccessful()) {
                        preferencesManager.setLoggedIn(true);
                        preferencesManager.setEmail(email);
                        preferencesManager.setLoginAttempts(0);
                        preferencesManager.setBlockUntil(0);
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        int intentos = preferencesManager.getLoginAttempts() + 1;
                        preferencesManager.setLoginAttempts(intentos);

                        Toast.makeText(this, "Correo o contraseña incorrectos. Intenta nuevamente.", Toast.LENGTH_SHORT).show();

                        if (intentos >= MAX_ATTEMPTS) {
                            long bloqueoHasta = System.currentTimeMillis() + BLOCK_TIME_MS;
                            preferencesManager.setBlockUntil(bloqueoHasta);
                            iniciarTemporizadorBloqueo(BLOCK_TIME_MS);
                            Toast.makeText(this, "Demasiados intentos. Espera 60 segundos.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void verificarBloqueoPersistente() {
        long bloqueadoHasta = preferencesManager.getBlockUntil();
        long ahora = System.currentTimeMillis();

        if (ahora < bloqueadoHasta) {
            iniciarTemporizadorBloqueo(bloqueadoHasta - ahora);
        }
    }

    private void iniciarTemporizadorBloqueo(long duracionMs) {
        btnLogin.setEnabled(false);
        countDownTimer = new CountDownTimer(duracionMs, 1000) {
            public void onTick(long millisUntilFinished) {
                int segundos = (int) (millisUntilFinished / 1000);
                btnLogin.setText("Reintenta en " + segundos + "s");
            }

            public void onFinish() {
                preferencesManager.setLoginAttempts(0);
                preferencesManager.setBlockUntil(0);
                btnLogin.setText("Iniciar sesión");
                btnLogin.setEnabled(true);
            }
        }.start();
    }

    private void startPizzaAnimation(boolean show) {
        if (show) {
            loadingPizza.setVisibility(View.VISIBLE);
            RotateAnimation rotate = new RotateAnimation(
                    0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(1000);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            loadingPizza.startAnimation(rotate);
        } else {
            loadingPizza.clearAnimation();
            loadingPizza.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoSalida() {
        new AlertDialog.Builder(this)
                .setTitle("¿Estás seguro?")
                .setMessage("¿Seguro que deseas salir? La pizza te va a extrañar")
                .setPositiveButton("Salir", (dialog, which) -> finishAffinity())
                .setNegativeButton("Quedarme", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
