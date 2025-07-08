package com.mammapasta.qrreader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.mammapasta.R;
import com.mammapasta.order.ConfirmacionActivity;

import java.util.Map;

public class QRActivity extends AppCompatActivity {

    EditText edtCodigoPromocional;
    Button btnEscanearQR, btnVerificarCodigo, btnVolverSinCodigo;
    TextView txtInstrucciones;

    private String codigoEscaneado = "";
    private DatabaseReference databaseReference;

    // Launcher para el escáner de códigos QR
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(QRActivity.this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                } else {
                    codigoEscaneado = result.getContents();
                    edtCodigoPromocional.setText(codigoEscaneado);
                    Toast.makeText(QRActivity.this, "QR escaneado: " + codigoEscaneado, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("codigos_promocionales");

        edtCodigoPromocional = findViewById(R.id.edtCodigoPromocional);
        btnEscanearQR = findViewById(R.id.btnEscanearQR);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo);
        btnVolverSinCodigo = findViewById(R.id.btnVolverSinCodigo);
        txtInstrucciones = findViewById(R.id.txtInstrucciones);

        // Botón para escanear QR
        btnEscanearQR.setOnClickListener(v -> {
            // Configurar opciones del escáner
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Enfoque el código QR con la cámara");
            options.setCameraId(0); // Usar cámara trasera
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(true);
            options.setOrientationLocked(false);

            // Iniciar el escáner
            barcodeLauncher.launch(options);
        });

        // Botón para verificar código promocional
        btnVerificarCodigo.setOnClickListener(v -> {
            String codigoIngresado = edtCodigoPromocional.getText().toString().trim().toUpperCase();

            if (codigoIngresado.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un código promocional", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mostrar progreso
            btnVerificarCodigo.setEnabled(false);
            btnVerificarCodigo.setText("🔍 Verificando...");

            // Verificar en Firebase
            verificarCodigoEnFirebase(codigoIngresado);
        });

        // Botón para volver sin código promocional
        btnVolverSinCodigo.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfirmacionActivity.class);

            // Pasar todos los datos originales sin descuento
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }

            startActivity(intent);
            finish();
        });
    }

    private void verificarCodigoEnFirebase(String codigo) {
        databaseReference.child(codigo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Restaurar botón
                btnVerificarCodigo.setEnabled(true);
                btnVerificarCodigo.setText("✅ Verificar código");

                if (dataSnapshot.exists()) {
                    // El código existe, verificar si está activo
                    Map<String, Object> codigoData = (Map<String, Object>) dataSnapshot.getValue();

                    if (codigoData != null) {
                        Boolean activo = (Boolean) codigoData.get("activo");

                        if (activo != null && activo) {
                            // Código válido y activo
                            Long descuentoLong = (Long) codigoData.get("descuento");
                            double descuento = descuentoLong != null ? descuentoLong.doubleValue() : 0.0;
                            String descripcion = (String) codigoData.get("descripcion");

                            // Mostrar mensaje de éxito
                            Toast.makeText(QRActivity.this,
                                    "🎉 ¡Código válido! " + descripcion,
                                    Toast.LENGTH_SHORT).show();

                            // Volver a ConfirmacionActivity con el descuento
                            volverConDescuento(codigo, descuento);

                        } else {
                            // Código inactivo
                            Toast.makeText(QRActivity.this,
                                    "❌ Este código promocional ya no está disponible",
                                    Toast.LENGTH_LONG).show();
                            edtCodigoPromocional.setText("");
                        }
                    }
                } else {
                    // Código no existe
                    Toast.makeText(QRActivity.this,
                            "❌ Código promocional inválido. Inténtalo nuevamente",
                            Toast.LENGTH_LONG).show();
                    edtCodigoPromocional.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error de conexión
                btnVerificarCodigo.setEnabled(true);
                btnVerificarCodigo.setText("✅ Verificar código");

                Toast.makeText(QRActivity.this,
                        "❌ Error de conexión. Verifica tu internet e inténtalo nuevamente",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void volverConDescuento(String codigo, double descuento) {
        Intent intent = new Intent(this, ConfirmacionActivity.class);

        // Pasar todos los datos originales
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }

        // Agregar información del descuento
        intent.putExtra("codigo_promocional", codigo);
        intent.putExtra("descuento", descuento);

        startActivity(intent);
        finish();
    }
}