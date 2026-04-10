package com.example.doan3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ── Thông tin tài khoản ───────────────────────────────────────────────────────
@Composable
fun UserInfoScreen(
    username: String,
    email: String,
    onBack: () -> Unit
) {
    val user = userAccounts.find { it.username == username }

    Scaffold(containerColor = SurfaceGray) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            ProfileSubTopBar("Thông tin tài khoản", onBack)

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape)
                        .background(PrimaryBlack), contentAlignment = Alignment.Center) {
                        Text(username.take(1).uppercase(), color = Color.White,
                            fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                InfoCard("Tên đăng nhập", username, Icons.Filled.Person)
                InfoCard("Email", email.ifBlank { "Chưa cập nhật" }, Icons.Outlined.MailOutline)
                InfoCard("Vai trò", user?.role ?: "user", Icons.Filled.ManageAccounts)
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceGray),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PrimaryBlack, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 11.sp, color = Color.Gray)
                Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PrimaryBlack)
            }
        }
    }
}

// ── Đơn hàng của user ─────────────────────────────────────────────────────────
@Composable
fun UserOrdersScreen(username: String, onBack: () -> Unit) {
    val myOrders = orderList.filter { it.username == username }
    var detailOrder by remember { mutableStateOf<Order?>(null) }

    if (detailOrder != null) {
        UserOrderDetailDialog(
            order = detailOrder!!,
            onDismiss = { detailOrder = null },
            onCancel = { order ->
                com.example.doan3.firebase.FirebaseManager.updateOrderStatus(order, "Đã hủy")
                detailOrder = null
            },
            onReorder = { order ->
                // Tạo đơn mới từ đơn cũ
                val newOrder = order.copy(id = nextOrderId(), status = "Chờ xác nhận", firestoreId = "")
                com.example.doan3.firebase.FirebaseManager.placeOrder(newOrder)
                detailOrder = null
            }
        )
    }

    Scaffold(containerColor = SurfaceGray) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            ProfileSubTopBar("Đơn hàng của tôi", onBack)
            if (myOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Chưa có đơn hàng nào", color = Color.Gray, fontSize = 15.sp)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()) {
                    items(myOrders, key = { it.id }) { order ->
                        UserOrderRow(order = order, onClick = { detailOrder = order })
                    }
                }
            }
        }
    }
}

@Composable
fun UserOrderRow(order: Order, onClick: () -> Unit) {
    val (statusColor, statusBg) = when (order.status) {
        "Chờ xác nhận" -> Color(0xFFF59E0B) to Color(0xFFFFF8E1)
        "Đang giao"    -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
        "Hoàn thành"   -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        "Đã hủy"       -> AccentRed to Color(0xFFFFEBEE)
        else           -> Color.Gray to SurfaceGray
    }
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(statusBg),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Receipt, null, tint = statusColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Đơn #${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${order.items.size} sản phẩm · ${formatPrice(order.total)}", fontSize = 12.sp, color = Color.Gray)
                if (order.address.isNotBlank())
                    Text(order.address, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusBg)
                .padding(horizontal = 10.dp, vertical = 5.dp)) {
                Text(order.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UserOrderDetailDialog(
    order: Order,
    onDismiss: () -> Unit,
    onCancel: (Order) -> Unit,
    onReorder: (Order) -> Unit
) {
    val canCancel  = order.status == "Chờ xác nhận"
    val canReceive = order.status == "Đang giao"
    val canReturn  = order.status == "Đã nhận hàng" || order.status == "Hoàn thành"
    val canReorder = order.status == "Hoàn thành" || order.status == "Đã hủy" || order.status == "Trả hàng"
    val canReview  = order.status == "Đã nhận hàng" || order.status == "Hoàn thành"

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewProduct by remember { mutableStateOf<CartItem?>(null) }

    if (showReviewDialog && reviewProduct != null) {
        ReviewDialog(
            product = reviewProduct!!.product,
            username = order.username,
            onDismiss = { showReviewDialog = false; reviewProduct = null },
            onSubmit = { stars, comment ->
                com.example.doan3.firebase.FirebaseManager.saveReview(
                    Review(id = nextReviewId(), productFirestoreId = reviewProduct!!.product.firestoreId,
                        username = order.username, stars = stars, comment = comment)
                )
                showReviewDialog = false; reviewProduct = null
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = { Text("Đơn hàng #${order.id}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Status badge
                val (sc, sb) = statusColor(order.status)
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(sb)
                    .padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(order.status, color = sc, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                if (order.address.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(order.address, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                HorizontalDivider()
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ${item.product.name} (${item.size}) ×${item.quantity}",
                            modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Text(formatPrice(parsePrice(item.product.price) * item.quantity),
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider()
                Text("Tổng: ${formatPrice(order.total)}", fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp, color = PrimaryBlack)

                // Action buttons
                if (canReceive) {
                    Button(onClick = { com.example.doan3.firebase.FirebaseManager.updateOrderStatus(order, "Đã nhận hàng") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp)); Text("Xác nhận đã nhận hàng", fontWeight = FontWeight.SemiBold)
                    }
                }
                if (canReview) {
                    OutlinedButton(onClick = { reviewProduct = order.items.firstOrNull(); showReviewDialog = true },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B))) {
                        Icon(Icons.Filled.Star, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp)); Text("Đánh giá sản phẩm", fontWeight = FontWeight.SemiBold)
                    }
                }
                if (canReturn) {
                    OutlinedButton(onClick = { com.example.doan3.firebase.FirebaseManager.updateOrderStatus(order, "Trả hàng") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))) {
                        Icon(Icons.Filled.Undo, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp)); Text("Trả hàng", fontWeight = FontWeight.SemiBold)
                    }
                }
                if (canCancel) {
                    OutlinedButton(onClick = { onCancel(order) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        border = ButtonDefaults.outlinedButtonBorder) {
                        Icon(Icons.Filled.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp)); Text("Hủy đơn hàng", fontWeight = FontWeight.SemiBold)
                    }
                }
                if (canReorder) {
                    Button(onClick = { onReorder(order) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack)) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp)); Text("Mua lại", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

fun statusColor(status: String): Pair<Color, Color> = when (status) {
    "Chờ xác nhận" -> Color(0xFFF59E0B) to Color(0xFFFFF8E1)
    "Đã xác nhận"  -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
    "Đang giao"    -> Color(0xFF6A1B9A) to Color(0xFFF3E5F5)
    "Đã nhận hàng" -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
    "Trả hàng"     -> Color(0xFFE65100) to Color(0xFFFFF3E0)
    "Hoàn thành"   -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
    "Đã hủy"       -> AccentRed to Color(0xFFFFEBEE)
    else           -> Color.Gray to Color(0xFFF5F5F5)
}

@Composable
fun ReviewDialog(
    product: Product,
    username: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var stars by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = { Text("Đánh giá sản phẩm", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                // Star selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { i ->
                        Icon(Icons.Filled.Star, null,
                            tint = if (i < stars) Color(0xFFFFC107) else Color(0xFFDDDDDD),
                            modifier = Modifier.size(32.dp).clickable { stars = i + 1 })
                    }
                }
                Text(when (stars) {
                    1 -> "😞 Rất tệ"; 2 -> "😕 Tệ"; 3 -> "😐 Bình thường"
                    4 -> "😊 Tốt"; else -> "🤩 Xuất sắc!"
                }, fontSize = 13.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = comment, onValueChange = { comment = it },
                    label = { Text("Nhận xét (tuỳ chọn)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlack))
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(stars, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                shape = RoundedCornerShape(10.dp)) { Text("Gửi đánh giá") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

// ── Đổi mật khẩu ─────────────────────────────────────────────────────────────
@Composable
fun ChangePasswordScreen(username: String, onBack: () -> Unit) {
    val user = userAccounts.find { it.username == username }
    var oldPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var confirmPw by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = SurfaceGray) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            ProfileSubTopBar("Đổi mật khẩu", onBack)
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icon
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(SurfaceGray),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Lock, null, tint = PrimaryBlack, modifier = Modifier.size(36.dp))
                    }
                }

                PwField("Mật khẩu cũ", oldPw, { oldPw = it }, oldVisible, { oldVisible = it })
                PwField("Mật khẩu mới", newPw, { newPw = it }, newVisible, { newVisible = it })
                PwField("Nhập lại mật khẩu mới", confirmPw, { confirmPw = it }, confirmVisible, { confirmVisible = it })

                if (errorMsg.isNotEmpty()) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFEBEE))
                        .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(errorMsg, color = AccentRed, fontSize = 13.sp)
                    }
                }
                if (successMsg.isNotEmpty()) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFE8F5E9))
                        .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(successMsg, color = Color(0xFF2E7D32), fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = {
                        errorMsg = ""
                        when {
                            oldPw.isBlank() || newPw.isBlank() || confirmPw.isBlank() ->
                                errorMsg = "Vui lòng điền đầy đủ"
                            newPw.length < 6 -> errorMsg = "Mật khẩu mới phải ít nhất 6 ký tự"
                            newPw != confirmPw -> errorMsg = "Mật khẩu nhập lại không khớp"
                            else -> {
                                isLoading = true
                                scope.launch {
                                    val fsId = user?.firestoreId ?: ""
                                    val ok = com.example.doan3.firebase.FirebaseManager
                                        .changePassword(fsId, oldPw, newPw)
                                    isLoading = false
                                    if (ok) { successMsg = "Đổi mật khẩu thành công!"; oldPw = ""; newPw = ""; confirmPw = "" }
                                    else errorMsg = "Mật khẩu cũ không đúng"
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Xác nhận đổi mật khẩu", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun PwField(label: String, value: String, onValueChange: (String) -> Unit,
            visible: Boolean, onToggle: (Boolean) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) }, singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Color.Gray) },
        trailingIcon = {
            Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                null, tint = Color.Gray, modifier = Modifier.clickable { onToggle(!visible) })
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlack, unfocusedBorderColor = Color.LightGray)
    )
}

// ── Shared top bar ────────────────────────────────────────────────────────────
@Composable
fun ProfileSubTopBar(title: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White)
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape).clickable { onBack() },
            contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
        }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp,
            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.size(38.dp))
    }
    HorizontalDivider(color = Color(0xFFF0F0F0))
}
