package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CommentItem
import com.example.data.model.VideoItem
import com.example.data.repository.GeminiAiRepository
import com.example.data.repository.ThinkingAnalysisResult
import com.example.data.repository.VideoRepository
import com.example.service.MediaPlaybackManager
import com.example.ui.components.AdFreeShieldBadge
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    video: VideoItem,
    videoRepository: VideoRepository,
    geminiAiRepository: GeminiAiRepository,
    onBackClick: () -> Unit,
    onRelatedVideoClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Handle back button
    BackHandler { onBackClick() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: AI Thinking, 1: Comments/Details, 2: Related
    var isLiked by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var downloadQuality by remember { mutableStateOf("1080p60 Full HD") }
    var userAiQuestion by remember { mutableStateOf("") }
    var isAiAnalyzing by remember { mutableStateOf(false) }
    var aiAnalysisResult by remember { mutableStateOf<ThinkingAnalysisResult?>(null) }
    var commentsList by remember { mutableStateOf(videoRepository.getCommentsForVideo(video.id)) }
    var newCommentText by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val relatedVideos = remember {
        videoRepository.getAllVideos().filter { it.id != video.id }
    }

    LaunchedEffect(video.id) {
        if (MediaPlaybackManager.currentVideo.value?.id != video.id) {
            MediaPlaybackManager.playVideo(video)
        } else if (MediaPlaybackManager.playbackState.value == com.example.service.PlaybackState.PAUSED) {
            MediaPlaybackManager.resume()
        }
        videoRepository.recordHistory(video)
        isDownloaded = videoRepository.isDownloaded(video.id)
        isLiked = videoRepository.isLiked(video.id)
        // Auto-run AI deep thinking analysis for the video
        isAiAnalyzing = true
        aiAnalysisResult = geminiAiRepository.analyzeVideoWithHighThinking(video)
        isAiAnalyzing = false
    }

    Scaffold(
        modifier = modifier.testTag("video_detail_screen"),
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(snackbarMessage ?: "")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Video Player
            VideoPlayerView(
                video = video,
                onCloseClick = onBackClick
            )

            // Scrollable Details & Tabs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Title and VIP Badge
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        AdFreeShieldBadge(
                            modifier = Modifier.padding(bottom = 8.dp),
                            adBlockedCount = 1428
                        )

                        Text(
                            text = video.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 23.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${video.viewCount} • ${video.publishedTime} • ${video.defaultQuality}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Like
                            ActionButton(
                                icon = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                label = video.likesCount,
                                tint = if (isLiked) YouTubeRed else MaterialTheme.colorScheme.onSurface,
                                onClick = {
                                    isLiked = !isLiked
                                    coroutineScope.launch {
                                        videoRepository.toggleLike(video, isLiked)
                                    }
                                }
                            )

                            // Offline Download
                            ActionButton(
                                icon = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                label = if (isDownloaded) "Đã tải" else "Tải xuống",
                                tint = if (isDownloaded) PremiumGold else MaterialTheme.colorScheme.onSurface,
                                onClick = {
                                    if (isDownloaded) {
                                        coroutineScope.launch {
                                            videoRepository.removeDownload(video.id)
                                            isDownloaded = false
                                            snackbarMessage = "Đã xóa video khỏi danh sách ngoại tuyến"
                                        }
                                    } else {
                                        showDownloadDialog = true
                                    }
                                }
                            )

                            // Background Audio Mode
                            val isAudioOnly by MediaPlaybackManager.isAudioOnly.collectAsState()
                            ActionButton(
                                icon = Icons.Default.Headphones,
                                label = if (isAudioOnly) "Chỉ âm thanh" else "Nghe nền",
                                tint = if (isAudioOnly) PremiumGold else MaterialTheme.colorScheme.onSurface,
                                onClick = {
                                    MediaPlaybackManager.toggleAudioOnly()
                                    snackbarMessage = "Đã bật chế độ phát âm thanh nền tiết kiệm pin khi tắt màn hình"
                                }
                            )

                            // Share
                            ActionButton(
                                icon = Icons.Default.Share,
                                label = "Chia sẻ",
                                tint = MaterialTheme.colorScheme.onSurface,
                                onClick = { snackbarMessage = "Đã sao chép liên kết video YouTube Premium" }
                            )

                            // Save Playlist
                            ActionButton(
                                icon = Icons.Default.PlaylistAdd,
                                label = "Lưu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                onClick = { snackbarMessage = "Đã thêm vào Danh sách phát VIP" }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Channel Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = video.channelAvatarUrl,
                                contentDescription = video.channelName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.channelName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${video.subscriberCount} người đăng ký",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { snackbarMessage = "Đã đăng ký kênh ${video.channelName}" },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Đã đăng ký", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Tab Selector
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = YouTubeRed
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Deep Thinking", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Mô tả & Bình luận") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Video liên quan") }
                        )
                    }
                }

                // TAB 0: AI Deep Thinking with gemini-3.1-pro-preview
                if (selectedTab == 0) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // High Thinking Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = "Thinking Mode",
                                            tint = PremiumGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Gemini 3.1 Pro • Thinking Level HIGH",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = PremiumGold
                                            )
                                            Text(
                                                text = "Phân tích chuyên sâu, trích xuất luận điểm & lý luận đa chiều",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Quick Question Chips
                                    Text(
                                        text = "Hỏi nhanh AI về video này:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "Tóm tắt 3 ý chính",
                                            "Các mốc thời gian",
                                            "Đánh giá chuyên sâu"
                                        ).forEach { chipText ->
                                            OutlinedButton(
                                                onClick = {
                                                    userAiQuestion = chipText
                                                    isAiAnalyzing = true
                                                    coroutineScope.launch {
                                                        aiAnalysisResult = geminiAiRepository.analyzeVideoWithHighThinking(video, chipText)
                                                        isAiAnalyzing = false
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Text(chipText, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Prompt Input
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextField(
                                            value = userAiQuestion,
                                            onValueChange = { userAiQuestion = it },
                                            placeholder = { Text("Đặt câu hỏi chi tiết về video...", fontSize = 13.sp) },
                                            singleLine = true,
                                            colors = TextFieldDefaults.colors(
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                if (userAiQuestion.isNotBlank()) {
                                                    isAiAnalyzing = true
                                                    coroutineScope.launch {
                                                        aiAnalysisResult = geminiAiRepository.analyzeVideoWithHighThinking(video, userAiQuestion)
                                                        isAiAnalyzing = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(YouTubeRed)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Thinking Result State
                            if (isAiAnalyzing) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = PremiumGold)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Gemini 3.1 Pro đang suy luận sâu (High Thinking Mode)...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PremiumGold
                                    )
                                }
                            } else if (aiAnalysisResult != null) {
                                val result = aiAnalysisResult!!
                                if (result.success) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                                            .padding(16.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Kết quả phân tích trí tuệ nhân tạo",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = PremiumGold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = result.text,
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Text(
                                        text = result.errorMessage ?: "Không thể phân tích video này.",
                                        color = YouTubeRed,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 1: Description, Chapters & Comments
                if (selectedTab == 1) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Description
                            Text(
                                text = "Mô tả video",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = video.description,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Chapters
                            if (video.chapters.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Các phân đoạn (Chapters)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                video.chapters.forEach { chapter ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                MediaPlaybackManager.seekTo(chapter.timeSeconds * 1000L)
                                            }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = String.format("%02d:%02d", chapter.timeSeconds / 60, chapter.timeSeconds % 60),
                                            color = YouTubeRed,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = chapter.title,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Comments Header
                            Text(
                                text = "Bình luận (${commentsList.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Add Comment Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Viết bình luận công khai...", fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            val newComment = CommentItem(
                                                id = "c_${System.currentTimeMillis()}",
                                                authorName = "Bạn (VIP Premium)",
                                                authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                                                text = newCommentText,
                                                timestamp = "Vừa xong",
                                                isVipMember = true
                                            )
                                            commentsList = listOf(newComment) + commentsList
                                            newCommentText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(YouTubeRed)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Comments List
                            commentsList.forEach { comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    AsyncImage(
                                        model = comment.authorAvatarUrl,
                                        contentDescription = comment.authorName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = comment.authorName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (comment.isVipMember) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "VIP",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PremiumGold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = comment.timestamp,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = comment.text,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: Related Videos
                if (selectedTab == 2) {
                    items(relatedVideos, key = { it.id }) { item ->
                        VideoCard(
                            video = item,
                            onClick = { onRelatedVideoClick(item) }
                        )
                    }
                }
            }
        }
    }

    // Download Dialog
    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = {
                Text("Tải video về thiết bị Android để xem Offline", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    listOf(
                        "1080p60 Full HD" to "145 MB • Chất lượng gốc",
                        "720p HD" to "68 MB • Chuẩn HD",
                        "480p Tiết kiệm" to "32 MB • Tải nhanh",
                        "Audio Only (MP3)" to "12 MB • Nghe offline trong nền"
                    ).forEach { (quality, size) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { downloadQuality = quality }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = quality,
                                    fontSize = 14.sp,
                                    fontWeight = if (downloadQuality == quality) FontWeight.Bold else FontWeight.Normal,
                                    color = if (downloadQuality == quality) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = size, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(
                                selected = downloadQuality == quality,
                                onClick = { downloadQuality = quality },
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
                            videoRepository.saveDownload(video, downloadQuality)
                            isDownloaded = true
                            showDownloadDialog = false
                            snackbarMessage = "Đã lưu video '$downloadQuality' vào thư viện ngoại tuyến!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text("Tải ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}
