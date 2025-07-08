# Data Models

**Package:** `com.mammapasta.models`

This document outlines the primary data model classes used within the Mamma Pasta application. These classes represent the core entities the application works with, such as pizzas, cart items, chat messages, and toppings.

## 1. `Pizza.java`

**Purpose:**
Represents a predefined pizza available in the application. This model is typically used for pizzas fetched from the local database (`DBHelper`) and displayed in `HomeActivity`.

**Properties:**
- `id` (int): A unique identifier for the pizza (likely corresponds to `id_pizza` in the database).
- `nombre` (String): The name of the pizza (e.g., "Napolitana", "Hawaiana").
- `descripcion` (String): A brief description of the pizza's main ingredients.
- `precioBase` (double): The base price of this predefined pizza.
- `imagenResource` (String): The name of the drawable resource used for displaying the pizza's image (e.g., "pizza_napolitana").

**Constructor:**
- `public Pizza(int id, String nombre, String descripcion, double precioBase, String imagenResource)`

**Getters:**
- `getId()`
- `getNombre()`
- `getDescripcion()`
- `getPrecioBase()`
- `getImagenResource()`

## 2. `CartItem.java`

**Purpose:**
Represents an item within the user's shopping cart. This can be a predefined pizza or a custom-built pizza. This class implements `java.io.Serializable`, allowing its instances to be serialized, which is utilized by `CartManager` when saving the cart to SharedPreferences via Gson.

**Properties:**
- `nombrePizza` (String): The name of the pizza (e.g., "Pepperoni", "Pizza Personalizada").
- `descripcion` (String): A description of the item. For predefined pizzas, this might be its standard description. For custom pizzas, this might be empty or hold custom details if not covered by `ingredientes`.
- `ingredientes` (String): A string detailing the ingredients. For predefined pizzas, this might be redundant if `descripcion` is sufficient. For custom pizzas, this holds the selected masa, salsa, toppings, etc. For cart orders (multiple items), this holds an aggregated list of all items.
- `precio` (double): The price of a single unit of this cart item. For custom pizzas, this is the calculated `precio_total`. For predefined pizzas, this is their `precioBase`.
- `cantidad` (int): The quantity of this specific item in the cart.
- `imagenResource` (String): The name of the drawable resource for the item's image.

**Constructors:**
- `public CartItem()`: Default constructor (likely for Gson deserialization).
- `public CartItem(String nombrePizza, String descripcion, String ingredientes, double precio, int cantidad, String imagenResource)`

**Getters and Setters:**
- `getNombrePizza()`, `setNombrePizza(String nombrePizza)`
- `getDescripcion()`, `setDescripcion(String descripcion)`
- `getIngredientes()`, `setIngredientes(String ingredientes)`
- `getPrecio()`, `setPrecio(double precio)`
- `getCantidad()`, `setCantidad(int cantidad)`
- `getImagenResource()`, `setImagenResource(String imagenResource)`

**Calculated Property (Method):**
- `public double getPrecioTotal()`: Returns `precio * cantidad`.

## 3. `Message.java`

**Purpose:**
Represents a single message in the chat interface (`ChatActivity`). It distinguishes between messages sent by the user and messages sent by the bot.

**Properties:**
- `text` (String): The content of the chat message.
- `isUser` (boolean): `true` if the message was sent by the user, `false` if it's a bot message.

**Constructor:**
- `public Message(String text, boolean isUser)`

**Getters:**
- `getText()`
- `isUser()`

## 4. `Topping.java`

**Purpose:**
Represents a pizza topping that can be added to a custom-built pizza. This model is used in `ConstruirPizzaActivity` and data is sourced from `DBHelper`.

**Properties:**
- `id` (int): A unique identifier for the topping (likely corresponds to `id_topping` in the database).
- `nombre` (String): The name of the topping (e.g., "Pepperoni", "Champiñones").
- `precioExtra` (double): The additional cost for adding this topping to a pizza.

**Constructor:**
- `public Topping(int id, String nombre, double precioExtra)`

**Getters:**
- `getId()`
- `getNombre()`
- `getPrecioExtra()`

---
**Note on `com.mammapasta.dispatch.Order.java`:**
An `Order.java` class exists within the `com.mammapasta.dispatch` package. It is intended for Firebase data representation. However, as noted in `docs/feature_dispatch.md`, its structure has some discrepancies with the actual data map being saved to Firebase by `DeliveryActivity.java`. `DeliveryActivity` constructs a `Map<String, Object>` directly for Firebase, and `SeguimientoPedidoActivity` primarily listens to the `estado` field rather than deserializing the entire order object using this `Order.java` model. For this reason, `dispatch.Order.java` is detailed more within the dispatch feature documentation rather than as a central application model here.
