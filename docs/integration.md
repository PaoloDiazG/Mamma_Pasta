# Mamma Pasta Application: Overview and Integration

This document provides a high-level overview of the Mamma Pasta application, detailing its overall architecture, user flow, and how different modules and components integrate with each other.

## 1. Application Architecture

The Mamma Pasta application is an Android native application built primarily in Java. It follows a standard Android architecture with Activities serving as screens and utilizing components like Services, Broadcast Receivers (implicitly via `ClosedHoursWatcher`), and local SQLite database for persistence. Firebase is used for backend services, specifically:

- **Firebase Authentication:** For user login and registration.
- **Firebase Realtime Database:**
    - To store and validate promotional codes (`/codigos_promocionales`).
    - To store and track delivery order details and status (`/orders`).

**Key Architectural Characteristics:**
- **Modular Design:** The application is divided into feature-specific packages (e.g., `login`, `home`, `builder`, `order`, `dispatch`, `chat`, `historial`, `qrreader`).
- **Centralized Utilities:** Common functionalities like SharedPreferences management (`PreferencesManager`), cart logic (`CartManager`), and database operations (`DBHelper`) are encapsulated in utility classes.
- **Activity-Based Navigation:** Navigation between different screens is primarily handled through Android Intents.
- **Data Persistence:**
    - **SQLite Database (`DBHelper`):** Stores predefined pizza and topping data, and user order history.
    - **SharedPreferences (`PreferencesManager`, `CartManager`):** Stores user session information, login attempt states, last order ID/status, and the current shopping cart contents.
- **Real-time Features:** Leverages Firebase Realtime Database for promotional code validation and order status tracking.

## 2. Core Application Flow

The typical user flow through the application can be summarized as follows:

1.  **App Launch (`MainActivity`):**
    *   Checks if the app is within closed hours (`TimeUtils`, `ClosedHoursWatcher`). If so, redirects to `ClosedHoursActivity`.
    *   Checks login status via `PreferencesManager`.
    *   If logged in, navigates to `HomeActivity`.
    *   If not logged in, navigates to `LoginActivity`.

2.  **Authentication (`login` module):**
    *   **Login (`LoginActivity`):** User enters email/password. Authenticates against Firebase Auth. Handles incorrect credentials, login attempt limits, and temporary blocking. On success, navigates to `HomeActivity`.
    *   **Registration (`RegisterActivity`):** User creates a new account. Data is validated, and a new user is created in Firebase Auth. On success, navigates to `HomeActivity`.
    *   Both activities also check for closed hours.

3.  **Main Application (`home` module - `HomeActivity`):**
    *   Displays a list of predefined pizzas (from `DBHelper`).
    *   Provides navigation to:
        *   Pizza Builder (`ConstruirPizzaActivity`).
        *   Order History (`HistorialActivity`).
        *   Support Chat (`ChatActivity`).
        *   Logout.
    *   **Shopping Cart:**
        *   Users can add pizzas to the cart (managed by `CartManager`).
        *   A slide-out panel shows cart contents, allows quantity updates, and item removal.
        *   Users can proceed to payment (`ConfirmacionActivity`) with cart items.

4.  **Pizza Customization (`builder` module - `ConstruirPizzaActivity`):**
    *   User selects crust, sauce, cheese, and toppings (toppings from `DBHelper`).
    *   Price is dynamically calculated.
    *   On confirmation, navigates to `ConfirmacionActivity` with custom pizza details.

5.  **Order Confirmation (`order` module - `ConfirmacionActivity`):**
    *   Receives order details (either from `HomeActivity` for cart orders or `ConstruirPizzaActivity` for custom pizzas).
    *   Displays order summary and total price.
    *   **Promotional Code (Optional via `qrreader` module - `QRActivity`):**
        *   User can navigate to `QRActivity` to scan or enter a promo code.
        *   `QRActivity` validates the code against Firebase (`/codigos_promocionales`).
        *   If valid, `QRActivity` returns to `ConfirmacionActivity` with discount details. `ConfirmacionActivity` recalculates the price.
    *   User confirms the purchase.
    *   Order is saved to local SQLite database (`DBHelper`).
    *   Navigates to `DeliveryActivity`.

6.  **Delivery Details & Submission (`dispatch` module - `DeliveryActivity`):**
    *   User provides delivery address (GPS or manual), phone number, and reference.
    *   Order details (including delivery info, customer info, and items) are compiled.
    *   A unique `orderId` is generated.
    *   The complete order is saved to Firebase Realtime Database under `/orders/{orderId}` with an initial status of "Recibido".
    *   `orderId` is saved in `PreferencesManager`.
    *   Navigates to `SeguimientoPedidoActivity`.

7.  **Order Tracking (`dispatch` module - `SeguimientoPedidoActivity`):**
    *   Displays the status of the order identified by `orderId`.
    *   Listens for real-time status updates from `/orders/{orderId}/estado` in Firebase.
    *   Also simulates a progression of states if Firebase updates are not immediate, pushing these simulated states to Firebase.
    *   When the state becomes "Entregado", a satisfaction survey dialog is shown.
    *   Upon survey submission, the state is set to "Completado", the cart is cleared (`CartManager`), and the user is navigated to `HomeActivity`.

## 3. Module Interactions and Dependencies

-   **`MainActivity` -> `LoginActivity` / `HomeActivity`**: Based on login state from `PreferencesManager`.
-   **`LoginActivity`, `RegisterActivity` -> `HomeActivity`**: On successful authentication. Uses `FirebaseAuth` and `PreferencesManager`.
-   **`HomeActivity` -> `ConstruirPizzaActivity`, `HistorialActivity`, `ChatActivity`, `ConfirmacionActivity`**: User navigation. Uses `DBHelper` for pizza list, `CartManager` for cart operations.
-   **`ConstruirPizzaActivity` -> `ConfirmacionActivity`**: Passes custom pizza details. Uses `DBHelper` for toppings.
-   **`ConfirmacionActivity` <-> `QRActivity`**: `ConfirmacionActivity` passes order details to `QRActivity`. `QRActivity` passes back original details + discount info (if any) to `ConfirmacionActivity`. `QRActivity` uses Firebase for code validation.
-   **`ConfirmacionActivity` -> `DeliveryActivity`**: Passes confirmed order details. Saves order to `DBHelper`.
-   **`DeliveryActivity` -> `SeguimientoPedidoActivity`**: Passes `orderId`. Saves full order to Firebase. Uses location services.
-   **`SeguimientoPedidoActivity`**: Listens to Firebase for status. Navigates to `HomeActivity` on completion. Uses `CartManager` to clear cart.
-   **`ClosedHoursWatcher` & `TimeUtils` -> `ClosedHoursActivity`**: Global mechanism affecting most activities by redirecting to `ClosedHoursActivity` if outside business hours.
-   **`DBHelper`**: Used by `HomeActivity` (pizzas), `ConstruirPizzaActivity` (toppings), `ConfirmacionActivity` (save order), `HistorialActivity` (get orders).
-   **`PreferencesManager`**: Used by `MainActivity`, `LoginActivity`, `HomeActivity`, `ConfirmacionActivity`, `DeliveryActivity`, `SeguimientoPedidoActivity`, `HistorialActivity` for various session and preference data.
-   **`CartManager`**: Used by `HomeActivity`, `PizzaAdapter` (implicitly), `CartAdapter` (implicitly), and `SeguimientoPedidoActivity` (to clear cart).
-   **Firebase Services**:
    - **Auth:** `LoginActivity`, `RegisterActivity`.
    - **Realtime DB:** `QRActivity` (promo codes), `DeliveryActivity` (save order), `SeguimientoPedidoActivity` (track status). `FirebaseDataLoader` for initial promo code setup.
-   **Models (`Pizza`, `CartItem`, `Topping`, `Message`)**: Used across relevant activities and adapters to structure data.

## 4. Key Cross-Cutting Concerns

-   **Business Hours:** Enforced globally by `ClosedHoursWatcher` and locally by initial checks in `LoginActivity` and `RegisterActivity`.
-   **User Session:** Managed by `PreferencesManager` and checked at app start and various points.
-   **Error Handling:** Primarily through Toasts for user feedback, especially for network operations or validation errors. Some dialogs are used for critical confirmations (logout, exit).
-   **Data Flow for Orders:**
    1.  Selection/Customization (`HomeActivity`/`ConstruirPizzaActivity`)
    2.  Cart (`CartManager` - optional for custom pizza)
    3.  Review & Discount (`ConfirmacionActivity`, `QRActivity`)
    4.  Local Save (`DBHelper` in `ConfirmacionActivity`)
    5.  Delivery Details & Firebase Save (`DeliveryActivity`)
    6.  Firebase Tracking (`SeguimientoPedidoActivity`)

This overview provides a map of how the Mamma Pasta application is structured and how its various parts work together to deliver the user experience.
---

# README.md (Main Project Readme)

This will be the main `README.md` file at the root of the project, providing an entry point to all other documentation.

```markdown
# Mamma Pasta - Android Application Documentation

Welcome to the Mamma Pasta Android application documentation. This document serves as the central hub for understanding the application's architecture, features, and code structure.

## Table of Contents

1.  **[Application Overview and Integration](./docs/integration.md)**
    *   High-level architecture, overall user flow, and how different modules connect.

2.  **[Core Components](./docs/core_components.md)**
    *   Documentation for `MainActivity`, `MammapastaApp`, `PreferencesManager`, and `DBHelper`.

3.  **Feature Modules:**
    *   **[Login and Registration](./docs/feature_login.md)** (`com.mammapasta.login`)
        *   Handles user authentication (login and new account registration) using Firebase.
    *   **[Home Screen & Shopping Cart](./docs/feature_home.md)** (`com.mammapasta.home`)
        *   Main screen displaying pizzas, managing the shopping cart, and navigation.
    *   **[Pizza Builder](./docs/feature_pizza_builder.md)** (`com.mammapasta.builder`)
        *   Allows users to customize their pizzas.
    *   **[Order Confirmation & Payment](./docs/feature_order.md)** (`com.mammapasta.order`)
        *   Order review, promotional code application, and purchase confirmation.
    *   **[QR Code Reader (Promotional Codes)](./docs/feature_qr_reader.md)** (`com.mammapasta.qrreader`)
        *   Scanning and manual entry of promotional codes.
    *   **[Order Dispatch and Tracking](./docs/feature_dispatch.md)** (`com.mammapasta.dispatch`)
        *   Collecting delivery details, submitting orders to Firebase, and real-time status tracking.
    *   **[Chat Support](./docs/feature_chat.md)** (`com.mammapasta.chat`)
        *   Simple rule-based chat for support and navigation.
    *   **[Order History](./docs/feature_history.md)** (`com.mammapasta.historial`)
        *   Displays the user's past orders from local database.

4.  **[Utility Classes and Mechanisms](./docs/utils.md)**
    *   Details on `CartManager`, `FirebaseDataLoader`, and the business hours enforcement system (`TimeUtils`, `ClosedHoursWatcher`, `ClosedHoursActivity`).

5.  **[Data Models](./docs/models.md)** (`com.mammapasta.models`)
    *   Information on primary data models like `Pizza`, `CartItem`, `Message`, and `Topping`.

## Project Structure

(A brief overview of the main package structure can be added here if desired, though much of it is covered by the module documentation.)

-   `com.mammapasta` - Root package
    -   `MainActivity.java` - Application entry point.
    -   `MammapastaApp.java` - Custom Application class.
    -   `builder/` - Pizza customization feature.
    -   `chat/` - Chat support feature.
    -   `closed/` - Closed hours display and logic.
    -   `db/` - Database helper.
    -   `detail/` - (Potentially for pizza detail view, though not explicitly documented in this pass).
    -   `dispatch/` - Order delivery and tracking.
    -   `guardian/` - Business hours watcher.
    -   `historial/` - Order history feature.
    -   `home/` - Main home screen and cart.
    -   `login/` - User authentication.
    -   `models/` - Data model classes.
    -   `order/` - Order confirmation.
    -   `qrreader/` - QR code scanning for promotions.
    -   `splash/` - (Likely a splash screen, e.g., `SplashActivity.java`).
    -   `utils/` - Utility classes like `PreferencesManager`, `CartManager`.

## How to Use This Documentation

-   Start with the **[Application Overview and Integration](./docs/integration.md)** for a general understanding.
-   Dive into specific **Feature Modules** or **Core Components** based on your area of interest.
-   Refer to **Utility Classes** and **Data Models** for details on shared functionalities and data structures.

```

This `README.md` will be created at the project root. The other detailed documents are already placed in the `docs/` directory.
