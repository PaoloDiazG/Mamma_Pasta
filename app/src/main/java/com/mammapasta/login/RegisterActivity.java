package com.mammapasta.login;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.mammapasta.home.HomeActivity;
import com.mammapasta.utils.PreferencesManager;
import com.mammapasta.R;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtConfirmPassword;
    Button btnRegister;
    TextView txtLogin;
    ProgressBar progressBar;

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
        txtLogin = findViewById(R.id.txtLogin);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        preferencesManager = new PreferencesManager(this);

        btnRegister.setOnClickListener(v -> registerUser());
        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
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

        if (password.isEmpty()) {
            edtPassword.setError("Ingrese la contraseña");
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        preferencesManager.setLoggedIn(true);
                        preferencesManager.setEmail(email);

                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}

