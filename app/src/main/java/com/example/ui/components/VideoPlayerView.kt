package com.example.ui.components

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.VideoItem
import com.example.service.MediaPlaybackManager
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    video: VideoItem,
    onCloseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPlaying by MediaPlaybackManager.isPlaying.collectAsState()
    val currentPositionMs by MediaPlaybackManager.currentPositionMs.collectAsState()
    val durationMs by MediaPlaybackManager.durationMs.collectAsState()
    val playbackSpeed by MediaPlaybackManager.playbackSpeed.collectAsState()
    val selectedQuality by MediaPlaybackManager.selectedQuality.collectAsState()
    val isAudioOnly by MediaPlaybackManager.isAudioOnly.collectAsState()
    val isLooping by MediaPlaybackManager.isLooping.collectAsState()
    val sleepTimerSec by MediaPlaybackManager.sleepTimerSeconds.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .testTag("video_player_container")
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Video SurfaceView or Audio-only animation banner
        if (isAudioOnly) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF2B0900), Color(0xFF0F0F0F))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Audio Only Mode",
                        tint = PremiumGold,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chế độ phát chỉ âm thanh (Tiết kiệm 95% Pin & Data)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Vẫn tiếp tục phát khi tắt màn hình hoặc chuyển app",
                        color = PremiumGold,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) {
                                MediaPlaybackManager.attachSurface(holder)
                            }

                            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                MediaPlaybackManager.detachSurface()
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
            ) {
                // Top Bar Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onCloseClick != null) {
                            IconButton(onClick = onCloseClick) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Minimize",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        // VIP Ad-Free Indicator
                        Box(
                            modifier = Modifier
                                .background(Color(0xCCFFD700), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "0 ADS • VIP",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Audio Only Toggle
                        IconButton(onClick = { MediaPlaybackManager.toggleAudioOnly() }) {
                            Icon(
                                imageVector = if (isAudioOnly) Icons.Default.Videocam else Icons.Default.Headphones,
                                contentDescription = "Toggle Audio/Video",
                                tint = if (isAudioOnly) PremiumGold else Color.White
                            )
                        }

                        // Loop Toggle
                        IconButton(onClick = { MediaPlaybackManager.toggleLooping() }) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Loop",
                                tint = if (isLooping) YouTubeRed else Color.White
                            )
                        }

                        // Quality / Speed / Sleep Settings
                        IconButton(onClick = { showQualityDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = "Quality",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = { showSpeedDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Playback Speed",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = { showSleepTimerDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimerSec > 0) PremiumGold else Color.White
                            )
                        }
                    }
                }

                // Center Play/Pause & 10s Seek Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { MediaPlaybackManager.seekBackward(10000L) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0x44000000), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play/Pause
                    IconButton(
                        onClick = { MediaPlaybackManager.togglePlayPause() },
                        modifier = Modifier
                            .testTag("player_play_pause_button")
                            .size(64.dp)
                            .background(Color(0x88000000), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { MediaPlaybackManager.seekForward(10000L) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0x44000000), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Timeline & Duration Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatDuration(currentPositionMs)} / ${formatDuration(durationMs)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "$selectedQuality • ${playbackSpeed}x" + if (sleepTimerSec > 0) " • Hẹn giờ: ${sleepTimerSec / 60}m" else "",
                            color = Color(0xFFDDDDDD),
                            fontSize = 11.sp
                        )
                    }

                    Slider(
                        value = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = { frac ->
                            MediaPlaybackManager.seekTo((frac * durationMs).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = YouTubeRed,
                            activeTrackColor = YouTubeRed,
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                }
            }
        }
    }

    // Speed Selection Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Tốc độ phát") },
            text = {
                Column {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    MediaPlaybackManager.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (speed == 1.0f) "Chuẩn (1.0x)" else "${speed}x",
                                fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                                color = if (playbackSpeed == speed) YouTubeRed else MaterialTheme.colorScheme.onSurface
                            )
                            if (playbackSpeed == speed) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = YouTubeRed)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Quality Selection Dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Chất lượng video") },
            text = {
                Column {
                    video.qualityList.forEach { quality ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    MediaPlaybackManager.setQuality(quality)
                                    showQualityDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = quality,
                                fontWeight = if (selectedQuality == quality) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedQuality == quality) PremiumGold else MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedQuality == quality) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PremiumGold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Hẹn giờ tắt nhạc / video") },
            text = {
                Column {
                    listOf(
                        0 to "Tắt hẹn giờ",
                        15 to "15 phút",
                        30 to "30 phút",
                        45 to "45 phút",
                        60 to "60 phút"
                    ).forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    MediaPlaybackManager.setSleepTimer(minutes)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (sleepTimerSec / 60 == minutes) FontWeight.Bold else FontWeight.Normal,
                                color = if (sleepTimerSec / 60 == minutes) PremiumGold else MaterialTheme.colorScheme.onSurface
                            )
                            if (sleepTimerSec / 60 == minutes) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PremiumGold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
