package com.mammapasta.models;

public class Topping {
    private int id;
    private String nombre;
    private double precioExtra;

    public Topping(int id, String nombre, double precioExtra) {
        this.id = id;
        this.nombre = nombre;
        this.precioExtra = precioExtra;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecioExtra() { return precioExtra; }
}

