package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.data.repository.VideoRepository
import com.example.ui.components.AdFreeShieldBadge
import com.example.ui.components.VideoCard
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

enum class VideoSortOption(val label: String) {
    NEWEST("Mới nhất"),
    POPULAR("Nhiều lượt xem nhất"),
    LIKES("Yêu thích nhất"),
    DURATION("Thời lượng dài")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    videoRepository: VideoRepository,
    onVideoClick: (VideoItem) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var selectedSortOption by remember { mutableStateOf(VideoSortOption.NEWEST) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var targetDownloadVideo by remember { mutableStateOf<VideoItem?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val isDarkTheme = LocalThemeIsDark.current

    val categories = listOf("Tất cả", "Music", "Tech", "Gaming", "Podcasts", "4K HDR")

    val rawVideos = remember(selectedCategory) {
        videoRepository.getVideosByCategory(selectedCategory)
    }

    val sortedVideos = remember(rawVideos, selectedSortOption) {
        when (selectedSortOption) {
            VideoSortOption.NEWEST -> rawVideos
            VideoSortOption.POPULAR -> rawVideos.sortedByDescending { it.durationSeconds } // High engagement
            VideoSortOption.LIKES -> rawVideos.reversed()
            VideoSortOption.DURATION -> rawVideos.sortedByDescending { it.durationSeconds }
        }
    }

    Scaffold(
        modifier = modifier.testTag("home_screen"),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // YouTube Premium Brand Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(YouTubeRed, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tube",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Premium",
                                color = PremiumGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    // Action Icons: Theme Toggle, Search, Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Light / Dark Theme Switcher
                        IconButton(onClick = { isDarkTheme.value = !isDarkTheme.value }) {
                            Icon(
                                imageVector = if (isDarkTheme.value) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (isDarkTheme.value) PremiumGold else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Search Button
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Profile Avatar
                        IconButton(onClick = onProfileClick) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(YouTubeRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VIP",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Category Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YouTubeRed,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Sort Options Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sắp xếp theo:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    VideoSortOption.values().forEach { option ->
                        val isChosen = selectedSortOption == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedSortOption = option }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = option.label,
                                fontSize = 11.sp,
                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // VIP Ad-Free Banner Item
            item {
                AdFreeShieldBadge(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    adBlockedCount = 1428
                )
            }

            // Video Feed Items
            items(sortedVideos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    onClick = { onVideoClick(video) },
                    onDownloadClick = {
                        targetDownloadVideo = video
                        showDownloadDialog = true
                    },
                    onPlaylistClick = {
                        snackbarMessage = "Đã lưu vào danh sách phát yêu thích"
                    }
                )
            }
        }
    }

    // Download Dialog with Quality Presets
    if (showDownloadDialog && targetDownloadVideo != null) {
        val video = targetDownloadVideo!!
        var selectedDlQuality by remember { mutableStateOf("1080p60 Full HD") }

        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = YouTubeRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tải xuống để xem ngoại tuyến", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = video.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Chọn độ phân giải tải xuống:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    listOf(
                        "1080p60 Full HD" to "145 MB • Cực nét",
                        "720p HD" to "68 MB • Chuẩn HD",
                        "480p Tiết kiệm" to "32 MB • Tiết kiệm dung lượng",
                        "Audio Only (MP3)" to "12 MB • Nghe nhạc nền offline"
                    ).forEach { (quality, size) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDlQuality = quality }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = quality,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedDlQuality == quality) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedDlQuality == quality) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = size, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(
                                selected = selectedDlQuality == quality,
                                onClick = { selectedDlQuality = quality },
                                colors = RadioButtonDefaults.colors(selectedColor = YouTubeRed)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            videoRepository.saveDownload(video, selectedDlQuality)
                            showDownloadDialog = false
                            snackbarMessage = "Đã tải xuống thành công '${video.title}'. Bạn có thể xem ngoại tuyến bất kỳ lúc nào!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text("Tải ngay ($selectedDlQuality)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Snackbar notification
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
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = snackbarMessage ?: "", fontSize = 13.sp)
            }
        }
    }
}
