# Feature: Pizza Builder

**Module Package:** `com.mammapasta.builder`

## Purpose

The Pizza Builder module allows users to create a custom pizza by selecting various options like masa (crust), salsa (sauce), cheese, and multiple toppings. It dynamically calculates the total price based on these selections and then passes the customized pizza details to the `ConfirmacionActivity` for order review.

## Key Components

### 1. `ConstruirPizzaActivity.java`

**Purpose:**
Provides the user interface and logic for customizing a pizza.

**UI Elements:**
- `spinnerMasa`: `Spinner` for selecting pizza crust type (Delgada, Gruesa, Integral).
- `spinnerSalsa`: `Spinner` for selecting pizza sauce type (Tomate, BBQ, Blanca).
- `chkQueso`: `CheckBox` to add extra cheese (fixed price of S/.2.0).
- `layoutToppings`: `LinearLayout` to dynamically add CheckBoxes for available toppings.
- `txtPrecioTotal`: `TextView` to display the dynamically calculated total price of the custom pizza.
- `btnConfirmar`: `Button` to finalize the custom pizza and proceed to `ConfirmacionActivity`.

**Instance Variables:**
- `toppingsList`: `List<Topping>` to store toppings fetched from the database.
- `toppingCheckboxes`: `Map<CheckBox, Topping>` to associate dynamically created CheckBoxes with their corresponding `Topping` objects.
- `precioBase`: `double` representing the base price for a custom pizza (initialized to S/.10.0).
- `precioTotal`: `double` to store the calculated total price.

**Functionality:**
- **Initialization (`onCreate`):**
    - Sets the layout `R.layout.activity_construir_pizza`.
    - Initializes all UI elements.
    - Calls `cargarSpinners()` to populate masa and salsa spinners.
    - Calls `cargarToppings()` to fetch toppings from `DBHelper` and create CheckBoxes for them.
    - Sets an `OnCheckedChangeListener` for `chkQueso` to recalculate the total price when its state changes.
    - Sets an `OnClickListener` for `btnConfirmar` to call `confirmarPizza()`.
- **Loading Spinners (`cargarSpinners`):**
    - Defines arrays for `masas` ("Delgada", "Gruesa", "Integral") and `salsas` ("Tomate", "BBQ", "Blanca").
    - Creates `ArrayAdapter`s for these options and sets them to `spinnerMasa` and `spinnerSalsa`.
    - *Note: Spinner selection changes do not trigger `calcularTotal()` directly in the provided code, implying their cost is part of `precioBase` or fixed.*
- **Loading Toppings (`cargarToppings`):**
    - Initializes `DBHelper`.
    - Fetches all available toppings using `dbHelper.getAllToppings()` and stores them in `toppingsList`.
    - Iterates through `toppingsList`:
        - Creates a new `CheckBox` for each topping.
        - Sets the CheckBox text to include the topping name and its extra price (e.g., "Pepperoni (+S/.5.0)").
        - Adds the CheckBox to `layoutToppings`.
        - Stores the CheckBox and its corresponding `Topping` object in the `toppingCheckboxes` map.
        - Sets an `OnCheckedChangeListener` for each topping CheckBox to call `calcularTotal()` when its state changes.
    - Calls `calcularTotal()` once after loading to set the initial price.
- **Price Calculation (`calcularTotal`):**
    - Starts with `total = precioBase` (S/.10.0).
    - If `chkQueso.isChecked()`, adds S/.2.0 to `total`.
    - Iterates through `toppingCheckboxes`:
        - If a topping's CheckBox is checked, adds `topping.getPrecioExtra()` to `total`.
    - Updates `precioTotal` with the calculated `total`.
    - Sets the `txtPrecioTotal` text to display the formatted `precioTotal` (e.g., "Total: S/.15.50").
- **Confirming Pizza (`confirmarPizza`):**
    - Retrieves the selected `masa` and `salsa` from the spinners.
    - Gets the boolean state of `conQueso` from `chkQueso`.
    - Creates a `List<String>` called `toppingsSeleccionados`.
    - Iterates through `toppingCheckboxes`: if a topping's CheckBox is checked, its name is added to `toppingsSeleccionados`.
    - Creates an `Intent` to navigate to `ConfirmacionActivity`.
    - Puts the following data as extras in the Intent:
        - `"masa"` (String): Selected masa type.
        - `"salsa"` (String): Selected salsa type.
        - `"queso"` (boolean): Whether extra cheese was selected.
        - `"toppings"` (String): A comma-separated string of selected topping names (e.g., "Pepperoni, Champiñones").
        - `"precio_total"` (double): The final calculated `precioTotal`.
    - Starts `ConfirmacionActivity`.

**Dependencies:**
- `com.mammapasta.R`: For layout and UI element IDs.
- `com.mammapasta.db.DBHelper`: To fetch the list of available toppings and their prices.
- `com.mammapasta.models.Topping`: Data model representing a pizza topping.
- `com.mammapasta.order.ConfirmacionActivity`: The next activity where the user reviews and confirms their custom pizza order.
- `android.widget.*`: For UI elements like Spinners, CheckBoxes, LinearLayout, TextView, Button.
- `androidx.appcompat.app.AppCompatActivity`: Base class for the activity.

## Interactions and Flow

1.  **Navigation:** The user navigates to `ConstruirPizzaActivity` typically from `HomeActivity` (by clicking `btnConstruirPizza`).
2.  **Initialization:**
    *   The activity loads crust and sauce options into spinners.
    *   It dynamically fetches toppings from the database and creates interactive CheckBox options for each.
    *   The initial total price is calculated and displayed.
3.  **Pizza Customization:**
    *   The user selects their desired `masa` from `spinnerMasa`.
    *   The user selects their desired `salsa` from `spinnerSalsa`.
    *   The user checks/unchecks `chkQueso` for extra cheese. This action immediately triggers `calcularTotal()`.
    *   The user checks/unchecks various topping CheckBoxes. Each change triggers `calcularTotal()`.
    *   `txtPrecioTotal` is updated in real-time to reflect the current selections.
4.  **Confirmation:**
    *   Once satisfied, the user clicks `btnConfirmar`.
5.  **Proceeding to Order Review:**
    *   `ConstruirPizzaActivity` gathers all the selected options (masa, salsa, cheese status, list of topping names) and the final `precioTotal`.
    *   It packages this information into an `Intent` and starts `ConfirmacionActivity`, where these details will be displayed for final confirmation before placing the order.

This module provides a highly interactive way for users to build their dream pizza. The dynamic price calculation and clear presentation of options enhance the user experience.
