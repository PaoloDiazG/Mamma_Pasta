package com.mammapasta.home;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mammapasta.db.DBHelper;
import com.mammapasta.models.Pizza;
import com.mammapasta.R;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PizzaAdapter pizzaAdapter;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        List<Pizza> pizzaList = dbHelper.getAllPizzas();

        pizzaAdapter = new PizzaAdapter(pizzaList, this);
        recyclerView.setAdapter(pizzaAdapter);
    }
}

