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
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.mammapasta.R;
import com.mammapasta.closed.ClosedHoursActivity;
import com.mammapasta.closed.TimeUtils;
import com.mammapasta.home.HomeActivity;
import com.mammapasta.utils.PreferencesManager;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtConfirmPassword;
    TextView txtEmailError, txtPasswordError, txtConfirmPasswordError;
    Button btnRegister;
    ImageView loadingPizza;

    FirebaseAuth auth;
    PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TimeUtils.isWithinClosedHours()) {
            startActivity(new Intent(this, ClosedHoursActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        txtEmailError = findViewById(R.id.txtEmailError);
        txtPasswordError = findViewById(R.id.txtPasswordError);
        txtConfirmPasswordError = findViewById(R.id.txtConfirmPasswordError);

        btnRegister = findViewById(R.id.btnRegister);
        loadingPizza = findViewById(R.id.loadingPizza);

        auth = FirebaseAuth.getInstance();
        preferencesManager = new PreferencesManager(this);

        btnRegister.setOnClickListener(v -> registerUser());

        TextView txtLogin = findViewById(R.id.txtLoginLink);
        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

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

        // Limpiar errores anteriores
        txtEmailError.setVisibility(View.GONE);
        txtPasswordError.setVisibility(View.GONE);
        txtConfirmPasswordError.setVisibility(View.GONE);

        if (email.isEmpty()) {
            txtEmailError.setText("Ingrese el correo");
            txtEmailError.setVisibility(View.VISIBLE);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmailError.setText("Formato de correo inválido");
            txtEmailError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.isEmpty()) {
            txtPasswordError.setText("Ingrese la contraseña");
            txtPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        if (!isValidPassword(password)) {
            return;
        }

        if (!password.equals(confirmPassword)) {
            txtConfirmPasswordError.setText("Las contraseñas no coinciden");
            txtConfirmPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        startPizzaAnimation(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    startPizzaAnimation(false);
                    if (task.isSuccessful()) {
                        preferencesManager.setLoggedIn(true);
                        preferencesManager.setEmail(email);
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            txtEmailError.setText("Este correo ya está registrado");
                            txtEmailError.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            txtPasswordError.setText("Debe tener al menos 8 caracteres");
            txtPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            txtPasswordError.setText("Debe incluir una letra mayúscula");
            txtPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else if (!Pattern.compile("[a-z]").matcher(password).find()) {
            txtPasswordError.setText("Debe incluir una letra minúscula");
            txtPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else if (!Pattern.compile("[0-9]").matcher(password).find()) {
            txtPasswordError.setText("Debe incluir un número");
            txtPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else if (!Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
            txtPasswordError.setText("Debe incluir un carácter especial");
            txtPasswordError.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
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
