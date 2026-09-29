package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.data.repository.VideoRepository
import com.example.ui.components.VideoCard
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.YouTubeRed

enum class SearchDurationFilter(val label: String) {
    ALL("Mọi thời lượng"),
    SHORT("Dưới 4 phút (< 4m)"),
    MEDIUM("4 - 20 phút"),
    LONG("Trên 20 phút (> 20m)")
}

enum class SearchDateFilter(val label: String) {
    ALL("Mọi thời điểm"),
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
    THIS_YEAR("Năm nay")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    videoRepository: VideoRepository,
    onVideoClick: (VideoItem) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    BackHandler { onBackClick() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(SearchDurationFilter.ALL) }
    var selectedDate by remember { mutableStateOf(SearchDateFilter.ALL) }
    var selectedChannel by remember { mutableStateOf("Tất cả kênh") }
    var selectedQuality by remember { mutableStateOf("Tất cả") }
    var sortBy by remember { mutableStateOf("Độ liên quan") }

    var showFilterSheet by remember { mutableStateOf(false) }

    val allVideos = remember { videoRepository.getAllVideos() }
    val channels = remember(allVideos) {
        listOf("Tất cả kênh") + allVideos.map { it.channelName }.distinct()
    }

    val trendingKeywords = listOf(
        "Lo-fi Chill",
        "AI 2026",
        "4K HDR",
        "Lập trình Android",
        "Hang Sơn Đoòng",
        "Podcast Tài chính",
        "Esports"
    )

    // Filter Logic
    val searchResults = remember(searchQuery, selectedDuration, selectedDate, selectedChannel, selectedQuality, sortBy) {
        var list = if (searchQuery.isBlank()) {
            allVideos
        } else {
            videoRepository.searchVideos(searchQuery)
        }

        // Duration Filter
        list = when (selectedDuration) {
            SearchDurationFilter.ALL -> list
            SearchDurationFilter.SHORT -> list.filter { it.durationSeconds < 240 }
            SearchDurationFilter.MEDIUM -> list.filter { it.durationSeconds in 240..1200 }
            SearchDurationFilter.LONG -> list.filter { it.durationSeconds > 1200 }
        }

        // Channel Filter
        if (selectedChannel != "Tất cả kênh") {
            list = list.filter { it.channelName.equals(selectedChannel, ignoreCase = true) }
        }

        // Quality Filter
        if (selectedQuality != "Tất cả") {
            list = list.filter {
                it.defaultQuality.contains(selectedQuality, ignoreCase = true) ||
                it.category.contains(selectedQuality, ignoreCase = true)
            }
        }

        // Sort By
        when (sortBy) {
            "Lượt xem cao nhất" -> list.sortedByDescending { it.durationSeconds }
            "Mới nhất" -> list
            else -> list
        }
    }

    val activeFilterCount = remember(selectedDuration, selectedDate, selectedChannel, selectedQuality, sortBy) {
        var count = 0
        if (selectedDuration != SearchDurationFilter.ALL) count++
        if (selectedDate != SearchDateFilter.ALL) count++
        if (selectedChannel != "Tất cả kênh") count++
        if (selectedQuality != "Tất cả") count++
        if (sortBy != "Độ liên quan") count++
        count
    }

    Scaffold(
        modifier = modifier.testTag("search_screen"),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Search Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm video không dấu hoặc có dấu...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Advanced Filter Button with Active Badge
                    Box {
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (activeFilterCount > 0) YouTubeRed else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Advanced Filter",
                                tint = if (activeFilterCount > 0) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Quick Trending Search & Filter Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = activeFilterCount > 0,
                            onClick = { showFilterSheet = true },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = {
                                Text(
                                    text = if (activeFilterCount > 0) "Bộ lọc ($activeFilterCount)" else "Bộ lọc nâng cao",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YouTubeRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    items(trendingKeywords) { keyword ->
                        val isSelected = searchQuery.equals(keyword, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                searchQuery = if (isSelected) "" else keyword
                                keyboardController?.hide()
                            },
                            label = { Text(text = keyword, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Không tìm thấy video nào cho '$searchQuery'",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hãy thử tìm kiếm với từ khóa khác (ví dụ: lofi, ai, 4k, lập trình, podcast) hoặc xóa bớt bộ lọc.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedDuration = SearchDurationFilter.ALL
                            selectedDate = SearchDateFilter.ALL
                            selectedChannel = "Tất cả kênh"
                            selectedQuality = "Tất cả"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                    ) {
                        Text("Xóa bộ lọc & Xem tất cả")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Kết quả cho \"$searchQuery\" (${searchResults.size})" else "Tất cả video (${searchResults.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (activeFilterCount > 0) {
                            Text(
                                text = "Đang áp dụng $activeFilterCount bộ lọc",
                                fontSize = 11.sp,
                                color = PremiumGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(searchResults, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video) }
                    )
                }
            }
        }
    }

    // Advanced Search Filter Modal Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bộ lọc tìm kiếm nâng cao",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            selectedDuration = SearchDurationFilter.ALL
                            selectedDate = SearchDateFilter.ALL
                            selectedChannel = "Tất cả kênh"
                            selectedQuality = "Tất cả"
                            sortBy = "Độ liên quan"
                        }
                    ) {
                        Text("Đặt lại", color = YouTubeRed)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Filter by Duration (Thời lượng)
                Text(
                    text = "Thời lượng video:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SearchDurationFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedDuration == filter,
                            onClick = { selectedDuration = filter },
                            label = { Text(filter.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YouTubeRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Filter by Upload Date (Ngày tải lên)
                Text(
                    text = "Ngày tải lên:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SearchDateFilter.values()) { dateFilter ->
                        FilterChip(
                            selected = selectedDate == dateFilter,
                            onClick = { selectedDate = dateFilter },
                            label = { Text(dateFilter.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YouTubeRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Filter by Channel (Kênh phát hành)
                Text(
                    text = "Kênh phát hành:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(channels) { ch ->
                        FilterChip(
                            selected = selectedChannel == ch,
                            onClick = { selectedChannel = ch },
                            label = { Text(ch, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PremiumGold,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Sort By (Sắp xếp theo)
                Text(
                    text = "Sắp xếp theo:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Độ liên quan", "Lượt xem cao nhất", "Mới nhất")) { s ->
                        FilterChip(
                            selected = sortBy == s,
                            onClick = { sortBy = s },
                            label = { Text(s, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YouTubeRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text("Áp dụng bộ lọc (${searchResults.size} kết quả)")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
