# Core Components Documentation

This document details the core components of the Mamma Pasta application.

## 1. `MainActivity.java`

**Purpose:**
`MainActivity` serves as the initial entry point of the application. Its primary responsibility is to direct the user to the appropriate screen based on their login status.

**Functionality:**
- Initializes `PreferencesManager` to check the user's login state.
- If the user is logged in (as determined by `preferencesManager.isLoggedIn()`), it navigates the user to `HomeActivity`.
- If the user is not logged in, it navigates the user to `LoginActivity`.
- After starting the target activity, `MainActivity` finishes itself to remove it from the back stack.

**Key Dependencies:**
- `androidx.appcompat.app.AppCompatActivity`: Base class for activities.
- `com.mammapasta.home.HomeActivity`: The main screen for logged-in users.
- `com.mammapasta.login.LoginActivity`: The screen for user login.
- `com.mammapasta.utils.PreferencesManager`: Used to check login status.

## 2. `MammapastaApp.java`

**Purpose:**
`MammapastaApp` is the custom `Application` class for the Mamma Pasta app. It is used for global application-level initializations that need to occur before any activity is created.

**Functionality:**
- Overrides the `onCreate()` method of the `Application` class.
- Initializes and starts `ClosedHoursWatcher`. This suggests that the app has a mechanism to handle behavior based on business operating hours (e.g., disabling ordering, showing a "closed" message).

**Key Dependencies:**
- `android.app.Application`: Base class for maintaining global application state.
- `com.mammapasta.guardian.ClosedHoursWatcher`: A class responsible for monitoring business hours.

## 3. `PreferencesManager.java`

**Located in:** `com.mammapasta.utils`

**Purpose:**
`PreferencesManager` is a utility class responsible for managing persistent application preferences using Android's `SharedPreferences`. It provides a centralized way to store and retrieve simple key-value data.

**Functionality:**
- **Initialization:**
    - Takes a `Context` in its constructor to get access to `SharedPreferences`.
    - Uses a private preferences file named `"MAMMAPASTA_PREFS"`.
- **Session Management:**
    - `setLoggedIn(boolean loggedIn)`: Stores the user's login status.
    - `isLoggedIn()`: Retrieves the user's login status (defaults to `false`).
- **User Email:**
    - `setEmail(String email)`: Stores the user's email.
    - `getEmail()`: Retrieves the user's email (defaults to `null`).
- **Login Security:**
    - `setLoginAttempts(int attempts)`: Stores the number of failed login attempts.
    - `getLoginAttempts()`: Retrieves the number of failed login attempts (defaults to `0`).
    - `setBlockUntil(long timestamp)`: Stores a timestamp until which the user's login is blocked.
    - `getBlockUntil()`: Retrieves the block timestamp (defaults to `0`).
- **Order Information:**
    - `setOrderId(String orderId)`: Stores the ID of the last order.
    - `getOrderId()`: Retrieves the last order ID.
    - `setOrderStatus(String status)`: Stores the status of the current/last order.
    - `getOrderStatus()`: Retrieves the order status.
- **Utility:**
    - `clearAll()`: Clears all preferences stored in the file.

**Key Constants for SharedPreferences Keys:**
- `PREFS_NAME`: `"MAMMAPASTA_PREFS"`
- `KEY_IS_LOGGED_IN`: `"isLoggedIn"`
- `KEY_EMAIL`: `"email"`
- `KEY_LOGIN_ATTEMPTS`: `"loginAttempts"`
- `KEY_BLOCK_UNTIL`: `"blockUntil"`
- `KEY_ORDER_ID`: `"ultimo_order_id"`
- `KEY_ESTADO_PEDIDO`: `"estado_pedido"`

## 4. `DBHelper.java`

**Located in:** `com.mammapasta.db`

**Purpose:**
`DBHelper` is a subclass of `SQLiteOpenHelper` and is responsible for managing the application's local SQLite database. This includes creating the database schema, inserting initial data, and providing methods to query and modify the data.

**Functionality:**
- **Database Setup:**
    - Database Name: `mammapasta.db`
    - Database Version: `5` (as of the current code)
    - Tables:
        - `TABLE_PIZZAS`: Stores information about predefined pizzas.
        - `TABLE_TOPPINGS`: Stores information about available pizza toppings.
        - `TABLE_PEDIDOS`: Stores user order history.
- **Schema Creation (`onCreate`):**
    - Defines and executes SQL `CREATE TABLE` statements for `TABLE_PIZZAS`, `TABLE_TOPPINGS`, and `TABLE_PEDIDOS`.
    - Calls `insertInitialData(db)` to populate the database with default pizzas and toppings.
- **Initial Data Insertion (`insertInitialData`):**
    - Inserts several predefined pizzas (Napolitana, Hawaiana, Pepperoni, Cuatro Quesos, Vegetariana, Carbonara) into `TABLE_PIZZAS`.
    - Inserts initial toppings (Pepperoni, Piña, Carne) into `TABLE_TOPPINGS`.
- **Database Upgrade (`onUpgrade`):**
    - Handles database schema upgrades by dropping all existing tables and recreating them. This is a simple upgrade strategy; more complex apps might migrate data.
- **Data Access Methods:**
    - `getAllPizzas()`: Retrieves a list of all `Pizza` objects from `TABLE_PIZZAS`.
    - `getAllToppings()`: Retrieves a list of all `Topping` objects from `TABLE_TOPPINGS`.
    - `insertarPedido(String email, String nombrePizza, String ingredientes, double precio)`: Inserts a new order into `TABLE_PEDIDOS` for the given user.
    - `getPedidosByEmail(String email)`: Retrieves a list of order details (formatted as strings) for a specific user email from `TABLE_PEDIDOS`.

**Table Schemas:**
- **`TABLE_PIZZAS`**:
    - `id_pizza` (INTEGER, PRIMARY KEY, AUTOINCREMENT)
    - `nombre` (TEXT)
    - `descripcion` (TEXT)
    - `precio_base` (REAL)
    - `imagen_resource` (TEXT) - Stores the name of the drawable resource for the pizza image.
- **`TABLE_TOPPINGS`**:
    - `id_topping` (INTEGER, PRIMARY KEY, AUTOINCREMENT)
    - `nombre` (TEXT)
    - `precio_extra` (REAL)
- **`TABLE_PEDIDOS`**:
    - `id_pedido` (INTEGER, PRIMARY KEY, AUTOINCREMENT)
    - `email` (TEXT) - User's email associated with the order.
    - `nombre_pizza` (TEXT) - Name of the pizza or "Pedido completo" for cart orders.
    - `ingredientes` (TEXT) - Details of the pizza/order.
    - `precio` (REAL) - Final price of the order.

**Key Dependencies:**
- `android.database.sqlite.SQLiteOpenHelper`: Base class for database management.
- `com.mammapasta.models.Pizza`: Data model for pizzas.
- `com.mammapasta.models.Topping`: Data model for toppings.
