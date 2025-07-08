package com.mammapasta.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.mammapasta.R;
import com.mammapasta.db.DBHelper;
import com.mammapasta.models.CartItem;
import com.mammapasta.models.Topping;
import com.mammapasta.home.HomeActivity;
import com.mammapasta.utils.CartManager;

import java.util.*;

public class DetallePizzaActivity extends AppCompatActivity {

    ImageView imgPizza;
    TextView txtNombrePizza, txtDescripcionPizza, txtPrecioBase, txtPrecioTotal;
    LinearLayout layoutToppings;
    Button btnAgregarPedido;
    TextView btnBack;

    double precioBase;
    double precioTotal;
    List<Topping> toppingsList;
    Map<CheckBox, Topping> toppingCheckboxes = new HashMap<>();
    String imagenResource;
    CartManager cartManager;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pizza);

        imgPizza = findViewById(R.id.imgPizza);
        txtNombrePizza = findViewById(R.id.txtNombrePizza);
        txtDescripcionPizza = findViewById(R.id.txtDescripcionPizza);
        txtPrecioBase = findViewById(R.id.txtPrecioBase);
        txtPrecioTotal = findViewById(R.id.txtPrecioTotal);
        layoutToppings = findViewById(R.id.layoutToppings);
        btnAgregarPedido = findViewById(R.id.btnAgregarPedido);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        dbHelper = new DBHelper(this);
        cartManager = CartManager.getInstance(this);

        // Obtener datos de la pizza seleccionada
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String descripcion = intent.getStringExtra("descripcion");
        precioBase = intent.getDoubleExtra("precio_base", 0.0);
        imagenResource = intent.getStringExtra("imagen_resource");

        txtNombrePizza.setText(nombre);
        txtDescripcionPizza.setText(descripcion);
        txtPrecioBase.setText("Base: S/." + String.format("%.2f", precioBase));

        int resId = getResources().getIdentifier(imagenResource, "drawable", getPackageName());
        if (resId != 0) {
            imgPizza.setImageResource(resId);
        }

        toppingsList = dbHelper.getAllToppings();

        for (Topping topping : toppingsList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(topping.getNombre() + " (+S/." + String.format("%.2f", topping.getPrecioExtra()) + ")");
            checkBox.setTextSize(16);
            checkBox.setPadding(16, 8, 16, 8);
            layoutToppings.addView(checkBox);
            toppingCheckboxes.put(checkBox, topping);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> calcularTotal());
        }

        precioTotal = precioBase;
        txtPrecioTotal.setText("Total: S/." + String.format("%.2f", precioTotal));

        btnAgregarPedido.setOnClickListener(v -> agregarPedido());
    }

    private void calcularTotal() {
        double extra = 0.0;
        for (Map.Entry<CheckBox, Topping> entry : toppingCheckboxes.entrySet()) {
            if (entry.getKey().isChecked()) {
                extra += entry.getValue().getPrecioExtra();
            }
        }
        precioTotal = precioBase + extra;
        txtPrecioTotal.setText("Total: S/." + String.format("%.2f", precioTotal));
    }

    private void agregarPedido() {
        // Crear un CartItem con los datos de la pizza
        CartItem cartItem = new CartItem(
                txtNombrePizza.getText().toString(),
                txtDescripcionPizza.getText().toString(),
                getToppingsSeleccionados(),
                precioTotal,
                1,
                imagenResource
        );

        // Agregar al carrito
        cartManager.addToCart(cartItem);

        // Mostrar mensaje de confirmación
        Toast.makeText(this, "Pizza agregada al carrito", Toast.LENGTH_SHORT).show();

        // Regresar a HomeActivity
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private String getToppingsSeleccionados() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<CheckBox, Topping> entry : toppingCheckboxes.entrySet()) {
            if (entry.getKey().isChecked()) {
                builder.append(entry.getValue().getNombre()).append(", ");
            }
        }
        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        } else {
            builder.append("Sin toppings extra");
        }
        return builder.toString();
    }
}