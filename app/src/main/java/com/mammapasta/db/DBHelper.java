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
    private static final int DB_VERSION = 2;

    private static final String TABLE_PEDIDOS = "table_pedidos";

    private static final String CREATE_PEDIDOS_TABLE =
            "CREATE TABLE " + TABLE_PEDIDOS + " (" +
                    "id_pedido INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre_pizza TEXT," +
                    "detalles TEXT," +
                    "precio REAL)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla pizzas
        db.execSQL("CREATE TABLE table_pizzas (" +
                "id_pizza INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "descripcion TEXT," +
                "precio_base REAL," +
                "imagen_resource TEXT)");

        // Crear tabla toppings
        db.execSQL("CREATE TABLE table_toppings (" +
                "id_topping INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "precio_extra REAL)");

        // Insertar datos iniciales
        db.execSQL(CREATE_PEDIDOS_TABLE);
        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // Insertar pizzas predeterminadas
        values.put("nombre", "Napolitana");
        values.put("descripcion", "Tomate, mozzarella, albahaca.");
        values.put("precio_base", 10.0);
        values.put("imagen_resource", "pizza_napo");
        db.insert("table_pizzas", null, values);

        values.clear();
        values.put("nombre", "Hawaiana");
        values.put("descripcion", "Jamón, piña, queso.");
        values.put("precio_base", 12.0);
        values.put("imagen_resource", "pizza_haw");
        db.insert("table_pizzas", null, values);

        values.clear();
        values.put("nombre", "Pepperoni");
        values.put("descripcion", "Queso, salsa, pepperoni.");
        values.put("precio_base", 14.0);
        values.put("imagen_resource", "pizza_pepperoni");
        db.insert("table_pizzas", null, values);

        // Insertar toppings
        ContentValues toppingValues = new ContentValues();
        toppingValues.put("nombre", "Pepperoni");
        toppingValues.put("precio_extra", 5.0);
        db.insert("table_toppings", null, toppingValues);

        toppingValues.clear();
        toppingValues.put("nombre", "Piña");
        toppingValues.put("precio_extra", 1.0);
        db.insert("table_toppings", null, toppingValues);

        toppingValues.clear();
        toppingValues.put("nombre", "Carne");
        toppingValues.put("precio_extra", 4.0);
        db.insert("table_toppings", null, toppingValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS table_pizzas");
        db.execSQL("DROP TABLE IF EXISTS table_toppings");
        db.execSQL("DROP TABLE IF EXISTS table_pedidos");
        onCreate(db);
    }

    public List<Pizza> getAllPizzas() {
        List<Pizza> pizzas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM table_pizzas", null);

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

    public List<Topping> getAllToppings() {
        List<Topping> toppings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM table_toppings", null);

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

    public void insertarPedido(String nombrePizza, String detalles, double precio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre_pizza", nombrePizza);
        values.put("detalles", detalles);
        values.put("precio", precio);
        db.insert(TABLE_PEDIDOS, null, values);
        db.close();
    }

    public List<String> getAllPedidos() {
        List<String> pedidos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PEDIDOS, null);

        if (cursor.moveToFirst()) {
            do {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre_pizza"));
                String detalles = cursor.getString(cursor.getColumnIndexOrThrow("detalles"));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                pedidos.add(nombre + " | " + detalles + " | $" + precio);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pedidos;
    }
}
