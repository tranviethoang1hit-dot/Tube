package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GeminiAiRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(
    geminiAiRepository: GeminiAiRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tạo Thumbnail AI, 1: Deep Video Reasoning

    // Image Generator State (gemini-3.1-flash-image-preview)
    var imagePrompt by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") } // "16:9", "1:1", "9:16"
    var isGeneratingImage by remember { mutableStateOf(false) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var generatedImageDescription by remember { mutableStateOf<String?>(null) }
    var imageErrorMessage by remember { mutableStateOf<String?>(null) }

    // Deep Thinking Reasoning State (gemini-3.1-pro-preview)
    var reasoningPrompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var reasoningResultText by remember { mutableStateOf<String?>(null) }
    var reasoningErrorMessage by remember { mutableStateOf<String?>(null) }

    val presetPrompts = listOf(
        "Thumbnail 4K YouTube lập trình AI 2026 với tone màu neon rực rỡ và robot tương lai",
        "Cover album Lofi chill hip hop phong cách anime Nhật Bản đêm mưa",
        "Bìa Shorts gaming cực ngầu về trận đấu chung kết Esports đỉnh cao",
        "Ảnh bìa podcast tài chính công nghệ cao cấp với đồ thị 3D hologram"
    )

    Scaffold(
        modifier = modifier.testTag("ai_studio_screen"),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Studio & Trợ Lý Sáng Tạo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

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
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tạo Thumbnail AI", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Deep Thinking AI", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TAB 0: Image Creation with gemini-3.1-flash-image-preview
            if (selectedTab == 0) {
                item {
                    // Header Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF2E0812), Color(0xFF150A22))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = PremiumGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mô hình: gemini-3.1-flash-image-preview",
                                    color = PremiumGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tạo ảnh thu nhỏ (Thumbnail), bìa album và banner YouTube chất lượng cao theo bất kỳ mô tả nào bạn muốn.",
                                color = Color(0xFFDDDDDD),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Prompt Input Box
                item {
                    Column {
                        Text(
                            text = "Nhập mô tả ảnh Thumbnail:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = imagePrompt,
                            onValueChange = { imagePrompt = it },
                            placeholder = { Text("Ví dụ: Bìa YouTube 4K về cuộc phiêu lưu thám hiểm rừng rậm nhiệt đới...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Aspect Ratio Selector
                item {
                    Column {
                        Text(
                            text = "Tỉ lệ khung hình:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "16:9" to "16:9 (YouTube Standard)",
                                "1:1" to "1:1 (Playlist/Album)",
                                "9:16" to "9:16 (Shorts/Reels)"
                            ).forEach { (ratio, label) ->
                                FilterChip(
                                    selected = selectedAspectRatio == ratio,
                                    onClick = { selectedAspectRatio = ratio },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = YouTubeRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Presets
                item {
                    Column {
                        Text(
                            text = "Gợi ý mẫu có sẵn:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        presetPrompts.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { imagePrompt = preset }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "✨ $preset",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Generate Button
                item {
                    Button(
                        onClick = {
                            if (imagePrompt.isNotBlank()) {
                                isGeneratingImage = true
                                imageErrorMessage = null
                                generatedBitmap = null
                                coroutineScope.launch {
                                    val result = geminiAiRepository.generateOrEditImage(
                                        prompt = imagePrompt,
                                        aspectRatio = selectedAspectRatio
                                    )
                                    isGeneratingImage = false
                                    if (result.success) {
                                        generatedBitmap = result.bitmap
                                        generatedImageDescription = result.textDescription
                                    } else {
                                        imageErrorMessage = result.errorMessage
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isGeneratingImage && imagePrompt.isNotBlank()
                    ) {
                        if (isGeneratingImage) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang vẽ ảnh với Gemini 3.1 Flash...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tạo Thumbnail ngay")
                        }
                    }
                }

                // Generated Image Result Display
                if (generatedBitmap != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Ảnh đã tạo thành công ($selectedAspectRatio):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PremiumGold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                bitmap = generatedBitmap!!.asImageBitmap(),
                                contentDescription = "Generated Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(if (selectedAspectRatio == "16:9") 16f / 9f else if (selectedAspectRatio == "9:16") 9f / 16f else 1f)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            if (generatedImageDescription != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = generatedImageDescription!!,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (imageErrorMessage != null) {
                    item {
                        Text(
                            text = imageErrorMessage!!,
                            color = YouTubeRed,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // TAB 1: Deep Video Reasoning with gemini-3.1-pro-preview
            if (selectedTab == 1) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1700), Color(0xFF121212))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = PremiumGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mô hình: gemini-3.1-pro-preview (Thinking Level HIGH)",
                                    color = PremiumGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Trợ lý suy luận đa bước cao cấp: Phân tích chiến lược nội dung, kịch bản video viral, giải thích thuật toán YouTube và giải quyết câu hỏi phức tạp.",
                                color = Color(0xFFDDDDDD),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Nội dung cần suy luận sâu:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = reasoningPrompt,
                            onValueChange = { reasoningPrompt = it },
                            placeholder = { Text("Ví dụ: Lập dàn ý chi tiết cho video tài liệu 20 phút về cuộc đua chip AI năm 2026 kèm các luận điểm phân tích...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (reasoningPrompt.isNotBlank()) {
                                isThinking = true
                                reasoningErrorMessage = null
                                reasoningResultText = null
                                coroutineScope.launch {
                                    val dummyVideo = com.example.data.remote.SampleVideoData.sampleVideos[0]
                                    val result = geminiAiRepository.analyzeVideoWithHighThinking(
                                        video = dummyVideo,
                                        userQuery = reasoningPrompt
                                    )
                                    isThinking = false
                                    if (result.success) {
                                        reasoningResultText = result.text
                                    } else {
                                        reasoningErrorMessage = result.errorMessage
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGoldDark),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isThinking && reasoningPrompt.isNotBlank()
                    ) {
                        if (isThinking) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini 3.1 Pro đang suy luận sâu...")
                        } else {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bắt đầu suy luận chuyên sâu", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (reasoningResultText != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PremiumGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bản phân tích chuyên sâu (Thinking Complete)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PremiumGold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = reasoningResultText!!,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (reasoningErrorMessage != null) {
                    item {
                        Text(
                            text = reasoningErrorMessage!!,
                            color = YouTubeRed,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
