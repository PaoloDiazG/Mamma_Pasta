package com.mammapasta.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDataLoader {

    public static void cargarCodigosPromocionalesIniciales() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("codigos_promocionales");

        Map<String, Object> codigos = new HashMap<>();

        // Código DESCUENTO10
        Map<String, Object> descuento10 = new HashMap<>();
        descuento10.put("descuento", 10);
        descuento10.put("activo", true);
        descuento10.put("descripcion", "Descuento del 10%");
        codigos.put("DESCUENTO10", descuento10);

        // Código DESCUENTO20
        Map<String, Object> descuento20 = new HashMap<>();
        descuento20.put("descuento", 20);
        descuento20.put("activo", true);
        descuento20.put("descripcion", "Descuento del 20%");
        codigos.put("DESCUENTO20", descuento20);

        // Código PROMO15
        Map<String, Object> promo15 = new HashMap<>();
        promo15.put("descuento", 15);
        promo15.put("activo", true);
        promo15.put("descripcion", "Promoción especial 15%");
        codigos.put("PROMO15", promo15);

        // Código MAMMAPASTA5
        Map<String, Object> mammapasta5 = new HashMap<>();
        mammapasta5.put("descuento", 5);
        mammapasta5.put("activo", true);
        mammapasta5.put("descripcion", "Descuento MammaPasta 5%");
        codigos.put("MAMMAPASTA5", mammapasta5);

        // Código BIENVENIDO25 (ejemplo de código especial)
        Map<String, Object> bienvenido25 = new HashMap<>();
        bienvenido25.put("descuento", 25);
        bienvenido25.put("activo", true);
        bienvenido25.put("descripcion", "¡Bienvenido! 25% de descuento");
        codigos.put("BIENVENIDO25", bienvenido25);

        // Código INACTIVO (ejemplo de código desactivado)
        Map<String, Object> inactivo = new HashMap<>();
        inactivo.put("descuento", 50);
        inactivo.put("activo", false);
        inactivo.put("descripcion", "Código vencido");
        codigos.put("VENCIDO50", inactivo);

        // Cargar a Firebase
        ref.setValue(codigos)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("✅ Códigos promocionales cargados exitosamente");
                })
                .addOnFailureListener(e -> {
                    System.err.println("❌ Error al cargar códigos: " + e.getMessage());
                });
    }
}