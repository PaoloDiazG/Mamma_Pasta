package com.mammapasta.builder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.mammapasta.R;
import com.mammapasta.db.DBHelper;
import com.mammapasta.models.Topping;
import com.mammapasta.order.ConfirmacionActivity;

import java.util.*;

public class ConstruirPizzaActivity extends AppCompatActivity {

    Spinner spinnerMasa, spinnerSalsa;
    CheckBox chkQueso;
    LinearLayout layoutToppings;
    TextView txtPrecioTotal;
    Button btnConfirmar;

    List<Topping> toppingsList;
    Map<CheckBox, Topping> toppingCheckboxes = new HashMap<>();

    double precioBase = 10.0;
    double precioTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_construir_pizza);

        spinnerMasa = findViewById(R.id.spinnerMasa);
        spinnerSalsa = findViewById(R.id.spinnerSalsa);
        chkQueso = findViewById(R.id.chkQueso);
        layoutToppings = findViewById(R.id.layoutToppings);
        txtPrecioTotal = findViewById(R.id.txtPrecioTotal);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        cargarSpinners();
        cargarToppings();

        chkQueso.setOnCheckedChangeListener((buttonView, isChecked) -> calcularTotal());

        btnConfirmar.setOnClickListener(v -> confirmarPizza());
    }

    private void cargarSpinners() {
        String[] masas = {"Delgada", "Gruesa", "Integral"};
        ArrayAdapter<String> adapterMasa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, masas);
        spinnerMasa.setAdapter(adapterMasa);

        String[] salsas = {"Tomate", "BBQ", "Blanca"};
        ArrayAdapter<String> adapterSalsa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, salsas);
        spinnerSalsa.setAdapter(adapterSalsa);
    }

    private void cargarToppings() {
        DBHelper dbHelper = new DBHelper(this);
        toppingsList = dbHelper.getAllToppings();

        for (Topping topping : toppingsList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(topping.getNombre() + " (+S/." + topping.getPrecioExtra() + ")");
            layoutToppings.addView(checkBox);
            toppingCheckboxes.put(checkBox, topping);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> calcularTotal());
        }

        calcularTotal();
    }

    private void calcularTotal() {
        double total = precioBase;

        if (chkQueso.isChecked()) {
            total += 2.0;
        }

        for (Map.Entry<CheckBox, Topping> entry : toppingCheckboxes.entrySet()) {
            if (entry.getKey().isChecked()) {
                total += entry.getValue().getPrecioExtra();
            }
        }

        precioTotal = total;
        txtPrecioTotal.setText("Total: S/." + String.format("%.2f", precioTotal));
    }

    private void confirmarPizza() {
        String masa = spinnerMasa.getSelectedItem().toString();
        String salsa = spinnerSalsa.getSelectedItem().toString();
        boolean conQueso = chkQueso.isChecked();

        List<String> toppingsSeleccionados = new ArrayList<>();
        for (Map.Entry<CheckBox, Topping> entry : toppingCheckboxes.entrySet()) {
            if (entry.getKey().isChecked()) {
                toppingsSeleccionados.add(entry.getValue().getNombre());
            }
        }

        // Enviar a ConfirmacionActivity
        Intent intent = new Intent(this, ConfirmacionActivity.class);
        intent.putExtra("masa", masa);
        intent.putExtra("salsa", salsa);
        intent.putExtra("queso", conQueso);
        intent.putExtra("toppings", String.join(", ", toppingsSeleccionados));
        intent.putExtra("precio_total", precioTotal);
        startActivity(intent);
    }
}
