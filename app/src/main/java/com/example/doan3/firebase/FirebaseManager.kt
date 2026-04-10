package com.example.doan3.firebase

import androidx.compose.ui.graphics.Color
import com.example.doan3.CartItem
import com.example.doan3.Order
import com.example.doan3.Product
import com.example.doan3.Review
import com.example.doan3.UserAccount
import com.example.doan3.orderList
import com.example.doan3.productList
import com.example.doan3.reviewList
import com.example.doan3.userAccounts
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

object FirebaseManager {

    private val db = Firebase.firestore
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init() {
        listenProducts()
        listenUsers()
        listenOrders()
        listenReviews()
    }

    // ---------------- PRODUCTS ----------------

    private fun listenProducts() {
        db.collection("products").addSnapshotListener { snap, err ->
            if (err != null) {
                android.util.Log.e("FirebaseManager", "listenProducts error: ${err.message}")
                return@addSnapshotListener
            }
            snap ?: return@addSnapshotListener
            val list = snap.documents.mapNotNull { doc ->
                try {
                    Product(
                        id          = doc.id.hashCode(),
                        name        = doc.getString("name") ?: "",
                        price       = doc.getString("price") ?: "",
                        category    = doc.getString("category") ?: "",
                        color       = parseColor(doc.getString("colorHex") ?: "#CCCCCC"),
                        firestoreId = doc.id,
                        imageUrl    = doc.getString("imageUrl") ?: "",
                        stock       = (doc.getLong("stock") ?: 0L).toInt()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "parse product error: ${e.message}")
                    null
                }
            }
            productList.clear()
            productList.addAll(list)
        }
    }

    fun addProduct(product: Product, onDone: () -> Unit = {}) = scope.launch {
        val model = ProductModel(
            name = product.name,
            price = product.price,
            category = product.category,
            colorHex = colorToHex(product.color),
            imageUrl = product.imageUrl,
            stock = product.stock,
            createdAt = System.currentTimeMillis().toDouble()
        )

        val ref = db.collection("products").add(model).await()

        db.collection("products")
            .document(ref.id)
            .update("id", ref.id)
            .await()

        onDone()
    }

    fun updateProduct(product: Product, onDone: () -> Unit = {}) = scope.launch {

        val fsId = product.firestoreId
        if (fsId.isEmpty()) return@launch

        db.collection("products")
            .document(fsId)
            .update(
                mapOf(
                    "name" to product.name,
                    "price" to product.price,
                    "category" to product.category,
                    "colorHex" to colorToHex(product.color),
                    "imageUrl" to product.imageUrl,
                    "stock" to product.stock
                )
            )
            .await()

        onDone()
    }

    fun deleteProduct(product: Product, onDone: () -> Unit = {}) = scope.launch {

        val fsId = product.firestoreId
        if (fsId.isEmpty()) return@launch

        db.collection("products")
            .document(fsId)
            .delete()
            .await()

        onDone()
    }

    // ---------------- USERS ----------------

    private fun listenUsers() {
        db.collection("users").addSnapshotListener { snap, err ->
            if (err != null) {
                android.util.Log.e("FirebaseManager", "listenUsers error: ${err.message}")
                return@addSnapshotListener
            }
            snap ?: return@addSnapshotListener
            val list = snap.documents.mapNotNull { doc ->
                try {
                    UserAccount(
                        id          = doc.id.hashCode(),
                        username    = doc.getString("username") ?: "",
                        email       = doc.getString("email") ?: "",
                        password    = "",
                        role        = doc.getString("role") ?: "user",
                        firestoreId = doc.id,
                        avatarUrl   = doc.getString("avatarUrl") ?: ""
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "parse user error: ${e.message}")
                    null
                }
            }
            userAccounts.clear()
            userAccounts.addAll(list)
        }
    }

    suspend fun login(username: String, password: String): UserAccount? {
        return try {
            withTimeout(10_000) {
                val snap = db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1).get().await()
                val doc = snap.documents.firstOrNull() ?: return@withTimeout null
                val storedPassword = doc.getString("password") ?: return@withTimeout null
                if (storedPassword != password) return@withTimeout null
                UserAccount(
                    id          = doc.id.hashCode(),
                    username    = doc.getString("username") ?: "",
                    email       = doc.getString("email") ?: "",
                    password    = password,
                    role        = doc.getString("role") ?: "user",
                    firestoreId = doc.id
                )
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.e("FirebaseManager", "Timeout login — kiểm tra Firestore Rules!")
            null
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Lỗi login: ${e.message}", e)
            null
        }
    }

    // Trả về null = thành công, String = thông báo lỗi cụ thể
    suspend fun register(
        username: String,
        email: String,
        phone: String,
        password: String
    ): String? {
        return try {
            withTimeout(10_000) {
                android.util.Log.d("FirebaseManager", "Bắt đầu đăng ký: $username")

                // Kiểm tra username trùng
                val usernameSnap = db.collection("users")
                    .whereEqualTo("username", username).limit(1).get().await()
                if (!usernameSnap.isEmpty) return@withTimeout "Tên đăng nhập đã được sử dụng"

                // Kiểm tra email trùng
                val emailSnap = db.collection("users")
                    .whereEqualTo("email", email).limit(1).get().await()
                if (!emailSnap.isEmpty) return@withTimeout "Email này đã được đăng ký"

                val model = mapOf(
                    "username"  to username,
                    "email"     to email,
                    "phone"     to phone,
                    "password"  to password,
                    "role"      to "user",
                    "avatarUrl" to "",
                    "createdAt" to System.currentTimeMillis().toDouble(),
                    "id"        to ""
                )
                val ref = db.collection("users").add(model).await()
                db.collection("users").document(ref.id).update("id", ref.id).await()
                android.util.Log.d("FirebaseManager", "Đăng ký thành công: ${ref.id}")
                null // thành công
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.e("FirebaseManager", "Timeout đăng ký — kiểm tra Firestore Rules!")
            "Hết thời gian kết nối — kiểm tra mạng và Firestore Rules"
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Lỗi đăng ký: ${e.message}", e)
            "Lỗi kết nối: ${e.message}"
        }
    }

    fun updateAvatar(firestoreId: String, avatarUrl: String) = scope.launch {
        try {
            db.collection("users").document(firestoreId).update("avatarUrl", avatarUrl).await()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "updateAvatar error: ${e.message}")
        }
    }

    fun updateUser(user: UserAccount, onDone: () -> Unit = {}) = scope.launch {
        val fsId = user.firestoreId
        if (fsId.isEmpty()) return@launch

        db.collection("users")
            .document(fsId)
            .update(
                mapOf(
                    "username" to user.username,
                    "email" to user.email,
                    "role" to user.role
                )
            )
            .await()

        onDone()
    }

    fun deleteUser(user: UserAccount, onDone: () -> Unit = {}) = scope.launch {

        val fsId = user.firestoreId
        if (fsId.isEmpty()) return@launch

        db.collection("users")
            .document(fsId)
            .delete()
            .await()

        onDone()
    }

    // ---------------- ORDERS ----------------

    private fun listenOrders() {
        db.collection("orders").addSnapshotListener { snap, err ->
            if (err != null) {
                android.util.Log.e("FirebaseManager", "listenOrders error: ${err.message}")
                return@addSnapshotListener
            }
            snap ?: return@addSnapshotListener
            val list = snap.documents.mapNotNull { doc ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    val rawItems = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val items = rawItems.map { item ->
                        CartItem(
                            product = Product(
                                id          = 0,
                                name        = item["productName"] as? String ?: "",
                                price       = item["price"] as? String ?: "",
                                category    = "",
                                color       = Color.Gray,
                                firestoreId = item["productId"] as? String ?: ""
                            ),
                            size     = item["size"] as? String ?: "",
                            quantity = (item["quantity"] as? Long)?.toInt() ?: 1
                        )
                    }
                    Order(
                        id          = doc.id.hashCode(),
                        username    = doc.getString("username") ?: "",
                        items       = items,
                        address     = doc.getString("address") ?: "",
                        total       = doc.getLong("total") ?: 0L,
                        status      = doc.getString("status") ?: "Chờ xác nhận",
                        firestoreId = doc.id
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "parse order error: ${e.message}")
                    null
                }
            }
            orderList.clear()
            orderList.addAll(list)
        }
    }

    fun placeOrder(order: Order, onDone: () -> Unit = {}) = scope.launch {
        val model = OrderModel(
            username = order.username,
            items = order.items.map { ci ->
                OrderItem(productId = ci.product.firestoreId, productName = ci.product.name,
                    price = ci.product.price, size = ci.size, quantity = ci.quantity)
            },
            address = order.address, total = order.total, status = order.status
        )
        val ref = db.collection("orders").add(model).await()
        db.collection("orders").document(ref.id).update("id", ref.id).await()
        // Trừ tồn kho
        order.items.forEach { ci ->
            if (ci.product.firestoreId.isNotEmpty()) decreaseStock(ci.product.firestoreId, ci.quantity)
        }
        onDone()
    }

    fun updateProductStock(firestoreId: String, stock: Int) = scope.launch {
        try {
            db.collection("products").document(firestoreId).update("stock", stock).await()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "updateStock error: ${e.message}")
        }
    }

    fun decreaseStock(firestoreId: String, qty: Int) = scope.launch {
        try {
            val doc = db.collection("products").document(firestoreId).get().await()
            val current = (doc.getLong("stock") ?: 0L).toInt()
            val newStock = maxOf(0, current - qty)
            db.collection("products").document(firestoreId).update("stock", newStock).await()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "decreaseStock error: ${e.message}")
        }
    }

    fun increaseStock(firestoreId: String, qty: Int) = scope.launch {
        try {
            val doc = db.collection("products").document(firestoreId).get().await()
            val current = (doc.getLong("stock") ?: 0L).toInt()
            db.collection("products").document(firestoreId).update("stock", current + qty).await()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "increaseStock error: ${e.message}")
        }
    }

    suspend fun changePassword(firestoreId: String, oldPassword: String, newPassword: String): Boolean {
        return try {
            val doc = db.collection("users").document(firestoreId).get().await()
            val stored = doc.getString("password") ?: return false
            if (stored != oldPassword) return false
            db.collection("users").document(firestoreId).update("password", newPassword).await()
            true
        } catch (e: Exception) { false }
    }

    fun saveReview(review: Review) = scope.launch {
        try {
            val model = mapOf(
                "productFirestoreId" to review.productFirestoreId,
                "username"  to review.username,
                "stars"     to review.stars,
                "comment"   to review.comment,
                "createdAt" to review.createdAt.toDouble()
            )
            val ref = db.collection("reviews").add(model).await()
            db.collection("reviews").document(ref.id).update("id", ref.id).await()
            // Cập nhật local
            reviewList.add(review.copy(id = ref.id.hashCode()))
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "saveReview error: ${e.message}")
        }
    }

    fun listenReviews() {
        db.collection("reviews").addSnapshotListener { snap, _ ->
            snap ?: return@addSnapshotListener
            val list = snap.documents.mapNotNull { doc ->
                try {
                    Review(
                        id = doc.id.hashCode(),
                        productFirestoreId = doc.getString("productFirestoreId") ?: "",
                        username  = doc.getString("username") ?: "",
                        stars     = (doc.getLong("stars") ?: 5L).toInt(),
                        comment   = doc.getString("comment") ?: "",
                        createdAt = (doc.getDouble("createdAt") ?: 0.0).toLong()
                    )
                } catch (e: Exception) { null }
            }
            reviewList.clear()
            reviewList.addAll(list)
        }
    }

    fun updateOrderStatus(order: Order, status: String, onDone: () -> Unit = {}) = scope.launch {        val fsId = order.firestoreId
        if (fsId.isEmpty()) return@launch
        db.collection("orders").document(fsId).update("status", status).await()
        if (status == "Đã hủy" || status == "Trả hàng") {
            order.items.forEach { ci ->
                if (ci.product.firestoreId.isNotEmpty()) increaseStock(ci.product.firestoreId, ci.quantity)
            }
        }
        onDone()
    }

    // ---------------- HELPERS ----------------

    private fun parseColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFFCCCCCC)
        }
    }

    private fun colorToHex(color: Color): String {

        val argb = color.value.toLong()

        val r = (argb shr 16 and 0xFF).toInt()
        val g = (argb shr 8 and 0xFF).toInt()
        val b = (argb and 0xFF).toInt()

        return "#%02X%02X%02X".format(r, g, b)
    }
}