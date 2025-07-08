package com.mammapasta.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mammapasta.models.Pizza;
import com.mammapasta.models.Topping;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "mammapasta.db";
    private static final int DB_VERSION = 5;

    private static final String TABLE_PIZZAS = "table_pizzas";
    private static final String TABLE_TOPPINGS = "table_toppings";
    private static final String TABLE_PEDIDOS = "table_pedidos";

    private static final String CREATE_PEDIDOS_TABLE =
            "CREATE TABLE " + TABLE_PEDIDOS + " (" +
                    "id_pedido INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT," +
                    "nombre_pizza TEXT," +
                    "ingredientes TEXT," +
                    "precio REAL)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla pizzas
        db.execSQL("CREATE TABLE " + TABLE_PIZZAS + " (" +
                "id_pizza INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "descripcion TEXT," +
                "precio_base REAL," +
                "imagen_resource TEXT)");

        // Crear tabla toppings
        db.execSQL("CREATE TABLE " + TABLE_TOPPINGS + " (" +
                "id_topping INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "precio_extra REAL)");

        // Crear tabla pedidos
        db.execSQL(CREATE_PEDIDOS_TABLE);

        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // Insertar pizzas predeterminadas
        values.put("nombre", "Napolitana");
        values.put("descripcion", "Tomate, mozzarella, albahaca.");
        values.put("precio_base", 20.0);
        values.put("imagen_resource", "pizza_napolitana");
        db.insert(TABLE_PIZZAS, null, values);

        values.clear();
        values.put("nombre", "Hawaiana");
        values.put("descripcion", "Jamón, piña, queso.");
        values.put("precio_base", 22.0);
        values.put("imagen_resource", "pizza_hawaiana");
        db.insert(TABLE_PIZZAS, null, values);

        values.clear();
        values.put("nombre", "Pepperoni");
        values.put("descripcion", "Queso, salsa, pepperoni.");
        values.put("precio_base", 19.0);
        values.put("imagen_resource", "pizza_pepperoni");
        db.insert(TABLE_PIZZAS, null, values);

        values.clear();
        values.put("nombre", "Cuatro Quesos");
        values.put("descripcion", "Mozzarella, parmesano, gorgonzola y provolone.");
        values.put("precio_base", 25.0);
        values.put("imagen_resource", "pizza_cuatro_quesos");
        db.insert(TABLE_PIZZAS, null, values);

        values.clear();
        values.put("nombre", "Vegetariana");
        values.put("descripcion", "Champiñones, pimientos, cebolla, aceitunas.");
        values.put("precio_base", 23.0);
        values.put("imagen_resource", "pizza_vegetariana");
        db.insert(TABLE_PIZZAS, null, values);

        values.clear();
        values.put("nombre", "Carbonara");
        values.put("descripcion", "Bacon, cebolla caramelizada y crema.");
        values.put("precio_base", 26.0);
        values.put("imagen_resource", "pizza_carbonara");
        db.insert(TABLE_PIZZAS, null, values);

        // Insertar toppings
        ContentValues toppingValues = new ContentValues();
        toppingValues.put("nombre", "Pepperoni");
        toppingValues.put("precio_extra", 5.0);
        db.insert(TABLE_TOPPINGS, null, toppingValues);

        toppingValues.clear();
        toppingValues.put("nombre", "Piña");
        toppingValues.put("precio_extra", 1.0);
        db.insert(TABLE_TOPPINGS, null, toppingValues);

        toppingValues.clear();
        toppingValues.put("nombre", "Carne");
        toppingValues.put("precio_extra", 4.0);
        db.insert(TABLE_TOPPINGS, null, toppingValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PIZZAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOPPINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEDIDOS);
        onCreate(db);
    }

    /**
     * Obtiene todas las pizzas predeterminadas
     */
    public List<Pizza> getAllPizzas() {
        List<Pizza> pizzas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PIZZAS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_pizza"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
                double precioBase = cursor.getDouble(cursor.getColumnIndexOrThrow("precio_base"));
                String imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen_resource"));

                pizzas.add(new Pizza(id, nombre, descripcion, precioBase, imagen));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pizzas;
    }

    /**
     * Obtiene todos los toppings disponibles
     */
    public List<Topping> getAllToppings() {
        List<Topping> toppings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TOPPINGS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_topping"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio_extra"));

                toppings.add(new Topping(id, nombre, precio));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return toppings;
    }

    /**
     * Inserta un pedido para un usuario
     */
    public void insertarPedido(String email, String nombrePizza, String ingredientes, double precio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("nombre_pizza", nombrePizza);
        values.put("ingredientes", ingredientes);
        values.put("precio", precio);
        db.insert(TABLE_PEDIDOS, null, values);
        db.close();
    }

    /**
     * Obtiene todos los pedidos de un usuario específico
     */
    public List<String> getPedidosByEmail(String email) {
        List<String> pedidos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PEDIDOS + " WHERE email = ?",
                new String[]{email}
        );

        if (cursor.moveToFirst()) {
            do {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre_pizza"));
                String detalles = cursor.getString(cursor.getColumnIndexOrThrow("ingredientes"));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));

                pedidos.add(nombre + "\n" +
                        detalles + "\n" +
                        "Precio: S/." + String.format("%.2f", precio));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pedidos;
    }

}
