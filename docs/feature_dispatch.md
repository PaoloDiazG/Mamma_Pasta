# Feature: Order Dispatch and Tracking

**Module Package:** `com.mammapasta.dispatch`

## Purpose

This module handles the process after an order is confirmed. It includes collecting delivery details, submitting the order to a backend (Firebase Realtime Database), and allowing the user to track the status of their delivery in real-time.

## Key Components

### 1. `DeliveryActivity.java`

**Purpose:**
Collects delivery information from the user, such as address (via GPS or manual input), reference points, and phone number. It then submits this information along with the order details to Firebase Realtime Database.

**UI Elements:**
- `btnUbicacionActual`: Button to use the device's current GPS location for the address.
- `btnUbicacionManual`: Button to allow manual input of the delivery address.
- `btnConfirmarEntrega`: Button to confirm the delivery details and submit the order.
- `editDireccion`: `TextInputEditText` for the delivery address (can be auto-filled by GPS or manually entered).
- `editReferencia`: `TextInputEditText` for additional delivery references.
- `editTelefono`: `TextInputEditText` for the user's phone number.
- `txtUbicacionStatus`: `TextView` to display status messages related to location fetching (e.g., "Obteniendo ubicación...", "Ubicación obtenida").
- `txtTiempoEntrega`: `TextView` (not actively used in the provided code for dynamic time updates, but present in layout).

**Instance Variables:**
- `locationManager`: `LocationManager` for accessing GPS services.
- `isUsingCurrentLocation`: `boolean` flag, true if GPS is selected.
- `isLocationObtained`: `boolean` flag, true if GPS location was successfully fetched.
- `nombrePizza`, `detallesPedido`, `precioPedido`, `userEmail`: Order details passed from `ConfirmacionActivity`.
- `customerName`: Customer's name (defaults to email if not available).
- `databaseReference`: `DatabaseReference` for Firebase Realtime Database.
- `orderId`: `String` (UUID) generated for the new order.

**Functionality:**
- **Initialization (`onCreate`):**
    - Initializes Firebase `databaseReference` and generates a unique `orderId`.
    - Calls `receiveOrderData()` to get order details from `ConfirmacionActivity`'s intent extras.
    - Initializes UI views.
    - Calls `setupListeners()` for button clicks and text changes.
    - Calls `setupLocationManager()`.
    - Sets up an `OnBackPressedCallback` to show a confirmation dialog (`mostrarConfirmacionSalir()`) if there are unsaved changes.
- **Firebase Initialization (`initializeFirebase`):** Sets up `databaseReference` to the root of Firebase and generates `orderId`.
- **Receive Order Data (`receiveOrderData`):** Retrieves pizza name, details, price, and user email from intent extras. Fetches `customerName` from `PreferencesManager`.
- **Listeners Setup (`setupListeners`):**
    - `btnUbicacionActual`: Sets `isUsingCurrentLocation = true`, calls `getCurrentLocation()`, updates button UI.
    - `btnUbicacionManual`: Sets `isUsingCurrentLocation = false`, calls `enableManualInput()`, updates button UI.
    - `btnConfirmarEntrega`: Calls `confirmarEntrega()`.
    - `TextWatcher` on `editDireccion` and `editTelefono` to call `validateFieldsAndUpdateButton()` for real-time validation.
- **Location Manager Setup (`setupLocationManager`):** Gets system `LocationManager` service.
- **Get Current Location (`getCurrentLocation`):**
    - Checks for `ACCESS_FINE_LOCATION` permission. Requests it if not granted using `requestLocationPermission()`.
    - Shows "🔄 Obteniendo ubicación..." status.
    - Tries to get `lastKnownLocation` from GPS or Network provider. If available, calls `onLocationChanged()`.
    - If no last known location, requests new location updates from GPS or Network provider.
    - Handles cases where GPS is disabled.
- **Permission Request (`requestLocationPermission`):** Requests location permissions.
- **Enable Manual Input (`enableManualInput`):** Enables `editDireccion`, clears its text, hides location status.
- **Update Button States (`updateButtonStates`):** Changes background tints of location mode buttons and enables/disables `editDireccion` based on `isUsingCurrentLocation`.
- **Location Status Display (`showLocationStatus`, `hideLocationStatus`):** Manages visibility and text of `txtUbicacionStatus`.
- **Field Validation (`validateFieldsAndUpdateButton`, `validateFields`):**
    - Checks if `editDireccion` is filled (if manual mode) or if `isLocationObtained` (if GPS mode).
    - Validates `editTelefono` for 9 digits.
    - Enables/disables `btnConfirmarEntrega` accordingly.
- **Confirm Delivery (`confirmarEntrega`):**
    - Disables `btnConfirmarEntrega` and sets text to "Confirmando...".
    - Re-validates fields; returns if invalid.
    - Calls `createOrderData()` to build the order map.
    - Saves the `orderData` to Firebase under `orders/{orderId}`.
        - **On Success:**
            - Calls `updateOrderStatus("Recibido")` to set the initial status in Firebase.
            - Shows "Pedido confirmado exitosamente" Toast.
            - Saves `orderId` to `PreferencesManager`.
            - Navigates to `SeguimientoPedidoActivity`, passing `orderId`.
            - Finishes `DeliveryActivity`.
        - **On Failure:** Shows error Toast, re-enables button.
- **Update Order Status (`updateOrderStatus`):** Updates `estado` and `fechaActualizacion` fields for the order in Firebase.
- **Create Order Data (`createOrderData`):**
    - Creates a `Map<String, Object>` with all order details:
        - `orderId`, `nombrePizza`, `detallesPedido`, `precioPedido`, `estado` ("Recibido").
        - `customerName`, `userEmail`, `telefono`.
        - `direccion`, `referencia`, `tipoUbicacion` ("GPS" or "Manual").
        - `fechaCreacion` (Firebase `ServerValue.TIMESTAMP`), `fechaFormatted` (human-readable date).
- **Location Handling (`onLocationChanged`, `getAddressFromLocation`):**
    - `onLocationChanged`: Called when location is found. Removes location updates and calls `getAddressFromLocation`.
    - `getAddressFromLocation`: Uses `Geocoder` to convert latitude/longitude to a street address. Sets `editDireccion` text, shows "✅ Ubicación obtenida", sets `isLocationObtained = true`, validates fields.
- **Permission Result (`onRequestPermissionsResult`):** If location permission granted, calls `getCurrentLocation()`. If denied, shows Toast and switches to manual input mode.
- **Exit Confirmation Dialog (`mostrarConfirmacionSalir`):** Asks user to confirm exiting if there are unsaved changes. If confirmed, navigates to `HomeActivity` clearing the task stack.
- **Lifecycle (`onDestroy`):** Removes location updates from `locationManager`.

**Input Extras Expected (from `ConfirmacionActivity`):**
- `"nombre_pizza"` (String)
- `"detalles_pedido"` (String)
- `"precio_pedido"` (double)
- `"user_email"` (String)

**Output Extras Sent (to `SeguimientoPedidoActivity`):**
- `"orderId"` (String)

**Dependencies:**
- Firebase Realtime Database: For storing order data.
- Android Location Services: For GPS functionality.
- `com.mammapasta.utils.PreferencesManager`: To save `orderId` and get customer name.
- `com.mammapasta.home.HomeActivity`: For back navigation.
- `SeguimientoPedidoActivity`: Next activity in the flow.

### 2. `SeguimientoPedidoActivity.java`

**Purpose:**
Displays the real-time status of the user's order. It listens to changes in the order status from Firebase and also simulates a progression of states if Firebase updates are not immediate. It includes a satisfaction survey upon delivery.

**UI Elements:**
- `txtEstado`: `TextView` to display the current order status (e.g., "Estado: Preparando").
- `iconoEstado`: `ImageView` to display an icon representing the current status.
- `progressBar`: `ProgressBar` (visibility managed, shown during active tracking).
- `txtMensaje`: `TextView` to show messages like "Esperando tu pedido..." or for the survey.

**Instance Variables:**
- `orderId`: `String` identifying the order to track.
- `handler`: `Handler` for simulating state changes.
- `estadoSimuladoRunnable`: `Runnable` for the simulation logic.
- `estadoIndex`: `int` to keep track of the current simulated state.
- `databaseReference`: `DatabaseReference` for Firebase.

**Functionality:**
- **Initialization (`onCreate`):**
    - Initializes UI views and Firebase `databaseReference`.
    - Retrieves `orderId` from Intent extras or `PreferencesManager`.
    - If `orderId` is missing, calls `mostrarMensajeEspera()` and returns.
    - Calls `escucharCambiosEstado()` to set up a Firebase listener for real-time status updates.
    - If it's the first time (or no real Firebase update yet), calls `iniciarSimulacionDeEstados()`.
    - Sets up an `OnBackPressedCallback`:
        - If order status (from `PreferencesManager`) is not "Completado", shows an alert dialog ("Aún no has completado tu pedido...").
        - If "Completado", allows back navigation (`finish()`).
- **Message Display (`mostrarMensajeEspera`, `ocultarMensajeEspera`):** Manages visibility of `txtMensaje` and `progressBar`.
- **Update UI State (`actualizarUIEstado`):**
    - Updates `txtEstado` text.
    - Changes `iconoEstado` resource based on the `estado` string ("Recibido", "Preparando", "En camino", "Entregado", "Completado").
    - If `estado` is "Entregado", calls `mostrarEncuestaAgradecimiento()`.
- **Simulate States (`iniciarSimulacionDeEstados`):**
    - Defines an array of `estados` and corresponding `tiemposEstados` (durations).
    - The `estadoSimuladoRunnable` iterates through these states:
        - Calls `actualizarUIEstado()` with the current simulated state.
        - Calls `updateOrderStatus()` to push this simulated state to Firebase (this might be overridden by actual backend updates).
        - If state is "Entregado", calls `mostrarEncuestaAgradecimiento()`.
        - Posts itself to the `handler` with the next state's delay.
    - Simulation starts only if `estadoIndex` is 0 (initial run).
- **Satisfaction Survey (`mostrarEncuestaAgradecimiento`):**
    - Shown when state becomes "Entregado".
    - Inflates a custom dialog layout (`R.layout.dialog_encuesta_satisfaccion`).
    - When "Enviar" button in dialog is clicked:
        - Calls `actualizarUIEstado("Completado")`.
        - Calls `updateOrderStatus("Completado")` to update Firebase.
        - Clears the cart using `CartManager.getInstance().clearCart()`.
        - Shows "Gracias por calificarnos" Toast.
        - Navigates to `HomeActivity` (clearing task stack) and finishes.
- **Listen for Firebase State Changes (`escucharCambiosEstado`):**
    - Attaches a `ValueEventListener` to `databaseReference.child("orders").child(orderId).child("estado")`.
    - `onDataChange`:
        - Retrieves `nuevoEstado` from Firebase.
        - If valid, calls `actualizarUIEstado(nuevoEstado)`.
        - Saves `nuevoEstado` to `PreferencesManager.setOrderStatus()`.
- **Update Order Status in Firebase (`updateOrderStatus`):**
    - Sets the `estado` child of the order in Firebase to `newStatus`.
    - Sets `fechaActualizacion` to current timestamp.
- **Error Display (`mostrarError`):** Shows an error dialog.
- **Lifecycle (`onDestroy`):** Removes callbacks from `handler`.

**Input Extras Expected:**
- `"orderId"` (String) - Can also be retrieved from `PreferencesManager`.

**Dependencies:**
- Firebase Realtime Database: For listening to order status updates.
- `com.mammapasta.utils.PreferencesManager`: To get `orderId` and get/set order status.
- `com.mammapasta.utils.CartManager`: To clear the cart after completion.
- `com.mammapasta.home.HomeActivity`: For navigation after completion or back press.

### 3. `Order.java`

**Purpose:**
A Plain Old Java Object (POJO) class representing the structure of an order as stored in Firebase Realtime Database. Marked with `@IgnoreExtraProperties` for Firebase.

**Fields:**
- `orderId` (String)
- `customerName` (String)
- `deliveryAddress` (String)
- `items` (List<String>) - *Note: `DeliveryActivity` sends `detallesPedido` (String) and `nombrePizza` (String) instead of a list of items. This POJO might be a bit out of sync or intended for a different structure.*
- `totalPrice` (double)
- `status` (String) - *Note: `DeliveryActivity` uses `estado` in its Firebase map, while this POJO has `status`.*
- `timestamp` (long)
- `customerEmail` (String)
- `phoneNumber` (String)

**Constructors:**
- Default empty constructor (required by Firebase).
- Parameterized constructors.

**Getters and Setters:**
- Standard getters and setters for all fields.

**Note on `Order.java` vs. Firebase Data:**
There are some discrepancies between the fields in `Order.java` (e.g., `items`, `status`) and the actual data map created in `DeliveryActivity` (`detallesPedido`, `nombrePizza`, `estado`). This suggests that `Order.java` might not be directly used for deserializing the full order object from Firebase in the current flow, or it's a general model that isn't perfectly aligned with the `DeliveryActivity`'s Firebase write structure. `SeguimientoPedidoActivity` specifically listens only to the `estado` child, not the whole order object.

## Interactions and Flow

1.  **Order Confirmed:** `ConfirmacionActivity` navigates to `DeliveryActivity`, passing basic order details.
2.  **Collect Delivery Info (`DeliveryActivity`):**
    *   User chooses GPS or manual address input.
    *   User enters phone number and optional reference.
    *   Fields are validated.
3.  **Submit to Firebase (`DeliveryActivity`):**
    *   User clicks "Confirmar Entrega".
    *   A comprehensive order object (as a Map) is created, including a unique `orderId`, delivery details, customer info, and initial status "Recibido".
    *   This map is saved to Firebase Realtime Database under `/orders/{orderId}`.
    *   `orderId` is saved to `PreferencesManager`.
4.  **Navigate to Tracking (`DeliveryActivity` -> `SeguimientoPedidoActivity`):** `DeliveryActivity` starts `SeguimientoPedidoActivity`, passing the `orderId`.
5.  **Track Order (`SeguimientoPedidoActivity`):**
    *   Retrieves `orderId`.
    *   Listens to `/orders/{orderId}/estado` in Firebase for real-time updates.
    *   Simultaneously, simulates a progression of states ("Recibido" -> "Preparando" -> "En camino" -> "Entregado" -> "Completado") with predefined delays. The simulated state is also pushed to Firebase. *This means either Firebase backend updates or this simulation will drive the displayed status.*
    *   UI (text and icon) is updated based on the current state.
6.  **Delivery and Survey (`SeguimientoPedidoActivity`):**
    *   When the state becomes "Entregado" (either from Firebase or simulation), a satisfaction survey dialog is shown.
    *   Upon survey submission:
        *   State is updated to "Completado" in UI and Firebase.
        *   User's cart is cleared.
        *   User is navigated back to `HomeActivity`.
7.  **Back Navigation (`SeguimientoPedidoActivity`):** Restricted until the order is "Completado".

This module bridges the gap between order placement and physical delivery, providing user feedback and leveraging Firebase for data persistence and real-time updates. The simulation aspect ensures users see progress even if backend updates are delayed.
