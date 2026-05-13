# MID-TERM PROJECT REPORT
## Software Engineering Course

---

## Project Information

| Field | Detail |
|---|---|
| **Project Title** | Fashion Shop Mobile Application |
| **Course** | Software Engineering |
| **Team Name** | Team DOAN3 |
| **Instructor** | *(Instructor Name)* |
| **University** | *(University Name)* |
| **Submission Date** | April 2026 |

---

## Team Members

| No | Student ID | Full Name | Role |
|---|---|---|---|
| 1 | *(ID)* | *(Name)* | Scrum Master / Lead Developer |
| 2 | *(ID)* | *(Name)* | Developer (UI/UX) |
| 3 | *(ID)* | *(Name)* | Developer (Backend/Firebase) |
| 4 | *(ID)* | *(Name)* | Tester / Documentation |

**Member Responsibilities:**
- **Scrum Master / Lead Developer:** Manages sprint planning, coordinates team tasks, implements core navigation and state management.
- **Developer (UI/UX):** Designs and implements all user-facing screens including Home, Product Detail, Cart, Favorites, and Profile.
- **Developer (Backend/Firebase):** Integrates Firebase Firestore, implements authentication logic, manages data synchronization.
- **Tester / Documentation:** Writes test cases, performs manual testing, prepares project documentation and demo video.

---

## 1. Introduction

### 1.1 Project Overview

This project develops a **Fashion Shop Mobile Application** for Android using Jetpack Compose and Firebase Firestore as the backend. The application addresses the growing demand for convenient online fashion shopping by providing a seamless, intuitive mobile experience for customers and a comprehensive management panel for administrators.

The target users are two groups: **customers** who browse, search, and purchase fashion products (clothing categories: Tops, Bottoms, Dresses), and **administrators** who manage the product catalog, inventory, orders, and user accounts. The solution provides real-time data synchronization through Firebase, ensuring that product availability, order statuses, and user reviews are always up to date across all devices.

### 1.2 Project Objectives

- Develop a fully functional Android mobile application prototype for a fashion e-commerce platform.
- Apply Agile (Scrum) development methodology with iterative sprints.
- Implement key features: user authentication, product browsing, shopping cart, order management, product reviews, and an admin dashboard.
- Integrate Firebase Firestore for real-time cloud database functionality.
- Demonstrate team collaboration using GitHub version control.
- Deliver a clean, modern UI following Material Design 3 principles.

### 1.3 Development Methodology

The project follows the **Scrum** methodology. Development is divided into three two-week sprints. Each sprint includes planning, development, testing, and a review/retrospective session.

| Tool | Purpose |
|---|---|
| GitHub | Source code management and version control |
| Trello | Sprint backlog and task management |
| Figma | UI wireframing and design reference |
| Firebase Console | Database management and monitoring |
| Android Studio | IDE for development and testing |

**Team roles:**
- **Scrum Master:** Facilitates daily stand-ups, removes blockers, tracks sprint progress.
- **Developers (×2):** Implement features from the sprint backlog.
- **Tester:** Validates features against acceptance criteria, reports bugs.

---

## 2. Requirements

### 2.1 Stakeholders

| Stakeholder | Description |
|---|---|
| **Customer (End User)** | Browses products, places orders, writes reviews, manages their account |
| **Administrator** | Manages products, inventory, orders, and user accounts via admin panel |
| **Development Team** | Designs, builds, and maintains the application |
| **Instructor (Supervisor)** | Evaluates the project against course requirements |

### 2.2 Functional Requirements

| ID | Requirement Description |
|---|---|
| FR1 | User can register a new account with username, email, phone, and password |
| FR2 | System validates that username and email are unique during registration |
| FR3 | User can log in with username and password |
| FR4 | Admin account is distinguished by role and redirected to the Admin Panel upon login |
| FR5 | User can browse products on the Home screen, filtered by category (All, Tops, Bottoms, Dresses) |
| FR6 | User can search for products by name |
| FR7 | User can view product details including image, price, stock status, color/size options, and reviews |
| FR8 | User can add products to the shopping cart with selected size |
| FR9 | User can view, update quantity, and remove items in the cart |
| FR10 | User must be logged in to place an order |
| FR11 | User can place an order by providing a delivery address |
| FR12 | User can view their order history and track order status |
| FR13 | User can cancel an order (only when status is "Pending Confirmation") |
| FR14 | User can confirm receipt of goods and request a return |
| FR15 | User can re-order from a completed or cancelled order |
| FR16 | User can add products to a Favorites list |
| FR17 | User can write a star rating (1–5) and comment on a product after receiving it |
| FR18 | User can update their profile avatar using a URL |
| FR19 | User can change their password by verifying the old password |
| FR20 | Admin can add, edit, and delete products including image URL and stock quantity |
| FR21 | Admin can update order status across 7 stages |
| FR22 | Admin can cancel any active order |
| FR23 | Admin can view and edit user accounts and change user roles |
| FR24 | Admin can delete non-admin user accounts |
| FR25 | System automatically decreases stock when an order is placed |
| FR26 | System automatically restores stock when an order is cancelled or returned |

### 2.3 Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR1 | The application must respond to user interactions within 2 seconds under normal network conditions |
| NFR2 | User passwords must be stored in Firebase Firestore (plain text for prototype; hashed in production) |
| NFR3 | The system must support concurrent access by multiple users via Firebase real-time listeners |
| NFR4 | The UI must follow Material Design 3 guidelines and be accessible on Android API 24+ |
| NFR5 | Product and order data must be synchronized in real time across all connected clients |
| NFR6 | The application must handle network errors gracefully with user-friendly error messages |
| NFR7 | Admin features must be completely inaccessible to regular user accounts |

### 2.4 User Stories

| ID | User Story |
|---|---|
| US1 | As a new user, I want to register an account so that I can access personalized features like order history and reviews. |
| US2 | As a user, I want to browse products by category so that I can quickly find the type of clothing I am looking for. |
| US3 | As a user, I want to view product details including stock availability so that I know whether the item is available before adding it to my cart. |
| US4 | As a user, I want to add products to my cart and adjust quantities so that I can manage my purchase before checking out. |
| US5 | As a user, I want to be prompted to log in when I attempt to place an order so that my order is linked to my account. |
| US6 | As a user, I want to track my order status so that I know when my items will arrive. |
| US7 | As a user, I want to write a review and star rating for a product I received so that I can share my experience with other shoppers. |
| US8 | As a user, I want to save products to my Favorites list so that I can easily find them later. |
| US9 | As an admin, I want to manage product inventory so that stock levels are always accurate. |
| US10 | As an admin, I want to update order statuses so that customers are informed about their delivery progress. |

---

## 3. System Design (C4 Model)

### 3.1 System Context Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Fashion Shop System                       │
│                                                             │
│  ┌──────────────┐    uses    ┌─────────────────────────┐   │
│  │   Customer   │──────────▶│  Android Mobile App      │   │
│  └──────────────┘           └────────────┬────────────┘   │
│                                          │                  │
│  ┌──────────────┐    uses                │                  │
│  │    Admin     │──────────▶             │                  │
│  └──────────────┘           ┌────────────▼────────────┐   │
│                              │  Firebase Firestore      │   │
│                              │  (Cloud Database)        │   │
│                              └─────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

**Description:** The system consists of a single Android mobile application used by both Customers and Administrators. All data is stored and synchronized in real time through Firebase Firestore. There is no separate backend server — the app communicates directly with Firebase using the Firebase Android SDK.

### 3.2 Container Diagram

| Container | Technology | Description |
|---|---|---|
| **Android Mobile App** | Kotlin + Jetpack Compose | The single application used by both customers and admins. Contains all UI screens and business logic. |
| **Firebase Firestore** | Google Firebase | NoSQL cloud database storing products, users, orders, and reviews in real time. |
| **Firebase Authentication** | Google Firebase | (Future) Handles secure user authentication. Currently managed manually via Firestore. |

```
┌──────────────────────────────────────────────────────────────┐
│                     Android Mobile App                        │
│                                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐ │
│  │  User UI    │  │  Admin UI   │  │  Firebase Manager    │ │
│  │  Screens    │  │  Screens    │  │  (Data Layer)        │ │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬───────────┘ │
│         └────────────────┴──────────────────────┘            │
│                          │                                    │
└──────────────────────────┼────────────────────────────────────┘
                           │ Firebase SDK
                           ▼
              ┌────────────────────────┐
              │   Firebase Firestore   │
              │  Collections:          │
              │  - products            │
              │  - users               │
              │  - orders              │
              │  - reviews             │
              └────────────────────────┘
```

### 3.3 Component Diagram

The Android application is structured into the following components:

| Component | File(s) | Description |
|---|---|---|
| **MainActivity / ShopApp** | `MainActivity.kt` | Root composable managing global navigation state and shared state (cart, favorites, auth) |
| **HomeScreen** | `MainActivity.kt` | Displays product grid with category filtering |
| **ProductDetailScreen** | `ProductDetailScreen.kt` | Shows product info, stock status, color/size selection, and review section |
| **CartScreen** | `CartScreen.kt` | Shopping cart with quantity controls, address input, and order placement |
| **SearchScreen** | `SearchScreen.kt` | Product search with real-time filtering |
| **FavoriteScreen** | `FavoriteScreen.kt` | Grid of favorited products |
| **AccountScreen / AccountTabScreen** | `AccountScreen.kt` | Login, register, and profile management |
| **UserProfileScreens** | `UserProfileScreens.kt` | User info, order history, change password, review dialog |
| **AdminScreen** | `AdminScreen.kt` | Admin dashboard with product, order, inventory, and user management |
| **FirebaseManager** | `firebase/FirebaseManager.kt` | Singleton handling all Firestore read/write operations and real-time listeners |
| **AppState** | `AppState.kt` | Global mutable state lists (productList, orderList, userAccounts, reviewList) |
| **Firestore Models** | `firebase/FirestoreModels.kt` | Data classes mapping to Firestore documents |

---

## 4. Development

### 4.1 Technologies Used

| Category | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Architecture** | Single-Activity, Composable-based navigation with state hoisting |
| **Database** | Firebase Firestore (NoSQL, real-time) |
| **Image Loading** | Coil (`coil-compose:2.6.0`) |
| **Build System** | Gradle with Kotlin DSL |
| **Min SDK** | Android API 24 (Android 7.0) |
| **Target SDK** | Android API 36 |
| **IDE** | Android Studio |
| **Version Control** | Git / GitHub |

### 4.2 Development Process

| Sprint | Duration | Features Implemented |
|---|---|---|
| **Sprint 1** | Week 1–2 | Project setup, Firebase integration, user authentication (register/login), product listing with category filter, product detail screen |
| **Sprint 2** | Week 3–4 | Shopping cart, order placement with stock management, favorites, search, admin dashboard (product CRUD, order management) |
| **Sprint 3** | Week 5–6 | User profile screens (order history, change password, avatar), product reviews & ratings, return/reorder flow, admin inventory management, UI polish |

**Key implementation decisions:**
- **State management:** Global `mutableStateListOf` lists in `AppState.kt` are updated by Firebase real-time listeners, causing automatic UI recomposition.
- **Navigation:** Stack-based navigation using boolean flags in `ShopApp` composable, avoiding the complexity of Navigation Compose for this project scope.
- **Admin separation:** Role-based access — admin login triggers `showAdmin = true`, rendering `AdminScreen` full-screen without the bottom navigation bar.

### 4.3 GitHub Repository

**GitHub Repository:** `https://github.com/[team-username]/DOAN3`

The repository contains:
- Full Kotlin source code organized by feature
- `google-services.json` configuration (excluded from public repos)
- Gradle build files with all dependencies

---

## 5. Testing

### 5.1 Testing Strategy

The project uses **manual black-box testing** as the primary testing approach, supplemented by Firebase Console monitoring for data integrity verification.

- **Unit Testing:** Core utility functions (`parsePrice`, `formatPrice`) verified manually.
- **Integration Testing:** Firebase read/write operations tested against live Firestore instance.
- **System Testing:** End-to-end user flows tested on Android Emulator (API 36) and physical device.

### 5.2 Test Cases

| Test ID | Feature | Input | Expected Result | Status |
|---|---|---|---|---|
| TC1 | Register | Valid unique username, email, password ≥ 6 chars | Account created, success dialog shown | ✅ Pass |
| TC2 | Register – Duplicate Username | Existing username | Error: "Tên đăng nhập đã được sử dụng" | ✅ Pass |
| TC3 | Register – Duplicate Email | Existing email | Error: "Email này đã được đăng ký" | ✅ Pass |
| TC4 | Login – Valid Credentials | Correct username + password | User logged in, profile tab shows user info | ✅ Pass |
| TC5 | Login – Wrong Password | Correct username, wrong password | Error: "Tên đăng nhập hoặc mật khẩu không đúng" | ✅ Pass |
| TC6 | Admin Login | username: admin, password: admin123 | Admin Panel opens full screen | ✅ Pass |
| TC7 | Add to Cart | Select size, tap "Add to Cart" | Item appears in cart with correct size and price | ✅ Pass |
| TC8 | Place Order – Not Logged In | Tap "Đặt hàng" without login | Dialog prompts user to log in | ✅ Pass |
| TC9 | Place Order – Logged In | Fill address, tap "Đặt hàng" | Order created in Firestore, stock decremented | ✅ Pass |
| TC10 | Cancel Order | Cancel order with status "Chờ xác nhận" | Status changes to "Đã hủy", stock restored | ✅ Pass |
| TC11 | Stock Warning | Product with stock ≤ 5 | Orange warning badge shown on product detail | ✅ Pass |
| TC12 | Out of Stock | Product with stock = 0 | "Hết hàng" badge shown, Add to Cart button disabled | ✅ Pass |
| TC13 | Write Review | Select 4 stars, enter comment, submit | Review appears in product detail with username and avatar | ✅ Pass |
| TC14 | Admin – Add Product | Enter name, price, category, image URL, stock | Product appears in list and on Home screen | ✅ Pass |
| TC15 | Admin – Update Order Status | Change status to "Đang giao" | Status updated in Firestore and reflected in user's order history | ✅ Pass |

### 5.3 Testing Results

All 15 test cases passed successfully. The following minor issues were identified and resolved during Sprint 3:

- **Bug:** `toObject()` Firestore deserialization crash due to type mismatch (`Long` vs `Double` for `createdAt`). **Fix:** Replaced `toObject()` with manual field reading using `getString()`, `getLong()`, `getDouble()`.
- **Bug:** App crash on startup due to missing `INTERNET` permission. **Fix:** Added permission to `AndroidManifest.xml`.
- **Bug:** Login always failed because `listenUsers()` stored empty password strings. **Fix:** Login now queries Firestore directly instead of checking local state.

**Possible improvements:**
- Implement Firebase Authentication for secure password hashing.
- Add pagination for large product lists.
- Implement push notifications for order status updates.

---

## 6. Deployment

### 6.1 Deployment Environment

| Component | Platform |
|---|---|
| **Mobile Application** | Android APK (sideloaded / Google Play Store) |
| **Database** | Firebase Firestore (Google Cloud, `dacs3-d4e36` project) |
| **Image Hosting** | External URLs provided by admin (no dedicated storage) |

### 6.2 System Access

- **Application:** Install APK on Android device (API 24+)
- **Firebase Project ID:** `dacs3-d4e36`
- **GitHub Repository:** `https://github.com/[team-username]/DOAN3`

### 6.3 Demo Video

**Demo Video Link:** *(Insert YouTube/Google Drive link)*

The demo covers:
1. User registration and login
2. Browsing products by category and searching
3. Viewing product details with stock status and reviews
4. Adding to cart and placing an order
5. Tracking order status and cancelling an order
6. Writing a product review with star rating
7. Admin login and product management (add/edit/delete)
8. Admin order management (updating status, cancelling)
9. Admin inventory management

**Recommended length:** 4–5 minutes.

---

## 7. Conclusion

This project successfully delivered a fully functional Fashion Shop Android application with real-time Firebase Firestore integration. The application supports two distinct user roles — customers and administrators — with comprehensive features including product browsing, cart management, order lifecycle tracking, inventory control, and a product review system. The team applied Scrum methodology across three sprints, iteratively building and refining features based on testing feedback. Key challenges included resolving Firebase type-mapping issues, implementing role-based navigation without a dedicated navigation library, and ensuring real-time state synchronization between Firestore and the Compose UI. Future improvements include integrating Firebase Authentication for secure password management, adding push notifications for order updates, and implementing image upload functionality to replace URL-based image management.

---

## 8. References

1. Android Developers. *Jetpack Compose Documentation*. https://developer.android.com/jetpack/compose
2. Google Firebase. *Cloud Firestore Documentation*. https://firebase.google.com/docs/firestore
3. Google Firebase. *Firebase Android SDK Setup*. https://firebase.google.com/docs/android/setup
4. Coil. *Coil Image Loading Library for Android*. https://coil-kt.github.io/coil/
5. Beck, K. et al. (2001). *Manifesto for Agile Software Development*. https://agilemanifesto.org
6. Schwaber, K. & Sutherland, J. (2020). *The Scrum Guide*. https://scrumguides.org
7. Material Design. *Material Design 3 Guidelines*. https://m3.material.io
8. Simon Brown. *The C4 Model for Software Architecture*. https://c4model.com
