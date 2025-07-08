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
    - Calls `addBotMessage()` to display an initial welcome message from the bot: "🍝 ¡Bienvenido al soporte de Mamá Pasta! ¿En qué te puedo ayudar hoy?".
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
        - `"historial"`, `"pedidos anteriores"`: Bot message "📜 Abriendo tu historial de pedidos..." and navigates to `HistorialActivity`.
        - `"pizza"`, `"construir"`, `"pedido nuevo"`: Bot message "🛠️ ¡Vamos a construir tu pizza perfecta!" and navigates to `ConstruirPizzaActivity`.
        - `"inicio"`, `"menú"`, `"volver"`: Bot message "🏠 Regresando a la pantalla principal..." and navigates to `HomeActivity`.
        - `"hola"`, `"buenas"`, `"saludos"`: Bot message "👋 ¡Hola! Puedes pedirme hacer un pedido, construir una pizza o revisar tu historial.".
        - `"gracias"`, `"thank you"`: Bot message "🍕 ¡Gracias a ti! Mamá Pasta siempre está para ayudarte.".
        - `"adiós"`, `"salir"`, `"chau"`: Bot message "👋 ¡Hasta luego! No olvides que aquí estaremos cuando necesites tu dosis de pasta.".
        - Default (no keywords matched): Bot message "🤔 Lo siento, no entendí eso. Puedes decirme: 'historial', 'construir pizza', 'ver menú' o 'volver al inicio'.".

**Dependencies:**
- `ChatAdapter`: To display messages.
- `com.mammapasta.models.Message`: Data model for a chat message. (Assumed to exist, defines `isUser()` and `getText()`).
- Activities for navigation: `HistorialActivity`, `ConstruirPizzaActivity`, `HomeActivity`.
- `com.mammapasta.R`: For layout and UI element IDs.

### 2. `ChatAdapter.java`

**Purpose:**
A `RecyclerView.Adapter` to display chat messages in the `recyclerView`. It handles different appearances for user messages versus bot messages by toggling visibility of two TextViews within the item layout.

**Functionality:**
- **Constructor:** Takes a `List<Message>`.
- **`onCreateViewHolder`:** Inflates `R.layout.item_message` for each message.
- **`onBindViewHolder`:**
    - Gets the `Message` object for the current position.
    - Sets visibility of `holder.userText` to `View.VISIBLE` and `holder.botText` to `View.GONE` if `message.isUser()` is true.
    - Sets visibility of `holder.userText` to `View.GONE` and `holder.botText` to `View.VISIBLE` if `message.isUser()` is false.
    - If `message.isUser()`, sets text to `holder.userText`.
    - Else (bot message), sets text to `holder.botText`.
- **`getItemCount`:** Returns the size of the `messages` list.
- **`ChatViewHolder` (static inner class):**
    - Holds `TextView userText` (from `R.id.textUser`) and `TextView botText` (from `R.id.textBot`).

**Layout (`item_message.xml` - Inferred from adapter):**
- Must contain two `TextViews`:
    - One with `android:id="@+id/textUser"` for user messages.
    - One with `android:id="@+id/textBot"` for bot messages.
- These TextViews are likely styled differently (e.g., alignment, background color) to visually distinguish user messages from bot messages.

**Dependencies:**
- `com.mammapasta.models.Message`: Data model.
- `com.mammapasta.R`: For layout inflation (`R.layout.item_message`) and view IDs (`R.id.textUser`, `R.id.textBot`).

## Interactions and Flow

1.  **Navigation:** User navigates to `ChatActivity` (e.g., from a "Soporte" button in `HomeActivity`).
2.  **Welcome:** An initial welcome message from the bot is displayed.
3.  **User Input:** User types a message in `edtMessage` and clicks `btnSend`.
4.  **Message Display:**
    *   The user's message is added to `messageList` and displayed by `ChatAdapter` (using the `textUser` TextView in `item_message.xml`).
5.  **Bot Response:**
    *   `simulateBotResponse` processes the user's input for keywords.
    *   A corresponding bot message is generated and added to `messageList`, then displayed by `ChatAdapter` (using the `textBot` TextView).
    *   If the user's query matches a navigation command (e.g., "historial"), the bot first sends a response message and then starts the corresponding Activity.

This module provides a basic, offline, rule-based support system. It does not connect to any external chat service or use advanced Natural Language Processing (NLP). The interaction is driven by simple keyword matching.
---

# Feature: Order History

**Module Package:** `com.mammapasta.historial`

## Purpose

This module allows logged-in users to view a list of their past orders. The order data is fetched from the local SQLite database managed by `DBHelper`.

## Key Components

### 1. `HistorialActivity.java`

**Purpose:**
Displays a list of past orders for the currently logged-in user.

**UI Elements:**
- `recyclerViewHistorial`: `RecyclerView` to display the list of orders.
- `txtEmpty`: `TextView` shown if there is no order history (e.g., "No hay pedidos aún...").
- `btnVolver`: `TextView` (styled as a button, with text "Volver") to navigate back to the previous screen.

**Functionality:**
- **Initialization (`onCreate`):**
    - Sets the layout `R.layout.activity_historial`.
    - Initializes UI elements: `recyclerView`, `txtEmpty`, `btnVolver`.
    - Sets a `LinearLayoutManager` for `recyclerViewHistorial`.
    - Sets an `OnClickListener` for `btnVolver` to call `finish()`, which closes the current activity and returns to the previous one.
    - **Fetch and Display Orders:**
        - Initializes `PreferencesManager` to get the current `userEmail`.
        - Initializes `DBHelper`.
        - Calls `dbHelper.getPedidosByEmail(email)` to retrieve a `List<String>` of order details. Each string in this list is a pre-formatted summary of an order as constructed by `DBHelper`.
        - **If `pedidos` list is empty:**
            - Sets `txtEmpty.setVisibility(View.VISIBLE)`.
            - Sets `recyclerView.setVisibility(View.GONE)`.
        - **Else (orders exist):**
            - Sets `txtEmpty.setVisibility(View.GONE)`.
            - Sets `recyclerView.setVisibility(View.VISIBLE)`.
            - Creates an instance of `HistorialAdapter` with the `pedidos` list.
            - Sets this adapter to `recyclerViewHistorial`.

**Dependencies:**
- `com.mammapasta.db.DBHelper`: To fetch order history from the local SQLite database.
- `com.mammapasta.utils.PreferencesManager`: To get the email of the logged-in user, which is used as the key to fetch their orders.
- `HistorialAdapter`: The RecyclerView adapter responsible for binding order data to list items.
- `com.mammapasta.R`: For layout (`R.layout.activity_historial`) and UI element IDs.

### 2. `HistorialAdapter.java`

**Purpose:**
A `RecyclerView.Adapter` to display individual order history items in the `recyclerViewHistorial`. Each item is represented as a formatted string.

**Functionality:**
- **Constructor:** Takes a `List<String>` (`pedidos`) which are the formatted order details.
- **`onCreateViewHolder`:** Inflates the layout `R.layout.item_pedido` for each order string.
- **`onBindViewHolder`:**
    - Gets the `pedido` string (the formatted order details) for the current `position`.
    - Sets `holder.txtPedidoTitulo` to "Pedido #[position + 1]" to give a sequential number to each order.
    - Sets `holder.txtPedidoDetalles` to the `pedido` string. It includes a `replace("$", "S/")` call, although `DBHelper.getPedidosByEmail` already formats price with "S/.". This might be a leftover or a safeguard.
- **`getItemCount`:** Returns the total number of orders in the `pedidos` list (or 0 if the list is null).
- **`updatePedidos(List<String> nuevosPedidos)`:** A public method to replace the current list of orders with a new one and then call `notifyDataSetChanged()` to refresh the RecyclerView. This method is not used by `HistorialActivity` in its current implementation but could be useful for dynamic updates.
- **`ViewHolder` (static inner class):**
    - Holds `TextView txtPedidoTitulo` (from `R.id.txtPedidoTitulo`) and `TextView txtPedidoDetalles` (from `R.id.txtPedidoDetalles`).

**Layout (`item_pedido.xml` - Inferred from adapter):**
- Must contain at least two `TextViews`:
    - One with `android:id="@+id/txtPedidoTitulo"` for the order title/number (e.g., "Pedido #1").
    - One with `android:id="@+id/txtPedidoDetalles"` for displaying the formatted string of order details.

**Dependencies:**
- `com.mammapasta.R`: For layout inflation (`R.layout.item_pedido`) and view IDs.

## Interactions and Flow

1.  **Navigation:** User navigates to `HistorialActivity` (e.g., from `HomeActivity` by clicking `btnHistorial`, or via a command in `ChatActivity`).
2.  **Fetch Data:**
    *   `HistorialActivity` retrieves the logged-in user's email from `PreferencesManager`.
    *   It then queries `DBHelper.getPedidosByEmail(email)`. This method in `DBHelper` fetches records from the `table_pedidos` in the SQLite database that match the user's email and formats each record into a human-readable string containing order name, details, and price.
3.  **Display Orders:**
    *   If `DBHelper` returns an empty list, `HistorialActivity` shows the `txtEmpty` message.
    *   If orders are found, they are passed to `HistorialAdapter`. The adapter then populates the `recyclerViewHistorial`, where each item displays a title like "Pedido #X" and the corresponding formatted details string from the list.
4.  **Go Back:** The user clicks the "Volver" button (`btnVolver`), and `HistorialActivity.finish()` is called, returning the user to the screen from which they navigated.

This module provides a straightforward way for users to review their past transactions. All data is sourced locally from the SQLite database, specific to the logged-in user.
