package com.example.doan3.firebase

object FirestoreCollections {
    const val PRODUCTS = "products"
    const val USERS    = "users"
    const val ORDERS   = "orders"
}

// ── products ─────────────────────────────────────────────
data class ProductModel(
    val id: String        = "",
    val name: String      = "",
    val price: String     = "",       // String: "500.000đ"
    val category: String  = "",
    val colorHex: String  = "#D4A5A5",
    val imageUrl: String  = "",
    val stock: Int        = 0,
    val createdAt: Double = 0.0       // Double trong Firestore
)

// ── users ────────────────────────────────────────────────
data class UserModel(
    val id: String        = "",
    val username: String  = "",
    val password: String  = "",
    val email: String     = "",
    val phone: String     = "",
    val role: String      = "user",
    val avatarUrl: String = "",
    val createdAt: Double = 0.0       // Double trong Firestore
)

// ── orders ───────────────────────────────────────────────
data class OrderModel(
    val id: String              = "",
    val userId: String          = "",
    val username: String        = "",
    val items: List<OrderItem>  = emptyList(),
    val address: String         = "",
    val total: Long             = 0L,
    val status: String          = "Chờ xác nhận",
    val createdAt: Double       = 0.0  // Double trong Firestore
)

// ── order items ──────────────────────────────────────────
data class OrderItem(
    val productId: String   = "",
    val productName: String = "",
    val price: String       = "",     // String: "500.000đ"
    val size: String        = "",
    val quantity: Int       = 1
)
