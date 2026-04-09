package com.example.doan3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doan3.ui.theme.DOAN3Theme

data class Product(
    val id: Int,
    val name: String,
    val price: String,
    val category: String,
    val color: Color,
    val firestoreId: String = "",
    val imageUrl: String = "",
    val stock: Int = 0
)

val sampleProducts get() = productList
val categories = listOf("Tất cả", "Quần", "Áo", "Váy/Đầm")

// Màu chủ đạo
val PrimaryBlack = Color(0xFF1A1A1A)
val SurfaceGray  = Color(0xFFF5F5F5)
val AccentRed    = Color(0xFFE53935)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try { com.example.doan3.firebase.FirebaseManager.init() }
        catch (e: Exception) { android.util.Log.e("MainActivity", "Firebase init error: ${e.message}", e) }
        setContent { DOAN3Theme { ShopApp() } }
    }
}

@Composable
fun ShopApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var showCart by remember { mutableStateOf(false) }
    var showAdmin by remember { mutableStateOf(false) }

    // Auth state — dùng chung toàn app
    var currentUsername by remember { mutableStateOf("") }   // "" = chưa đăng nhập
    var currentEmail by remember { mutableStateOf("") }

    val cartItems = remember { mutableStateListOf<CartItem>() }
    val favorites = remember { mutableStateSetOf<Int>() }

    val isLoggedIn = currentUsername.isNotEmpty()

    fun addToCart(product: Product, size: String) {
        val existing = cartItems.find { it.product.id == product.id && it.size == size }
        if (existing != null) existing.quantity++ else cartItems.add(CartItem(product, size, 1))
    }
    fun toggleFavorite(product: Product) {
        if (product.id in favorites) favorites.remove(product.id) else favorites.add(product.id)
    }

    // Admin overlay — full screen, không có bottom nav
    if (showAdmin) { AdminScreen(onLogout = { showAdmin = false }); return }

    // Product detail overlay
    if (selectedProduct != null) {
        ProductDetailScreen(
            product = selectedProduct!!,
            onBack = { selectedProduct = null },
            onAddToCart = { product, size -> addToCart(product, size); selectedProduct = null }
        )
        return
    }

    // Search overlay
    if (showSearch) {
        SearchScreen(onBack = { showSearch = false }, onProductClick = { showSearch = false; selectedProduct = it })
        return
    }

    // Cart overlay
    if (showCart) {
        CartScreen(
            cartItems = cartItems,
            username = currentUsername,
            isLoggedIn = isLoggedIn,
            onBack = { showCart = false },
            onRequireLogin = { showCart = false; selectedTab = 3 }
        )
        return
    }

    // Main scaffold với bottom nav luôn hiển thị
    Scaffold(
        bottomBar = {
            BottomNavBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        },
        containerColor = SurfaceGray
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onProductClick = { selectedProduct = it },
                onSearchClick = { showSearch = true },
                onCartClick = { showCart = true },
                cartCount = cartItems.sumOf { it.quantity },
                favorites = favorites,
                onToggleFavorite = { toggleFavorite(it) }
            )
            1 -> FavoriteScreen(
                modifier = Modifier.padding(innerPadding),
                favorites = favorites,
                onToggleFavorite = { toggleFavorite(it) },
                onProductClick = { selectedProduct = it }
            )
            2 -> PlaceholderScreen("Đơn hàng", Icons.Outlined.List, Modifier.padding(innerPadding))
            3 -> AccountTabScreen(
                modifier = Modifier.padding(innerPadding),
                isLoggedIn = isLoggedIn,
                username = currentUsername,
                email = currentEmail,
                onLoginSuccess = { user, email, isAdmin ->
                    if (isAdmin) { showAdmin = true }
                    else { currentUsername = user; currentEmail = email }
                },
                onLogout = { currentUsername = ""; currentEmail = "" }
            )
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onProductClick: (Product) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    cartCount: Int = 0,
    favorites: Set<Int> = emptySet(),
    onToggleFavorite: (Product) -> Unit = {}
) {
    val filtered = if (selectedCategory == "Tất cả") sampleProducts
    else sampleProducts.filter { it.category == selectedCategory }

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(onSearchClick = onSearchClick, onCartClick = onCartClick, cartCount = cartCount)
        CategoryTabs(selectedCategory = selectedCategory, onCategorySelected = onCategorySelected)
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Đang tải sản phẩm...", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) },
                        isFavorite = product.id in favorites,
                        onFavoriteClick = { onToggleFavorite(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(onSearchClick: () -> Unit = {}, onCartClick: () -> Unit = {}, cartCount: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Khám phá", fontSize = 12.sp, color = Color.Gray)
            Text("Shop Thời Trang", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PrimaryBlack)
        }
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Default.Search, "Tìm kiếm", modifier = Modifier.size(24.dp), tint = PrimaryBlack)
        }
        Box {
            IconButton(onClick = onCartClick) {
                Icon(Icons.Outlined.ShoppingCart, "Giỏ hàng", modifier = Modifier.size(24.dp), tint = PrimaryBlack)
            }
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(AccentRed, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (cartCount > 9) "9+" else cartCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
}

@Composable
fun CategoryTabs(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) PrimaryBlack else Color.Transparent)
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF0F0F0))
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit = {},
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(product.color.copy(alpha = 0.9f), product.color.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Ảnh sản phẩm
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl).crossfade(true).build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Icon(Icons.Outlined.Person, null, tint = Color.White.copy(alpha = 0.35f), modifier = Modifier.size(72.dp))
                }
                // Category badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(product.category, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                // Placeholder icon
                Icon(Icons.Outlined.Person, null, tint = Color.White.copy(alpha = 0.35f), modifier = Modifier.size(72.dp))
                // Heart button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) AccentRed else Color.Gray,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = PrimaryBlack)
                Spacer(modifier = Modifier.height(3.dp))
                Text(product.price, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PrimaryBlack)
            }
        }
    }
}

@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    data class NavItem(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)
    val items = listOf(
        NavItem("Trang chủ", Icons.Outlined.Home, Icons.Filled.Home),
        NavItem("Yêu thích", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        NavItem("Đơn hàng", Icons.Outlined.List, Icons.Filled.List),
        NavItem("Tài khoản", Icons.Outlined.Person, Icons.Filled.Person),
    )
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp,
        modifier = Modifier.shadow(12.dp)) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(if (selectedTab == index) item.selectedIcon else item.icon, item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlack, selectedTextColor = PrimaryBlack,
                    unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFF0F0F0)
                )
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(64.dp), tint = Color(0xFFDDDDDD))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.LightGray, fontSize = 16.sp)
        }
    }
}
