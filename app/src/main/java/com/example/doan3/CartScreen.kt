
package com.example.doan3

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

// ── Config ────────────────────────────────────────────────────────────────────
object SePayConfig {
    const val ACCOUNT_NO   = "0383562784"
    const val ACCOUNT_NAME = "HUYNH DIEP"
}

// ── Data & utils ──────────────────────────────────────────────────────────────
data class CartItem(val product: Product, val size: String, var quantity: Int)

fun parsePrice(price: String): Long =
    price.replace(".", "").replace("đ", "").replace(",", "").trim().toLongOrNull() ?: 0L

fun formatPrice(amount: Long): String {
    val s = amount.toString()
    val sb = StringBuilder()
    s.reversed().forEachIndexed { i, c -> if (i > 0 && i % 3 == 0) sb.append('.'); sb.append(c) }
    return sb.reverse().toString() + "đ"
}

enum class PaymentMethod { COD, SEPAY }

// ── Generate VietQR offline ───────────────────────────────────────────────────
fun generateVietQRBitmap(amount: Long, description: String): android.graphics.Bitmap? {
    return try {
        val acct = SePayConfig.ACCOUNT_NO
        val amt  = amount.toString()
        val desc = description.take(25).replace(Regex("[^A-Za-z0-9 ]"), "")
        fun pad2(n: Int) = n.toString().padStart(2, '0')
        fun tlv(tag: String, v: String) = "$tag${pad2(v.length)}$v"
        val mai = tlv("38", tlv("00","A000000727") + tlv("01","970422") + tlv("02",acct) + tlv("08","QRIBFTTA"))
        val add = tlv("62", tlv("08", desc))
        val raw = tlv("00","01") + tlv("01","12") + mai + tlv("53","704") + tlv("54",amt) + tlv("58","VN") + add + "6304"
        var crc = 0xFFFF
        for (c in raw) { crc = crc xor (c.code shl 8); repeat(8) { crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1; crc = crc and 0xFFFF } }
        val qrStr = raw + crc.toString(16).uppercase().padStart(4, '0')
        val hints = hashMapOf(com.google.zxing.EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M, com.google.zxing.EncodeHintType.MARGIN to 2)
        val matrix = com.google.zxing.qrcode.QRCodeWriter().encode(qrStr, com.google.zxing.BarcodeFormat.QR_CODE, 600, 600, hints)
        val bmp = android.graphics.Bitmap.createBitmap(600, 600, android.graphics.Bitmap.Config.ARGB_8888)
        for (x in 0 until 600) for (y in 0 until 600) bmp.setPixel(x, y, if (matrix[x,y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        bmp
    } catch (e: Exception) { null }
}

// ═════════════════════════════════════════════════════════════════════════════
// CART SCREEN
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun CartScreen(
    cartItems: MutableList<CartItem>,
    username: String = "khách",
    isLoggedIn: Boolean = false,
    onBack: () -> Unit,
    onRequireLogin: () -> Unit = {}
) {
    var address       by remember { mutableStateOf("") }
    var payMethod     by remember { mutableStateOf(PaymentMethod.COD) }
    var showSuccess   by remember { mutableStateOf(false) }
    var showLogin     by remember { mutableStateOf(false) }
    var showQR        by remember { mutableStateOf(false) }

    // ── Promo code state ──────────────────────────────────────────────────────
    var promoInput    by remember { mutableStateOf("") }
    var appliedPromo  by remember { mutableStateOf<PromoCode?>(null) }
    var promoMessage  by remember { mutableStateOf("") }  // thông báo lỗi hoặc thành công
    var promoSuccess  by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { parsePrice(it.product.price) * it.quantity }
    val discount  = if (appliedPromo != null) {
        val raw = subtotal * appliedPromo!!.discountPercent / 100L
        if (appliedPromo!!.maxDiscount > 0) minOf(raw, appliedPromo!!.maxDiscount) else raw
    } else 0L
    val total    = subtotal - discount

    val orderDesc = "DOAN3 $username ${System.currentTimeMillis() / 1000}"

    fun placeOrder() {
        com.example.doan3.firebase.FirebaseManager.placeOrder(
            Order(id = nextOrderId(), username = username,
                items = cartItems.toList(), address = address, total = total)
        )
        // Tăng usedCount nếu có dùng mã
        val promoFsId = appliedPromo?.firestoreId
        if (!promoFsId.isNullOrEmpty()) {
            com.example.doan3.firebase.FirebaseManager.incrementPromoUsage(promoFsId)
        }
    }

    if (showSuccess) {
        OrderSuccessDialog(payMethod) {
            cartItems.clear(); appliedPromo = null; promoInput = ""
            showSuccess = false; onBack()
        }
    }
    if (showLogin) {
        AlertDialog(onDismissRequest = { showLogin = false }, containerColor = Color.White,
            title = { Text("Yêu cầu đăng nhập", fontWeight = FontWeight.Bold) },
            text  = { Text("Bạn cần đăng nhập để đặt hàng.") },
            confirmButton = { Button(onClick = { showLogin = false; onRequireLogin() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                shape = RoundedCornerShape(10.dp)) { Text("Đăng nhập ngay") } },
            dismissButton = { TextButton(onClick = { showLogin = false }) { Text("Để sau") } })
    }
    if (showQR) {
        SePayQRScreen(total = total, description = orderDesc,
            onPaid = { placeOrder(); showQR = false; showSuccess = true },
            onDismiss = { showQR = false })
        return
    }

    Scaffold(containerColor = SurfaceGray,
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {

                    // ── Địa chỉ ──────────────────────────────────────────────
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .background(Color.White).padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(value = address, onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                            textStyle = TextStyle(fontSize = 14.sp, color = PrimaryBlack),
                            cursorBrush = SolidColor(PrimaryBlack),
                            decorationBox = { inner ->
                                if (address.isEmpty()) Text("Nhập địa chỉ nhận hàng...", color = Color.LightGray, fontSize = 14.sp)
                                inner()
                            })
                    }

                    Spacer(Modifier.height(10.dp))

                    // ── Mã khuyến mãi ─────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = when {
                                        promoSuccess -> Color(0xFF2E7D32)
                                        promoMessage.isNotEmpty() && !promoSuccess -> AccentRed
                                        else -> Color(0xFFE0E0E0)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.LocalOffer, null,
                                tint = if (promoSuccess) Color(0xFF2E7D32) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = promoInput,
                                onValueChange = {
                                    promoInput = it.uppercase().replace(" ", "")
                                    // Reset khi người dùng xóa hoặc thay đổi mã
                                    if (appliedPromo != null && it.uppercase().trim() != appliedPromo!!.code) {
                                        appliedPromo = null; promoMessage = ""; promoSuccess = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = if (promoSuccess) Color(0xFF2E7D32) else PrimaryBlack,
                                    fontWeight = if (promoSuccess) FontWeight.Bold else FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(PrimaryBlack),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (promoInput.isEmpty()) Text("Nhập mã khuyến mãi...", color = Color.LightGray, fontSize = 13.sp)
                                    inner()
                                }
                            )
                            // Nút xóa mã đã áp dụng
                            if (appliedPromo != null) {
                                Spacer(Modifier.width(4.dp))
                                Box(modifier = Modifier.size(20.dp).clickable {
                                    appliedPromo = null; promoInput = ""; promoMessage = ""; promoSuccess = false
                                }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (promoInput.isBlank()) return@Button
                                val found = com.example.doan3.firebase.FirebaseManager.validatePromoCode(promoInput, subtotal)
                                if (found != null) {
                                    appliedPromo = found
                                    promoSuccess = true
                                    promoMessage = "Giảm ${found.discountPercent}%${if (found.maxDiscount > 0) " (tối đa ${formatPrice(found.maxDiscount)})" else ""}"
                                } else {
                                    appliedPromo = null
                                    promoSuccess = false
                                    promoMessage = when {
                                        promoList.none { it.code.equals(promoInput.trim(), true) } -> "Mã không tồn tại"
                                        promoList.any { it.code.equals(promoInput.trim(), true) && !it.active } -> "Mã đã hết hạn"
                                        else -> "Không áp dụng được cho đơn này"
                                    }
                                }
                            },
                            enabled = appliedPromo == null,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (promoSuccess) Color(0xFF2E7D32) else Color(0xFF00897B),
                                disabledContainerColor = Color(0xFF2E7D32)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (promoSuccess) Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp))
                            else Text("Áp dụng", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Thông báo mã (lỗi hoặc thành công)
                    if (promoMessage.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (promoSuccess) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                null,
                                tint = if (promoSuccess) Color(0xFF2E7D32) else AccentRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                promoMessage,
                                fontSize = 12.sp,
                                color = if (promoSuccess) Color(0xFF2E7D32) else AccentRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // ── Phương thức thanh toán ────────────────────────────────
                    Text("Phương thức thanh toán", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PaymentOption("💵", "Tiền mặt\n(COD)", payMethod == PaymentMethod.COD, { payMethod = PaymentMethod.COD }, Modifier.weight(1f))
                        PaymentOption("📱", "Chuyển khoản\n(SePay QR)", payMethod == PaymentMethod.SEPAY, { payMethod = PaymentMethod.SEPAY }, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(10.dp))

                    // ── Tóm tắt giá ──────────────────────────────────────────
                    if (appliedPromo != null) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Tạm tính", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                            Text(formatPrice(subtotal), fontSize = 13.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Giảm giá (${appliedPromo!!.code})", fontSize = 12.sp, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                            Text("−${formatPrice(discount)}", fontSize = 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(4.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                            Text(formatPrice(total), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryBlack)
                        }
                        Button(onClick = {
                            if (cartItems.isEmpty()) return@Button
                            if (!isLoggedIn) { showLogin = true; return@Button }
                            if (address.isBlank()) return@Button
                            when (payMethod) {
                                PaymentMethod.COD -> { placeOrder(); showSuccess = true }
                                PaymentMethod.SEPAY -> showQR = true
                            }
                        }, modifier = Modifier.height(50.dp).widthIn(min = 140.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (payMethod == PaymentMethod.SEPAY) Color(0xFF1A73E8) else PrimaryBlack)) {
                            if (payMethod == PaymentMethod.SEPAY) {
                                Icon(Icons.Filled.QrCode, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Thanh toán QR", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            } else Text("Đặt hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).shadow(4.dp, CircleShape).background(Color.White, CircleShape).clickable { onBack() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                }
                Text("GIỎ HÀNG", fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(Modifier.size(38.dp))
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            if (cartItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        Text("Giỏ hàng trống", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Hãy thêm sản phẩm yêu thích!", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(cartItems, key = { it.product.id }) { item ->
                        CartItemRow(item, onIncrease = { item.quantity++ }, onDecrease = { if (item.quantity > 1) item.quantity-- else cartItems.remove(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOption(icon: String, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))
        .border(if (selected) 2.dp else 1.dp, if (selected) PrimaryBlack else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
        .background(if (selected) PrimaryBlack.copy(alpha = 0.05f) else Color.White)
        .clickable { onClick() }.padding(vertical = 10.dp, horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp); Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) PrimaryBlack else Color.Gray, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SEPAY QR SCREEN — thủ công, không cần API
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun SePayQRScreen(total: Long, description: String, onPaid: () -> Unit, onDismiss: () -> Unit) {
    var isPaid by remember { mutableStateOf(false) }

    LaunchedEffect(isPaid) {
        if (isPaid) { kotlinx.coroutines.delay(1200); onPaid() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
        LazyColumn(modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Back
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(0.1f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Thanh toán chuyển khoản", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                }
            }

            // Status
            item {
                Row(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (isPaid) Color(0xFF2E7D32) else Color.White.copy(0.12f))
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (isPaid) { Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)) }
                    Text(if (isPaid) "✅ Thanh toán thành công!" else "⏳ Chờ xác nhận thanh toán",
                        color = Color.White, fontSize = 13.sp, fontWeight = if (isPaid) FontWeight.Bold else FontWeight.Normal)
                }
            }

            // QR Code
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                        if (isPaid) {
                            Box(Modifier.fillMaxSize().background(Color(0xFF2E7D32), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(72.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Đã xác nhận!", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                }
                            }
                        } else {
                            val qrBitmap = remember(total, description) { generateVietQRBitmap(total, description) }
                            if (qrBitmap != null) {
                                Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "QR",
                                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(10.dp))
                            } else {
                                CircularProgressIndicator(color = Color(0xFF1A73E8))
                            }
                        }
                    }
                }
            }

            // Bank info
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFE8F5E9)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Số tiền", fontSize = 13.sp, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                            Text(formatPrice(total), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF2E7D32))
                        }
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFF8E1)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Nội dung CK", fontSize = 13.sp, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                            Text(description, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFF59E0B))
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        InfoRowLight("Ngân hàng", "MB Bank")
                        InfoRowLight("Số tài khoản", SePayConfig.ACCOUNT_NO)
                        InfoRowLight("Chủ tài khoản", SePayConfig.ACCOUNT_NAME)
                    }
                }
            }

            item {
                Text("Quét QR bằng app ngân hàng · Nhập đúng nội dung CK\nSau khi chuyển xong, bấm nút bên dưới",
                    fontSize = 12.sp, color = Color.White.copy(0.5f), textAlign = TextAlign.Center, lineHeight = 18.sp)
            }

            // Nút xác nhận thủ công
            if (!isPaid) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { isPaid = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                            Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("✅  Tôi đã chuyển khoản xong", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Hủy thanh toán", color = Color.White.copy(0.3f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRowLight(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlack)
    }
}

// ── Cart item row ─────────────────────────────────────────────────────────────
@Composable
fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    var qty by remember { mutableIntStateOf(item.quantity) }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(item.product.color.copy(0.6f)), contentAlignment = Alignment.Center) {
                if (item.product.imageUrl.isNotBlank()) {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(item.product.imageUrl).crossfade(true).build(),
                        contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)))
                } else Text(item.product.name.take(2), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = PrimaryBlack)
                Spacer(Modifier.height(3.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SurfaceGray).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("Size: ${item.size}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(SurfaceGray), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clickable { onDecrease(); if (qty > 1) qty-- }, contentAlignment = Alignment.Center) { Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack) }
                        Text(" $qty ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
                        Box(modifier = Modifier.size(32.dp).clickable { onIncrease(); qty++ }, contentAlignment = Alignment.Center) { Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack) }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(formatPrice(parsePrice(item.product.price) * qty), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PrimaryBlack)
                }
            }
        }
    }
}

// ── Order success ─────────────────────────────────────────────────────────────
@Composable
fun OrderSuccessDialog(paymentMethod: PaymentMethod = PaymentMethod.COD, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🎉", fontSize = 40.sp); Spacer(Modifier.height(8.dp))
                Text("Đặt hàng thành công!", fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
            }
        },
        text = { Text(if (paymentMethod == PaymentMethod.SEPAY) "Đơn hàng chờ xác nhận.\nCảm ơn bạn đã mua sắm!" else "Đơn hàng đã được ghi nhận.\nCảm ơn bạn đã mua sắm!", textAlign = TextAlign.Center, color = Color.Gray, lineHeight = 20.sp) },
        confirmButton = { Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack)) { Text("Tuyệt vời!", fontWeight = FontWeight.Bold) } })
}

// Legacy compat
@Composable fun BankInfoRow(label: String, value: String, highlight: Boolean = false) = InfoRowLight(label, value)
@Composable fun QRInfoRow(label: String, value: String, highlight: Boolean = false) = InfoRowLight(label, value)
@Composable fun QRInfoRowLight(label: String, value: String) = InfoRowLight(label, value)
