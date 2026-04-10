package com.example.doan3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class ProductColor(val name: String, val color: Color)

@Composable
fun ProductDetailScreen(
    product: Product,
    currentUsername: String = "",
    onBack: () -> Unit,
    onAddToCart: (Product, String) -> Unit = { _, _ -> }
) {
    val colorOptions = listOf(
        ProductColor("Đen",      Color(0xFF2B2B2B)),
        ProductColor("Xanh rêu", Color(0xFF5A7A6A)),
        ProductColor("Nâu",      Color(0xFF8B6347)),
    )
    val sizes = listOf("S", "M", "L", "XL")

    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var selectedSize by remember { mutableStateOf("S") }
    var isFavorite by remember { mutableStateOf(false) }
    var addedToCart by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wishlist button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .clickable { isFavorite = !isFavorite },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) AccentRed else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Add to cart button
                    Button(
                        onClick = {
                            if (product.stock > 0) {
                                onAddToCart(product, selectedSize)
                                addedToCart = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = product.stock > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (product.stock > 0) PrimaryBlack else Color.Gray
                        )
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when {
                                product.stock == 0 -> "Hết hàng"
                                addedToCart -> "Đã thêm ✓"
                                else -> "Thêm vào giỏ"
                            },
                            fontSize = 15.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                }
                Text(
                    "THÔNG TIN SẢN PHẨM",
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(38.dp))
            }

            // ── Product image ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colorOptions[selectedColorIndex].color.copy(alpha = 0.25f),
                                colorOptions[selectedColorIndex].color.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl).crossfade(true).build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.size(180.dp).shadow(8.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(colorOptions[selectedColorIndex].color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(product.name.take(2), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                // Category badge
                Box(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                        .clip(RoundedCornerShape(8.dp)).background(PrimaryBlack)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(product.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Stock warning ─────────────────────────────────────────────────
            if (product.stock in 1..5) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFF3E0))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = AdminOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chỉ còn ${product.stock} sản phẩm!", fontSize = 13.sp, color = AdminOrange, fontWeight = FontWeight.SemiBold)
                }
            } else if (product.stock == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFEBEE))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sản phẩm đã hết hàng", fontSize = 13.sp, color = AccentRed, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // ── Name & price ──────────────────────────────────────────────
                Text(product.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PrimaryBlack)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.price, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = PrimaryBlack)
                    Spacer(modifier = Modifier.weight(1f))
                    // Rating thật từ reviews
                    val productReviews = reviewList.filter { it.productFirestoreId == product.firestoreId }
                    val avgStars = if (productReviews.isEmpty()) 0f
                    else productReviews.sumOf { it.stars }.toFloat() / productReviews.size
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(Icons.Filled.Star, null,
                                tint = if (i < avgStars.toInt()) Color(0xFFFFC107) else Color(0xFFDDDDDD),
                                modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (productReviews.isEmpty()) "Chưa có đánh giá"
                            else "${"%.1f".format(avgStars)} (${productReviews.size})",
                            fontSize = 12.sp, color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(20.dp))

                // ── Color selector ────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Màu sắc", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(colorOptions[selectedColorIndex].name, fontSize = 13.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorOptions.forEachIndexed { index, pc ->
                        val isSelected = selectedColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(pc.color)
                                .border(if (isSelected) 3.dp else 0.dp, Color.White, CircleShape)
                                .clickable { selectedColorIndex = index }
                        ) {
                            if (isSelected) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Size selector ─────────────────────────────────────────────
                Text("Kích thước", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    sizes.forEach { size ->
                        val isSelected = selectedSize == size
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrimaryBlack else Color.White)
                                .border(1.dp, if (isSelected) PrimaryBlack else Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                .clickable { selectedSize = size },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(size, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                color = if (isSelected) Color.White else PrimaryBlack)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(20.dp))

                // ── Description ───────────────────────────────────────────────
                Text("Mô tả sản phẩm", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sản phẩm được làm từ chất liệu cao cấp, thoáng mát và bền đẹp. " +
                    "Thiết kế hiện đại, phù hợp với nhiều phong cách thời trang. " +
                    "Dễ dàng kết hợp với nhiều trang phục khác nhau. " +
                    "Phù hợp cho cả đi làm, đi chơi và các hoạt động hàng ngày.",
                    fontSize = 14.sp, color = Color(0xFF666666), lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(20.dp))

                // ── Reviews section ───────────────────────────────────────────
                ReviewSection(
                    product = product,
                    currentUsername = currentUsername
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun ReviewSection(product: Product, currentUsername: String) {
    val productReviews = reviewList.filter { it.productFirestoreId == product.firestoreId }
    val avgStars = if (productReviews.isEmpty()) 0f
    else productReviews.sumOf { it.stars }.toFloat() / productReviews.size

    // Tỉ lệ từng sao
    val starCounts = (5 downTo 1).map { star ->
        star to productReviews.count { it.stars == star }
    }

    var showWriteReview by remember { mutableStateOf(false) }
    var newStars by remember { mutableIntStateOf(5) }
    var newComment by remember { mutableStateOf("") }
    val alreadyReviewed = productReviews.any { it.username == currentUsername }

    Column {
        // ── Header ────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Đánh giá & Bình luận", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            if (productReviews.isNotEmpty()) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFF8E1)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("${productReviews.size} đánh giá", fontSize = 11.sp, color = Color(0xFFF59E0B))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (productReviews.isNotEmpty()) {
            // ── Tổng quan rating ──────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Điểm trung bình lớn
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(80.dp)) {
                    Text("${"%.1f".format(avgStars)}", fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp, color = PrimaryBlack)
                    Row {
                        repeat(5) { i ->
                            Icon(Icons.Filled.Star, null,
                                tint = if (i < avgStars) Color(0xFFFFC107) else Color(0xFFDDDDDD),
                                modifier = Modifier.size(14.dp))
                        }
                    }
                    Text("${productReviews.size} đánh giá", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Thanh tỉ lệ từng sao
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    starCounts.forEach { (star, count) ->
                        val ratio = if (productReviews.isEmpty()) 0f else count.toFloat() / productReviews.size
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$star", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(12.dp))
                            Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFEEEEEE))) {
                                Box(modifier = Modifier.fillMaxHeight()
                                    .fillMaxWidth(ratio).background(Color(0xFFFFC107), RoundedCornerShape(3.dp)))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$count", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Form viết đánh giá ────────────────────────────────────────────────
        if (currentUsername.isBlank()) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(SurfaceGray).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Đăng nhập để đánh giá sản phẩm", fontSize = 13.sp, color = Color.Gray)
            }
        } else if (alreadyReviewed) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F5E9)).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bạn đã đánh giá sản phẩm này", fontSize = 13.sp, color = Color(0xFF2E7D32))
                }
            }
        } else {
            // Nút mở form
            if (!showWriteReview) {
                OutlinedButton(
                    onClick = { showWriteReview = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlack)
                ) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Viết đánh giá", fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Form đánh giá
                Card(shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Đánh giá của bạn", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        // Star picker
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(5) { i ->
                                Icon(Icons.Filled.Star, null,
                                    tint = if (i < newStars) Color(0xFFFFC107) else Color(0xFFDDDDDD),
                                    modifier = Modifier.size(36.dp).clickable { newStars = i + 1 })
                            }
                        }
                        Text(when (newStars) {
                            1 -> "😞 Rất tệ"; 2 -> "😕 Tệ"; 3 -> "😐 Bình thường"
                            4 -> "😊 Tốt"; else -> "🤩 Xuất sắc!"
                        }, fontSize = 13.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = newComment, onValueChange = { newComment = it },
                            placeholder = { Text("Chia sẻ cảm nhận của bạn về sản phẩm...", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlack, unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showWriteReview = false; newComment = ""; newStars = 5 },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                                Text("Hủy")
                            }
                            Button(
                                onClick = {
                                    com.example.doan3.firebase.FirebaseManager.saveReview(
                                        Review(id = nextReviewId(),
                                            productFirestoreId = product.firestoreId,
                                            username = currentUsername,
                                            stars = newStars, comment = newComment.trim())
                                    )
                                    showWriteReview = false; newComment = ""; newStars = 5
                                },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack)
                            ) { Text("Gửi đánh giá", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Danh sách bình luận ───────────────────────────────────────────────
        if (productReviews.isNotEmpty()) {
            productReviews.reversed().forEach { review ->
                val reviewer = userAccounts.find { it.username == review.username }
                Card(shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(PrimaryBlack), contentAlignment = Alignment.Center) {
                                if (!reviewer?.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(reviewer!!.avatarUrl).crossfade(true).build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Text(review.username.take(1).uppercase(), color = Color.White,
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(review.username, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row {
                                    repeat(5) { i ->
                                        Icon(Icons.Filled.Star, null,
                                            tint = if (i < review.stars) Color(0xFFFFC107) else Color(0xFFDDDDDD),
                                            modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            // Highlight nếu là review của mình
                            if (review.username == currentUsername) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF0F0F0)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("Của bạn", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                        if (review.comment.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(review.comment, fontSize = 13.sp, color = Color(0xFF444444), lineHeight = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
