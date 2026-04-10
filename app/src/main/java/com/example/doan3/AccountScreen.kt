package com.example.doan3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


@Composable
fun AccountScreen(onBack: () -> Unit, onAdminLogin: () -> Unit = {}, onUserLogin: (String) -> Unit = {}) {
    var screen by remember { mutableStateOf("main") }
    var loggedInUser by remember { mutableStateOf("") }
    var loggedInEmail by remember { mutableStateOf("") }

    when (screen) {
        "main"     -> AccountMainScreen(onBack = onBack, onLogin = { screen = "login" }, onRegister = { screen = "register" })
        "login"    -> LoginScreen(
            onBack = { screen = "main" },
            onGoRegister = { screen = "register" },
            onLoginSuccess = { user, email, isAdmin ->
                if (isAdmin) { onAdminLogin() }
                else {
                    loggedInUser = user
                    loggedInEmail = email
                    onUserLogin(user)
                    screen = "profile"
                }
            }
        )
        "register" -> RegisterScreen(onBack = { screen = "main" }, onGoLogin = { screen = "login" })
        "profile"  -> UserProfileScreen(
            username = loggedInUser,
            email = loggedInEmail,
            onBack = onBack,
            onLogout = { screen = "main" }
        )
    }
}

@Composable
fun AccountMainScreen(onBack: () -> Unit, onLogin: () -> Unit, onRegister: () -> Unit, showBack: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại",
                    modifier = Modifier.size(24.dp).clickable { onBack() })
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
            Text(
                text = "Thông tin tài khoản",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Avatar placeholder
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color(0xFFBBBBBB),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bạn chưa đăng nhập? Vui lòng",
                fontSize = 15.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("hoặc", fontSize = 13.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRegister,
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Đăng ký", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
fun LoginScreen(onBack: () -> Unit, onGoRegister: () -> Unit = {}, onLoginSuccess: (String, String, Boolean) -> Unit = { _, _, _ -> }) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp).clickable { onBack() }
            )
            Text(
                text = "LOGIN",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(24.dp))
        }

        Text(
            text = "Welcome back",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Login to your account",
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Illustration placeholder
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE8E8E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tên đăng nhập") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Hiện/ẩn mật khẩu",
                    tint = Color.Gray,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    loginError = "Vui lòng nhập đầy đủ thông tin"
                    return@Button
                }
                isLoading = true
                loginError = ""
                scope.launch {
                    val found = com.example.doan3.firebase.FirebaseManager.login(username.trim(), password)
                    isLoading = false
                    if (found != null) onLoginSuccess(found.username, found.email, found.role == "admin")
                    else loginError = "Tên đăng nhập hoặc mật khẩu không đúng"
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loginError.isNotEmpty()) {
            Text(loginError, color = Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row {
            Text("Bạn chưa có tài khoản?", fontSize = 13.sp, color = Color.Gray)
            Text(
                text = "Đăng ký ngay",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.clickable { onGoRegister() }
            )
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onGoLogin: () -> Unit = {}) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false; onBack() },
            title = { Text("Đăng ký thành công!", fontWeight = FontWeight.Bold) },
            text = { Text("Tài khoản của bạn đã được tạo.") },
            confirmButton = {
                TextButton(onClick = { showSuccess = false; onBack() }) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    androidx.compose.foundation.rememberScrollState().let { scrollState ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
                Text(
                    text = "Đăng ký tài khoản",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(24.dp))
            }

            // Full name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tên người dùng", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color.DarkGray) },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tên đăng nhập", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color.DarkGray) },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Outlined.MailOutline, contentDescription = null, tint = Color.DarkGray) },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Số điện thoại", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.DarkGray) },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Mật khẩu", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.DarkGray) },
                trailingIcon = {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập lại mật khẩu", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.DarkGray) },
                trailingIcon = {
                    Icon(
                        imageVector = if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.clickable { confirmVisible = !confirmVisible }
                    )
                },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                visualTransformation = if (confirmVisible) androidx.compose.ui.text.input.VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMsg, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    errorMsg = when {
                        fullName.isBlank() || username.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() ->
                            "Vui lòng điền đầy đủ thông tin"
                        !email.contains("@") || !email.contains(".") ->
                            "Email không hợp lệ"
                        username.trim().length < 3 ->
                            "Tên đăng nhập phải ít nhất 3 ký tự"
                        password.length < 6 ->
                            "Mật khẩu phải ít nhất 6 ký tự"
                        password != confirmPassword -> "Mật khẩu nhập lại không khớp"
                        else -> {
                            isLoading = true
                            scope.launch {
                                val error = com.example.doan3.firebase.FirebaseManager.register(
                                    username = username.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    password = password
                                )
                                isLoading = false
                                if (error == null) showSuccess = true
                                else errorMsg = error
                            }
                            ""
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.width(220.dp).height(52.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bạn đã có tài khoản?", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = "Đăng nhập ngay!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable { onGoLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun UserProfileScreen(
    username: String,
    email: String,
    avatarUrl: String = "",
    firestoreId: String = "",
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var avatarInput by remember { mutableStateOf(avatarUrl) }
    var currentAvatar by remember { mutableStateOf(avatarUrl) }
    var subScreen by remember { mutableStateOf("") }

    when (subScreen) {
        "info"     -> { UserInfoScreen(username = username, email = email, onBack = { subScreen = "" }); return }
        "orders"   -> { UserOrdersScreen(username = username, onBack = { subScreen = "" }); return }
        "password" -> { ChangePasswordScreen(username = username, onBack = { subScreen = "" }); return }
    }

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            containerColor = Color.White,
            title = { Text("Cập nhật ảnh đại diện", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (avatarInput.isNotBlank()) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(avatarInput),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = androidx.compose.ui.Modifier.size(80.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .align(androidx.compose.ui.Alignment.CenterHorizontally)
                        )
                    }
                    OutlinedTextField(
                        value = avatarInput, onValueChange = { avatarInput = it },
                        label = { Text("URL ảnh đại diện") },
                        placeholder = { Text("https://...", color = Color.LightGray, fontSize = 12.sp) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlack)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    currentAvatar = avatarInput
                    if (firestoreId.isNotEmpty())
                        com.example.doan3.firebase.FirebaseManager.updateAvatar(firestoreId, avatarInput)
                    showAvatarDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                    shape = RoundedCornerShape(10.dp)) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showAvatarDialog = false }) { Text("Hủy") } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất không?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy", color = Color.Black) } },
            containerColor = Color.White
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F3EE))) {
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F3EE))
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(24.dp))
            Text("ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color(0xFFDDDDDD)),
                    contentAlignment = Alignment.Center) {
                    if (currentAvatar.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(currentAvatar).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Filled.Person, null, tint = Color(0xFF888888), modifier = Modifier.size(54.dp))
                    }
                }
                Box(modifier = Modifier.size(26.dp).clip(CircleShape).background(Color.Black)
                    .clickable { showAvatarDialog = true },
                    contentAlignment = Alignment.Center) {
                    Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(username, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(email, fontSize = 13.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(28.dp))

        val menuItems = listOf(
            Triple(Icons.Outlined.Info,         "Thông tin tài khoản", "info"),
            Triple(Icons.Outlined.Receipt,       "Đơn hàng của tôi",   "orders"),
            Triple(Icons.Outlined.Lock,          "Đổi mật khẩu",       "password"),
            Triple(Icons.AutoMirrored.Outlined.ExitToApp, "Đăng xuất", "logout"),
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            menuItems.forEach { (icon, label, action) ->
                Card(shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (action == "logout") showLogoutDialog = true
                        else subScreen = action
                    }) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, label, tint = if (action == "logout") Color.Red else Color.Black,
                            modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                            color = if (action == "logout") Color.Red else Color.Black,
                            modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ── Tab tài khoản — hiển thị trực tiếp trong bottom nav ──────────────────────
@Composable
fun AccountTabScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    username: String,
    email: String,
    onLoginSuccess: (String, String, Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var subScreen by remember { mutableStateOf("main") } // main | login | register

    // Khi đã đăng nhập → luôn hiện profile
    if (isLoggedIn) {
        val user = userAccounts.find { it.username == username }
        UserProfileScreen(
            username = username,
            email = email,
            avatarUrl = user?.avatarUrl ?: "",
            firestoreId = user?.firestoreId ?: "",
            onBack = {},
            onLogout = onLogout
        )
        return
    }

    // Chưa đăng nhập
    when (subScreen) {
        "main"     -> AccountMainScreen(
            onBack     = {},
            showBack   = false,
            onLogin    = { subScreen = "login" },
            onRegister = { subScreen = "register" }
        )
        "login"    -> LoginScreen(
            onBack         = { subScreen = "main" },
            onGoRegister   = { subScreen = "register" },
            onLoginSuccess = { user, em, isAdmin ->
                if (isAdmin) onLoginSuccess(user, em, true)
                else onLoginSuccess(user, em, false)
            }
        )
        "register" -> RegisterScreen(
            onBack    = { subScreen = "main" },
            onGoLogin = { subScreen = "login" }
        )
    }
}
