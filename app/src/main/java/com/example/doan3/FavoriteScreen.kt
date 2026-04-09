package com.example.doan3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
    favorites: Set<Int>,
    onToggleFavorite: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    onBack: () -> Unit = {}
) {
    val favoriteProducts = productList.filter { it.id in favorites }

    Scaffold(modifier = modifier, containerColor = SurfaceGray) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("YÊU THÍCH", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                // Count badge
                Box(
                    modifier = Modifier.size(38.dp).background(
                        if (favoriteProducts.isNotEmpty()) AccentRed else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (favoriteProducts.isNotEmpty()) {
                        Text("${favoriteProducts.size}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))

            if (favoriteProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Favorite, null, tint = Color(0xFFEEEEEE), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chưa có sản phẩm yêu thích", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Bấm ❤️ để lưu sản phẩm yêu thích", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favoriteProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isFavorite = true,
                            onFavoriteClick = { onToggleFavorite(product) },
                            onClick = { onProductClick(product) }
                        )
                    }
                }
            }
        }
    }
}
