package com.example.doan3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Admin color palette ───────────────────────────────────────────────────────
val AdminPurple     = Color(0xFF6C3FC5)
val AdminPurpleLight= Color(0xFFEDE7F6)
val AdminBg         = Color(0xFFF8F7FC)
val AdminCard       = Color.White
val AdminGreen      = Color(0xFF2E7D32)
val AdminOrange     = Color(0xFFE65100)
val AdminBlue       = Color(0xFF1565C0)

// ═════════════════════════════════════════════════════════════════════════════
// ROOT — điều hướng giữa các màn hình admin
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    var screen by remember { mutableStateOf("home") }

    when (screen) {
        "home"     -> AdminHomeScreen(
            onLogout        = onLogout,
            onProducts      = { screen = "products" },
            onOrders        = { screen = "orders" },
            onUsers         = { screen = "users" },
            onInventory     = { screen = "inventory" }
        )
        "products"  -> AdminProductsScreen(onBack = { screen = "home" })
        "orders"    -> AdminOrdersScreen(onBack = { screen = "home" })
        "users"     -> AdminUsersScreen(onBack = { screen = "home" }, onLogout = onLogout)
        "inventory" -> AdminInventoryScreen(onBack = { screen = "home" })
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// HOME — Dashboard
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit,
    onProducts: () -> Unit,
    onOrders: () -> Unit,
    onUsers: () -> Unit,
    onInventory: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi Admin?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Đăng xuất", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") } }
        )
    }

    Scaffold(
        containerColor = AdminBg,
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = AccentRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Đăng xuất", color = AccentRed, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text("HỆ THỐNG ADMIN", fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp, color = AdminPurple)
                    Text("Quản lý toàn bộ cửa hàng", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = AdminPurpleLight, thickness = 2.dp)
                }
            }

            // Stats row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard("Sản phẩm", productList.size.toString(),
                        Icons.Filled.Inventory, AdminPurple, Modifier.weight(1f))
                    AdminStatCard("Đơn hàng", orderList.size.toString(),
                        Icons.Filled.Receipt, AdminBlue, Modifier.weight(1f))
                    AdminStatCard("Người dùng", userAccounts.size.toString(),
                        Icons.Filled.Group, AdminGreen, Modifier.weight(1f))
                }
            }

            // Menu items
            item {
                Text("Quản lý", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            }

            item { AdminMenuItem("Quản lý Sản phẩm", "Thêm, sửa, xóa sản phẩm",
                Icons.Filled.Inventory, AdminPurple, onClick = onProducts) }
            item { AdminMenuItem("Quản lý Đơn hàng", "Xem và cập nhật trạng thái",
                Icons.Filled.Receipt, AdminBlue, onClick = onOrders) }
            item { AdminMenuItem("Quản lý Tồn kho", "Cập nhật số lượng sản phẩm",
                Icons.Filled.Warehouse, AdminOrange, onClick = onInventory) }
            item { AdminMenuItem("Quản lý Người dùng", "Xem và chỉnh sửa tài khoản",
                Icons.Filled.Group, AdminGreen, onClick = onUsers) }
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminCard),
        elevation = CardDefaults.cardElevation(3.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Text(label, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AdminMenuItem(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminCard),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PrimaryBlack)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// PRODUCTS
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun AdminProductsScreen(onBack: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Product?>(null) }
    var deleteTarget by remember { mutableStateOf<Product?>(null) }
    var filterCat by remember { mutableStateOf("Tất cả") }

    val filtered = if (filterCat == "Tất cả") productList
    else productList.filter { it.category == filterCat }

    if (showAddDialog || editTarget != null) {
        ProductFormDialog(
            existing = editTarget,
            onDismiss = { showAddDialog = false; editTarget = null },
            onSave = { p ->
                if (editTarget != null) com.example.doan3.firebase.FirebaseManager.updateProduct(p)
                else com.example.doan3.firebase.FirebaseManager.addProduct(p)
                showAddDialog = false; editTarget = null
            }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Color.White,
            title = { Text("Xóa sản phẩm", fontWeight = FontWeight.Bold) },
            text = { Text("Xóa \"${deleteTarget!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = { com.example.doan3.firebase.FirebaseManager.deleteProduct(deleteTarget!!); deleteTarget = null }) {
                    Text("Xóa", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Hủy") } }
        )
    }

    Scaffold(containerColor = AdminBg,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true },
                containerColor = AdminPurple, contentColor = Color.White,
                shape = CircleShape) {
                Icon(Icons.Filled.Add, "Thêm sản phẩm", modifier = Modifier.size(26.dp))
            }
        }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            AdminSubTopBar("Quản lý Sản phẩm", onBack)
            // Filter chips
            Row(modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { cat ->
                    FilterChip(selected = filterCat == cat, onClick = { filterCat = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdminPurple, selectedLabelColor = Color.White))
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có sản phẩm", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { product ->
                        AdminProductRow(product, onEdit = { editTarget = product }, onDelete = { deleteTarget = product })
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AdminCard),
        elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Ảnh sản phẩm
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                    .background(product.color.copy(0.6f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl).crossfade(true).build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Text(product.name.take(2), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 3.dp)) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        .background(AdminPurpleLight).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(product.category, fontSize = 10.sp, color = AdminPurple, fontWeight = FontWeight.Medium)
                    }
                    Text(product.price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminPurple)
                }
                // Stock badge
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (stockColor, stockBg) = when {
                        product.stock == 0  -> AccentRed to Color(0xFFFFEBEE)
                        product.stock <= 5  -> AdminOrange to Color(0xFFFFF3E0)
                        else                -> AdminGreen to Color(0xFFE8F5E9)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(stockBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(
                            text = if (product.stock == 0) "Hết hàng" else "Còn: ${product.stock}",
                            fontSize = 11.sp, color = stockColor, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Sửa", tint = AdminBlue, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Xóa", tint = AccentRed, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
fun ProductFormDialog(existing: Product?, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var price by remember { mutableStateOf(existing?.price ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "Áo") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var stock by remember { mutableStateOf(existing?.stock?.toString() ?: "0") }
    var error by remember { mutableStateOf("") }
    val colorMap = mapOf("Quần" to Color(0xFFB5A898), "Áo" to Color(0xFFD6CFC7), "Váy/Đầm" to Color(0xFFD4A5A5))

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = { Text(if (existing == null) "Thêm sản phẩm" else "Sửa sản phẩm", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Image preview
                if (imageUrl.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp)
                        .clip(RoundedCornerShape(12.dp)).background(SurfaceGray)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Tên sản phẩm") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPurple))
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("Giá (vd: 250.000đ)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPurple))
                OutlinedTextField(value = stock, onValueChange = { stock = it.filter { c -> c.isDigit() } },
                    label = { Text("Tồn kho") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Inventory, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPurple))
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it },
                    label = { Text("Link ảnh (URL)") },
                    placeholder = { Text("https://...", color = Color.LightGray, fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Image, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPurple))
                Text("Danh mục:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Quần", "Áo", "Váy/Đầm").forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdminPurple, selectedLabelColor = Color.White))
                    }
                }
                if (error.isNotEmpty()) Text(error, color = AccentRed, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || price.isBlank()) { error = "Vui lòng điền đầy đủ"; return@Button }
                onSave(Product(id = existing?.id ?: nextProductId(), name = name.uppercase().trim(),
                    price = price.trim(), category = category,
                    color = colorMap[category] ?: Color(0xFFCCCCCC),
                    firestoreId = existing?.firestoreId ?: "",
                    imageUrl = imageUrl.trim(),
                    stock = stock.toIntOrNull() ?: 0))
            }, colors = ButtonDefaults.buttonColors(containerColor = AdminPurple),
                shape = RoundedCornerShape(10.dp)) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray) } }
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// ORDERS
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun AdminOrdersScreen(onBack: () -> Unit) {
    var filterStatus by remember { mutableStateOf("Tất cả") }
    var detailOrder by remember { mutableStateOf<Order?>(null) }
    val statusOptions = listOf("Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đang giao", "Đã nhận hàng", "Trả hàng", "Hoàn thành", "Đã hủy")
    val filtered = if (filterStatus == "Tất cả") orderList else orderList.filter { it.status == filterStatus }

    if (detailOrder != null) {
        OrderDetailDialog(order = detailOrder!!, onDismiss = { detailOrder = null })
        return
    }

    Scaffold(containerColor = AdminBg) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            AdminSubTopBar("Quản lý Đơn hàng", onBack)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(statusOptions) { s ->
                    FilterChip(selected = filterStatus == s, onClick = { filterStatus = s },
                        label = { Text(s, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdminBlue, selectedLabelColor = Color.White))
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có đơn hàng", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { order ->
                        AdminOrderRow(order, onClick = { detailOrder = order })
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderRow(order: Order, onClick: () -> Unit) {
    val (statusColor, statusBg) = when (order.status) {
        "Chờ xác nhận" -> Color(0xFFF59E0B) to Color(0xFFFFF8E1)
        "Đã xác nhận"  -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
        "Đang giao"    -> Color(0xFF6A1B9A) to Color(0xFFF3E5F5)
        "Đã nhận hàng" -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        "Trả hàng"     -> Color(0xFFE65100) to Color(0xFFFFF3E0)
        "Hoàn thành"   -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        "Đã hủy"       -> AccentRed to Color(0xFFFFEBEE)
        else           -> Color.Gray to Color(0xFFF5F5F5)
    }
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AdminCard),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(AdminBlue.copy(0.12f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Receipt, null, tint = AdminBlue, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("#${order.id} · ${order.username}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${order.items.size} sp · ${formatPrice(order.total)}", fontSize = 12.sp, color = Color.Gray)
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
fun OrderDetailDialog(order: Order, onDismiss: () -> Unit) {
    val statusOptions = listOf("Chờ xác nhận", "Đã xác nhận", "Đang giao", "Đã nhận hàng", "Trả hàng", "Hoàn thành", "Đã hủy")
    var currentStatus by remember { mutableStateOf(order.status) }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = { Text("Đơn hàng #${order.id}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row { Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(order.username, fontSize = 14.sp) }
                Row { Icon(Icons.Outlined.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(order.address.ifBlank { "Chưa có địa chỉ" }, fontSize = 13.sp, color = Color.Gray) }
                HorizontalDivider()
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ${item.product.name} (${item.size}) ×${item.quantity}", modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Text(formatPrice(parsePrice(item.product.price) * item.quantity), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider()
                Text("Tổng: ${formatPrice(order.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AdminPurple)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cập nhật trạng thái:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statusOptions.forEach { s ->
                        FilterChip(selected = currentStatus == s, onClick = {
                            currentStatus = s
                            val idx = orderList.indexOfFirst { it.id == order.id }
                            if (idx >= 0) orderList[idx] = orderList[idx].copy(status = s)
                            com.example.doan3.firebase.FirebaseManager.updateOrderStatus(order, s)
                        }, label = { Text(s, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdminBlue, selectedLabelColor = Color.White))
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Admin có thể hủy đơn
                if (order.status != "Đã hủy" && order.status != "Hoàn thành") {
                    OutlinedButton(
                        onClick = {
                            val idx = orderList.indexOfFirst { it.id == order.id }
                            if (idx >= 0) orderList[idx] = orderList[idx].copy(status = "Đã hủy")
                            com.example.doan3.firebase.FirebaseManager.updateOrderStatus(order, "Đã hủy")
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Hủy đơn", color = AccentRed) }
                }
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AdminPurple),
                    shape = RoundedCornerShape(10.dp)) { Text("Đóng") }
            }
        }
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// INVENTORY — Quản lý tồn kho
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun AdminInventoryScreen(onBack: () -> Unit) {
    var editTarget by remember { mutableStateOf<Product?>(null) }
    var newStock by remember { mutableStateOf("") }

    if (editTarget != null) {
        AlertDialog(
            onDismissRequest = { editTarget = null }, containerColor = Color.White,
            title = { Text("Cập nhật tồn kho", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(editTarget!!.name, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = newStock, onValueChange = { newStock = it },
                        label = { Text("Số lượng tồn kho") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminOrange))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val fsId = editTarget!!.firestoreId
                    if (fsId.isNotEmpty()) {
                        com.example.doan3.firebase.FirebaseManager.updateProductStock(
                            fsId, newStock.toIntOrNull() ?: 0
                        )
                    }
                    editTarget = null; newStock = ""
                }, colors = ButtonDefaults.buttonColors(containerColor = AdminOrange),
                    shape = RoundedCornerShape(10.dp)) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { editTarget = null; newStock = "" }) { Text("Hủy") } }
        )
    }

    Scaffold(containerColor = AdminBg) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            AdminSubTopBar("Quản lý Tồn kho", onBack)
            LazyColumn(contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(productList, key = { it.id }) { product ->
                    Card(shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Ảnh sản phẩm
                            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp))
                                .background(product.color.copy(0.6f)), contentAlignment = Alignment.Center) {
                                if (product.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(product.imageUrl).crossfade(true).build(),
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Text(product.name.take(2), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(product.category, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Tồn kho
                                    val (stockColor, stockBg) = when {
                                        product.stock == 0 -> AccentRed to Color(0xFFFFEBEE)
                                        product.stock <= 5 -> AdminOrange to Color(0xFFFFF3E0)
                                        else -> AdminGreen to Color(0xFFE8F5E9)
                                    }
                                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(stockBg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                        Text(
                                            if (product.stock == 0) "Hết hàng" else "Tồn: ${product.stock}",
                                            fontSize = 11.sp, color = stockColor, fontWeight = FontWeight.Bold
                                        )
                                    }
                                    // Đã bán (tính từ orderList)
                                    val sold = orderList
                                        .filter { it.status != "Đã hủy" }
                                        .sumOf { order -> order.items.filter { it.product.firestoreId == product.firestoreId }.sumOf { it.quantity } }
                                    if (sold > 0) {
                                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                            .background(AdminPurpleLight).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                            Text("Đã bán: $sold", fontSize = 11.sp, color = AdminPurple, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { editTarget = product; newStock = product.stock.toString() }) {
                                Icon(Icons.Filled.Edit, "Sửa", tint = AdminOrange, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// USERS
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun AdminUsersScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    var deleteTarget by remember { mutableStateOf<UserAccount?>(null) }
    var editTarget by remember { mutableStateOf<UserAccount?>(null) }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null }, containerColor = Color.White,
            title = { Text("Xóa tài khoản", fontWeight = FontWeight.Bold) },
            text = { Text("Xóa tài khoản \"${deleteTarget!!.username}\"?") },
            confirmButton = {
                TextButton(onClick = { com.example.doan3.firebase.FirebaseManager.deleteUser(deleteTarget!!); deleteTarget = null }) {
                    Text("Xóa", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Hủy") } }
        )
    }

    if (editTarget != null) {
        UserEditDialog(user = editTarget!!, onDismiss = { editTarget = null }, onSave = { updated ->
            com.example.doan3.firebase.FirebaseManager.updateUser(updated); editTarget = null
        })
    }

    Scaffold(containerColor = AdminBg) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            AdminSubTopBar("Quản lý Người dùng", onBack)
            LazyColumn(contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(userAccounts, key = { it.id }) { user ->
                    Card(shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                                .background(if (user.role == "admin") AdminPurple else AdminGreen),
                                contentAlignment = Alignment.Center) {
                                Text(user.username.take(1).uppercase(), color = Color.White,
                                    fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(if (user.role == "admin") AdminPurple else AdminGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(user.role, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(user.email, fontSize = 12.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { editTarget = user }) {
                                Icon(Icons.Filled.Edit, "Sửa", tint = AdminBlue, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { if (user.role != "admin") deleteTarget = user },
                                enabled = user.role != "admin") {
                                Icon(Icons.Filled.Delete, "Xóa",
                                    tint = if (user.role != "admin") AccentRed else Color.LightGray,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserEditDialog(user: UserAccount, onDismiss: () -> Unit, onSave: (UserAccount) -> Unit) {
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    var role by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = { Text("Sửa tài khoản", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = username, onValueChange = { username = it },
                    label = { Text("Tên đăng nhập") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPurple))
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPurple))
                Text("Vai trò:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("user", "admin").forEach { r ->
                        FilterChip(selected = role == r, onClick = { role = r },
                            label = { Text(r, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdminPurple, selectedLabelColor = Color.White))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(user.copy(username = username.trim(), email = email.trim(), role = role)) },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPurple),
                shape = RoundedCornerShape(10.dp)) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray) } }
    )
}

// ── Shared top bar cho sub-screens ───────────────────────────────────────────
@Composable
fun AdminSubTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(38.dp).shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape).clickable { onBack() },
            contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp), tint = AdminPurple)
        }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp,
            modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = PrimaryBlack)
        Spacer(modifier = Modifier.size(38.dp))
    }
    HorizontalDivider(color = Color(0xFFF0F0F0))
}
