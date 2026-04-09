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
    val firestoreId: String   = ""
)
