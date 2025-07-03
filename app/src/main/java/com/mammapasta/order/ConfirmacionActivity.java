package com.mammapasta.order;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.mammapasta.R;
import com.mammapasta.db.DBHelper;
import com.mammapasta.utils.PreferencesManager;

public class ConfirmacionActivity extends AppCompatActivity {

    TextView txtTituloPizza, txtDetalles, txtPrecioFinal;
    Button btnConfirmarCompra;

    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacion);

        txtTituloPizza = findViewById(R.id.txtTituloPizza);
        txtDetalles = findViewById(R.id.txtDetalles);
        txtPrecioFinal = findViewById(R.id.txtPrecioFinal);
        btnConfirmarCompra = findViewById(R.id.btnConfirmarCompra);

        extras = getIntent().getExtras();

        if (extras != null) {

            // Para pizza personalizada
            if (extras.containsKey("masa")) {
                txtTituloPizza.setText("Pizza Personalizada");

                String masa = extras.getString("masa");
                String salsa = extras.getString("salsa");
                boolean queso = extras.getBoolean("queso");
                String toppings = extras.getString("toppings");
                double precioTotal = extras.getDouble("precio_total");

                String detalles =
                        "Masa: " + masa + "\n" +
                                "Salsa: " + salsa + "\n" +
                                "Queso: " + (queso ? "Sí (+S/2)" : "No") + "\n" +
                                "Toppings: " + (toppings.isEmpty() ? "Ninguno" : toppings);

                txtDetalles.setText(detalles);
                txtPrecioFinal.setText("Total: S/" + String.format("%.2f", precioTotal));

            } else {
                // Para pizza predeterminada
                String nombre = extras.getString("nombre_pizza");
                String ingredientes = extras.getString("ingredientes");
                double precio = extras.getDouble("precio");

                txtTituloPizza.setText(nombre);
                txtDetalles.setText("Ingredientes: " + ingredientes);
                txtPrecioFinal.setText("Total: S/" + String.format("%.2f", precio));
            }
        }

        btnConfirmarCompra.setOnClickListener(v -> {

            // Obtener email del usuario logueado
            PreferencesManager prefs = new PreferencesManager(this);
            String userEmail = prefs.getEmail();

            // Preparar datos del pedido
            String nombrePizza;
            String detalles;
            double precio;

            if (extras.containsKey("masa")) {
                nombrePizza = "Pizza Personalizada";
                detalles = txtDetalles.getText().toString();
                precio = obtenerPrecio();
            } else {
                nombrePizza = extras.getString("nombre_pizza");
                detalles = txtDetalles.getText().toString();
                precio = obtenerPrecio();
            }

            DBHelper dbHelper = new DBHelper(this);
            dbHelper.insertarPedido(userEmail, nombrePizza, detalles, precio);

            Toast.makeText(this, "¡Pedido guardado!", Toast.LENGTH_SHORT).show();

            // Redireccionar a HomeActivity
            Intent intent = new Intent(this, com.mammapasta.home.HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });


    }

    private double obtenerPrecio() {
        String precioStr = txtPrecioFinal.getText().toString().replace("Total: S/", "");
        return Double.parseDouble(precioStr);
    }

}

