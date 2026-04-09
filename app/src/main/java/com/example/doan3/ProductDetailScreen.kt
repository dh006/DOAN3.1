package com.example.doan3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class ProductColor(val name: String, val color: Color)

@Composable
fun ProductDetailScreen(
    product: Product,
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
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(Icons.Filled.Star, null,
                                tint = if (i < 4) Color(0xFFFFC107) else Color(0xFFDDDDDD),
                                modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("4.0", fontSize = 12.sp, color = Color.Gray)
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
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
