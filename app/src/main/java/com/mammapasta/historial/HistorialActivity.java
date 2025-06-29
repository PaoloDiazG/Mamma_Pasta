package com.mammapasta.historial;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;
import com.mammapasta.db.DBHelper;

import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    HistorialAdapter adapter;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        recyclerView = findViewById(R.id.recyclerViewHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        List<String> pedidos = dbHelper.getAllPedidos();

        adapter = new HistorialAdapter(pedidos);
        recyclerView.setAdapter(adapter);
    }
}
