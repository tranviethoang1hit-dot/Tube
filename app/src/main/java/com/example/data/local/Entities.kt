package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.VideoItem

@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val channelName: String,
    val channelAvatarUrl: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val durationFormatted: String,
    val viewCount: String,
    val publishedTime: String,
    val category: String,
    val quality: String,
    val localFilePath: String? = null,
    val downloadedAt: Long = System.currentTimeMillis()
) {
    fun toVideoItem(): VideoItem = VideoItem(
        id = id,
        title = title,
        description = description,
        channelName = channelName,
        channelAvatarUrl = channelAvatarUrl,
        thumbnailUrl = thumbnailUrl,
        videoUrl = videoUrl,
        durationSeconds = durationSeconds,
        durationFormatted = durationFormatted,
        viewCount = viewCount,
        publishedTime = publishedTime,
        category = category,
        isDownloaded = true
    )
}

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val durationFormatted: String,
    val lastPositionSeconds: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_playlists")
data class SavedPlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val videoIdsJson: String, // comma or json array
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "liked_videos")
data class LikedVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val likedAt: Long = System.currentTimeMillis()
)
