# Feature: QR Code Reader (Promotional Codes)

**Module Package:** `com.mammapasta.qrreader`

## Purpose

This module allows users to scan QR codes or manually enter a promotional code to apply discounts to their order. It verifies the code against a Firebase Realtime Database and, if valid and active, returns the discount information to `ConfirmacionActivity`.

## Key Components

### 1. `QRActivity.java`

**Purpose:**
Provides an interface for scanning QR codes or entering codes manually. It then validates these codes with Firebase.

**UI Elements:**
- `edtCodigoPromocional`: `EditText` for manual entry of a promotional code.
- `btnEscanearQR`: `Button` to launch the QR code scanner.
- `btnVerificarCodigo`: `Button` to verify the manually entered or scanned code.
- `btnVolverSinCodigo`: `Button` to return to `ConfirmacionActivity` without applying any code.
- `txtInstrucciones`: `TextView` for instructions (content not specified but likely guides the user).

**Instance Variables:**
- `codigoEscaneado`: `String` to store the content from a scanned QR code.
- `databaseReference`: `DatabaseReference` pointing to `/codigos_promocionales` in Firebase Realtime Database.
- `barcodeLauncher`: `ActivityResultLauncher<ScanOptions>` for handling the result from the QR scanner.

**Functionality:**
- **Initialization (`onCreate`):**
    - Sets the layout `R.layout.activity_qr`.
    - Initializes Firebase `databaseReference`.
    - Initializes UI elements.
    - **QR Scanner Launcher:**
        - `barcodeLauncher` is registered using `ScanContract`.
        - On result:
            - If scan cancelled, shows "Escaneo cancelado" Toast.
            - If successful, sets `codigoEscaneado` to the result, updates `edtCodigoPromocional` with the scanned code, and shows a Toast with the scanned code.
    - **Button Click Listeners:**
        - `btnEscanearQR`:
            - Creates `ScanOptions` (sets desired format to QR_CODE, prompt, camera ID, beep, etc.).
            - Launches the scanner using `barcodeLauncher.launch(options)`.
        - `btnVerificarCodigo`:
            - Gets `codigoIngresado` from `edtCodigoPromocional` (trimmed and uppercased).
            - If empty, shows "Por favor ingresa un código promocional" Toast.
            - Disables the button and sets text to "🔍 Verificando...".
            - Calls `verificarCodigoEnFirebase(codigoIngresado)`.
        - `btnVolverSinCodigo`:
            - Creates an `Intent` for `ConfirmacionActivity`.
            - Retrieves the original `extras` passed to `QRActivity` (these contain the order details) and puts them back into the intent for `ConfirmacionActivity`.
            - Starts `ConfirmacionActivity` and `finish()`es `QRActivity`.
- **Verify Code in Firebase (`verificarCodigoEnFirebase`):**
    - Attaches a `addListenerForSingleValueEvent` to `databaseReference.child(codigo)`.
    - **`onDataChange`:**
        - Re-enables `btnVerificarCodigo` and resets its text.
        - If `dataSnapshot.exists()`:
            - The code exists in Firebase. Retrieves the code data as `Map<String, Object>`.
            - Checks `activo` (Boolean) field:
                - If `activo` is true:
                    - Retrieves `descuento` (Long, converted to double) and `descripcion` (String).
                    - Shows "🎉 ¡Código válido! [descripcion]" Toast.
                    - Calls `volverConDescuento(codigo, descuento)`.
                - Else (code not active): Shows "❌ Este código promocional ya no está disponible" Toast, clears `edtCodigoPromocional`.
            - Else (code data malformed): (Implicitly does nothing further if `codigoData` is null).
        - Else (code does not exist): Shows "❌ Código promocional inválido..." Toast, clears `edtCodigoPromocional`.
    - **`onCancelled` (DatabaseError):**
        - Re-enables `btnVerificarCodigo`.
        - Shows "❌ Error de conexión..." Toast.
- **Return with Discount (`volverConDescuento`):**
    - Creates an `Intent` for `ConfirmacionActivity`.
    - Retrieves the original `extras` (containing order details) passed to `QRActivity` and adds them to the new intent.
    - Adds new extras:
        - `"codigo_promocional"` (String): The validated code.
        - `"descuento"` (double): The discount percentage.
    - Starts `ConfirmacionActivity` and `finish()`es `QRActivity`.

**Input Extras Expected (from `ConfirmacionActivity`):**
- A `Bundle` containing all details of the current order (e.g., pizza details, price). This bundle is passed back to `ConfirmacionActivity`.

**Output Extras Sent (back to `ConfirmacionActivity` if code is valid):**
- The original `Bundle` of order details.
- `"codigo_promocional"` (String)
- `"descuento"` (double) - Percentage of discount.

**Firebase Structure Expected (`/codigos_promocionales/{CODE_ID}`):**
```json
{
  "PROMO10": {
    "activo": true,
    "descuento": 10, // Percentage
    "descripcion": "10% de descuento en tu orden"
  },
  "PIZZAFREE": {
    "activo": false,
    "descuento": 100,
    "descripcion": "Pizza gratis (expirado)"
  }
}
```

## Interactions and Flow

1.  **Navigation:** User is in `ConfirmacionActivity` and clicks `btnCodigoPromocional`. `ConfirmacionActivity` starts `QRActivity`, passing along all current order details in the intent extras. `ConfirmacionActivity` then finishes itself.
2.  **Code Input:**
    *   **Scan QR:** User clicks `btnEscanearQR`, scans a code. The scanned code populates `edtCodigoPromocional`.
    *   **Manual Entry:** User types a code into `edtCodigoPromocional`.
3.  **Verification:**
    *   User clicks `btnVerificarCodigo`.
    *   `QRActivity` queries Firebase at `/codigos_promocionales/{entered_code}`.
4.  **Result:**
    *   **Valid & Active Code:** Firebase returns data indicating the code is active and provides a discount percentage. `QRActivity` shows a success message and then starts `ConfirmacionActivity` again. It passes back the original order extras *plus* the new `codigo_promocional` and `descuento` extras. `QRActivity` finishes.
    *   **Invalid/Inactive Code:** An appropriate error Toast is shown. The user can try again or use `btnVolverSinCodigo`.
    *   **Error:** A connection error Toast is shown.
5.  **Return Without Code:**
    *   User clicks `btnVolverSinCodigo`.
    *   `QRActivity` starts `ConfirmacionActivity` again, passing back only the original order extras (no discount information). `QRActivity` finishes.

This module effectively integrates an external system (QR codes potentially distributed physically or digitally) with the app's ordering flow via Firebase.
---

# Feature: Chat Support

**Module Package:** `com.mammapasta.chat`

## Purpose

This module provides a simple, rule-based chat interface for users to get help or navigate to common app features. It simulates responses from a bot based on keywords in the user's input.

## Key Components

### 1. `ChatActivity.java`

**Purpose:**
Manages the chat UI, handles user input, and generates simulated bot responses.

**UI Elements:**
- `recyclerView`: `RecyclerView` to display the chat messages.
- `edtMessage`: `EditText` for the user to type their message.
- `btnSend`: `Button` to send the message.

**Instance Variables:**
- `adapter`: `ChatAdapter` for the `recyclerView`.
- `messageList`: `List<Message>` holding all chat messages.

**Functionality:**
- **Initialization (`onCreate`):**
    - Sets the layout `R.layout.activity_chat`.
    - Initializes UI elements.
    - Creates `ChatAdapter` with `messageList`.
    - Sets `LinearLayoutManager` and the adapter to `recyclerView`.
    - Calls `addBotMessage()` to display an initial welcome message from the bot.
    - Sets `OnClickListener` for `btnSend`:
        - Gets `userText` from `edtMessage`.
        - If not empty:
            - Calls `addUserMessage(userText)` to display the user's message.
            - Clears `edtMessage`.
            - Calls `simulateBotResponse(userText)` to generate and display a bot reply.
- **Add User Message (`addUserMessage`):**
    - Creates a new `Message` object with `isUser = true`.
    - Adds it to `messageList`.
    - Notifies the adapter (`notifyItemInserted`) and scrolls `recyclerView` to the bottom.
- **Add Bot Message (`addBotMessage`):**
    - Creates a new `Message` object with `isUser = false`.
    - Adds it to `messageList`.
    - Notifies the adapter and scrolls `recyclerView` to the bottom.
- **Simulate Bot Response (`simulateBotResponse`):**
    - Converts `userInput` to lowercase.
    - Uses a series of `if-else if` statements to check for keywords:
        - "historial", "pedidos anteriores": Bot message "📜 Abriendo tu historial de pedidos..." and navigates to `HistorialActivity`.
        - "pizza", "construir", "pedido nuevo": Bot message "🛠️ ¡Vamos a construir tu pizza perfecta!" and navigates to `ConstruirPizzaActivity`.
        - "inicio", "menú", "volver": Bot message "🏠 Regresando a la pantalla principal..." and navigates to `HomeActivity`.
        - "hola", "buenas", "saludos": Bot message "👋 ¡Hola! Puedes pedirme..."
        - "gracias", "thank you": Bot message "🍕 ¡Gracias a ti!..."
        - "adiós", "salir", "chau": Bot message "👋 ¡Hasta luego!..."
        - Default (no keywords matched): Bot message "🤔 Lo siento, no entendí eso..."

**Dependencies:**
- `ChatAdapter`: To display messages.
- `com.mammapasta.models.Message`: Data model for a chat message.
- Activities for navigation: `HistorialActivity`, `ConstruirPizzaActivity`, `HomeActivity`.

### 2. `ChatAdapter.java`

**Purpose:**
A `RecyclerView.Adapter` to display chat messages in the `recyclerView`. It handles different layouts or appearances for user messages versus bot messages.

**Functionality:**
- **Constructor:** Takes a `List<Message>`.
- **`onCreateViewHolder`:** Inflates `R.layout.item_message` for each message.
- **`onBindViewHolder`:**
    - Gets the `Message` object for the current position.
    - Sets visibility of `holder.userText` and `holder.botText` based on `message.isUser()`.
    - If `message.isUser()`, sets text to `holder.userText`.
    - Else, sets text to `holder.botText`.
- **`getItemCount`:** Returns the size of the `messages` list.
- **`ChatViewHolder` (static inner class):**
    - Holds `TextView userText` (from `R.id.textUser`) and `TextView botText` (from `R.id.textBot`).

**Layout (`item_message.xml` - Inferred):**
- Likely contains two `TextViews`, one for user messages and one for bot messages. These might be styled differently (e.g., alignment, background color). The adapter controls which one is visible and populated for each message.

**Dependencies:**
- `com.mammapasta.models.Message`: Data model.
- `com.mammapasta.R`: For layout inflation and view IDs.

## Interactions and Flow

1.  **Navigation:** User navigates to `ChatActivity` (e.g., from a "Support" button in `HomeActivity`).
2.  **Welcome:** An initial welcome message from the bot is displayed.
3.  **User Input:** User types a message in `edtMessage` and clicks `btnSend`.
4.  **Message Display:**
    *   The user's message is added to `messageList` and displayed by `ChatAdapter` (styled as a user message).
5.  **Bot Response:**
    *   `simulateBotResponse` processes the user's input for keywords.
    *   A corresponding bot message is generated and added to `messageList`, then displayed by `ChatAdapter` (styled as a bot message).
    *   If the user's query matches a navigation command (e.g., "historial"), the bot first sends a message and then starts the corresponding Activity.

This module provides a basic, offline, rule-based support system. It does not connect to any external chat service or use advanced NLP.
---

# Feature: Order History

**Module Package:** `com.mammapasta.historial`

## Purpose

This module allows logged-in users to view a list of their past orders. The order data is fetched from the local SQLite database.

## Key Components

### 1. `HistorialActivity.java`

**Purpose:**
Displays a list of past orders for the currently logged-in user.

**UI Elements:**
- `recyclerViewHistorial`: `RecyclerView` to display the list of orders.
- `txtEmpty`: `TextView` shown if there is no order history, saying something like "No hay pedidos en tu historial."
- `btnVolver`: `TextView` (styled as a button) to navigate back to the previous screen.

**Functionality:**
- **Initialization (`onCreate`):**
    - Sets the layout `R.layout.activity_historial`.
    - Initializes UI elements.
    - Sets `LinearLayoutManager` for `recyclerViewHistorial`.
    - Sets `OnClickListener` for `btnVolver` to call `finish()`, effectively going back.
    - **Fetch and Display Orders:**
        - Initializes `PreferencesManager` to get the current `userEmail`.
        - Initializes `DBHelper`.
        - Calls `dbHelper.getPedidosByEmail(email)` to retrieve a `List<String>` of order details. Each string in this list is a pre-formatted summary of an order.
        - **If `pedidos` list is empty:**
            - Sets `txtEmpty.setVisibility(View.VISIBLE)`.
            - Sets `recyclerView.setVisibility(View.GONE)`.
        - **Else (orders exist):**
            - Sets `txtEmpty.setVisibility(View.GONE)`.
            - Sets `recyclerView.setVisibility(View.VISIBLE)`.
            - Creates `HistorialAdapter` with the `pedidos` list.
            - Sets the adapter to `recyclerViewHistorial`.

**Dependencies:**
- `com.mammapasta.db.DBHelper`: To fetch order history from the local database.
- `com.mammapasta.utils.PreferencesManager`: To get the email of the logged-in user.
- `HistorialAdapter`: To display the orders in the RecyclerView.
- `com.mammapasta.R`: For layout and UI element IDs.

### 2. `HistorialAdapter.java`

**Purpose:**
A `RecyclerView.Adapter` to display individual order history items (which are strings) in the `recyclerViewHistorial`.

**Functionality:**
- **Constructor:** Takes a `List<String>` representing the formatted order details.
- **`onCreateViewHolder`:** Inflates `R.layout.item_pedido` for each order string.
- **`onBindViewHolder`:**
    - Gets the `pedido` string for the current position.
    - Sets `holder.txtPedidoTitulo` to "Pedido #[position + 1]".
    - Sets `holder.txtPedidoDetalles` to the `pedido` string (replaces "$" with "S/" for currency symbol consistency, though `DBHelper` already formats with "S/.").
- **`getItemCount`:** Returns the size of the `pedidos` list.
- **`updatePedidos(List<String> nuevosPedidos)`:** A method to update the list of orders and refresh the adapter (though not explicitly used by `HistorialActivity` in its initial load).
- **`ViewHolder` (static inner class):**
    - Holds `txtPedidoTitulo` (from `R.id.txtPedidoTitulo`) and `txtPedidoDetalles` (from `R.id.txtPedidoDetalles`).

**Layout (`item_pedido.xml` - Inferred):**
- Contains at least two `TextViews`:
    - One for the order title/number (e.g., "Pedido #1").
    - One for the detailed string of the order.

**Dependencies:**
- `com.mammapasta.R`: For layout inflation and view IDs.

## Interactions and Flow

1.  **Navigation:** User navigates to `HistorialActivity` (e.g., from `HomeActivity` or via `ChatActivity`).
2.  **Fetch Data:**
    *   `HistorialActivity` retrieves the logged-in user's email from `PreferencesManager`.
    *   It then queries `DBHelper` for all orders associated with that email. `DBHelper.getPedidosByEmail()` returns a list of pre-formatted strings, each representing an order's details.
3.  **Display Orders:**
    *   If no orders are found, a "no history" message (`txtEmpty`) is shown.
    *   If orders exist, they are passed to `HistorialAdapter`, which populates the `recyclerViewHistorial`. Each item in the RecyclerView shows a title like "Pedido #X" and the formatted details of that order.
4.  **Go Back:** User clicks `btnVolver`, and `HistorialActivity` finishes, returning to the previous screen.

This module provides a straightforward way for users to review their past transactions, relying on the local database for persistence.
