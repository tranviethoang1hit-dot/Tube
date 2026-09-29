package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.AuthRepository
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authRepository.userProfile.collectAsState()
    val isLoading by authRepository.isLoading.collectAsState()
    val isDarkTheme = LocalThemeIsDark.current

    var backgroundPlaybackEnabled by remember { mutableStateOf(true) }
    var selectedDefaultQuality by remember { mutableStateOf("1080p60 HDR") }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authEmail by remember { mutableStateOf("") }
    var authPass by remember { mutableStateOf("") }
    var authDisplayName by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.testTag("profile_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tài khoản & Cài đặt Premium",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // VIP Membership Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2E000A), Color(0xFF1F0F00))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (userProfile?.photoUrl?.isNotBlank() == true) {
                                AsyncImage(
                                    model = userProfile?.photoUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(YouTubeRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userProfile?.displayName ?: "VIP Premium User",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = userProfile?.email ?: "vip.user@tubepremium.app",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCCCCCC)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(PremiumGold, shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PREMIUM VIP", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0x33FFFFFF))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PremiumGold)
                                Text("Quảng cáo", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("1,428+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PremiumGold)
                                Text("Ads đã chặn", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("100%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PremiumGold)
                                Text("Phát nền", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                            }
                        }
                    }
                }
            }

            // Google / Firebase Sign-In Button
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = authRepository.signInWithGoogle(context)
                            if (result.isSuccess) {
                                snackbarMessage = "Đăng nhập Google với Firebase thành công!"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PremiumGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng nhập Google & Đồng bộ Firestore",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Preferences Section Header
            item {
                Text(
                    text = "Tùy chỉnh & Cài đặt hệ thống",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Theme Switcher (Chế độ Sáng / Tối)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme.value) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = YouTubeRed
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isDarkTheme.value) "Chế độ giao diện Tối (AMOLED Dark)" else "Chế độ giao diện Sáng (Light)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Nhấn để chuyển đổi màu sắc giao diện",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isDarkTheme.value,
                            onCheckedChange = { isDarkTheme.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = YouTubeRed, checkedTrackColor = Color(0x55FF0000))
                        )
                    }
                }
            }

            // Background Playback Switch (Phát trong nền khi thoát app hoặc tắt màn hình)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = PremiumGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Phát trong nền (Background Playback)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tự động tiếp tục phát âm thanh khi bạn thoát ứng dụng hoặc tắt màn hình điện thoại.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = backgroundPlaybackEnabled,
                            onCheckedChange = {
                                backgroundPlaybackEnabled = it
                                authRepository.updateSettings(it, selectedDefaultQuality)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PremiumGold, checkedTrackColor = Color(0x55FFD700))
                        )
                    }
                }
            }

            // Preferred Resolution
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HighQuality, contentDescription = null, tint = YouTubeRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Chất lượng phát video mặc định",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("4K HDR", "1080p60 HDR", "720p HD", "Tự động").forEach { q ->
                                FilterChip(
                                    selected = selectedDefaultQuality == q,
                                    onClick = { selectedDefaultQuality = q },
                                    label = { Text(q, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = YouTubeRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Sign Out / Email Login Button
            item {
                OutlinedButton(
                    onClick = { showAuthDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Quản lý tài khoản Firebase Email / Đăng xuất", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    // Email Auth Dialog
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text(if (isSignUpMode) "Đăng ký tài khoản VIP" else "Đăng nhập Firebase")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isSignUpMode) {
                        TextField(
                            value = authDisplayName,
                            onValueChange = { authDisplayName = it },
                            placeholder = { Text("Tên hiển thị") },
                            singleLine = true
                        )
                    }
                    TextField(
                        value = authEmail,
                        onValueChange = { authEmail = it },
                        placeholder = { Text("Email") },
                        singleLine = true
                    )
                    TextField(
                        value = authPass,
                        onValueChange = { authPass = it },
                        placeholder = { Text("Mật khẩu") },
                        singleLine = true
                    )
                    TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                        Text(if (isSignUpMode) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isSignUpMode) {
                                authRepository.signUpWithEmail(authEmail, authPass, authDisplayName)
                            } else {
                                authRepository.signInWithEmail(authEmail, authPass)
                            }
                            showAuthDialog = false
                            snackbarMessage = "Đồng bộ tài khoản thành công!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text(if (isSignUpMode) "Đăng ký" else "Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    if (snackbarMessage != null) {
        LaunchedEffect(snackbarMessage) {
            kotlinx.coroutines.delay(3000)
            snackbarMessage = null
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(snackbarMessage ?: "")
            }
        }
    }
}
