package com.mammapasta.models;

public class Pizza {
    private int id;
    private String nombre;
    private String descripcion;
    private double precioBase;
    private String imagenResource;

    public Pizza(int id,
                 String nombre,
                 String descripcion,
                 double precioBase,
                 String imagenResource) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.imagenResource = imagenResource;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecioBase() { return precioBase; }
    public String getImagenResource() { return imagenResource; }
}
