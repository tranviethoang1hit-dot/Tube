package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.data.model.VideoItem
import com.example.data.repository.VideoRepository
import com.example.service.MediaPlaybackManager
import com.example.ui.components.ShortsVideoCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortsScreen(
    videoRepository: VideoRepository,
    onVideoClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val shorts = remember { videoRepository.getShorts() }
    val pagerState = rememberPagerState(pageCount = { shorts.size })

    LaunchedEffect(pagerState.currentPage) {
        if (shorts.isNotEmpty()) {
            val currentShort = shorts[pagerState.currentPage]
            MediaPlaybackManager.playVideo(currentShort)
        }
    }

    Box(
        modifier = modifier
            .testTag("shorts_screen")
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val shortVideo = shorts[page]
            ShortsVideoCard(
                video = shortVideo,
                onClick = { onVideoClick(shortVideo) }
            )
        }
    }
}
