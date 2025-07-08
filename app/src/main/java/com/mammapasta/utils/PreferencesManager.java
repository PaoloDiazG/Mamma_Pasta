package com.mammapasta.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferencesManager {
    private static final String PREFS_NAME = "MAMMAPASTA_PREFS";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LOGIN_ATTEMPTS = "loginAttempts";
    private static final String KEY_BLOCK_UNTIL = "blockUntil";
    private static final String KEY_ORDER_ID = "ultimo_order_id";
    private static final String KEY_ESTADO_PEDIDO = "estado_pedido";  // Nueva constante para estado

    private SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Métodos para la sesión
    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Métodos para el correo
    public void setEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Métodos para los intentos fallidos
    public void setLoginAttempts(int attempts) {
        prefs.edit().putInt(KEY_LOGIN_ATTEMPTS, attempts).apply();
    }

    public int getLoginAttempts() {
        return prefs.getInt(KEY_LOGIN_ATTEMPTS, 0);
    }

    // Métodos para el bloqueo temporal
    public void setBlockUntil(long timestamp) {
        prefs.edit().putLong(KEY_BLOCK_UNTIL, timestamp).apply();
    }

    public long getBlockUntil() {
        return prefs.getLong(KEY_BLOCK_UNTIL, 0);
    }

    // Limpieza (opcional para cerrar sesión u otros)
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    public void setOrderId(String orderId) {
        SharedPreferences.Editor editor = prefs.edit();  // Cambié sharedPreferences a prefs
        editor.putString("orderId", orderId);  // Guardas el orderId
        editor.apply();
    }

    public String getOrderId() {
        return prefs.getString("orderId", null);  // Cambié sharedPreferences a prefs
    }

    // Dentro de PreferencesManager.java

    public void setOrderStatus(String status) {
        prefs.edit().putString(KEY_ESTADO_PEDIDO, status).apply();
    }

    public String getOrderStatus() {
        return prefs.getString(KEY_ESTADO_PEDIDO, "");  // Devuelve el estado guardado o un valor por defecto vacío
    }


}
