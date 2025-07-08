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
