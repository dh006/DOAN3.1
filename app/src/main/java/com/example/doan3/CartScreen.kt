package com.example.doan3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CartItem(
    val product: Product,
    val size: String,
    var quantity: Int
)

fun parsePrice(price: String): Long =
    price.replace(".", "").replace("đ", "").replace(",", "").trim().toLongOrNull() ?: 0L

fun formatPrice(amount: Long): String {
    val s = amount.toString()
    val result = StringBuilder()
    s.reversed().forEachIndexed { i, c ->
        if (i > 0 && i % 3 == 0) result.append('.')
        result.append(c)
    }
    return result.reverse().toString() + "đ"
}

@Composable
fun CartScreen(
    cartItems: MutableList<CartItem>,
    username: String = "khách",
    isLoggedIn: Boolean = false,
    onBack: () -> Unit,
    onRequireLogin: () -> Unit = {}
) {
    var address by remember { mutableStateOf("") }
    var showOrderSuccess by remember { mutableStateOf(false) }
    var showLoginRequired by remember { mutableStateOf(false) }
    val total = cartItems.sumOf { parsePrice(it.product.price) * it.quantity }

    if (showOrderSuccess) {
        OrderSuccessDialog(onDismiss = { cartItems.clear(); showOrderSuccess = false; onBack() })
    }

    if (showLoginRequired) {
        AlertDialog(
            onDismissRequest = { showLoginRequired = false },
            containerColor = Color.White,
            title = { Text("Yêu cầu đăng nhập", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn cần đăng nhập để đặt hàng.") },
            confirmButton = {
                Button(onClick = { showLoginRequired = false; onRequireLogin() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                    shape = RoundedCornerShape(10.dp)) {
                    Text("Đăng nhập ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginRequired = false }) { Text("Để sau", color = Color.Gray) }
            }
        )
    }

    Scaffold(
        containerColor = SurfaceGray,
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    // Address field with border
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Outlined.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                            textStyle = TextStyle(fontSize = 14.sp, color = PrimaryBlack),
                            cursorBrush = SolidColor(PrimaryBlack),
                            decorationBox = { inner ->
                                if (address.isEmpty()) Text("Nhập địa chỉ nhận hàng...", color = Color.LightGray, fontSize = 14.sp)
                                inner()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Total + order button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                            Text(formatPrice(total), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryBlack)
                        }
                        Button(
                            onClick = {
                                if (cartItems.isEmpty()) return@Button
                                if (!isLoggedIn) { showLoginRequired = true; return@Button }
                                com.example.doan3.firebase.FirebaseManager.placeOrder(
                                    Order(id = nextOrderId(), username = username,
                                        items = cartItems.toList(), address = address, total = total)
                                )
                                showOrderSuccess = true
                            },
                            modifier = Modifier.height(50.dp).widthIn(min = 160.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack)
                        ) {
                            Text("Đặt hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape).clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                }
                Text("GIỎ HÀNG", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Box(modifier = Modifier.size(38.dp)) // spacer
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))

            if (cartItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Giỏ hàng trống", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Hãy thêm sản phẩm yêu thích!", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cartItems, key = { it.product.id }) { item ->
                        CartItemRow(
                            item = item,
                            onIncrease = { item.quantity++ },
                            onDecrease = { if (item.quantity > 1) item.quantity-- else cartItems.remove(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    var qty by remember { mutableIntStateOf(item.quantity) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image placeholder with gradient
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(
                        listOf(item.product.color.copy(alpha = 0.9f), item.product.color.copy(alpha = 0.5f))
                    )),
                contentAlignment = Alignment.Center
            ) {
                Text(item.product.name.take(2), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, color = PrimaryBlack)
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        .background(SurfaceGray).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("Size: ${item.size}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Qty controls
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(SurfaceGray),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(32.dp).clickable { onDecrease(); if (qty > 1) qty-- },
                            contentAlignment = Alignment.Center) {
                            Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
                        }
                        Text(" $qty ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
                        Box(modifier = Modifier.size(32.dp).clickable { onIncrease(); qty++ },
                            contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(formatPrice(parsePrice(item.product.price) * qty),
                        fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PrimaryBlack)
                }
            }
        }
    }
}

@Composable
fun OrderSuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🎉", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Đặt hàng thành công!", fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
            }
        },
        text = { Text("Đơn hàng của bạn đã được ghi nhận.\nCảm ơn bạn đã mua sắm!", textAlign = TextAlign.Center, color = Color.Gray) },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack)) {
                Text("Tuyệt vời!", fontWeight = FontWeight.Bold)
            }
        }
    )
}
