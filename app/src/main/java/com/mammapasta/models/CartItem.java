package com.mammapasta.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String nombrePizza;
    private String descripcion;
    private String ingredientes;
    private double precio;
    private int cantidad;
    private String imagenResource;

    public CartItem() {}

    public CartItem(String nombrePizza, String descripcion, String ingredientes, double precio, int cantidad, String imagenResource) {
        this.nombrePizza = nombrePizza;
        this.descripcion = descripcion;
        this.ingredientes = ingredientes;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imagenResource = imagenResource;
    }

    // Getters y Setters
    public String getNombrePizza() {
        return nombrePizza;
    }

    public void setNombrePizza(String nombrePizza) {
        this.nombrePizza = nombrePizza;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getImagenResource() {
        return imagenResource;
    }

    public void setImagenResource(String imagenResource) {
        this.imagenResource = imagenResource;
    }

    public double getPrecioTotal() {
        return precio * cantidad;
    }


}