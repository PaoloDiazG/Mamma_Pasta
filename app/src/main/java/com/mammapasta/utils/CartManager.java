package com.mammapasta.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mammapasta.models.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREFS_NAME = "cart_prefs";
    private static final String CART_KEY = "cart_items";
    private static CartManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    public void addToCart(CartItem item) {
        List<CartItem> cartItems = getCartItems();

        // Verificar si el item ya existe en el carrito
        boolean found = false;
        for (CartItem existingItem : cartItems) {
            if (existingItem.getNombrePizza().equals(item.getNombrePizza()) &&
                    existingItem.getIngredientes().equals(item.getIngredientes())) {
                existingItem.setCantidad(existingItem.getCantidad() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            item.setCantidad(1);
            cartItems.add(item);
        }

        saveCartItems(cartItems);
    }

    public List<CartItem> getCartItems() {
        String json = sharedPreferences.getString(CART_KEY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<CartItem>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void removeFromCart(int position) {
        List<CartItem> cartItems = getCartItems();
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            saveCartItems(cartItems);
        }
    }

    public void updateQuantity(int position, int newQuantity) {
        List<CartItem> cartItems = getCartItems();
        if (position >= 0 && position < cartItems.size()) {
            if (newQuantity <= 0) {
                cartItems.remove(position);
            } else {
                cartItems.get(position).setCantidad(newQuantity);
            }
            saveCartItems(cartItems);
        }
    }

    public void clearCart() {
        saveCartItems(new ArrayList<>());
    }

    public double getTotalPrice() {
        List<CartItem> cartItems = getCartItems();
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrecioTotal();
        }
        return total;
    }

    public int getTotalItems() {
        List<CartItem> cartItems = getCartItems();
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getCantidad();
        }
        return total;
    }

    private void saveCartItems(List<CartItem> cartItems) {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(CART_KEY, json).apply();
    }

}