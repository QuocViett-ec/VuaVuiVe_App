# Vựa Vui Vẻ – Online Grocery E-commerce Platform Build Summary

## 1. Project Overview

* **Project Name**: Vựa Vui Vẻ (VuaVuiVe)
* **Project Type**: Full-stack e-commerce application (comprising a Spring Boot REST API backend and native Android mobile clients)
* **Business Domain**: Online grocery, fresh food, organic agriculture, and clean produce (VietGAP standards)
* **Main Purpose of the System**: Streamline and automate the online grocery shopping experience—from customer ordering and voucher usage to payment gateways, automated stock management, delivery assignment, and shipper status tracking.
* **Target Users**:
  * **Customers**: Shoppers who browse products/recipes, build a cart, apply vouchers, and make purchases.
  * **Shippers (Drivers)**: Delivery staff who accept orders, navigate to customer locations, and manage delivery statuses.
  * **Administrators (Back-office staff)**: Store owners and auditors who manage products, categories, vouchers, orders, shipments, user profiles, and view audit reports.
* **Short Description of the Project**: Vựa Vui Vẻ is a multi-module e-commerce system featuring a Spring Boot REST API server, a PostgreSQL database, a shared library module, and three native Android applications (Customer, Shipper, and Admin) integrated with external services like VNPay, MoMo, Cloudinary, and Google Gemini AI.
* **Why this System was Built**: The system was developed to provide an end-to-end, real-time platform for clean retail food distribution, handling complex transactional workflows such as safe stock check-outs, role-based order processing, payment sandboxes, and virtual AI assistance.

---

## 2. My Role and Project Context

* **My Role**: Full-stack Developer / Web & Mobile Developer / Project Contributor (inferred from codebase scope)
* **Timeline**: *To be manually added if not found*
* **Team Size**: *To be manually added if not found* (Codebase notes indicate a single contributor working on recent milestones, with an original reference to a 4-person parallel task matrix)
* **Course/Project Context**: *To be manually added if not found*
* **Main Contribution Areas**:
  * Developing and refactoring REST API endpoints for user authentication, product management, order processing, and payment callbacks.
  * Restructuring mobile flows, including separating the Shipper functionality from the main Customer app into a standalone native `app-shipper` Android application.
  * Fixing critical data synchronization issues, such as standardizing the order status transition logic and order-filtering mappings (`parseOrderStatus()`).
  * Building UI components, layouts, data binding adapters, and search behaviors in native Android (Java/XML).
  * Integrating SQLite Room database schemas in the `shared` module for offline caching of products and shopping carts.

---

## 3. Technology Stack

### Frontend (Mobile Client Applications)

* **Framework/Library**: Native Android SDK (Customer, Shipper, and Admin modules).
* **Programming Language**: Java.
* **Styling Tools**: Material Components for Android (XML layouts, styles, themes).
* **UI Libraries**: RecyclerView, ViewPager2, SwipeRefreshLayout, Glide (for image loading).
* **State Management**: Android Architecture Components (LiveData, ViewModel).
* **Routing**: Intent-based navigation, parent-activity navigation, ViewPager adapters.
* **API Communication Method**: Retrofit 2, OkHttp 3, Gson Converter.
* **Dependency Injection**: Dagger Hilt (for `app-customer` and `app-shipper`).

### Backend (REST API Server)

* **Runtime**: Java Development Kit (JDK) 21.
* **Framework**: Spring Boot 3.3.0.
* **REST API Structure**: Spring MVC Controller-Service-Repository pattern.
* **Authentication/Session/JWT Mechanism**: Spring Security with custom JWT Filter (`JwtAuthFilter`) extracting tokens from HTTP Headers or cookies (`vvv.customer.sid`, `vvv.admin.sid`, `vvv.shipper.sid`).
* **Middleware**: Custom security filters, request interceptors, and validation handlers.
* **Error Handling**: Centralized exception handler (`AppException`) throwing structured JSON responses (`ApiResponse`).
* **Security Tools**: Spring Security (Role-Based Access Control), BCrypt password hashing.

### Database

* **Database Type**: PostgreSQL (version 15+ used in development; migrated from an initial SQLite file `vuavuive_v2.db` via Python script).
* **ORM/ODM**: Spring Data JPA / Hibernate (DDL Auto-update mode).
* **Local Mobile Database**: SQLite Room Database (configured inside the `shared` module: `AppDatabase` with `ProductDao`, `CartDao`, and related entities).
* **Data Validation**: Hibernate Validator (`jakarta.validation.constraints` annotations like `@Valid`, `@NotBlank`, `@Email`).

### External Integrations

* **VNPay**: Integration with VNPay sandbox for online checkout, including payment URL generation and IPN/Return verification callbacks.
* **MoMo**: Integration with MoMo sandbox (`captureWallet` request type) for payment gateway redirects and IPN handlers.
* **Google Gemini AI**: Integration with Google Gemini (`gemini-1.5-flash` model) API for virtual chat assistance (`GeminiService.java`).
* **Cloudinary**: HTTP-based Cloudinary storage client (`cloudinary-http44`) for product image management.
* **Telegram Bot**: Telegram notification integration (`telegram.bot-token` and `chat-id`).
* **Static File Uploads**: Fallback local file system upload handling (`/api/uploads/images`) with static resources mapping.

### Development Tools

* **IDE**: Android Studio, VS Code.
* **Version Control**: Git / GitHub.
* **Package Managers**: Maven (backend), Gradle Kotlin DSL (`.gradle.kts` for Android client modules).
* **API Testing**: OpenAPI / Swagger UI (`http://localhost:3000/swagger-ui.html`), Postman.
* **Database Tools**: pgAdmin, SQLite Database Browser.

---

## 4. Project Architecture

The system follows a distributed client-server architecture with three native mobile applications interacting with a centralized Spring Boot backend server.

### Overall Architecture Diagram
```
  [app-customer]        [app-shipper]        [app-admin]
  (Hilt, Room, RF)     (Hilt, Room, RF)    (Mock Data / RF)
         |                    |                   |
         v                    v                   v
+---------------------------------------------------------+
|                  REST API (Spring Boot)                 |
|             Port 3000 / Swagger Documentation           |
+---------------------------------------------------------+
         |                    |                   |
         v                    v                   v
+------------------+  +-----------------+  +--------------+
|   PostgreSQL     |  | Google Gemini / |  | VNPay / MoMo |
|    Database      |  | Cloudinary API  |  | Sandboxes    |
+------------------+  +-----------------+  +--------------+
```

### Key Architectural Layers:
1. **Presentation Layer (Mobile Clients)**: Built as modular Android projects. `app-customer` uses live Retrofit APIs and Hilt DI, `app-admin` uses a mixture of live product APIs and local `MockRepository` datasets, and `app-shipper` connects to dedicated shipper APIs.
2. **Shared Library Module (`shared`)**: Built as an Android library containing models (DTOs), Retrofit interface definitions (`AuthApi`, `ProductApi`, `OrderApi`, etc.), local Room SQLite schemas, and general helpers (`SessionManager`).
3. **API Routing & Security Layer (Spring Boot)**: Receives HTTP requests, validates JWT payloads, and enforces Role-Based Access Control (RBAC) rules.
4. **Business Logic Layer (Spring Boot Services)**: Orchestrates transactional processes such as ordering, updating stock quantities, and computing voucher discounts.
5. **Persistence Layer (PostgreSQL)**: Stores system records using relational database schemas mapped via Spring Data JPA.

---

## 5. Folder Structure

```
VuaVuiVe_App/
├── settings.gradle.kts          # Multi-project gradle configuration listing shared & client apps
├── shared/                      # Android library module containing shared assets and business code
│   └── src/main/java/vn/vuavuive/shared/
│       ├── data/
│       │   ├── api/            # Retrofit REST API client interfaces (ProductApi, OrderApi, etc.)
│       │   ├── dto/            # Data Transfer Objects (Product, Order, User, Voucher DTOs)
│       │   └── local/          # Room local database schemas (AppDatabase, CartDao, ProductDao)
│       └── util/               # Shared utilities (SessionManager)
├── app-customer/                # Native Android application for customer shopping
│   └── src/main/
│       ├── java/vn/vuavuive/customer/   # UI view controllers (Activities/Fragments) and ViewModels
│       └── res/layout/                  # Customer UI layouts (activity_main, fragment_home, item_cart, etc.)
├── app-shipper/                 # Standalone native Android application for delivery agents
│   └── src/main/
│       ├── java/vn/vuavuive/shipper/    # Shipper UI screens (ShipperMainActivity, ShipperOrderDetailActivity)
│       └── res/layout/                  # Shipper layouts (activity_shipper_main, item_shipper_order)
├── app-admin/                   # Native Android application for store management and auditing
│   └── src/main/
│       ├── java/vn/vuavuive/admin/      # Admin screens, ViewModels, and MockRepository data provider
│       └── res/layout/                  # Admin UI components (fragment_dashboard, activity_product_edit)
├── app-backend/                 # Spring Boot application source folder
│   ├── src/main/java/vn/vuavuive/backend/
│   │   ├── config/              # SecurityConfig, StaticResourceConfig, WebConfig definitions
│   │   ├── core/                # Core classes (ApiResponse wrapper)
│   │   ├── exception/           # Exception definitions (AppException)
│   │   └── modules/             # Spring Boot domain modules (auth, product, order, payment, ai)
│   ├── src/main/resources/
│   │   ├── application.yml      # Base configurations
│   │   └── application-dev.yml  # Dev profile containing DB URL, JWT secrets, and API sandbox credentials
│   ├── migrate_to_postgres.py   # Python utility script migrating data from SQLite to PostgreSQL
│   └── run_backend.bat          # Startup script for Windows environments
└── payment/                     # Node.js and external integration sandboxes for research
```

---

## 6. User Roles and Main Modules

### 1. Anonymous Visitor
* **Permissions**: Public read-only access.
* **Actions**: Browse the product catalog, read recipes, use search filters.
* **Access Control**: No token required.

### 2. Customer
* **Permissions**: Authenticated shopping access.
* **Screens**: Home, Category Shortcuts, Search, Product Details, Shopping Cart, Checkout, Order History, Virtual Chatbot.
* **Actions**: Add items to cart, select delivery addresses, input vouchers, check out via COD/VNPay/MoMo, cancel pending orders, write product reviews.
* **Access Control**: Requires a valid JWT token mapped to role `CUSTOMER`.

### 3. Shipper
* **Permissions**: Delivery management access.
* **Screens**: Shipper Main, Active Delivery List, Delivery History, Shipper Order Details.
* **Actions**: Toggle delivery online/offline status, initiate delivery (`SHIPPING` -> `IN_TRANSIT`), mark delivery as success/fail, trigger quick phone dialer, load Google Maps route.
* **Access Control**: Requires a valid JWT token mapped to role `SHIPPER`.

### 4. Admin
* **Permissions**: System control access.
* **Screens**: Dashboard, Order Listing, Product Manager, Voucher Manager, Member Directory, Audit Log, AI Analytics.
* **Actions**: Add/update/delete products, configure category spinners, upload images, manage voucher rules, modify order status, assign shippers, inspect security logs, export CSV logs.
* **Access Control**: Restricted to user profiles with `admin` scope.

### 5. Staff
* **Permissions**: Operations management access.
* **Actions**: Update orders, modify product catalog details, manage voucher lists. Restricted from viewing security audits or updating back-office user roles.
* **Access Control**: Restricted to user profiles with `staff` scope.

### 6. Audit
* **Permissions**: Read-only business review access.
* **Actions**: View logs, view metrics. Restricted from altering records, processing orders, or exporting sensitive transaction CSV files.
* **Access Control**: Restricted to user profiles with `audit` scope.

---

## 7. Main Features Built

### Customer Core Features
* **Authentication**: Login and registration with fields validator. Support for SessionManager token storage. Google OAuth login (`GoogleLoginRequest`) integration hooks.
* **Home & Product Catalog**: Debounced search (400ms), category filter shortcuts, grid layout, and a promo popup that tracks impressions in SharedPreferences.
* **Recipe Catalog**: Category filtering for recipes and a one-click "Buy Ingredients" feature that adds all recipe components to the cart.
* **Local Cart Caching**: Offline cart capability implemented using Room database (`CartDao`, `CartItemEntity`) that syncs with backend APIs on successful internet connections.
* **Checkout Flow**: Real-time total calculation based on product pricing, fixed/percentage voucher discounts, shipping fees, and client-side validation of recipient details.

### Shipper Delivery Features
* **Delivery Status Transition**: Safe status flows from `SHIPPING` -> `IN_TRANSIT` -> `DELIVERED` / `FAILED` with validation rules to prevent illegal status transitions.
* **In-app Utilities**: One-tap phone call launcher (`Intent.ACTION_DIAL`) and map navigation shortcut (`geo:` intents loading Google Maps).

### Admin Back-Office Features
* **Interactive Dashboard**: Metric cards tracking total orders, monthly revenues, new users, and listings highlighting low-stock items.
* **Product Manager**: CRUD form featuring dropdown category selectors loading real categories from `GET /api/categories` and Multipart image uploaders (`POST /api/uploads/images`).
* **Voucher Rules Engine**: Controls validating min-order thresholds, maximum usage caps, active periods, and coupon expiration states.
* **Audit & Reports**: Security log auditing viewer tracking operator actions and target details, and a CSV exporter saving transaction data into standard spreadsheet tables.
* **AI Consultant Bot**: A chatbot interface designed to analyze store statuses and answer operational questions.

---

## 8. Database and Data Model

The application stores persistent data in a relational PostgreSQL database. Schemas are managed automatically via Hibernate mappings (`ddl-auto: update`).

### Database Models (Spring Boot Entities)
1. **User (`users` table)**:
   * Fields: `id` (UUID), `email`, `password_hash`, `full_name`, `phone`, `role` (CUSTOMER, SHIPPER, ADMIN, STAFF, AUDIT), `is_active`, `provider` (LOCAL, GOOGLE), `created_at`.
2. **Product (`products` table)**:
   * Fields: `id` (UUID), `name`, `slug`, `price` (original), `selling_price` (discounted), `category_id`, `description`, `image_url`, `stock_quantity`, `unit`, `is_active`, `rating`, `review_count`, `sold_count`.
3. **Category (`categories` table)**:
   * Fields: `id` (UUID), `name`, `slug`, `image_url`, `is_active`.
4. **Order (`orders` table)**:
   * Fields: `id` (UUID), `user_id` (FK), `shipper_id` (FK), `payment_method` (COD, VNPAY, MOMO), `payment_status` (UNPAID, PENDING, PAID, REFUNDED), `status` (PENDING, CONFIRMED, SHIPPING, PREPARING, READY_FOR_PICKUP, IN_TRANSIT, DELIVERED, FAILED, RETURNED, CANCELLED), `total_amount`, `final_amount`, `delivery_address`, `delivery_name`, `delivery_phone`, `note`, `created_at`.
5. **OrderItem (`order_items` table)**:
   * Fields: `id` (UUID), `order_id` (FK), `product_id` (FK), `quantity`, `unit_price`, `subtotal`.
6. **OrderStatusLog (`order_status_logs` table)**:
   * Fields: `id` (UUID), `order_id` (FK), `status`, `note`, `updated_by_role`, `updated_by_name`, `created_at`.
7. **Shipper (`shippers` table)**:
   * Fields: `id` (UUID), `user_id` (FK, @OneToOne link), `full_name`, `phone`, `vehicle_number`, `current_status` (AVAILABLE, DELIVERING), `is_active`.
8. **Recipe (`recipes` table)**:
   * Fields: `id` (UUID), `title`, `description`, `image_url`, `prep_time_minutes`, `cook_time_minutes`, `servings`, `instructions`.

---

## 9. API / Backend Implementation

### Auth Module
* `POST /api/auth/register` - Registers a new user. Support for standard JSON fields mapping (`fullName` alias parser).
* `POST /api/auth/login` - Authenticates credentials; returns access/refresh JWT tokens and places secure cookies.
* `POST /api/auth/shipper/login` - Authenticates shippers specifically; returns tokens and sets the `vvv.shipper.sid` cookie.
* `POST /api/auth/refresh` - Issues a new access token via a valid refresh token.
* `POST /api/auth/logout` - Clears active tokens and deletes HTTP cookies.

### Product & Category Modules
* `GET /api/products` - Returns a paged list of active products with category/search filters.
* `GET /api/products/{id}` - Returns specific product details.
* `GET /api/products/{id}/reviews` - Fetches customer reviews for the product.
* `GET /api/categories` - Returns category listings.

### Order Module
* `POST /api/orders` - Creates a new order. Handles transactional inventory reductions and returns payment URLs for gateway checkout.
* `GET /api/orders/my` - Returns order history for the authenticated customer.
* `GET /api/orders/shipper` - Fetches orders assigned to the current shipper.
* `PATCH /api/orders/{id}/cancel` - Cancels pending orders and restores inventory.
* `PATCH /api/orders/{id}/status` - Admin status updater.
* `PATCH /api/orders/{id}/paid` - Marks an order as paid.

### Payment Module
* `POST /api/payments/momo` - Generates a MoMo sandbox payment link.
* `GET /api/payments/vnpay/ipn` - VNPay IPN webhook handler. Validates signatures and updates order statuses.
* `GET /api/payments/vnpay/return` - VNPay redirect handler.
* `POST /api/payments/momo/ipn` - MoMo IPN webhook handler.

### AI & Chatbot Modules
* `POST /api/chat` - Submits chatbot inquiries to the Google Gemini model.

---

## 10. Frontend / UI Implementation

The Android applications are built with native components that bind structured JSON payloads directly to Material UI widgets.

* **Structured Data Binding**: Views subscribe to `LiveData` observables emitted by ViewModels. Items such as pricing formats, product images (loaded via Glide), and status badges are updated dynamically.
* **Offline Fallback Caching**: The repository layer handles connectivity drops by intercepting API errors and serving stored entities from Room local SQLite databases (`ProductDao`).
* **Conditional UI Rendering**:
  * Shopping cart screens render an empty state panel (layout with shopping prompts) if the cart size is zero.
  * Add-to-Cart buttons are disabled, and out-of-stock labels are rendered when a product's stock count reaches 0.
* **Role-Based Views**: The Admin dashboard toggles shortcuts based on active privileges (e.g., hiding member management controls or disabling status spinners for read-only Audit profiles).

---

## 11. Payment and Checkout Flow

```
[Customer Cart] ---> Click Checkout ---> Verify Recipient Details
                                                    |
                                                    v
Choose payment: COD <------------------------ [Payment Gateway Selection]
   |                                          |                  |
   v                                          v (VNPay)          v (MoMo)
Save Order (UNPAID)                    Generate Url       Generate Url
   |                                          |                  |
   v                                          v                  v
Clear Local Cart                       Redirect to Web View (Sandbox Page)
   |                                          |                  |
   v                                          v                  v
Open Order History                     Verify Payment Details (OTP PIN)
                                              |
                                              v
                                       Gateway IPN Webhook Called
                                              |
                                              v
                                       [Validation Success?]
                                       /                   \
                                    (Yes)                  (No)
                                     /                       \
                      Update Order: CONFIRMED           Update Order: CANCELLED
                      Update Payment: PAID              Restore Stock Inventory
```

---

## 12. Recommendation / ML Module

* **Recommendation Architecture**: The platform defines REST API contracts (`RecommendApi` Retrofit client and `RecommendController` backend endpoints) for collecting user events and outputting relevant items.
* **Backend Skeleton Status**: The backend `RecommendController.java` contains stub endpoints (e.g., returning empty arrays `List.of()`), indicating that the ML recommendation engine or the Flask ML service is designed as an architectural shell rather than fully implemented in this version.
* **Frontend Event Logging**: The Android application is prepared to dispatch event logging requests (`sendRecommendEvent()`) on actions like viewing a product or adding items to the cart.

---

## 13. Testing, Debugging, and Validation

* **Automated Integration Tests**: Spring Boot tests are executed using standard commands (`mvn test`), verifying auth lifecycle flows and REST endpoints.
* **API Validation**: Validated via Swagger UI and Postman collections, confirming proper HTTP 403 Forbidden checks on protected endpoints and valid JSON shapes.
* **Manual Verification (Checklists)**:
  * **Auth**: Testing role routing (e.g., ensuring shipper credentials route to `ShipperMainActivity` and customers to `MainActivity`).
  * **Cart**: Verifying that the minus button clamps quantity values at 1 and does not delete cart rows unless an explicit swipe-to-delete is triggered.
  * **Stock Check**: Verifying that out-of-stock products cannot be added to the cart on either the product listing or detail views.
* **Sandbox Verification**: Simulated transactions performed via VNPay and MoMo test card configurations.

---

## 14. Build and Run Process

### Prerequisites
* **Java Development Kit**: JDK 21.
* **PostgreSQL**: Version 15+.
* **Python**: Version 3.8+ (for data seed migration).
* **Android SDK**: Android Studio with SDK build tools.

### 1. Database Configuration
1. Initialize the PostgreSQL server on port `5432`.
2. Create a database named `vuavuive_app`.
3. Set database credentials in `app-backend/src/main/resources/application-dev.yml` (e.g., username `postgres`, password `your_password`).

### 2. Migration and Seeding
Run the Python migration script to copy structured categories, users, and product catalogs:
```bash
cd app-backend
python migrate_to_postgres.py
```

### 3. Running the Backend
Start the Spring Boot REST API server on local port `3000`:
* **Windows**:
  ```powershell
  cd app-backend
  .\run_backend.bat
  ```
* **macOS / Linux**:
  ```bash
  cd app-backend
  chmod +x mvn
  ./mvn spring-boot:run -Dspring-boot.run.profiles=dev
  ```

### 4. Running the Mobile Clients
1. Open the project root `VuaVuiVe_App` directory in Android Studio.
2. Allow Gradle sync to download modules (`shared`, `app-customer`, `app-shipper`, `app-admin`).
3. Build the applications:
   ```bash
   ./gradlew :app-customer:assembleDebug
   ./gradlew :app-shipper:assembleDebug
   ./gradlew :app-admin:assembleDebug
   ```
4. Run the desired mobile module on an Android emulator or hardware device.

---

## 15. My Possible Contributions

* **Backend API Development**: Implemented and secured REST API endpoints for user authentication, product catalogs, shopping carts, and order status updates using Spring Security and JWT.
* **Database Migration & Schemas**: Configured PostgreSQL database mappings using Spring Data JPA/Hibernate, and wrote migration scripts transferring legacy SQLite data.
* **Native Mobile Engineering**: Refactored the monolithic shopper interface into a standalone native `app-shipper` Android application utilizing Dagger Hilt DI, Retrofit networking, and Room local database caches.
* **Validation & Security Controls**: Created transactional checkout validations blocking purchases when stock counts are insufficient and designed Role-Based Access Controls for back-office operators.
* **Debugging & Optimization**: Resolved critical user issues such as incorrect order status query filters, UI focus freezes, and quantity clamping bugs.

---

## 16. Knowledge and Skills Applied

### Technical Skills
* **Android Development**: Native Java, Android Architecture Components (LiveData, ViewModel), Room Persistence Library, Retrofit 2, Dagger Hilt.
* **Backend Development**: Spring Boot 3, Spring Security, JWT, Spring Data JPA, Hibernate.
* **Relational Database Management**: PostgreSQL, SQL query optimization, database seeding scripts.
* **API Integration**: REST API design, JSON serialization (Gson, Jackson), VNPay & MoMo Sandboxes, Google Gemini AI.
* **Development Workflows**: Git, Maven build tools, Gradle build automation.

### E-commerce & Business Logic
* **Stock Controls**: Transaction-isolated inventory checks preventing oversell scenarios.
* **Pricing & Discounts**: Computation structures for voucher validations, percentage reductions, and shipping rates.
* **Workflow Transitions**: Implementing order state validation matrices (e.g., preventing orders in `IN_TRANSIT` from jumping back to `PENDING`).

### System Analysis
* **Modularization**: Decoupling shipper views from customer activities.
* **Performance Caching**: Offline-first patterns utilizing local Room caching for catalog browsing.

---

## 17. Relevance to Template Developer JD

This project directly aligns with the competencies required for a Template Developer position:
* **Structured Data Mapping**: Extensive practice mapping database schemas (PostgreSQL) to middle-tier DTOs and rendering them on mobile client layouts.
* **JSON API Binding**: Building Retrofit adapters that serialize client actions into JSON requests and bind backend response payloads to UI components.
* **Conditional Layout Rendering**: Experience designing templates that adjust dynamically based on contextual parameters (e.g., hiding buy buttons for out-of-stock items, toggle switches based on active roles).
* **Testing & UI Debugging**: Fixing mobile form constraints, list adapter refreshes, and back-button focus handlers.

---

## 18. CV-Ready Version

**Vựa Vui Vẻ – Online Grocery E-commerce Platform** | Full-stack Developer / Web-Mobile Contributor
* **Timeline**: *To be manually added*
* **Team Size**: *To be manually added*
* **Technologies**: Spring Boot 3, Java 21, Spring Security (JWT), PostgreSQL, Android SDK, SQLite Room, Retrofit, Dagger Hilt, Gemini AI API, VNPay & MoMo Sandbox.
* Developed and secured Spring Boot REST APIs using JWT filters and Spring Security to manage shopping carts, user sessions, and order lifecycles.
* Built a standalone, Hilt-injected native Android application for Shippers, implementing transitions for delivery states.
* Configured local Room SQLite database schemas inside a shared library module to implement offline-first caching for product catalogs.
* Integrated VNPay and MoMo payment sandboxes with automated signature checking and IPN webhooks for secure, digital checkout processing.
* Programmed transaction-safe checkouts using Spring Data JPA, locking inventory records during checkout to prevent oversell.
* Connected Google Gemini AI models using prompt engineering templates to provide in-app automated customer advice.

---

## 19. Portfolio-Ready Version

### Project Overview
Vựa Vui Vẻ is a full-stack, Clean Agriculture E-commerce platform comprising native Android client applications and a Java Spring Boot REST backend. The system enables users to buy organic produce and recipes, handles digital checkouts via payment sandboxes, and coordinates delivery assignments for shippers.

### Problem
Managing grocery deliveries requires tight data synchronization between customer checkout, active inventory counts, and delivery agents. A lack of strict transaction rules can lead to oversell issues, while network disconnects interrupt customer catalog browsing.

### Solution
* Developed transactional backend services isolating stock reductions to prevent race conditions during peak checkout periods.
* Built an offline-friendly local cache in the Android client using Room SQLite, allowing users to browse products during network drops.
* Decoupled delivery operations by introducing a dedicated shipper mobile application that uses quick-dial intents, Google Maps navigation, and strict delivery status state controls.

### Key Learnings
* **Transactional Reliability**: Leveraging Spring Boot `@Transactional` properties to roll back database states when API pipelines fail.
* **Modular Clean Code**: Managing shared assets, interfaces, and utilities via a local library module (`shared`) shared across three distinct native applications.
* **API Callback Integrations**: Configuring and testing secure IPN callback routes using local tunnel proxies (Ngrok).

---

## 20. Missing Information

The following items are not fully defined in the source code repository and should be manually added:
* **Exact Project Timeline**: (e.g., Start Date - End Date).
* **Team Size & Composition**: (e.g., Solo project vs. details of the 4-person parallel group plan).
* **Personal Contribution Percentage**: Custom weightings for specific modules.
* **Staging/Deployment URL**: Web links for the API or Swagger UI.
* **Application Screenshots & Demo Recording Paths**: Visual mockups for portfolios.
* **GitHub Repository URL**: Remote repository paths.
