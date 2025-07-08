package com.mammapasta.historial;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;
import com.mammapasta.db.DBHelper;
import com.mammapasta.utils.PreferencesManager;

import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    HistorialAdapter adapter;
    DBHelper dbHelper;
    TextView txtEmpty;
    TextView btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        recyclerView = findViewById(R.id.recyclerViewHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        txtEmpty = findViewById(R.id.txtEmpty);
        btnVolver = findViewById(R.id.btnVolver);

        // Click → volver atrás
        btnVolver.setOnClickListener(v -> {
            finish();
        });

        // Obtener email del usuario logueado
        PreferencesManager preferencesManager = new PreferencesManager(this);
        String email = preferencesManager.getEmail();

        dbHelper = new DBHelper(this);
        List<String> pedidos = dbHelper.getPedidosByEmail(email);

        if (pedidos.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new HistorialAdapter(pedidos);
            recyclerView.setAdapter(adapter);
        }
    }
}
