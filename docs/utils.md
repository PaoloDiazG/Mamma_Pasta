# Utility Classes and Mechanisms

This document covers various utility classes and mechanisms used throughout the Mamma Pasta application, including cart management, Firebase data loading, and business hours enforcement.

## 1. Cart Management

**Class:** `com.mammapasta.utils.CartManager`

**Purpose:**
`CartManager` is a singleton class responsible for managing the user's shopping cart. It handles adding, removing, and updating quantities of `CartItem` objects and persists the cart state using `SharedPreferences` by serializing/deserializing the cart list with Gson.

**Key Features:**
- **Singleton Pattern:** Ensured by `getInstance(Context context)` which provides a single, application-wide instance.
- **Persistence:** Uses `SharedPreferences` (file name: `"cart_prefs"`, key: `"cart_items"`) to save the list of `CartItem` objects as a JSON string.
- **Gson Integration:** Uses `Gson` for serializing `List<CartItem>` to JSON and deserializing it back.

**Methods:**
- **`getInstance(Context context)`:** Static method to get the singleton instance.
- **`addToCart(CartItem item)`:**
    - Retrieves the current list of `CartItem`s.
    - Checks if an identical item (same name and ingredients) already exists.
        - If found, increments its quantity.
        - If not found, sets the new item's quantity to 1 and adds it to the list.
    - Saves the updated list to SharedPreferences.
- **`getCartItems()`:**
    - Retrieves the JSON string from SharedPreferences.
    - If empty, returns a new empty `ArrayList`.
    - Otherwise, deserializes the JSON string back into a `List<CartItem>` using Gson and `TypeToken`.
- **`removeFromCart(int position)`:**
    - Removes the item at the given `position` from the cart list.
    - Saves the updated list.
- **`updateQuantity(int position, int newQuantity)`:**
    - Updates the quantity of the item at `position`.
    - If `newQuantity` is 0 or less, removes the item.
    - Otherwise, sets the item's quantity to `newQuantity`.
    - Saves the updated list.
- **`clearCart()`:**
    - Saves an empty `ArrayList` to SharedPreferences, effectively clearing the cart.
- **`getTotalPrice()`:**
    - Calculates and returns the sum of `item.getPrecioTotal()` for all items in the cart.
- **`getTotalItems()`:**
    - Calculates and returns the sum of `item.getCantidad()` for all items in the cart.
- **`saveCartItems(List<CartItem> cartItems)` (private):**
    - Serializes the given `cartItems` list to a JSON string using Gson.
    - Saves the JSON string to SharedPreferences.

**Dependencies:**
- `android.content.SharedPreferences`: For data persistence.
- `com.google.gson.Gson`: For JSON serialization/deserialization.
- `com.google.gson.reflect.TypeToken`: For specifying the generic type `List<CartItem>` to Gson.
- `com.mammapasta.models.CartItem`: The data model for items in the cart. (Assumed to have methods like `getNombrePizza()`, `getIngredientes()`, `setCantidad()`, `getCantidad()`, `getPrecioTotal()`).

## 2. Firebase Data Loader

**Class:** `com.mammapasta.utils.FirebaseDataLoader`

**Purpose:**
`FirebaseDataLoader` provides a utility method to populate the Firebase Realtime Database with initial data, specifically for promotional codes. This is likely intended for development or initial setup.

**Methods:**
- **`cargarCodigosPromocionalesIniciales()` (static):**
    - Gets a Firebase `DatabaseReference` to `/codigos_promocionales`.
    - Creates a `Map<String, Object>` to hold multiple promotional codes.
    - Defines several sample codes (e.g., "DESCUENTO10", "PROMO15", "VENCIDO50") with attributes:
        - `descuento` (long): The discount percentage.
        - `activo` (boolean): Whether the code is currently active.
        - `descripcion` (String): A brief description of the code.
    - Calls `ref.setValue(codigos)` to overwrite the entire `/codigos_promocionales` path in Firebase with this new map.
    - Includes `addOnSuccessListener` and `addOnFailureListener` to print success or error messages to `System.out` or `System.err`.

**Usage:**
This method would typically be called once during application setup or from a development/admin tool to ensure Firebase has the necessary promotional code data for `QRActivity` to function. It's not part of the regular user flow.

**Dependencies:**
- `com.google.firebase.database.DatabaseReference`
- `com.google.firebase.database.FirebaseDatabase`

## 3. Business Hours Enforcement

This mechanism prevents users from accessing the app's main functionalities outside of defined operating hours.

### a. `com.mammapasta.closed.TimeUtils`

**Purpose:**
A utility class providing static methods to check current time against defined business hours.

**Constants:**
- `START_HOUR = 22` (10 PM)
- `END_HOUR = 10` (10 AM the next day)

**Methods:**
- **`isWithinClosedHours()` (static):**
    - Gets the current hour of the day (0-23).
    - Returns `true` if the current hour is `>= START_HOUR` (e.g., 10 PM, 11 PM) OR `< END_HOUR` (e.g., midnight through 9 AM). This defines the closed period.
    - Returns `false` otherwise (i.e., during open hours, 10 AM to 9:59 PM).
- **`getCurrentHour()` (static):** Returns the current hour of the day (0-23).
- **`getCurrentMinute()` (static):** Returns the current minute of the hour (0-59).

### b. `com.mammapasta.guardian.ClosedHoursWatcher`

**Purpose:**
Implements `Application.ActivityLifecycleCallbacks` to monitor activity lifecycle events. On every `onActivityResumed`, it checks if the app is within closed hours and redirects to `ClosedHoursActivity` if necessary.

**Functionality:**
- **Constructor (`ClosedHoursWatcher(Application app)`):**
    - Stores the `Application` instance.
    - Registers itself as an activity lifecycle callback: `app.registerActivityLifecycleCallbacks(this)`.
- **`checkAndRedirect(Activity activity)` (private):**
    - If the current `activity` is already `ClosedHoursActivity`, it returns to prevent loops.
    - **Warning Toast:**
        - If the current time is near closing (e.g., 9:55 PM - 9:59 PM, specifically checks `currentHour == 21 && currentMinute >= 55`) and a warning hasn't been shown yet (`!alreadyWarned`), it displays a Toast: "¡Atención! En pocos segundos la app se dormirá 🍕💤". Sets `alreadyWarned = true`.
    - **Redirection Logic:**
        - Uses `handler.postDelayed` with a 1-second delay. This delay might be to allow the activity to fully resume before redirecting or to give the warning toast time to be seen.
        - Inside the delayed task, it calls `TimeUtils.isWithinClosedHours()`.
        - If true, creates an `Intent` for `ClosedHoursActivity` with flags `Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK` (to clear the back stack and make `ClosedHoursActivity` the new root).
        - Starts `ClosedHoursActivity`.
- **`onActivityResumed(Activity activity)`:**
    - This is the primary callback method used. It calls `checkAndRedirect(activity)`.
- Other `ActivityLifecycleCallbacks` methods are implemented but empty.

**Initialization:**
- An instance of `ClosedHoursWatcher` is created in `MammapastaApp.onCreate()`, ensuring it monitors all activities from the start of the application.

### c. `com.mammapasta.closed.ClosedHoursActivity`

**Purpose:**
A simple activity displayed when the user tries to access the app during closed hours. It informs the user and provides an option to exit the application.

**UI Elements:**
- `btnSalir`: `Button` to exit the application.

**Functionality:**
- **Initialization (`onCreate`):**
    - Sets the layout `R.layout.activity_closed_hours`. (Layout likely contains a message indicating the app is closed and the exit button).
    - Initializes `btnSalir`.
    - Sets `OnClickListener` for `btnSalir`:
        - Calls `finishAffinity()` to close all activities in the current task.
        - Calls `System.exit(0)` to terminate the application process forcefully. This is a very strong way to exit and generally discouraged, but used here to ensure complete shutdown.
    - **Back Button Handling:**
        - Registers an `OnBackPressedCallback` that does nothing (`handleOnBackPressed()` is empty). This effectively disables the back button, preventing the user from navigating away from this screen.

**Interactions:**
1.  `MammapastaApp` initializes `ClosedHoursWatcher`.
2.  When any activity (except `ClosedHoursActivity` itself) is resumed, `ClosedHoursWatcher.onActivityResumed` is called.
3.  `ClosedHoursWatcher` uses `TimeUtils.isWithinClosedHours()` to check the time.
4.  If within closed hours, `ClosedHoursWatcher` redirects the user to `ClosedHoursActivity`.
5.  `LoginActivity` and `RegisterActivity` also perform an initial check using `TimeUtils.isWithinClosedHours()` in their `onCreate` methods and redirect to `ClosedHoursActivity` if closed, preventing even the login/register screens from being shown.
6.  Once on `ClosedHoursActivity`, the user can only exit the app.

This system ensures that app usage is restricted to the defined open hours.
---

**Note on `PreferencesManager.java`:**
While `PreferencesManager` is located in the `com.mammapasta.utils` package and is a utility, it was documented as part of the "Core Components" because of its fundamental role in managing user sessions and global app settings, making it more integral than a typical helper utility. Its documentation can be found in `docs/core_components.md`.
