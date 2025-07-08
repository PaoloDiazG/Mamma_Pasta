# Feature: Order Confirmation and Payment

**Module Package:** `com.mammapasta.order`

## Purpose

This module is responsible for displaying the final details of an order (either a custom-built pizza or items from the shopping cart), allowing the application of promotional codes, and confirming the purchase. Upon confirmation, the order is saved to the local database, and the user is navigated to a delivery/tracking screen.

## Key Components

### 1. `ConfirmacionActivity.java`

**Purpose:**
Presents a summary of the order to the user, handles discount application via QR codes, and finalizes the purchase by saving it to the database.

**UI Elements:**
- `txtTituloPizza`: `TextView` to display the title of the order (e.g., "Pizza Personalizada" or "Pedido completo").
- `txtDetalles`: `TextView` to display the detailed breakdown of the order (e.g., masa, salsa, toppings, or list of cart items).
- `txtPrecioFinal`: `TextView` to display the final calculated price after any discounts.
- `txtDescuento`: `TextView` to show the amount of discount applied (e.g., "Descuento: -S/.5.00").
- `txtCodigoAplicado`: `TextView` to show the promotional code that was applied (e.g., "Código: PROMO123").
- `btnConfirmarCompra`: `Button` for the user to confirm the purchase.
- `btnCodigoPromocional`: `Button` to navigate to `QRActivity` to scan a promotional code.
- `layoutDescuento`: `View` (likely a LinearLayout or similar container) that holds `txtDescuento` and `txtCodigoAplicado`; its visibility is toggled based on whether a discount is applied.

**Instance Variables:**
- `extras`: `Bundle` to store the data passed via Intent from the previous activity (`ConstruirPizzaActivity` or `HomeActivity`).
- `precioOriginal`: `double` to store the price of the order before any discounts.
- `descuentoAplicado`: `double` to store the percentage of discount applied (e.g., 10 for 10%). Defaults to `0.0`.

**Functionality:**
- **Initialization (`onCreate`):**
    - Sets the layout `R.layout.activity_confirmacion`.
    - Initializes all UI elements.
    - Retrieves the `extras` from the `getIntent()`.
    - **Populating Order Details:**
        - If `extras` is not null:
            - **Custom Pizza Order (from `ConstruirPizzaActivity`):**
                - Checks if `extras.containsKey("masa")`.
                - Sets `txtTituloPizza` to "Pizza Personalizada".
                - Retrieves `masa`, `salsa`, `queso` (boolean), `toppings` (String), and `precio_total` (assigned to `precioOriginal`) from `extras`.
                - Formats these details into a descriptive string and sets it to `txtDetalles`.
            - **Cart Order (from `HomeActivity`):**
                - Else (if not a custom pizza, it's a cart order).
                - Retrieves `nombre_pizza` ("Pedido completo"), `ingredientes` (aggregated cart item details), and `precio` (assigned to `precioOriginal`) from `extras`.
                - Sets `txtTituloPizza` and `txtDetalles`.
            - **Discount Handling:**
                - Checks if `extras.containsKey("descuento")`. This implies that `QRActivity` returns to `ConfirmacionActivity` with discount info in the intent.
                - If a discount exists:
                    - Retrieves `descuento` (percentage) and `codigo_promocional` from `extras`.
                    - Makes `layoutDescuento` visible.
                    - Sets `txtCodigoAplicado` to show the promotional code.
                    - Calculates and displays the discount amount in `txtDescuento` (e.g., "Descuento: -S/.[value]").
            - Calls `actualizarPrecio()` to calculate and display the final price.
- **Button Click Listeners:**
    - **`btnCodigoPromocional`:**
        - Creates an `Intent` for `QRActivity`.
        - If `extras` is not null, it passes the current `extras` bundle to `QRActivity`. This is crucial so that `QRActivity` can return to `ConfirmacionActivity` with the original order details plus any scanned discount.
        - Starts `QRActivity`.
        - `finish()` is called, meaning `ConfirmacionActivity` is closed. When `QRActivity` finishes, it will restart `ConfirmacionActivity` with potentially new extras (including discount).
    - **`btnConfirmarCompra`:**
        - Initializes `PreferencesManager` to get the logged-in `userEmail`.
        - Determines `nombrePizza` and `detalles` based on whether it was a custom pizza (extras contain "masa") or a cart order.
        - Calls `obtenerPrecioFinal()` to get the final price after discounts.
        - If `descuentoAplicado > 0`, appends discount information (percentage and code) to the `detalles` string.
        - Initializes `DBHelper`.
        - Calls `dbHelper.insertarPedido(userEmail, nombrePizza, detalles, precioFinal)` to save the order to the local database.
        - Shows a "¡Pedido guardado!" Toast.
        - Creates an `Intent` for `DeliveryActivity`.
        - Puts `nombre_pizza`, `detalles_pedido` (the full details including discount), `precio_pedido` (final price), and `user_email` into the intent extras for `DeliveryActivity`.
        - Starts `DeliveryActivity`.
        - `finish()` is called to remove `ConfirmacionActivity` from the back stack.
- **Price Update (`actualizarPrecio`):**
    - Calls `obtenerPrecioFinal()`.
    - Sets `txtPrecioFinal` to display the formatted final price (e.g., "Total: S/.XX.XX").
- **Get Final Price (`obtenerPrecioFinal`):**
    - Calculates `precioOriginal - (precioOriginal * descuentoAplicado / 100)`.
    - Returns the result.

**Dependencies:**
- `com.mammapasta.R`: For layout and UI element IDs.
- `com.mammapasta.db.DBHelper`: To save the confirmed order.
- `com.mammapasta.dispatch.DeliveryActivity`: The next activity after order confirmation, likely for showing delivery status.
- `com.mammapasta.qrreader.QRActivity`: Activity used to scan promotional QR codes.
- `com.mammapasta.utils.PreferencesManager`: To get the current user's email for saving the order.
- `androidx.appcompat.app.AppCompatActivity`: Base class for the activity.

## Input Extras Expected

From `ConstruirPizzaActivity` (for a custom pizza):
- `"masa"` (String)
- `"salsa"` (String)
- `"queso"` (boolean)
- `"toppings"` (String)
- `"precio_total"` (double) - This becomes `precioOriginal`.

From `HomeActivity` (for a cart order):
- `"nombre_pizza"` (String, usually "Pedido completo")
- `"ingredientes"` (String, aggregated details of cart items)
- `"precio"` (double) - This becomes `precioOriginal`.

From `QRActivity` (when returning with a discount):
- All the original extras passed to it.
- `"descuento"` (double, e.g., 10.0 for 10%)
- `"codigo_promocional"` (String)

## Output Extras Sent (to `DeliveryActivity`)

- `"nombre_pizza"` (String)
- `"detalles_pedido"` (String, including any discount info)
- `"precio_pedido"` (double, final price)
- `"user_email"` (String)

## Interactions and Flow

1.  **Navigation:**
    *   User arrives from `ConstruirPizzaActivity` after building a custom pizza.
    *   User arrives from `HomeActivity` after clicking "Proceder Pago" with items in the cart.
    *   User arrives from `QRActivity` after scanning a QR code (this is effectively a re-entry with added discount information).
2.  **Display Order:** The activity populates its UI fields with the order details received in the `Intent` extras. If a discount was previously applied (i.e., returning from `QRActivity`), discount information is also displayed.
3.  **Apply Promotional Code (Optional):**
    *   User clicks `btnCodigoPromocional`.
    *   `ConfirmacionActivity` finishes itself and starts `QRActivity`, passing along the current order details.
    *   `QRActivity` (not detailed here) presumably scans a code, validates it, and if valid, restarts `ConfirmacionActivity`, adding `"descuento"` and `"codigo_promocional"` to the Intent extras.
4.  **Confirm Purchase:**
    *   User reviews the order (including any discounts) and clicks `btnConfirmarCompra`.
    *   The order details (including user email, final price, and discount info) are saved to the local SQLite database via `DBHelper`.
    *   The user is then navigated to `DeliveryActivity`, passing along relevant order information. `ConfirmacionActivity` is finished.

This activity acts as a crucial checkpoint before finalizing an order, ensuring the user can review all details and apply discounts.
