package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.data.remote.SampleVideoData
import com.example.data.repository.AuthRepository
import com.example.data.repository.GeminiAiRepository
import com.example.data.repository.VideoRepository
import com.example.service.MediaPlaybackManager
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.*
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.YouTubeRed

enum class AppScreen(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Trang chủ", Icons.Filled.Home, Icons.Outlined.Home),
    SHORTS("Shorts", Icons.Filled.SlowMotionVideo, Icons.Outlined.SlowMotionVideo),
    AI_STUDIO("AI Studio", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    LIBRARY("Thư viện", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    PROFILE("Cá nhân", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle),
    SEARCH("Tìm kiếm", Icons.Filled.Search, Icons.Outlined.Search),
    VIDEO_DETAIL("Xem video", Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow)
}

class MainActivity : ComponentActivity() {

    private lateinit var videoRepository: VideoRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var geminiAiRepository: GeminiAiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MediaPlaybackManager.init(this)
        videoRepository = VideoRepository(this)
        authRepository = AuthRepository(this)
        geminiAiRepository = GeminiAiRepository()

        setContent {
            val isDarkThemeState = remember { mutableStateOf(true) }

            CompositionLocalProvider(LocalThemeIsDark provides isDarkThemeState) {
                MyApplicationTheme(darkTheme = isDarkThemeState.value) {
                    MainAppScaffold(
                        videoRepository = videoRepository,
                        authRepository = authRepository,
                        geminiAiRepository = geminiAiRepository
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    videoRepository: VideoRepository,
    authRepository: AuthRepository,
    geminiAiRepository: GeminiAiRepository
) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var previousScreen by remember { mutableStateOf(AppScreen.HOME) }
    var activeDetailVideo by remember { mutableStateOf<VideoItem?>(null) }

    val currentPlayingVideo by MediaPlaybackManager.currentVideo.collectAsState()
    val isMiniPlayerVisible by MediaPlaybackManager.isMiniPlayerVisible.collectAsState()

    // Handle back press
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        if (currentScreen == AppScreen.VIDEO_DETAIL || currentScreen == AppScreen.SEARCH) {
            currentScreen = previousScreen
        } else {
            currentScreen = AppScreen.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Floating MiniPlayer Bar when not in VideoDetailScreen
                if (currentScreen != AppScreen.VIDEO_DETAIL && isMiniPlayerVisible && currentPlayingVideo != null) {
                    MiniPlayerBar(
                        onExpandClick = {
                            activeDetailVideo = currentPlayingVideo
                            previousScreen = currentScreen
                            currentScreen = AppScreen.VIDEO_DETAIL
                        }
                    )
                }

                // Bottom Navigation Bar (Hidden when watching full screen video detail)
                if (currentScreen != AppScreen.VIDEO_DETAIL) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        listOf(
                            AppScreen.HOME,
                            AppScreen.SHORTS,
                            AppScreen.AI_STUDIO,
                            AppScreen.LIBRARY,
                            AppScreen.PROFILE
                        ).forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    previousScreen = currentScreen
                                    currentScreen = screen
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.label,
                                        tint = if (isSelected) {
                                            if (screen == AppScreen.AI_STUDIO) PremiumGold else YouTubeRed
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) {
                                            if (screen == AppScreen.AI_STUDIO) PremiumGold else YouTubeRed
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        videoRepository = videoRepository,
                        onVideoClick = { video ->
                            activeDetailVideo = video
                            previousScreen = AppScreen.HOME
                            currentScreen = AppScreen.VIDEO_DETAIL
                        },
                        onSearchClick = {
                            previousScreen = AppScreen.HOME
                            currentScreen = AppScreen.SEARCH
                        },
                        onProfileClick = {
                            previousScreen = AppScreen.HOME
                            currentScreen = AppScreen.PROFILE
                        }
                    )
                }
                AppScreen.SHORTS -> {
                    ShortsScreen(
                        videoRepository = videoRepository,
                        onVideoClick = { shortVideo ->
                            activeDetailVideo = shortVideo
                            previousScreen = AppScreen.SHORTS
                            currentScreen = AppScreen.VIDEO_DETAIL
                        }
                    )
                }
                AppScreen.AI_STUDIO -> {
                    AiStudioScreen(
                        geminiAiRepository = geminiAiRepository
                    )
                }
                AppScreen.LIBRARY -> {
                    LibraryScreen(
                        videoRepository = videoRepository,
                        onVideoClick = { video ->
                            activeDetailVideo = video
                            previousScreen = AppScreen.LIBRARY
                            currentScreen = AppScreen.VIDEO_DETAIL
                        }
                    )
                }
                AppScreen.PROFILE -> {
                    ProfileScreen(
                        authRepository = authRepository
                    )
                }
                AppScreen.SEARCH -> {
                    SearchScreen(
                        videoRepository = videoRepository,
                        onVideoClick = { video ->
                            activeDetailVideo = video
                            previousScreen = AppScreen.SEARCH
                            currentScreen = AppScreen.VIDEO_DETAIL
                        },
                        onBackClick = {
                            currentScreen = previousScreen
                        }
                    )
                }
                AppScreen.VIDEO_DETAIL -> {
                    val videoToPlay = activeDetailVideo ?: SampleVideoData.sampleVideos[0]
                    VideoDetailScreen(
                        video = videoToPlay,
                        videoRepository = videoRepository,
                        geminiAiRepository = geminiAiRepository,
                        onBackClick = {
                            currentScreen = previousScreen
                        },
                        onRelatedVideoClick = { related ->
                            activeDetailVideo = related
                        }
                    )
                }
            }
        }
    }
}
