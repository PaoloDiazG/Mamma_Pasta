package com.mammapasta.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.card.MaterialCardView;

import com.mammapasta.R;
import com.mammapasta.builder.ConstruirPizzaActivity;
import com.mammapasta.chat.ChatActivity;
import com.mammapasta.db.DBHelper;
import com.mammapasta.historial.HistorialActivity;
import com.mammapasta.login.LoginActivity;
import com.mammapasta.models.CartItem;
import com.mammapasta.models.Pizza;
import com.mammapasta.order.ConfirmacionActivity;
import com.mammapasta.utils.CartManager;
import com.mammapasta.utils.PreferencesManager;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements CartAdapter.OnCartUpdateListener {

    RecyclerView recyclerView;
    PizzaAdapter pizzaAdapter;
    DBHelper dbHelper;
    Button btnConstruir;
    Button btnHistorial;
    Button btnLogout;
    Button btnSoporte;

    // Carrito
    Button btnCarrito;
    MaterialCardView layoutCarrito;
    RecyclerView recyclerViewCarrito;
    TextView txtTotalCarrito;
    Button btnProcederPago;
    Button btnVaciarCarrito;
    CartAdapter cartAdapter;
    CartManager cartManager;
    boolean carritoVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar vistas normales
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        btnConstruir = findViewById(R.id.btnConstruirPizza);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnLogout = findViewById(R.id.btnLogout);
        btnSoporte = findViewById(R.id.btnSoporte);

        // Inicializar vistas del carrito
        btnCarrito = findViewById(R.id.btnCarrito);
        layoutCarrito = findViewById(R.id.layoutCarrito);
        recyclerViewCarrito = findViewById(R.id.recyclerViewCarrito);
        txtTotalCarrito = findViewById(R.id.txtTotalCarrito);
        btnProcederPago = findViewById(R.id.btnProcederPago);
        btnVaciarCarrito = findViewById(R.id.btnVaciarCarrito);

        // Inicializar managers
        dbHelper = new DBHelper(this);
        cartManager = CartManager.getInstance(this);

        // Configurar RecyclerView principal
        List<Pizza> pizzaList = dbHelper.getAllPizzas();
        pizzaAdapter = new PizzaAdapter(pizzaList, this);
        recyclerView.setAdapter(pizzaAdapter);

        // Configurar RecyclerView del carrito
        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(this));
        updateCartUI();

        // Configurar listeners
        btnConstruir.setOnClickListener(v -> {
            startActivity(new Intent(this, ConstruirPizzaActivity.class));
        });

        btnHistorial.setOnClickListener(v -> {
            startActivity(new Intent(this, HistorialActivity.class));
        });

        btnSoporte.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatActivity.class));
        });

        btnLogout.setOnClickListener(v -> mostrarDialogoCerrarSesion());

        btnCarrito.setOnClickListener(v -> toggleCarrito());

        btnProcederPago.setOnClickListener(v -> procederConPago());

        btnVaciarCarrito.setOnClickListener(v -> mostrarDialogoVaciarCarrito());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartUI();
    }

    private void toggleCarrito() {
        if (carritoVisible) {
            // Ocultar carrito
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            layoutCarrito.startAnimation(slideUp);
            layoutCarrito.setVisibility(View.GONE);
            carritoVisible = false;
        } else {
            // Mostrar carrito
            updateCartUI();
            layoutCarrito.setVisibility(View.VISIBLE);
            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            layoutCarrito.startAnimation(slideDown);
            carritoVisible = true;
        }
    }

    private void updateCartUI() {
        List<CartItem> cartItems = cartManager.getCartItems();
        int totalItems = cartManager.getTotalItems();
        double totalPrice = cartManager.getTotalPrice();

        // Actualizar botón del carrito
        btnCarrito.setText("🛒 Carrito (" + totalItems + ")");

        // Actualizar adapter del carrito
        cartAdapter = new CartAdapter(cartItems, this, this);
        recyclerViewCarrito.setAdapter(cartAdapter);

        // Actualizar total
        txtTotalCarrito.setText("Total: S/." + String.format("%.2f", totalPrice));

    }

    @Override
    public void onCartUpdated() {
        updateCartUI();
    }

    private void procederConPago() {
        List<CartItem> cartItems = cartManager.getCartItems();
        if (cartItems.isEmpty()) {
            // Mostrar el Toast, pero no deshabilitar el botón
            Toast.makeText(this, "¡Ups! No hay pizzas para proceder al pago... 😅", Toast.LENGTH_SHORT).show();
            return;  // Impide que procedas al pago si el carrito está vacío.
        }

        // Continuar con el proceso de pago si hay productos en el carrito
        StringBuilder allItems = new StringBuilder();
        double totalPrice = 0;

        for (CartItem item : cartItems) {
            allItems.append(item.getNombrePizza())
                    .append(" x")
                    .append(item.getCantidad())
                    .append(" (")
                    .append(item.getIngredientes())
                    .append("), ");
            totalPrice += item.getPrecioTotal();
        }

        if (allItems.length() > 2) {
            allItems.setLength(allItems.length() - 2);
        }

        // Ir a confirmación con todos los items
        Intent intent = new Intent(this, ConfirmacionActivity.class);
        intent.putExtra("nombre_pizza", "Pedido completo");
        intent.putExtra("ingredientes", allItems.toString());
        intent.putExtra("precio", totalPrice);
        startActivity(intent);
    }
    private void mostrarDialogoVaciarCarrito() {
        List<CartItem> cartItems = cartManager.getCartItems();
        if (cartItems.isEmpty()) {
            // Mostrar el Toast, pero no deshabilitar el botón
            Toast.makeText(this, "¡No tienes pizzas para vaciar! 🧐", Toast.LENGTH_SHORT).show();
            return;  // Impide vaciar el carrito si está vacío.
        }

        // Continuar con el proceso de vaciar carrito si hay productos
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vaciar carrito");
        builder.setMessage("¿Estás seguro de que deseas vaciar el carrito?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            cartManager.clearCart();
            updateCartUI();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void mostrarDialogoCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que deseas cerrar sesión?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            PreferencesManager preferencesManager = new PreferencesManager(this);
            preferencesManager.setLoggedIn(false);
            preferencesManager.setEmail(null);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}