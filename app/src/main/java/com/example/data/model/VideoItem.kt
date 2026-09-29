package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val channelName: String,
    val channelAvatarUrl: String,
    val subscriberCount: String = "1.2M",
    val thumbnailUrl: String,
    val videoUrl: String, // Streaming MP4/HLS direct playable URL
    val durationSeconds: Int,
    val durationFormatted: String,
    val viewCount: String,
    val publishedTime: String,
    val category: String, // "Trending", "Music", "Gaming", "Tech", "Podcasts", "4K HDR", "Shorts"
    val isShort: Boolean = false,
    val likesCount: String = "45K",
    val qualityList: List<String> = listOf("1080p60 HDR", "720p60", "480p", "360p", "Audio Only"),
    val defaultQuality: String = "1080p60 HDR",
    val transcript: String = "",
    val chapters: List<VideoChapter> = emptyList(),
    val isDownloaded: Boolean = false,
    val isLiked: Boolean = false,
    val isSavedToPlaylist: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VideoChapter(
    val timeSeconds: Int,
    val title: String
)

@JsonClass(generateAdapter = true)
data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val coverUrl: String = "",
    val videoCount: Int = 0,
    val videos: List<VideoItem> = emptyList(),
    val isCustomCreated: Boolean = false
)
