package com.example.doan3

import androidx.compose.runtime.mutableStateListOf

// ── Shared mutable lists — được Firebase sync realtime ───────────────────────
val productList  = mutableStateListOf<Product>()
val userAccounts = mutableStateListOf<UserAccount>()
val orderList    = mutableStateListOf<Order>()

fun nextProductId() = (productList.maxOfOrNull { it.id } ?: 0) + 1
fun nextUserId()    = (userAccounts.maxOfOrNull { it.id } ?: 0) + 1
fun nextOrderId()   = (orderList.maxOfOrNull { it.id } ?: 0) + 1

// ── User accounts ─────────────────────────────────────────────────────────────
data class UserAccount(
    val id: Int           = 0,
    val username: String  = "",
    val email: String     = "",
    val password: String  = "",
    val role: String      = "user",
    val firestoreId: String = "",
    val avatarUrl: String = ""
)

// ── Orders ────────────────────────────────────────────────────────────────────
data class Order(
    val id: Int               = 0,
    val username: String      = "",
    val items: List<CartItem> = emptyList(),
    val address: String       = "",
    val total: Long           = 0L,
    var status: String        = "Chờ xác nhận",
    // Chờ xác nhận | Đã xác nhận | Đang giao | Đã nhận hàng | Trả hàng | Hoàn thành | Đã hủy
    val firestoreId: String   = ""
)

// ── Reviews ───────────────────────────────────────────────────────────────────
data class Review(
    val id: Int            = 0,
    val productFirestoreId: String = "",
    val username: String   = "",
    val stars: Int         = 5,       // 1–5
    val comment: String    = "",
    val createdAt: Long    = System.currentTimeMillis()
)

val reviewList = androidx.compose.runtime.mutableStateListOf<Review>()
fun nextReviewId() = (reviewList.maxOfOrNull { it.id } ?: 0) + 1
