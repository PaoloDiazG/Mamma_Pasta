package com.mammapasta.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.mammapasta.R;
import com.mammapasta.db.DBHelper;
import com.mammapasta.dispatch.DeliveryActivity;
import com.mammapasta.qrreader.QRActivity;
import com.mammapasta.utils.PreferencesManager;

public class ConfirmacionActivity extends AppCompatActivity {

    TextView txtTituloPizza, txtDetalles, txtPrecioFinal, txtDescuento, txtCodigoAplicado;
    Button btnConfirmarCompra, btnCodigoPromocional;
    View layoutDescuento;

    Bundle extras;
    double precioOriginal;
    double descuentoAplicado = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacion);

        // Vincular vistas
        txtTituloPizza = findViewById(R.id.txtTituloPizza);
        txtDetalles = findViewById(R.id.txtDetalles);
        txtPrecioFinal = findViewById(R.id.txtPrecioFinal);
        txtDescuento = findViewById(R.id.txtDescuento);
        txtCodigoAplicado = findViewById(R.id.txtCodigoAplicado);
        btnConfirmarCompra = findViewById(R.id.btnConfirmarCompra);
        btnCodigoPromocional = findViewById(R.id.btnCodigoPromocional);
        layoutDescuento = findViewById(R.id.layoutDescuento); // ahora es View, no LinearLayout

        extras = getIntent().getExtras();

        if (extras != null) {
            // Pizza personalizada
            if (extras.containsKey("masa")) {
                txtTituloPizza.setText("Pizza Personalizada");

                String masa = extras.getString("masa");
                String salsa = extras.getString("salsa");
                boolean queso = extras.getBoolean("queso");
                String toppings = extras.getString("toppings");
                precioOriginal = extras.getDouble("precio_total");

                String detalles =
                        "Masa: " + masa + "\n" +
                                "Salsa: " + salsa + "\n" +
                                "Queso: " + (queso ? "Sí (+S/2)" : "No") + "\n" +
                                "Toppings: " + (toppings.isEmpty() ? "Ninguno" : toppings);

                txtDetalles.setText(detalles);

            } else {
                // Pedido desde el carrito (múltiples pizzas)
                String nombre = extras.getString("nombre_pizza", "Pedido completo");
                String ingredientes = extras.getString("ingredientes");
                precioOriginal = extras.getDouble("precio");

                txtTituloPizza.setText(nombre);
                txtDetalles.setText("Ingredientes: " + ingredientes);
            }

            // Si hay descuento
            if (extras.containsKey("descuento")) {
                descuentoAplicado = extras.getDouble("descuento");
                String codigoPromocional = extras.getString("codigo_promocional");

                layoutDescuento.setVisibility(View.VISIBLE);
                txtCodigoAplicado.setText("Código: " + codigoPromocional);
                txtDescuento.setText("Descuento: -S/." +
                        String.format("%.2f", (precioOriginal * descuentoAplicado / 100)));
            }

            actualizarPrecio();
        }

        // Ir a lector QR para aplicar código
        btnCodigoPromocional.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRActivity.class);
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent);
            finish();
        });

        // Confirmar compra y guardar en BD
        btnConfirmarCompra.setOnClickListener(v -> {
            PreferencesManager prefs = new PreferencesManager(this);
            String userEmail = prefs.getEmail();

            String nombrePizza;
            String detalles;
            double precioFinal = obtenerPrecioFinal();

            if (extras.containsKey("masa")) {
                nombrePizza = "Pizza Personalizada";
                detalles = txtDetalles.getText().toString();
            } else {
                nombrePizza = extras.getString("nombre_pizza", "Pedido completo");
                detalles = txtDetalles.getText().toString();
            }

            if (descuentoAplicado > 0) {
                String codigoPromocional = extras.getString("codigo_promocional");
                detalles += "\nDescuento aplicado: " + descuentoAplicado + "% (Código: " + codigoPromocional + ")";
            }

            DBHelper dbHelper = new DBHelper(this);
            dbHelper.insertarPedido(userEmail, nombrePizza, detalles, precioFinal);

            Toast.makeText(this, "¡Pedido guardado!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, DeliveryActivity.class);
            intent.putExtra("nombre_pizza", nombrePizza);
            intent.putExtra("detalles_pedido", detalles);
            intent.putExtra("precio_pedido", precioFinal);
            intent.putExtra("user_email", userEmail);

            startActivity(intent);
            finish();
        });
    }

    private void actualizarPrecio() {
        double precioFinal = obtenerPrecioFinal();
        txtPrecioFinal.setText("Total: S/." + String.format("%.2f", precioFinal));
    }

    private double obtenerPrecioFinal() {
        return precioOriginal - (precioOriginal * descuentoAplicado / 100);
    }
}
