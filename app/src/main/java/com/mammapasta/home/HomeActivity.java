package com.mammapasta.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;
import com.mammapasta.builder.ConstruirPizzaActivity;
import com.mammapasta.db.DBHelper;
import com.mammapasta.historial.HistorialActivity;
import com.mammapasta.login.LoginActivity;
import com.mammapasta.models.Pizza;
import com.mammapasta.utils.PreferencesManager;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PizzaAdapter pizzaAdapter;
    DBHelper dbHelper;
    Button btnConstruir;
    Button btnHistorial;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        btnConstruir = findViewById(R.id.btnConstruirPizza);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnLogout = findViewById(R.id.btnLogout);

        dbHelper = new DBHelper(this);
        List<Pizza> pizzaList = dbHelper.getAllPizzas();

        pizzaAdapter = new PizzaAdapter(pizzaList, this);
        recyclerView.setAdapter(pizzaAdapter);

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistorialActivity.class);
            startActivity(intent);
        });

        btnConstruir.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConstruirPizzaActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mostrarDialogoSalida();
        });
    }

    private void mostrarDialogoSalida() {
        new AlertDialog.Builder(this)
                .setTitle("¿Estás seguro?")
                .setMessage("¿Seguro que deseas salir? La pizza te va a extrañar")
                .setPositiveButton("Salir", (dialog, which) -> {
                    // Aquí realizamos el logout
                    PreferencesManager preferencesManager = new PreferencesManager(this);
                    preferencesManager.setLoggedIn(false);
                    preferencesManager.setEmail(null);
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("Quedarme", null)
                .show();
    }
}
