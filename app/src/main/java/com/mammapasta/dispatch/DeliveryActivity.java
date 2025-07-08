package com.mammapasta.dispatch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.mammapasta.R;
import com.mammapasta.home.HomeActivity;
import com.mammapasta.utils.CartManager;
import com.mammapasta.utils.PreferencesManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DeliveryActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private MaterialButton btnUbicacionActual;
    private MaterialButton btnUbicacionManual;
    private MaterialButton btnConfirmarEntrega;
    private TextInputEditText editDireccion;
    private TextInputEditText editReferencia;
    private TextInputEditText editTelefono;
    private TextView txtUbicacionStatus;
    private TextView txtTiempoEntrega;

    private LocationManager locationManager;
    private boolean isUsingCurrentLocation = false;
    private boolean isLocationObtained = false;

    // Variables para los datos del pedido
    private String nombrePizza;
    private String detallesPedido;
    private double precioPedido;
    private String userEmail;
    private String customerName;

    // Firebase
    private DatabaseReference databaseReference;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        // Inicializar Firebase
        initializeFirebase();

        // Recibir datos del pedido
        receiveOrderData();

        // Inicializar vistas
        initializeViews();

        // Configurar escuchadores (listeners)
        setupListeners();

        // Configurar el LocationManager
        setupLocationManager();

        // Crear un callback para manejar la acción de retroceso
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled */) {
            @Override
            public void handleOnBackPressed() {
                // Verificamos si hay cambios no guardados
                if (isUsingCurrentLocation && !isLocationObtained) {
                    mostrarConfirmacionSalir();
                } else if (!editDireccion.getText().toString().trim().isEmpty() || !editTelefono.getText().toString().trim().isEmpty()) {
                    // Si los campos de dirección o teléfono contienen datos no confirmados
                    mostrarConfirmacionSalir();
                } else {
                    // Si todo está correcto, simplemente retrocedemos
                    finish();  // Usamos finish() para cerrar la actividad sin llamar recursivamente al método
                }
            }
        };

        // Registrar el callback para interceptar el gesto de regreso
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        orderId = UUID.randomUUID().toString();
    }

    private void receiveOrderData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nombrePizza = extras.getString("nombre_pizza", "");
            detallesPedido = extras.getString("detalles_pedido", "");
            precioPedido = extras.getDouble("precio_pedido", 0.0);
            userEmail = extras.getString("user_email", "");
        }

        // Obtener el nombre del cliente desde PreferencesManager
        PreferencesManager prefs = new PreferencesManager(this);
        if (customerName == null || customerName.isEmpty()) {
            customerName = userEmail; // Usar email si no hay nombre
        }
    }

    private void initializeViews() {
        btnUbicacionActual = findViewById(R.id.btnUbicacionActual);
        btnUbicacionManual = findViewById(R.id.btnUbicacionManual);
        btnConfirmarEntrega = findViewById(R.id.btnConfirmarEntrega);
        editDireccion = findViewById(R.id.editDireccion);
        editReferencia = findViewById(R.id.editReferencia);
        editTelefono = findViewById(R.id.editTelefono);
        txtUbicacionStatus = findViewById(R.id.txtUbicacionStatus);
        txtTiempoEntrega = findViewById(R.id.txtTiempoEntrega);
    }

    private void setupListeners() {
        btnUbicacionActual.setOnClickListener(v -> {
            isUsingCurrentLocation = true;
            getCurrentLocation();
            updateButtonStates();
        });

        btnUbicacionManual.setOnClickListener(v -> {
            isUsingCurrentLocation = false;
            enableManualInput();
            updateButtonStates();
        });

        btnConfirmarEntrega.setOnClickListener(v -> {
            confirmarEntrega();
        });

        // TextWatcher para validar campos en tiempo real
        TextWatcher fieldWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFieldsAndUpdateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editDireccion.addTextChangedListener(fieldWatcher);
        editTelefono.addTextChangedListener(fieldWatcher);
    }

    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        showLocationStatus("🔄 Obteniendo ubicación...");

        try {
            // Intentar obtener la última ubicación conocida primero
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                onLocationChanged(lastKnownLocation);
            } else {
                // Si no hay ubicación conocida, solicitar una nueva
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, this);
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            0, 0, this);
                } else {
                    showLocationStatus("❌ GPS desactivado");
                    Toast.makeText(this, "Por favor activa el GPS", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SecurityException e) {
            showLocationStatus("❌ Error de permisos");
            Toast.makeText(this, "Error al acceder a la ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void enableManualInput() {
        editDireccion.setEnabled(true);
        editDireccion.setText("");
        hideLocationStatus();
        isLocationObtained = false;
        validateFieldsAndUpdateButton();
    }

    private void updateButtonStates() {
        if (isUsingCurrentLocation) {
            btnUbicacionActual.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
            btnUbicacionManual.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, android.R.color.white));
            editDireccion.setEnabled(false);
        } else {
            btnUbicacionActual.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, android.R.color.holo_green_light));
            btnUbicacionManual.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, android.R.color.white));
            editDireccion.setEnabled(true);
        }
    }

    private void showLocationStatus(String message) {
        txtUbicacionStatus.setText(message);
        txtUbicacionStatus.setVisibility(View.VISIBLE);
    }

    private void hideLocationStatus() {
        txtUbicacionStatus.setVisibility(View.GONE);
    }

    private void validateFieldsAndUpdateButton() {
        boolean isValid = validateFields();
        btnConfirmarEntrega.setEnabled(isValid);
    }

    private boolean validateFields() {
        String direccion = editDireccion.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();

        boolean telefonoValido = telefono.matches("\\d{9}");

        if (!telefonoValido) {
            editTelefono.setError("El número debe tener 9 dígitos");
        } else {
            editTelefono.setError(null);
        }

        if (isUsingCurrentLocation) {
            return isLocationObtained && telefonoValido;
        } else {
            return !direccion.isEmpty() && telefonoValido;
        }
    }
    private void confirmarEntrega() {
        // Mostrar loading
        btnConfirmarEntrega.setEnabled(false);
        btnConfirmarEntrega.setText("Confirmando...");

        Log.d("DeliveryActivity", "Se presionó el botón de confirmar entrega");

        // Validar campos antes de proceder
        if (!validateFields()) {
            Log.d("DeliveryActivity", "Campos inválidos, validación fallida.");
            // Restaurar el botón para permitir corrección
            btnConfirmarEntrega.setEnabled(true);
            btnConfirmarEntrega.setText("Confirmar Entrega");
            return;  // Si la validación falla, no continuar con el proceso
        }

        // Si todo esta bien, proceder a confirmar el pedido
        Log.d("DeliveryActivity", "Campos válidos, confirmando pedido.");

        // Crear el mapa de datos para el pedido
        Map<String, Object> orderData = createOrderData();

        // Guardar el pedido en Firebase
        databaseReference.child("orders").child(orderId)
                .setValue(orderData)  // Guardar pedido
                .addOnSuccessListener(aVoid -> {
                    // Ahora que el pedido fue guardado, actualizamos el estado
                    updateOrderStatus("Recibido");

                    // Pedido guardado exitosamente
                    Toast.makeText(this, "Pedido confirmado exitosamente", Toast.LENGTH_SHORT).show();

                    // Redirigir a la actividad de seguimiento del pedido
                    Intent intent = new Intent(this, SeguimientoPedidoActivity.class);
                    intent.putExtra("orderId", orderId);  // Pasar el orderId para el seguimiento
                    startActivity(intent);
                    finish();  // Cerrar la actividad actual
                })
                .addOnFailureListener(e -> {
                    Log.d("DeliveryActivity", "Error al guardar el pedido en Firebase: " + e.getMessage());
                    Toast.makeText(this, "Error al confirmar pedido: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // Restaurar el botón para intentar nuevamente
                    btnConfirmarEntrega.setEnabled(true);
                    btnConfirmarEntrega.setText("Confirmar Entrega");
                });
        // Guardar el orderId en SharedPreferences
        PreferencesManager preferencesManager = new PreferencesManager(this);
        preferencesManager.setOrderId(orderId);  // Aquí guardas el orderId
    }

    private void updateOrderStatus(String newStatus) {
        Log.d("SeguimientoPedido", "Actualizando estado: " + newStatus);

        // Solo actualizar el estado, sin necesidad de reescribir todo el pedido
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", newStatus);
        updates.put("fechaActualizacion", ServerValue.TIMESTAMP);

        // Actualiza el estado del pedido en Firebase
        databaseReference.child("orders").child(orderId)
                .updateChildren(updates)  // Solo actualizamos el estado
                .addOnSuccessListener(aVoid -> {
                    Log.d("SeguimientoPedido", "Estado actualizado en Firebase a: " + newStatus);
                })
                .addOnFailureListener(e -> {
                    Log.e("SeguimientoPedido", "Error al actualizar estado en Firebase", e);
                    Toast.makeText(this, "Error al actualizar estado en Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> createOrderData() {
        Map<String, Object> orderData = new HashMap<>();

        // Datos del pedido
        orderData.put("orderId", orderId);
        orderData.put("nombrePizza", nombrePizza);
        orderData.put("detallesPedido", detallesPedido);
        orderData.put("precioPedido", precioPedido);
        orderData.put("estado", "Recibido");  // Asegúrate de agregar este campo

        // Datos del cliente
        orderData.put("customerName", customerName);
        orderData.put("userEmail", userEmail);
        orderData.put("telefono", editTelefono.getText().toString().trim());

        // Datos de entrega
        orderData.put("direccion", editDireccion.getText().toString().trim());
        orderData.put("referencia", editReferencia.getText().toString().trim());
        orderData.put("tipoUbicacion", isUsingCurrentLocation ? "GPS" : "Manual");

        // Datos de timestamp
        orderData.put("fechaCreacion", ServerValue.TIMESTAMP);
        orderData.put("fechaFormatted", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));

        return orderData;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationManager.removeUpdates(this);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Convertir coordenadas a dirección
        getAddressFromLocation(latitude, longitude);
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);

                editDireccion.setText(addressText);
                showLocationStatus("✅ Ubicación obtenida");
                isLocationObtained = true;
                validateFieldsAndUpdateButton();

                // Ocultar el status después de 2 segundos
                txtUbicacionStatus.postDelayed(() -> hideLocationStatus(), 2000);

            } else {
                showLocationStatus("❌ No se pudo obtener la dirección");
                Toast.makeText(this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            showLocationStatus("❌ Error al obtener dirección");
            Toast.makeText(this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                showLocationStatus("❌ Permisos denegados");
                Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show();

                // Cambiar a modo manual
                isUsingCurrentLocation = false;
                enableManualInput();
                updateButtonStates();
            }
        }
    }
    private void mostrarConfirmacionSalir() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar salida")
                .setMessage("¿Estás seguro de que deseas salir? Se perderán los cambios no guardados.")
                .setCancelable(false)  // Hace que no se pueda cerrar tocando fuera del diálogo
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    // Si el usuario confirma, redirige al HomeActivity
                    Intent intent = new Intent(DeliveryActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Esto asegura que no regrese a la actividad actual
                    startActivity(intent);
                    finish(); // Cierra la actividad actual para que no quede en la pila
                })
                .setNegativeButton("Cancelar", null)  // No hace nada al cancelar
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    // Métodos requeridos por LocationListener
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "GPS desactivado", Toast.LENGTH_SHORT).show();
    }

}

