package com.mammapasta.detail;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.mammapasta.R;
import com.mammapasta.db.DBHelper;
import com.mammapasta.models.Topping;
import java.util.*;

public class DetallePizzaActivity extends AppCompatActivity {

    ImageView imgPizza;
    TextView txtNombrePizza, txtDescripcionPizza, txtPrecioBase, txtPrecioTotal;
    LinearLayout layoutToppings;
    Button btnAgregarPedido;

    double precioBase;
    double precioTotal;
    List<Topping> toppingsList;
    Map<CheckBox, Topping> toppingCheckboxes = new HashMap<>();

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

        dbHelper = new DBHelper(this);

        // Obtener datos de la pizza seleccionada
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String descripcion = intent.getStringExtra("descripcion");
        precioBase = intent.getDoubleExtra("precio_base", 0.0);
        String imagenResource = intent.getStringExtra("imagen_resource");

        txtNombrePizza.setText(nombre);
        txtDescripcionPizza.setText(descripcion);
        txtPrecioBase.setText("Base: $" + precioBase);

        int resId = getResources().getIdentifier(imagenResource, "drawable", getPackageName());
        if (resId != 0) {
            imgPizza.setImageResource(resId);
        }

        toppingsList = dbHelper.getAllToppings();

        for (Topping topping : toppingsList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(topping.getNombre() + " (+$" + topping.getPrecioExtra() + ")");
            layoutToppings.addView(checkBox);
            toppingCheckboxes.put(checkBox, topping);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> calcularTotal());
        }

        precioTotal = precioBase;
        txtPrecioTotal.setText("Total: $" + precioTotal);

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
        txtPrecioTotal.setText("Total: $" + precioTotal);
    }

    private void agregarPedido() {
        Toast.makeText(this, "Pizza agregada por $" + precioTotal, Toast.LENGTH_SHORT).show();
        // Aquí podrías enviar datos al carrito o a SQLite, etc.
        finish();
    }
}
