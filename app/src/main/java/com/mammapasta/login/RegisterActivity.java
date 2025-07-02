package com.mammapasta.login;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.mammapasta.R;
import com.mammapasta.home.HomeActivity;
import com.mammapasta.utils.PreferencesManager;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtConfirmPassword;
    Button btnRegister;
    TextView txtLogin;
    ImageView loadingPizza;

    FirebaseAuth auth;
    PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLoginLink);
        loadingPizza = findViewById(R.id.loadingPizza); // NUEVO: Pizza giratoria

        auth = FirebaseAuth.getInstance();
        preferencesManager = new PreferencesManager(this);

        btnRegister.setOnClickListener(v -> registerUser());

        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Manejo botón atrás moderno
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Ingrese el correo");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Formato de correo inválido");
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Ingrese la contraseña");
            return;
        }

        if (!isValidPassword(password)) {
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        startPizzaAnimation(true); // Mostrar loading

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    startPizzaAnimation(false); // Ocultar loading
                    if (task.isSuccessful()) {
                        preferencesManager.setLoggedIn(true);
                        preferencesManager.setEmail(email);

                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            edtEmail.setError("Este correo ya está registrado");
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isValidPassword(String password) {
        boolean valid = true;

        if (password.length() < 8) {
            edtPassword.setError("Debe tener al menos 8 caracteres");
            valid = false;
        } else if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            edtPassword.setError("Debe incluir una letra mayúscula");
            valid = false;
        } else if (!Pattern.compile("[a-z]").matcher(password).find()) {
            edtPassword.setError("Debe incluir una letra minúscula");
            valid = false;
        } else if (!Pattern.compile("[0-9]").matcher(password).find()) {
            edtPassword.setError("Debe incluir un número");
            valid = false;
        } else if (!Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
            edtPassword.setError("Debe incluir un carácter especial");
            valid = false;
        }

        return valid;
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
}