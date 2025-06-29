package com.mammapasta.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.builder.ConstruirPizzaActivity;
import com.mammapasta.db.DBHelper;
import com.mammapasta.historial.HistorialActivity;
import com.mammapasta.models.Pizza;
import com.mammapasta.R;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PizzaAdapter pizzaAdapter;
    DBHelper dbHelper;
    Button btnConstruir;
    Button btnHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnConstruir = findViewById(R.id.btnConstruirPizza);
        btnHistorial = findViewById(R.id.btnHistorial);

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
    }
}
