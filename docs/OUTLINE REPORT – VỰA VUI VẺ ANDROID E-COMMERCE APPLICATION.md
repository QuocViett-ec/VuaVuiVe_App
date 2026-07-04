# OUTLINE REPORT - VUA VUI VE ANDROID E-COMMERCE APPLICATION

> This file is an English report outline. Each section contains key points only, so the team can expand them into full academic paragraphs.

---

## ACKNOWLEDGEMENT

- Thank the lecturer/supervisor for guidance and feedback.
- Thank team members for contributing to Android apps, backend, Firebase/database setup, testing, and documentation.
- Thank official documentation sources: Android Developers, Spring Boot, Firebase, MoMo, ZaloPay, and Material Design.
- Mention support from classmates or testers who helped test the apps on Android Emulator or physical devices.

---

## COMMITMENT

- The project and report were completed honestly by the team.
- Source code, screenshots, diagrams, and references are used responsibly.
- Testing results, limitations, and unfinished parts are reported truthfully.
- The team takes responsibility for the submitted report and source code.

---

## ABSTRACT

- Project name: Vua Vui Ve Android E-Commerce Application.
- Project domain: online grocery shopping / mobile commerce.
- Main purpose:
  - Help customers browse grocery products, manage cart, place orders, pay, and track delivery.
  - Help admins manage products, orders, vouchers, users, and shipper assignment.
  - Help shippers view assigned orders and update delivery progress.
- System components:
  - Customer Android app.
  - Admin Android app.
  - Shipper Android app.
  - Shared Android module.
  - Spring Boot backend.
  - Firebase Realtime Database and Firebase Authentication.
- Important features:
  - Product browsing and product detail.
  - Cart and checkout.
  - COD, MoMo sandbox, and ZaloPay sandbox.
  - Admin order management and shipper assignment.
  - Shipper delivery status updates.
  - Review, recipe, chatbot, and statistics support.

---

## ARCHITECTURE CLARIFICATION FOR REPORT

The codebase uses a hybrid architecture. Do not describe the system as if every Android app always accesses Firebase only through Spring Boot.

| Component | Actual Role in the Codebase |
| --- | --- |
| Customer app | Mainly uses Retrofit API interfaces to call backend services. |
| Admin app | Uses many Firebase-based API implementations directly for products, orders, users, vouchers, dashboard, and audit data. |
| Shipper app | Firebase-based flow using Firebase Authentication and Realtime Database. |
| Spring Boot backend | Provides REST APIs, business logic, payment callbacks, JWT/role authorization, and Firebase Admin SDK integration. |
| Firebase RTDB | Main cloud database storing JSON nodes for users, products, orders, carts, reviews, recipes, vouchers, etc. |
| Firebase Auth | Used especially by the Shipper app for login/session. |

Safe wording for the report:

- "The system follows a hybrid backend-cloud architecture."
- "Spring Boot acts as the REST API and business logic layer."
- "Firebase Realtime Database is the primary cloud database."
- "Some mobile modules also use Firebase SDK directly for realtime workflows."
- "Spring Boot connects to Firebase through Firebase Admin SDK."

Avoid these incorrect claims:

- "All Android apps always access Firebase only through Spring Boot."
- "Firebase connects itself to Spring Boot."
- "The project uses PostgreSQL" unless PostgreSQL is actually added later.
- "There is a dedicated backend voucher module" unless that module is added later.

**Figure 0.1: Overall Hybrid Architecture Diagram**

- Customer app -> Spring Boot REST API -> Firebase RTDB.
- Admin app -> Firebase RTDB directly for current Firebase APIs; Spring Boot for server-side APIs/chat/payment-related workflows.
- Shipper app -> Firebase Auth + Firebase RTDB directly.
- Payment gateways -> Spring Boot callbacks/IPN -> order/payment status updates.

Suggested paragraph points:

- Start by explaining why a hybrid architecture was chosen: REST APIs are useful for business logic and payment callbacks, while Firebase is useful for realtime synchronization.
- Mention that the architecture reflects the current implementation state of the codebase, not a purely theoretical design.
- Explain that Firebase RTDB is suitable for demo and realtime order status updates, but future production versions may centralize more writes through backend APIs for stronger validation and security.

---

## HOW TO USE THIS OUTLINE

- Use the tables as report structure.
- Use bullet points as paragraph ideas.
- Use each `Figure ...` note as a placeholder for screenshots, BPMN, DFD, ERD, or UI mockups.
- Do not copy code into the report unless the lecturer asks for implementation snippets.
- When expanding with ChatGPT, ask it to write in academic report style and keep claims aligned with this outline.

---

## LIST OF ABBREVIATIONS

| Abbreviation | Meaning |
| --- | --- |
| API | Application Programming Interface |
| APK | Android Package |
| AVD | Android Virtual Device |
| B2C | Business-to-Consumer |
| BPMN | Business Process Modeling Notation |
| COD | Cash on Delivery |
| CRUD | Create, Read, Update, Delete |
| DB | Database |
| DFD | Data Flow Diagram |
| ERD | Entity Relationship Diagram |
| IDE | Integrated Development Environment |
| IPN | Instant Payment Notification |
| JWT | JSON Web Token |
| RTDB | Firebase Realtime Database |
| UI | User Interface |
| UX | User Experience |
| XML | Extensible Markup Language |

---

# CHAPTER I. PROJECT ANALYSIS

## 1.1. Overview of the Project

- Vua Vui Ve is an Android grocery e-commerce application.
- The system supports three main operational roles: Customer, Admin, and Shipper.
- The business problem:
  - Customers need a convenient way to buy groceries online.
  - Store operators need tools to manage products, orders, payment status, and delivery.
  - Shippers need a clear delivery workflow.
- The project scope:
  - Three Android applications.
  - One shared Android module.
  - Spring Boot backend.
  - Firebase Realtime Database and Firebase Authentication.
- Business model: B2C online grocery shopping.

**Figure 1.1: Project Scope Overview**

- Show Customer, Admin, Shipper, Backend, Firebase, and Payment Gateway.

### 1.1.1. Reason for Choosing the Topic

- Online grocery shopping is increasingly common and convenient.
- Traditional grocery shopping requires time, travel, and manual checkout.
- The project is suitable for applying mobile application development knowledge.
- The topic covers a complete e-commerce workflow:
  - Product browsing.
  - Cart.
  - Checkout.
  - Payment.
  - Admin approval.
  - Delivery.
- The system also helps the team practice:
  - Android Java/XML.
  - REST API integration.
  - Firebase RTDB/Auth.
  - Role-based access.
  - Payment sandbox integration.

### 1.1.2. Users that the System Serves

| User Group | Description | Main Needs |
| --- | --- | --- |
| Guest | User who has not logged in | Browse/search products, view product detail |
| Customer | Grocery buyer | Cart, checkout, payment, order tracking, review |
| Admin | Store/system manager | Manage products, orders, users, vouchers, shipper assignment |
| Staff | Internal operator with limited admin permissions | Support product/order operations |
| Audit | Read-only observer | View data without editing |
| Shipper | Delivery staff | View assigned orders and update delivery status |

### 1.1.3. Competitor Research

| Criteria | Vua Vui Ve | BachHoaXanh | WinMart | GrabMart |
| --- | --- | --- | --- | --- |
| Product type | Grocery/local products | Grocery products | Supermarket products | Multi-store grocery delivery |
| Platform | Android project | Website/app | Website/app | Super app |
| Ordering flow | Focused academic flow | Full retail flow | Full retail flow | Platform-based flow |
| Delivery tracking | Status-based tracking | Supported | Supported | Supported |
| Admin operation | Custom admin app | Enterprise system | Enterprise system | Partner/platform system |
| Strength | Full role flow for local grocery model | Strong brand/product range | Retail chain ecosystem | Fast delivery ecosystem |
| Limitation | Limited scale and demo environment | High operation scale | Chain-dependent | Partner-dependent |

Key points to expand:

- Explain why Vua Vui Ve is suitable for a local grocery model.
- Explain that the project focuses on learning and implementing a complete workflow rather than competing at enterprise scale.

### 1.1.4. Project Goals

| Goal Type | Key Goals |
| --- | --- |
| Short-term | Build Customer/Admin/Shipper Android apps |
| Short-term | Implement browse -> cart -> checkout -> order -> delivery workflow |
| Short-term | Support COD, MoMo sandbox, and ZaloPay sandbox |
| Short-term | Implement admin order management and shipper assignment |
| Short-term | Implement shipper delivery updates |
| Long-term | Deploy backend to cloud |
| Long-term | Move payment from sandbox to production |
| Long-term | Add push notifications and realtime delivery map |
| Long-term | Improve chatbot and recommendation |
| Long-term | Add automated testing and security hardening |

### 1.1.5. Business Overview

| Item | Description |
| --- | --- |
| Brand name | Vua Vui Ve |
| Project type | Android B2C grocery e-commerce application |
| Target customers | Students, office workers, busy families, urban users |
| Main value | Convenient grocery ordering and delivery tracking |
| Store value | Product, order, payment, and delivery management |
| Vision | A practical local grocery ordering solution |
| Mission | Help customers buy grocery products more conveniently |

### 1.1.6. Product Scope

| Product Category | Notes |
| --- | --- |
| Vegetables | `veg` category/filter |
| Fruits | `fruit` category/filter |
| Meat | `meat` category/filter |
| Drinks | `drink` category/filter |
| Dry/packaged food | `dry` category/filter |
| Spices | `spice` category/filter |
| Household | `household` category/filter |
| Sweet/snack | `sweet` category/filter |
| Frozen | `frozen` category/filter |
| Other | fallback category |

Product fields to describe:

- Name.
- Price.
- Original price.
- Stock quantity.
- Unit.
- Category.
- Description.
- Image URL.
- Rating/review count/sold count when available.
- Active/inactive status.

### 1.1.7. App Requirements

#### Functional Requirements

| App/Role | Functional Requirements |
| --- | --- |
| Customer | Login/register, browse products, search/filter, product detail, cart, voucher, checkout, payment, order history, cancel/return/review, profile, recipes, chat |
| Admin | Login as admin/staff/audit, dashboard, product management, order management, mark paid, assign shipper, voucher management, user management, audit/read-only behavior, chatbot |
| Shipper | Firebase login, assigned orders, active/history tabs, call customer, navigation, start delivery, delivered/failed update, statistics, profile |

#### Non-functional Requirements

| Requirement | Key Points |
| --- | --- |
| Usability | Bottom navigation, tabs, readable cards, clear actions |
| Performance | Product/cart/order loading should be responsive for demo data |
| Security | Role-based access, session/token handling, Firebase rules |
| Reliability | Order status and payment status must remain consistent |
| Maintainability | Multi-module Gradle structure, shared DTO/API/util module |
| Scalability | Future migration to stronger indexing, push notification, cloud deployment |

### 1.1.8. Project Scope and Assumptions

| Item | Scope / Assumption |
| --- | --- |
| Platform | Android application only; no iOS app in current version |
| User roles | Customer, Admin, Staff, Audit, Shipper |
| Database | Firebase RTDB is the primary runtime database |
| Backend | Spring Boot handles REST APIs, payment callbacks, authorization, and selected business workflows |
| Payment | MoMo and ZaloPay are sandbox/mock-oriented for academic demo |
| Delivery | Delivery is status-based; realtime GPS map is future work |
| Deployment | Local/emulator testing is the main testing environment |
| Admin web | Not included; admin is implemented as Android app |
| Inventory | Product stock is tracked, but advanced restock automation is future work |

Key paragraph points:

- Clearly separate current implementation from future improvements.
- Explain that the project focuses on an end-to-end academic workflow rather than enterprise production deployment.
- Mention that sandbox payment and emulator testing are acceptable for the project scope.

### 1.1.9. Success Criteria

| Area | Success Criteria |
| --- | --- |
| Customer flow | User can browse products, add to cart, checkout, and view order status |
| Payment flow | COD works and MoMo/ZaloPay sandbox/mock flow can update payment status |
| Admin flow | Admin can manage products/orders and assign shipper |
| Shipper flow | Shipper can see assigned order and update delivery status |
| Role control | Wrong role cannot enter restricted app/function |
| Data consistency | Order status is reflected across Customer, Admin, and Shipper flows |
| Build/demo | Apps can build and install on Android Emulator |

### 1.1.10. Project Risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Emulator performance limitation | Apps may run slowly or crash if RAM is low | Test one app at a time or reduce emulator resources |
| Firebase direct writes | Validation may be split across app/backend | Document hybrid architecture and improve backend validation later |
| Payment sandbox dependency | Real bank/payment behavior is not fully tested | Use sandbox/mock evidence in report |
| Encoding issues in source text | Some UI strings may appear corrupted | Clean strings before final release |
| Client-side Firebase filtering | Performance issue with large datasets | Add indexed query structure or backend pagination later |

---

## 1.2. Theoretical Fundamentals

### 1.2.1. Android Studio

- Main IDE for Android development.
- Supports Java/XML coding, Gradle build, debugging, and emulator testing.
- Used to build and install:
  - `app-customer`.
  - `app-admin`.
  - `app-shipper`.

### 1.2.2. Java and XML

| Technology | Role in the Project |
| --- | --- |
| Java | Activity, Fragment, Adapter, ViewModel, Repository, API/Firebase logic |
| XML | Screen layouts, cards, buttons, RecyclerView, TabLayout, BottomNavigation |

### 1.2.3. Spring Boot Backend

- Provides REST APIs.
- Handles business logic for backend-supported modules.
- Connects to Firebase RTDB through Firebase Admin SDK.
- Important backend files:
  - `FirebaseConfig`.
  - `FirebaseRepositoryHelper`.
  - module controllers/services/repositories.
- Suitable for:
  - Validation.
  - JWT authentication.
  - Role authorization.
  - Payment callbacks/IPN.
  - Server-side order/payment workflow.

### 1.2.4. Firebase

| Firebase Service | Usage in Codebase |
| --- | --- |
| Firebase Realtime Database | Main runtime JSON data store |
| Firebase Authentication | Shipper login/session |
| Firebase Admin SDK | Backend connection to RTDB |
| Firebase Android SDK | Direct RTDB/Auth access in some app modules |

Firebase RTDB data examples:

- users.
- products.
- categories.
- carts.
- orders.
- reviews.
- recipes.
- vouchers.

### 1.2.5. Payment Gateway

| Payment Method | Flow |
| --- | --- |
| COD | Customer places order and pays when receiving goods |
| MoMo sandbox | Backend creates payment URL/deeplink, receives IPN/return/mock result |
| ZaloPay sandbox | Backend creates payment URL/deeplink, receives callback/return/mock result |

Important report points:

- Payment gateway callbacks should be handled by backend, not directly by Android apps.
- `PaymentResultActivity` opens the payment URL/deeplink and checks status.
- Debug mock success/fail endpoints support demo testing.

### 1.2.6. Git/GitHub

- Source code management.
- Version history.
- Team collaboration.
- Evidence for contribution table.

---

# CHAPTER II. SYSTEM ANALYSIS AND DESIGN

## 2.1. Actors and Use Cases

### 2.1.1. Customer Use Cases

| Use Case | Main Notes |
| --- | --- |
| Register/Login/Logout | Auth flow, session check |
| Browse products | Home, product list, categories |
| Search/filter | Product search and category filters |
| Product detail | Image, price, stock, description, reviews |
| Add to cart | Quantity validation and cart badge |
| Manage cart | Update quantity, remove, saved section |
| Checkout | Delivery info, voucher, payment method |
| Payment | COD/MoMo/ZaloPay |
| Order tracking | Tabs by status group |
| Cancel order | Only allowed for valid pending/confirmed states |
| Return request | Delivered orders only |
| Review product | Delivered orders with products |
| Recipe feature | Browse recipes and ingredients |
| Chat support | Customer chat screen |
| Account | Profile, password, my reviews, shipments |

### 2.1.2. Admin Use Cases

| Use Case | Main Notes |
| --- | --- |
| Login/Logout | Admin/Staff/Audit roles |
| Dashboard | Overview statistics |
| Manage products | Add/edit/delete/filter/low stock/export |
| Manage orders | Search/filter/detail/status/bulk update |
| Mark paid | For unpaid non-online orders |
| Assign shipper | Select active shipper for assignable order |
| Return review | Approve/reject return request |
| Manage vouchers | List/add/edit voucher records |
| Manage users | Search/filter/lock/change role |
| Audit mode | Read-only behavior |
| Admin chatbot | Operational assistant |

### 2.1.3. Shipper Use Cases

| Use Case | Main Notes |
| --- | --- |
| Login/Logout | Firebase Auth, role must be SHIPPER |
| Online/offline | Writes online status to user node |
| View assigned orders | Filters by current Firebase UID/shipperId |
| Search/filter orders | By order id, customer, phone, address |
| View detail | Customer info, products, payment instruction |
| Call customer | Opens dialer |
| Navigate | Opens Google Maps or geo fallback |
| Start delivery | Status changes to IN_TRANSIT |
| Delivery success | Status changes to DELIVERED |
| Delivery failure | Status changes to FAILED with reason |
| Statistics/profile | Revenue, success count, failed count, profile |

---

### 2.1.4. Role Permission Matrix

| Feature | Guest | Customer | Admin | Staff | Audit | Shipper |
| --- | --- | --- | --- | --- | --- | --- |
| View product list | Yes | Yes | Yes | Yes | Yes | No |
| Add to cart | No | Yes | No | No | No | No |
| Checkout | No | Yes | No | No | No | No |
| View own orders | No | Yes | No | No | No | No |
| Manage products | No | No | Yes | Limited | Read-only | No |
| Manage orders | No | No | Yes | Limited | Read-only | No |
| Assign shipper | No | No | Yes | Yes if allowed by flow | No | No |
| Manage users | No | No | Yes | No | Read-only/blocked actions | No |
| Update delivery status | No | No | No | No | No | Yes |
| View delivery statistics | No | No | No | No | No | Yes |

Report points:

- Explain that role separation reduces accidental access to admin/delivery operations.
- Mention that Audit is intentionally read-only.
- Mention that Shipper can only work with assigned orders.

---

## 2.2. BPMN Diagrams to Include

| Figure | Diagram Name | Key Flow |
| --- | --- | --- |
| Figure 2.1 | Login and Role Redirection BPMN | Login -> validate -> role check -> app main screen or error |
| Figure 2.2 | Product Browsing and Cart BPMN | Browse -> detail -> quantity -> add cart -> update badge |
| Figure 2.3 | Checkout and Payment BPMN | Cart -> delivery info -> voucher -> payment -> order status |
| Figure 2.4 | Admin Order Approval BPMN | Admin views order -> confirms/cancels -> mark paid if needed |
| Figure 2.5 | Admin Shipper Assignment BPMN | Confirmed order -> select active shipper -> status SHIPPING |
| Figure 2.6 | Shipper Delivery BPMN | Assigned order -> IN_TRANSIT -> DELIVERED/FAILED |
| Figure 2.7 | Product Management BPMN | Admin add/edit/delete -> validation -> Firebase/API update |

Key points for BPMN section:

- Keep diagrams role-specific.
- Show decision gateways for invalid login, empty cart, payment method, payment success/failure, and delivery success/failure.
- Show Firebase/Backend as service tasks where relevant.

---

## 2.3. Database Design

### 2.3.1. Database Overview

- Firebase RTDB is the main runtime data store.
- Data is stored as JSON nodes.
- Spring Boot uses Firebase Admin SDK to read/write RTDB.
- Some Android modules also access RTDB directly using Firebase SDK.
- Some fields exist in both snake_case and camelCase for compatibility.

**Figure 2.8: Firebase RTDB Node Structure**

- Show top-level nodes: users, products, categories, carts, orders, reviews, recipes, vouchers.

### 2.3.2. Main Firebase Nodes

| Node | Important Fields |
| --- | --- |
| `/users` | uid, name/full_name, email, phone, role, is_active/isActive, points, onlineStatus |
| `/products` | id, name, price, original_price/originalPrice, stock_quantity, unit, category_id, image_url, description, is_active |
| `/categories` | id, name, description, image |
| `/carts/{uid}` | product_id, product_name, price, quantity, savedForLater |
| `/orders` | id, user_id, delivery fields, items, subtotal, shipping_fee, discount, final_amount, payment_method, payment_status, status, shipper_id, shipperId, status_logs |
| `/reviews` | orderId, productId, userId, rating, comment, createdAt |
| `/recipes` | id, name, description, ingredients, imageUrl |
| `/vouchers` | code, discount type/value, active, date range |
| `/shippers` or `/users` with role SHIPPER | shipper profile/status depending current flow |

### 2.3.3. ERD / Relationship Notes

| Relationship | Description |
| --- | --- |
| User - Order | One user can have many orders |
| User - Cart | One user has one cart with many cart items |
| Order - OrderItem | One order contains many embedded order items |
| Category - Product | One category contains many products |
| Product - OrderItem | One product can appear in many order items |
| Order - Shipper | One order can be assigned to one shipper through shipperId |
| Order - StatusLog | One order has many status logs |
| User - Review | One user can write many reviews |
| Product - Review | One product can have many reviews |
| Voucher - Order | One voucher code can be applied to many orders |

**Figure 2.9: ERD / Firebase Relationship Diagram**

### 2.3.4. Data Flow Diagram

| Figure | DFD Name | Notes |
| --- | --- | --- |
| Figure 2.10 | Context DFD | Customer/Admin/Shipper, Spring Boot, Firebase RTDB, Payment Gateway |
| Figure 2.11 | Product and Cart DFD | Customer product browsing and cart data |
| Figure 2.12 | Checkout and Payment DFD | Order creation, payment URL, callback, status update |
| Figure 2.13 | Admin Order Management DFD | Order list, detail, status update, assign shipper |
| Figure 2.14 | Shipper Delivery DFD | Assigned order, delivery update, history/statistics |

Data access paths to show:

- Customer app -> REST API -> backend -> Firebase RTDB for core customer flows.
- Admin app -> Firebase RTDB directly for current Firebase admin APIs; backend for selected server-side APIs.
- Shipper app -> Firebase Auth/RTDB directly for assigned order realtime flow.
- Payment Gateway -> Spring Boot callback/IPN -> Firebase/order update.

---

### 2.3.5. Order Status Design

| Status | Created/Updated By | Meaning in Workflow |
| --- | --- | --- |
| PENDING | Customer/backend | New order waiting for review |
| PENDING_PAYMENT | Customer/backend | Online payment not completed |
| PENDING_APPROVAL | Payment/backend | Online payment completed, waiting admin |
| CONFIRMED | Admin | Order approved |
| PREPARING | Admin/store operation | Store is preparing order |
| READY_FOR_PICKUP | Admin/store operation | Order ready for shipper |
| SHIPPING | Admin assignment | Shipper assigned |
| IN_TRANSIT | Shipper | Shipper started delivery |
| DELIVERED | Shipper/Admin | Delivery completed |
| FAILED | Shipper | Delivery failed |
| RETURN_REQUESTED | Customer | Customer requested return |
| RETURNED | Admin/flow | Return accepted/completed |
| CANCELLED | Customer/Admin | Order cancelled |

Important design points:

- Order status connects all three apps.
- Payment status is separate from order status.
- Shipper assignment writes shipper id/name and changes order status to SHIPPING.
- Shipper app uses status groups to separate active and history orders.

**Figure 2.14b: Order Status State Diagram**

---

## 2.4. System Architecture Design

| Layer | Components | Responsibility |
| --- | --- | --- |
| Presentation layer | Customer/Admin/Shipper Android apps | UI, user interaction, local validation, navigation |
| Shared Android layer | `shared` module | DTOs, API interfaces, utilities, session helper |
| Backend service layer | Spring Boot modules | REST APIs, business logic, security, payment callbacks |
| Cloud data layer | Firebase RTDB | Runtime data storage and realtime updates |
| Authentication layer | JWT/Firebase Auth/SessionManager | Role/session management depending app |
| External services | MoMo, ZaloPay, AI provider | Payment and chatbot support |

### 2.4.1. Android App Architecture

- Activities and Fragments handle screens and navigation.
- ViewModels expose data/state to UI.
- Adapters render RecyclerView lists.
- Repositories/API implementations handle Retrofit or Firebase data access.
- Hilt provides dependencies.
- ViewBinding reduces manual `findViewById` mistakes in screens that use binding.

### 2.4.2. Backend Architecture

- Controller layer:
  - Receives HTTP requests.
  - Maps request body/path/query.
  - Returns API response.
- Service layer:
  - Handles business logic.
  - Validates workflow rules.
  - Coordinates repositories.
- Repository/helper layer:
  - Reads/writes Firebase RTDB.
  - Maps data entities.
- Security layer:
  - JWT filter.
  - role-based route protection.

### 2.4.3. Data Synchronization Design

| Data Type | Sync Method |
| --- | --- |
| Products | Backend/Firebase APIs update product nodes; customer/admin can refresh |
| Cart | User-specific cart data |
| Orders | Firebase order nodes and backend APIs |
| Shipper assignment | Admin writes shipper id/name to order |
| Delivery status | Shipper writes status updates; other apps refresh/listen |
| Payment status | Backend payment callbacks/mock endpoints update order/payment fields |

**Figure 2.18b: Layered Architecture Diagram**

---

## 2.5. Sitemap

### 2.5.1. Customer App Sitemap

| Area | Screens |
| --- | --- |
| Authentication | Login, Register, Forgot Password |
| Main navigation | Home, Products, Cart, Orders, Account |
| Product flow | Product List, Search, Flash Sale, Product Detail |
| Cart/Checkout | Cart, Saved section, Checkout, Payment Result |
| Order flow | Order List, Order Detail, Return Request, Review |
| Account flow | Edit Profile, Change Password, Recipes, Shipments, My Reviews, Chat |

**Figure 2.15: Customer App Sitemap**

### 2.5.2. Admin App Sitemap

| Area | Screens |
| --- | --- |
| Authentication | Admin Login |
| Main navigation | Dashboard, Orders, Products, Vouchers, Chatbot |
| Order flow | Order List, Order Detail, Status Update, Mark Paid, Assign Shipper, Return Review |
| Product flow | Product List, Add/Edit Product, Image Upload/Selection |
| Other admin screens | User Management, Shipment List/Detail, Audit Logs |

**Figure 2.16: Admin App Sitemap**

### 2.5.3. Shipper App Sitemap

| Area | Screens |
| --- | --- |
| Authentication | Shipper Login |
| Main screen | Header, Online Toggle, Logout |
| Tabs | Active Orders, History, Statistics, Profile |
| Delivery flow | Order Detail, Call, Navigate, Start Delivery, Delivered, Failed |

**Figure 2.17: Shipper App Sitemap**

---

## 2.6. Mockup / UI Design Notes

### 2.6.1. Logo and Color Palette

| Design Element | Notes |
| --- | --- |
| Brand feeling | Fresh, friendly, local grocery, trustworthy |
| Main color | Green primary color |
| Background | Light/cream surfaces |
| Status colors | Success, warning, error |
| UI style | Cards, lists, tabs, spinners, bottom navigation |

**Figure 2.18: Logo and Color Palette**

### 2.6.2. Customer Screenshots to Include

| Figure | Screen |
| --- | --- |
| Figure 2.19 | Customer Login |
| Figure 2.20 | Customer Home |
| Figure 2.21 | Product List/Search |
| Figure 2.22 | Product Detail |
| Figure 2.23 | Cart |
| Figure 2.24 | Checkout |
| Figure 2.25 | Payment Result |
| Figure 2.26 | Order List/Order Detail |
| Figure 2.27 | Account/Profile |

### 2.6.3. Admin Screenshots to Include

| Figure | Screen |
| --- | --- |
| Figure 2.28 | Admin Login |
| Figure 2.29 | Dashboard |
| Figure 2.30 | Product List |
| Figure 2.31 | Add/Edit Product |
| Figure 2.32 | Order List |
| Figure 2.33 | Order Detail |
| Figure 2.34 | Assign Shipper |
| Figure 2.35 | Voucher/User Management |

### 2.6.4. Shipper Screenshots to Include

| Figure | Screen |
| --- | --- |
| Figure 2.36 | Shipper Login |
| Figure 2.37 | Active Orders |
| Figure 2.38 | Order Detail |
| Figure 2.39 | Start Delivery |
| Figure 2.40 | Failure Reason Dialog |
| Figure 2.41 | History |
| Figure 2.42 | Statistics/Profile |

---

# CHAPTER III. DEVELOPMENT APPLICATION

## 3.1. Development Environment

| Category | Tools/Technologies |
| --- | --- |
| IDE | Android Studio, optional IntelliJ for backend |
| Android language | Java |
| Android UI | XML |
| Backend | Spring Boot |
| Build system | Gradle Kotlin DSL |
| DI/network | Hilt, Retrofit, Gson |
| Firebase | Firebase Auth, Firebase Realtime Database, Firebase Admin SDK |
| UI/Data | ViewModel, LiveData, ViewBinding, RecyclerView |
| Chart | MPAndroidChart |
| Testing | Android Emulator Pixel 7, Postman/Swagger, Gradle build/install |

Project modules:

| Module | Role |
| --- | --- |
| `app-customer` | Customer Android app |
| `app-admin` | Admin Android app |
| `app-shipper` | Shipper Android app |
| `shared` | Shared DTO/API/util classes |
| `app-backend` | Spring Boot backend |

---

## 3.2. Backend Development

### 3.2.1. Backend Module Overview

| Backend Module | Main Responsibility |
| --- | --- |
| `auth` | Registration, login, token refresh, role validation |
| `user` | User profile, role, active status |
| `product` | Product listing, detail, search, management |
| `category` | Product category data |
| `cart` | Cart operations |
| `order` | Order creation, order history, status lifecycle |
| `payment` | MoMo/ZaloPay sandbox, IPN/callback, payment status |
| `shipper` | Shipper profile, assignment, delivery status |
| `review` | Product review and rating |
| `recipe` | Recipe data and recipe browsing |
| `recommend` | Product recommendation/event tracking support |
| `ai` | Chatbot/AI-related API |
| `upload` | Image/file upload support |

Backend writing points:

- Explain the backend by module instead of listing only endpoints.
- Mention that some features are fully backend-oriented, while some admin/shipper data operations currently use Firebase directly in mobile code.
- Explain that this mixed design is acceptable for the current project but can be centralized more in backend in future development.

### 3.2.2. Authentication Module

| Endpoint | Purpose |
| --- | --- |
| `/api/auth/register` | Customer registration |
| `/api/auth/login` | Customer login |
| `/api/auth/admin/login` | Admin/Staff/Audit login |
| `/api/auth/shipper/login` | Shipper login |
| `/api/auth/me` | Current user profile |
| `/api/auth/logout` | Logout |
| `/api/auth/refresh` | Refresh access token |

Key points:

- JWT-based backend authentication.
- Role check for admin and shipper login.
- Customer role cannot enter Admin app.
- Non-shipper role cannot enter Shipper app.
- Session is saved by SessionManager and/or FirebaseAuth depending app.

### 3.2.3. Product Module

- Supports product listing, detail, search, category filtering.
- Admin can create, update, delete, activate/deactivate products.
- Important product fields:
  - name.
  - price.
  - original price.
  - stock quantity.
  - unit.
  - category.
  - description.
  - image URL.
  - active status.
- Product validation in admin form:
  - name required.
  - price > 0.
  - original price >= price.
  - stock >= 0.
  - unit and category required.

### 3.2.4. Category Module

- Provides product categories.
- Used by:
  - Home category chips.
  - Product filters.
  - Admin product form.
- Category examples: all, veg, fruit, meat, drink, dry, spice, household, sweet, frozen, other.

### 3.2.5. Cart Module

- Customer cart features:
  - Add item.
  - Update quantity.
  - Remove item.
  - Saved-for-later section.
  - Calculate subtotal/total.
  - Show cart badge.
- Checkout requires login.

### 3.2.6. Order Module

| Endpoint | Purpose |
| --- | --- |
| `POST /api/orders` | Create order |
| `GET /api/orders/my` or `/api/orders/me` | Get customer orders |
| `GET /api/orders/{id}` | Get order detail |
| `PATCH /api/orders/{id}/cancel` | Cancel order |
| `PATCH/PUT /api/orders/{id}/status` | Admin updates order status |
| `PATCH /api/orders/{id}/paid` | Mark order as paid |
| `PATCH /api/orders/{id}/refund` | Mark order as refunded |
| `GET /api/orders/shipper` | Get shipper orders through backend path |

Order status lifecycle:

| Status | Meaning |
| --- | --- |
| PENDING | Waiting for review/confirmation |
| PENDING_PAYMENT | Waiting for online payment |
| PENDING_APPROVAL | Online payment completed, waiting approval |
| CONFIRMED | Approved by admin |
| PREPARING | Being prepared |
| READY_FOR_PICKUP | Ready for shipper |
| SHIPPING | Assigned to shipper |
| IN_TRANSIT | Shipper is delivering |
| DELIVERED | Delivered successfully |
| FAILED | Delivery failed |
| RETURN_REQUESTED | Customer requested return |
| RETURNED | Returned |
| CANCELLED | Cancelled |

### 3.2.7. Payment Module

| Payment Method | Implementation Notes |
| --- | --- |
| COD | Order is created; payment can be marked paid manually or after delivery depending flow |
| MoMo sandbox | Backend creates payment URL/deeplink and handles IPN/return/mock result |
| ZaloPay sandbox | Backend creates payment URL/deeplink and handles callback/return/mock result |

Important endpoints:

- `POST /api/payments/momo`.
- `POST /api/payments/zalopay`.
- `POST /api/payments/momo/ipn`.
- `POST /api/payments/zalopay/callback`.
- `GET /api/payments/{orderId}/status`.
- Mock success/fail endpoints for demo.

**Figure 3.1: Payment Processing Flow**

### 3.2.8. Voucher Feature

- Voucher is currently an app/Firebase-side feature, not a dedicated Spring Boot module in `app-backend`.
- Admin app has voucher list/edit screens and Firebase implementation.
- Customer checkout validates supported voucher codes in `CheckoutActivity`.

| Voucher Code | Effect |
| --- | --- |
| `VUAVUIVE` | 15% discount |
| `FREESHIP24` | Shipping fee discount |
| `FREESHIP` | Shipping fee discount |

Do not claim there is a dedicated backend voucher module unless that module is added later.

### 3.2.9. Review Module

- Customer can review products after delivery.
- Review data:
  - orderId.
  - productId.
  - userId.
  - rating.
  - comment.
  - createdAt.
- Product detail can display product reviews.

### 3.2.10. Recipe Module

- Shows recipe list and detail.
- Recipe includes name, description, image, and ingredients.
- Customer can browse recipes from Home/Account.
- Some flows match ingredient names with products and add them to cart.

### 3.2.11. AI Chatbot Module

- Backend AI module supports chatbot functionality.
- Customer has `ChatActivity`.
- Admin has `AdminChatFragment`.
- Purpose:
  - Grocery/product questions.
  - Operational support.
  - Demonstration of AI integration in e-commerce.

### 3.2.12. Recommendation Module

- Tracks behavior events:
  - view product.
  - add to cart.
  - purchase.
- Current level:
  - Basic behavior tracking/recommendation support.
- Future direction:
  - Personalized recommendation and collaborative filtering.

### 3.2.13. Admin/Shipper Module

| Endpoint/Flow | Purpose |
| --- | --- |
| `GET /api/shippers` | Get shipper list |
| `POST /api/shippers/{id}/assign/{orderId}` | Assign shipper to order |
| `PUT /api/shippers/{id}/orders/{orderId}/delivery` | Update delivery status |
| Shipper online/status/location endpoints | Support delivery monitoring |

Admin assignment flow:

- Select active shipper.
- Update order shipper fields.
- Change order status to SHIPPING.
- Shipper app reads assigned order by shipperId.

---

## 3.3. Customer Android App Development

### 3.3.1. Login and Register

- `LoginActivity` validates identifier and password.
- Customer test account is prefilled in code.
- Shipper account is rejected in Customer app.
- Register and Forgot Password screens exist.
- `MainActivity` checks session and redirects to login if invalid.

### 3.3.2. Homepage

- `HomeFragment` includes:
  - greeting/member points.
  - address/search.
  - banner slider.
  - categories.
  - product section.
  - vouchers/promo dialog.
  - recipe section.

**Figure 3.2: Customer Home Screen**

### 3.3.3. Product List and Product Detail

- Product list supports search/category.
- Product detail shows:
  - image slider.
  - name.
  - price.
  - original price/discount.
  - rating.
  - sold count.
  - stock.
  - description.
  - reviews.
  - similar products.
- Add-to-cart validation:
  - Quantity cannot exceed stock.
  - Out-of-stock disables button.
  - Mock/test product is blocked from purchase.

**Figure 3.3: Product Detail Screen**

### 3.3.4. Shopping Cart

- `CartFragment` features:
  - RecyclerView cart items.
  - Quantity update.
  - Swipe left to delete.
  - Saved-for-later section.
  - Subtotal/total calculation.
  - Empty state and shop now button.
  - Login required for checkout.

**Figure 3.4: Cart Screen**

### 3.3.5. Checkout

- `CheckoutActivity` requires:
  - receiver name.
  - phone.
  - address.
- Optional:
  - note.
  - voucher code.
- Payment methods:
  - COD.
  - MoMo.
  - ZaloPay.
- Creates `CreateOrderRequest`.
- Tracks purchase events.

**Figure 3.5: Checkout Screen**

### 3.3.6. Payment Result

- `PaymentResultActivity`:
  - opens payment URL/deeplink.
  - checks payment status.
  - supports debug mock success.
  - clears cart when payment is paid.
  - navigates to order list.

**Figure 3.6: Payment Result Screen**

### 3.3.7. Order History and Detail

| Order Tab | Status Group |
| --- | --- |
| All | All orders |
| Waiting | pending, pending_payment, pending_approval, confirmed, preparing, ready_for_pickup |
| Shipping | shipping, in_transit |
| Delivered | delivered |
| Cancelled | cancelled, failed |

Order detail shows:

- order id.
- status.
- created date.
- receiver info.
- payment.
- items.
- total.
- cancel/return/review buttons depending status.

**Figure 3.7: Order List and Order Detail**

### 3.3.8. Recipe Feature

- Recipe list/detail.
- Ingredients display.
- Product matching by ingredient names.
- Add matching ingredients to cart when possible.

**Figure 3.8: Recipe Feature Screen**

### 3.3.9. Chat and Account

- Chat support screen.
- Account screen:
  - guest vs logged-in state.
  - avatar/name/phone/email.
  - edit profile.
  - change password.
  - my orders.
  - recipes.
  - shipments.
  - my reviews.
  - logout.

**Figure 3.9: Account and Chat Screens**

---

## 3.4. Admin Android App Development

### 3.4.1. Admin Login

| Role | Test Email | Test Password | Expected Access |
| --- | --- | --- | --- |
| Admin | `admin@vuavuive.vn` | `Admin@123` | Full admin access |
| Staff | `staff@vuavuive.vn` | `Staff@123` | Limited backoffice access |
| Audit | `audit@vuavuive.vn` | `Audit@123` | Read-only access |
| Customer | `customer@gmail.com` | `Customer@123` | Blocked from Admin app |

Main points:

- `AdminLoginActivity` has role spinner.
- `MainActivity` checks session and backoffice role.
- Invalid role is logged out/blocked.

**Figure 3.10: Admin Login Screen**

### 3.4.2. Dashboard

- `DashboardFragment`.
- Can describe:
  - overview metrics.
  - order/revenue/product/user monitoring.
  - quick admin overview.

**Figure 3.11: Admin Dashboard**

### 3.4.3. Product Management

| Feature | Notes |
| --- | --- |
| Search | Search product by name |
| Category filter | all, veg, fruit, meat, drink, dry, spice, household, sweet, frozen, other |
| Low stock filter | active products with stock <= 10 |
| Add product | FAB opens product form |
| Edit product | Click product item |
| Delete product | Long click product item |
| CSV export | Export product list |
| Audit behavior | Read-only |

**Figure 3.12: Admin Product Management**

### 3.4.4. Order Management

- `AdminOrderListFragment`:
  - status tabs.
  - search by order id/name/phone.
  - export CSV.
  - bulk update.
- `AdminOrderDetailActivity`:
  - customer and delivery info.
  - order items.
  - payment section.
  - price breakdown.
  - status spinner.
  - mark paid.
  - return review.
  - assign shipper.

Admin order status spinner:

| Status Code | Meaning |
| --- | --- |
| pending | Waiting |
| confirmed | Confirmed |
| preparing | Preparing |
| ready_for_pickup | Ready for pickup |
| shipping | Assigned to shipper |
| in_transit | Delivering |
| delivered | Delivered |
| cancelled | Cancelled |

**Figure 3.13: Admin Order List**

**Figure 3.14: Admin Order Detail**

### 3.4.5. Shipper Assignment

- Admin selects an active shipper from users with role SHIPPER.
- Valid assign statuses:
  - confirmed.
  - preparing.
  - ready_for_pickup.
  - shipping.
- On assign:
  - write `shipper_id`.
  - write `shipperId`.
  - write shipper name.
  - set status to SHIPPING.
  - write status log.
- Shipper app receives the order because it filters by current Firebase UID/shipperId.

**Figure 3.15: Admin Assign Shipper Card**

### 3.4.6. Voucher Management

- `VoucherListFragment`.
- `VoucherEditActivity`.
- Admin can add/edit.
- Staff/Audit have limited/read-only behavior depending role.
- Voucher feature supports promotion and discount demonstration.

**Figure 3.16: Voucher Management Screen**

### 3.4.7. User Management

| Feature | Notes |
| --- | --- |
| Search | name/email/phone |
| Role filters | Customer, Shipper, Staff/Admin/Audit |
| Active toggle | Lock/unlock user |
| Detail dialog | View user information |
| Change role | Admin only |
| Export CSV | Audit blocked |
| Staff limitation | Staff blocked from User Management |

**Figure 3.17: User Management Screen**

### 3.4.8. Shipment and Audit

- Shipment screens:
  - pending.
  - processing.
  - shipping.
  - delivered.
  - failed.
- Shipment update requires note.
- Audit role cannot update shipment.
- Audit logs represent read-only history/log review.

**Figure 3.18: Shipment and Audit Screens**

### 3.4.9. Admin Chatbot

- `AdminChatFragment`.
- Supports operational questions.
- Demonstrates AI integration for admin support.

**Figure 3.19: Admin Chatbot Screen**

---

## 3.5. Shipper Android App Development

### 3.5.1. Shipper Login

| Test Email | Test Password | Expected Result |
| --- | --- | --- |
| `shipper@gmail.com` | `Shipper@123` | Login success |
| `customer@gmail.com` | `Customer@123` | Rejected because role is not SHIPPER |

Main points:

- `ShipperLoginActivity`.
- FirebaseAuth login.
- Reads `/users/{uid}`.
- Role must be SHIPPER.
- Saves local session.

**Figure 3.20: Shipper Login Screen**

### 3.5.2. Main Screen and Tabs

| Tab | Purpose |
| --- | --- |
| Active Orders | Assigned orders that need delivery |
| History | Delivered/failed/returned orders |
| Statistics | Delivery revenue and counts |
| Profile | Shipper information and logout |

Other points:

- Header displays shipper name.
- Online/offline switch writes `/users/{uid}/onlineStatus`.

**Figure 3.21: Shipper Main Tabs**

### 3.5.3. Assigned Orders

- `FirebaseShipperRepository`:
  - reads `/orders`.
  - maps snapshots to `Order`.
  - filters by `shipperId == currentUid`.
- Active order statuses:
  - CONFIRMED.
  - PREPARING.
  - READY_FOR_PICKUP.
  - SHIPPING.
  - IN_TRANSIT.
- History statuses:
  - DELIVERED.
  - FAILED.
  - RETURNED.
- Search fields:
  - order id.
  - customer name.
  - phone.
  - address.

**Figure 3.22: Shipper Active Orders**

### 3.5.4. Order Detail

- Shows:
  - order id.
  - status.
  - customer name.
  - phone.
  - address.
  - note.
  - items.
  - total.
  - payment method/status.
- Actions:
  - Call customer.
  - Navigate with Google Maps or geo fallback.

**Figure 3.23: Shipper Order Detail**

### 3.5.5. Delivery Status Update

| Current Status | Action | New Status |
| --- | --- | --- |
| CONFIRMED/PREPARING/READY_FOR_PICKUP/SHIPPING | Start delivery | IN_TRANSIT |
| IN_TRANSIT | Delivered | DELIVERED |
| IN_TRANSIT | Failed with reason | FAILED |

Failure reasons:

- Customer does not answer phone.
- Wrong address / cannot find address.
- Customer refuses to receive.
- Customer asks to reschedule.
- Other custom reason.

**Figure 3.24: Delivery Status Update Flow**

### 3.5.6. Statistics and Profile

| Statistic | Meaning |
| --- | --- |
| Total revenue | Sum of delivered orders |
| COD amount | Delivered non-online/MoMo-unpaid style orders |
| Online amount | Delivered MoMo paid orders in current logic |
| Success count | Delivered orders |
| Failed count | Failed/returned orders |

Profile points:

- name.
- email.
- phone.
- success rate.
- logout.

**Figure 3.25: Shipper Statistics and Profile**

---

## 3.6. Testing

### 3.6.0. Test Accounts

| Role | Email | Password | App |
| --- | --- | --- | --- |
| Customer | `customer@gmail.com` | `Customer@123` | Customer app |
| Admin | `admin@vuavuive.vn` | `Admin@123` | Admin app |
| Staff | `staff@vuavuive.vn` | `Staff@123` | Admin app |
| Audit | `audit@vuavuive.vn` | `Audit@123` | Admin app |
| Shipper | `shipper@gmail.com` | `Shipper@123` | Shipper app |

Notes:

- Customer account should be blocked from Admin app.
- Shipper account should be blocked from Customer app if role is checked.
- Audit account should be read-only in admin operations.

### 3.6.1. Functional Testing Matrix

| Role/App | Test Case | Expected Result |
| --- | --- | --- |
| Customer | Login successfully | Customer enters main app |
| Customer | Search product | Product list is filtered |
| Customer | Add product to cart | Cart badge and total update |
| Customer | Checkout COD | Order is created and cart clears |
| Customer | Checkout MoMo/ZaloPay mock | Payment result updates order/payment status |
| Customer | Cancel order | Allowed only for valid statuses |
| Customer | Return delivered order | Return request is created |
| Customer | Review delivered product | Review is submitted/displayed |
| Admin | Login as admin/staff/audit | Correct role behavior |
| Admin | Add/edit product | Product is saved and visible |
| Admin | Update order status | Order status changes |
| Admin | Mark paid | Payment status changes |
| Admin | Assign shipper | Order gets shipperId and status SHIPPING |
| Admin | Audit edit attempt | Action is blocked |
| Shipper | Login as shipper | Shipper enters main app |
| Shipper | View assigned order | Only assigned orders show |
| Shipper | Start delivery | Status becomes IN_TRANSIT |
| Shipper | Mark delivered | Status becomes DELIVERED |
| Shipper | Mark failed | Status becomes FAILED with reason |

### 3.6.2. Integration Testing

| Integration Flow | Steps |
| --- | --- |
| Full order delivery | Customer creates order -> Admin confirms -> Admin assigns shipper -> Shipper delivers -> Customer/Admin see delivered status |
| Online payment | Customer chooses MoMo/ZaloPay -> PaymentResultActivity opens payment -> mock success -> backend/app updates paid status |
| Return flow | Customer requests return -> Admin approves/rejects -> order status updates |
| Role security | Customer/Admin/Shipper/Audit attempt correct and incorrect actions |

### 3.6.3. API Testing

- Tools:
  - Postman.
  - Swagger.
- API groups:
  - Auth.
  - Product.
  - Category.
  - Cart.
  - Order.
  - Payment.
  - Shipper.
  - User/Admin.
  - Review.
  - Recipe.
  - AI.
- Check:
  - success response.
  - validation error.
  - unauthorized.
  - forbidden role.
  - callback/mock payment result.

### 3.6.4. UI Testing

| UI Aspect | What to Check |
| --- | --- |
| Layout | No overlap on emulator screen |
| Navigation | Bottom nav, tabs, back button |
| Forms | Required fields, invalid input, keyboard behavior |
| Loading/Error | Toasts and empty states |
| Lists | RecyclerView rendering, search/filter |
| Payment | Payment result screen and navigation |
| Shipper | Call/map/status buttons |

### 3.6.5. Security and Role Testing

| Rule | Expected Result |
| --- | --- |
| Customer opens Admin app | Blocked |
| Customer opens Shipper app | Blocked |
| Shipper opens Customer/Admin flow | Blocked or redirected |
| Audit edits data | Blocked/read-only |
| Staff accesses restricted user management | Blocked |
| Shipper views unassigned orders | Not shown |
| Logout | Session is cleared |

### 3.6.6. Build and Deployment Testing

| Command | Purpose |
| --- | --- |
| `./gradlew :app-customer:installDebug` | Build/install Customer app |
| `./gradlew :app-admin:installDebug` | Build/install Admin app |
| `./gradlew :app-shipper:installDebug` | Build/install Shipper app |
| `./gradlew :app-backend:bootRun` | Run backend locally if needed |

Notes:

- Reinstall APK after UI/backend-related app changes.
- Emulator needs enough RAM/CPU.
- Local backend networking may require correct emulator network setup or ADB reverse depending configuration.

**Figure 3.26: Testing Evidence Screenshots**

### 3.6.7. Recommended Demo Scenario

| Step | Actor | Action | Expected Result |
| --- | --- | --- | --- |
| 1 | Customer | Login and browse products | Product list loads |
| 2 | Customer | Add one product to cart | Cart badge/total updates |
| 3 | Customer | Checkout with COD or payment mock | Order is created |
| 4 | Admin | Login and open order list | New order appears |
| 5 | Admin | Confirm order | Status becomes CONFIRMED |
| 6 | Admin | Assign shipper | Order gets shipperId and status SHIPPING |
| 7 | Shipper | Login and open active orders | Assigned order appears |
| 8 | Shipper | Start delivery | Status becomes IN_TRANSIT |
| 9 | Shipper | Mark delivered | Status becomes DELIVERED |
| 10 | Customer | Refresh order history | Customer sees delivered status |

**Figure 3.27: End-to-End Demo Evidence**

### 3.6.8. Recommended Screenshots for Testing Evidence

| Evidence | Screenshot Needed |
| --- | --- |
| Customer login | Login success screen |
| Product/cart | Product detail and cart total |
| Checkout | Checkout form and order result |
| Admin order | Order detail before/after status update |
| Assign shipper | Admin assign shipper card |
| Shipper active order | Shipper active order list |
| Delivery update | IN_TRANSIT and DELIVERED/FAILED evidence |
| Customer final status | Customer order detail with final status |
| Build output | Gradle `BUILD SUCCESSFUL` output |

---

# CHAPTER IV. CONCLUSION

## 4.1. Achieved Results

| Area | Result |
| --- | --- |
| Customer app | Product browsing, cart, checkout, payment, order tracking |
| Admin app | Product/order/user/voucher management, shipper assignment |
| Shipper app | Assigned orders, delivery updates, history/statistics |
| Backend | Spring Boot modules for auth, product, order, payment, shipper, AI, etc. |
| Database | Firebase RTDB/Auth integration |
| Payment | COD, MoMo sandbox, ZaloPay sandbox |
| Extra features | Review, recipe, chatbot, recommendation support |

## 4.2. Limitations

| Limitation | Explanation |
| --- | --- |
| Payment sandbox | MoMo/ZaloPay are mainly demo/mock/sandbox flows |
| Delivery tracking | Full realtime GPS tracking is not production-ready |
| Firebase direct access | Some admin/shipper flows access Firebase directly |
| Recommendation | Current recommendation is basic |
| Automated testing | No full test suite yet |
| Encoding/UI polish | Some source text/labels may need cleanup |
| Scalability | Some Firebase flows use client-side filtering |
| Deployment | Production backend/security hardening not complete |

## 4.3. Future Development

- Deploy backend to cloud.
- Move payment integration to production mode.
- Add push notifications for order/payment/delivery changes.
- Add realtime shipper GPS tracking map.
- Improve recommendation with behavior history.
- Improve chatbot context with product/order data.
- Add inventory alerts and restock management.
- Add admin web dashboard if required.
- Add iOS or responsive web app.
- Add automated unit/UI/API tests.
- Clean encoding and improve UI consistency.

---

# REFERENCES

| Source | Purpose |
| --- | --- |
| Android Developers documentation | Android components, lifecycle, UI, emulator |
| Spring Boot official documentation | REST API, security, backend structure |
| Firebase Realtime Database documentation | RTDB data structure and rules |
| Firebase Authentication documentation | Firebase login/session |
| MoMo Developer documentation | MoMo sandbox payment |
| ZaloPay Developer documentation | ZaloPay sandbox payment |
| Material Design documentation | UI design guidelines |
| Retrofit/Gson/Hilt documentation | Android API/DI integration |
| Mobile commerce references | Market/background discussion |

---

# APPENDICES

## Appendix A - Source Code

| Item | Content to Add |
| --- | --- |
| Repository link | GitHub/source code link |
| Customer app | `app-customer` |
| Admin app | `app-admin` |
| Shipper app | `app-shipper` |
| Shared module | `shared` |
| Backend | `app-backend` |

## Appendix B - Database Sample

- Add Firebase RTDB export or screenshots.
- Include nodes:
  - users.
  - products.
  - orders.
  - carts.
  - reviews.
  - vouchers.

**Figure B.1: Firebase RTDB Sample**

## Appendix C - API Documentation

- Add Swagger screenshots or Postman collection.
- Include auth, product, order, payment, shipper APIs.

**Figure C.1: Swagger / Postman API Evidence**

## Appendix D - UI Screenshots

- Add Customer screenshots.
- Add Admin screenshots.
- Add Shipper screenshots.
- Add Payment mock screenshots.

**Figure D.1-D.n: Application UI Screenshots**

## Appendix E - BPMN / DFD / ERD

- Login/register BPMN.
- Checkout/payment BPMN.
- Admin order + shipper assignment BPMN.
- Shipper delivery BPMN.
- Firebase node/ERD diagram.
- DFD Level 0/Level 1.

**Figure E.1-E.n: System Diagrams**

## Appendix F - Testing Evidence

| Evidence | Description |
| --- | --- |
| Login success | Customer/Admin/Shipper login screenshots |
| Checkout success | COD/payment checkout evidence |
| Payment mock success | MoMo/ZaloPay mock result |
| Admin assign shipper | Order detail with shipper assignment |
| Shipper delivery | IN_TRANSIT/DELIVERED/FAILED evidence |
| Customer updated status | Customer order status after delivery |
| Build output | Gradle build/install output screenshots |

## Appendix G - Presentation Slides

- Add slide file/link.
- Suggested demo flow:
  - Customer creates order.
  - Admin confirms and assigns shipper.
  - Shipper delivers order.
  - Customer sees updated status.

## Appendix H - Team Contribution Table

| Member | Main Tasks | Evidence | Contribution |
| --- | --- | --- | --- |
| Member 1 | To be filled | commits/screenshots | % |
| Member 2 | To be filled | commits/screenshots | % |
| Member 3 | To be filled | commits/screenshots | % |
| Member 4 | To be filled | commits/screenshots | % |
