package com.mammapasta.dispatch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.mammapasta.R;
import com.mammapasta.home.HomeActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mammapasta.utils.CartManager;
import com.mammapasta.utils.PreferencesManager;

public class SeguimientoPedidoActivity extends AppCompatActivity {

    private TextView txtEstado;
    private ImageView iconoEstado;
    private ProgressBar progressBar;
    private TextView txtMensaje;

    private String orderId;
    private Handler handler = new Handler();
    private Runnable estadoSimuladoRunnable;
    private int estadoIndex = 0; // Para mantener el índice del estado actual

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_pedido);

        // Inicializar vistas
        txtEstado = findViewById(R.id.txtEstado);
        iconoEstado = findViewById(R.id.iconoEstado);
        progressBar = findViewById(R.id.progressBarSeguimiento);
        txtMensaje = findViewById(R.id.txtMensaje);

        // Inicializamos Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Obtener el ID del pedido desde el Intent
        orderId = getIntent().getStringExtra("orderId");

        // Si no se pasa el orderId en el Intent, recuperarlo de SharedPreferences
        if (orderId == null || orderId.isEmpty()) {
            PreferencesManager prefs = new PreferencesManager(this);
            orderId = prefs.getOrderId();
        }

        // Si no se pasa el orderId y no está guardado, mostrar mensaje de espera
        if (orderId == null || orderId.isEmpty()) {
            mostrarMensajeEspera();
            return;
        }

        // Escuchar cambios en el estado de Firebase
        escucharCambiosEstado();

        // Si no se está simulando, no iniciar la simulación
        if (estadoIndex == 0) {
            iniciarSimulacionDeEstados();
        }

        // Bloquear el retroceso hasta que el pedido se complete
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Verificar el estado del pedido
                PreferencesManager prefs = new PreferencesManager(SeguimientoPedidoActivity.this);
                String orderStatus = prefs.getOrderStatus(); // Obtener el estado guardado

                // Si el pedido no ha sido completado, mostrar un mensaje de alerta
                if (!"Completado".equals(orderStatus)) {
                    new AlertDialog.Builder(SeguimientoPedidoActivity.this)
                            .setTitle("¡Espera un momento!")
                            .setMessage("Aún no has completado tu pedido. ¡Recuerda pagar cuando se entregue tu pedido!")
                            .setPositiveButton("Entendido", (dialog, which) -> {
                                // No hacer nada, solo cerrar el diálogo
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    // Si el pedido está completado, permitir salir de la actividad
                    finish(); // Permite salir si el estado es "Completado"
                }
            }
        });
    }

    private void mostrarMensajeEspera() {
        txtMensaje.setVisibility(View.VISIBLE);
        txtMensaje.setText("Esperando tu pedido...");
        progressBar.setVisibility(View.GONE);
    }

    private void ocultarMensajeEspera() {
        txtMensaje.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void actualizarUIEstado(String estado) {
        // Asegurémonos de que estamos en el hilo principal
        runOnUiThread(() -> {
            Log.d("SeguimientoPedido", "Actualizando UI con el estado: " + estado);

            // Mostrar el estado en el TextView
            txtEstado.setText("Estado: " + estado);

            // Cambiar el icono según el estado
            switch (estado) {
                case "Recibido":
                    iconoEstado.setImageResource(R.drawable.ic_recibido);
                    break;
                case "Preparando":
                    iconoEstado.setImageResource(R.drawable.ic_preparando);
                    break;
                case "En camino":
                    iconoEstado.setImageResource(R.drawable.ic_encamino);
                    break;
                case "Entregado":
                    iconoEstado.setImageResource(R.drawable.ic_entregado);
                    mostrarEncuestaAgradecimiento();
                    break;
                case "Completado":
                    iconoEstado.setImageResource(R.drawable.ic_completado);
                    break;
            }
        });
    }

    private void iniciarSimulacionDeEstados() {
        estadoSimuladoRunnable = new Runnable() {
            int estadoIndex = 0;
            String[] estados = {"Recibido", "Preparando", "En camino", "Entregado", "Completado"};
            int[] tiemposEstados = {10000, 15000, 15000, 15000, 10000};  // Tiempos en milisegundos para cada estado

            @Override
            public void run() {
                if (estadoIndex < estados.length) {
                    String estado = estados[estadoIndex];
                    Log.d("SeguimientoPedido", "Simulando estado: " + estado);

                    // Si el estado es "Entregado", se mostrará la encuesta
                    if ("Entregado".equals(estado)) {
                        mostrarEncuestaAgradecimiento();
                    }

                    // Actualizamos la UI con el nuevo estado
                    actualizarUIEstado(estado);

                    // Actualizamos el estado en Firebase
                    updateOrderStatus(estado);

                    // Avanzar al siguiente estado
                    estadoIndex++;

                    // Continuar simulando después del retraso
                    int delay = tiemposEstados[estadoIndex - 1];
                    handler.postDelayed(this, delay);  // Continuar con el siguiente estado después del delay
                } else {
                    Log.d("SeguimientoPedido", "Simulación terminada.");
                }
            }
        };

        // Iniciar simulación solo si no hay cambios reales de estado en Firebase
        if (estadoIndex == 0) {  // Solo iniciar simulación si es la primera vez
            handler.post(estadoSimuladoRunnable);
            Log.d("SeguimientoPedido", "Simulación iniciada.");
        }
    }

    private void mostrarEncuestaAgradecimiento() {
        Log.d("SeguimientoPedido", "Mostrando encuesta de agradecimiento...");
        new Handler().postDelayed(() -> {
            View customLayout = getLayoutInflater().inflate(R.layout.dialog_encuesta_satisfaccion, null);
            AlertDialog dialog = new AlertDialog.Builder(SeguimientoPedidoActivity.this)
                    .setTitle("🎉 ¡Gracias por tu pedido!")
                    .setView(customLayout)
                    .setCancelable(false)
                    .create();

            customLayout.findViewById(R.id.btnEnviarSatisfaccion).setOnClickListener(v -> {
                // Actualizamos el estado a "Completado"
                actualizarUIEstado("Completado");

                // Actualizamos el estado en Firebase
                updateOrderStatus("Completado");  // Asegúrate de hacer esto también

                // Vaciar el carrito
                CartManager cartManager = CartManager.getInstance(SeguimientoPedidoActivity.this);
                cartManager.clearCart();

                // Redirigir al HomeActivity
                Toast.makeText(SeguimientoPedidoActivity.this, "Gracias por calificarnos", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                // Redirigir al HomeActivity
                Intent intent = new Intent(SeguimientoPedidoActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });

            dialog.show();
        }, 2000); // Después de 2 segundos
    }

    private void escucharCambiosEstado() {
        Log.d("SeguimientoPedido", "Escuchando cambios en el estado del pedido en Firebase...");

        DatabaseReference orderRef = databaseReference.child("orders").child(orderId);
        orderRef.child("estado").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nuevoEstado = dataSnapshot.getValue(String.class);
                if (nuevoEstado != null && !nuevoEstado.isEmpty()) {
                    Log.d("SeguimientoPedido", "Nuevo estado recibido desde Firebase: " + nuevoEstado);

                    // Actualizamos la UI
                    actualizarUIEstado(nuevoEstado);

                    // Guardar el estado actual en Preferences
                    PreferencesManager prefs = new PreferencesManager(SeguimientoPedidoActivity.this);
                    prefs.setOrderStatus(nuevoEstado);  // Nueva función en PreferencesManager
                } else {
                    Log.w("SeguimientoPedido", "Estado recibido vacío o nulo desde Firebase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("SeguimientoPedido", "Error al escuchar cambios de estado: " + databaseError.getMessage());
            }
        });
    }

    // Actualiza el estado del pedido en Firebase
    private void updateOrderStatus(String newStatus) {
        // Actualiza el estado en Firebase
        databaseReference.child("orders").child(orderId)
                .child("estado").setValue(newStatus);

        // Actualiza la fecha de actualización en Firebase
        databaseReference.child("orders").child(orderId)
                .child("fechaActualizacion").setValue(System.currentTimeMillis());
    }

    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null); // Limpia todos los callbacks pendientes
        }
    }
}
