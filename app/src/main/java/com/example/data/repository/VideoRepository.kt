package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.CommentItem
import com.example.data.model.Playlist
import com.example.data.model.VideoItem
import com.example.data.remote.SampleVideoData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.regex.Pattern

class VideoRepository(context: Context) {

    private val dao = TubeDatabase.getDatabase(context).tubeDao()
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Video streams
    fun getAllVideos(): List<VideoItem> = SampleVideoData.sampleVideos.filter { !it.isShort }

    fun getShorts(): List<VideoItem> = SampleVideoData.sampleVideos.filter { it.isShort }

    fun getVideoById(id: String): VideoItem? {
        return SampleVideoData.sampleVideos.find { it.id == id }
    }

    fun getVideosByCategory(category: String): List<VideoItem> {
        if (category == "Tất cả" || category == "All") {
            return getAllVideos()
        }
        return getAllVideos().filter { it.category.equals(category, ignoreCase = true) }
    }

    private fun String.unaccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(temp).replaceAll("")
            .replace('đ', 'd')
            .replace('Đ', 'd')
            .lowercase()
            .trim()
    }

    fun searchVideos(query: String): List<VideoItem> {
        if (query.isBlank()) return SampleVideoData.sampleVideos
        val normalizedQuery = query.unaccent()
        val tokens = normalizedQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }

        return SampleVideoData.sampleVideos.filter { video ->
            val fullSearchableText = "${video.title} ${video.channelName} ${video.description} ${video.category}".unaccent()
            // Match all search tokens (flexible order and accent-insensitive)
            tokens.all { token -> fullSearchableText.contains(token) }
        }
    }

    fun getCommentsForVideo(videoId: String): List<CommentItem> {
        return SampleVideoData.sampleComments
    }

    // Room Downloads
    fun getDownloadedVideos(): Flow<List<VideoItem>> {
        return dao.getAllDownloads().map { entities ->
            entities.map { it.toVideoItem() }
        }
    }

    suspend fun saveDownload(video: VideoItem, quality: String = "1080p60 HDR") = withContext(Dispatchers.IO) {
        val entity = DownloadedVideoEntity(
            id = video.id,
            title = video.title,
            description = video.description,
            channelName = video.channelName,
            channelAvatarUrl = video.channelAvatarUrl,
            thumbnailUrl = video.thumbnailUrl,
            videoUrl = video.videoUrl,
            durationSeconds = video.durationSeconds,
            durationFormatted = video.durationFormatted,
            viewCount = video.viewCount,
            publishedTime = video.publishedTime,
            category = video.category,
            quality = quality
        )
        dao.insertDownload(entity)
    }

    suspend fun removeDownload(videoId: String) = withContext(Dispatchers.IO) {
        dao.deleteDownload(videoId)
    }

    suspend fun isDownloaded(videoId: String): Boolean = withContext(Dispatchers.IO) {
        dao.isDownloaded(videoId)
    }

    // Watch History
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = dao.getWatchHistory()

    suspend fun recordHistory(video: VideoItem, currentPositionSeconds: Int = 0) = withContext(Dispatchers.IO) {
        val entity = WatchHistoryEntity(
            id = "${video.id}_${System.currentTimeMillis()}",
            videoId = video.id,
            title = video.title,
            channelName = video.channelName,
            thumbnailUrl = video.thumbnailUrl,
            videoUrl = video.videoUrl,
            durationSeconds = video.durationSeconds,
            durationFormatted = video.durationFormatted,
            lastPositionSeconds = currentPositionSeconds
        )
        dao.insertHistory(entity)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearHistory()
    }

    // Liked Videos
    fun getLikedVideos(): Flow<List<LikedVideoEntity>> = dao.getLikedVideos()

    suspend fun toggleLike(video: VideoItem, isLiked: Boolean, userId: String? = null) = withContext(Dispatchers.IO) {
        if (isLiked) {
            dao.insertLikedVideo(
                LikedVideoEntity(
                    videoId = video.id,
                    title = video.title,
                    channelName = video.channelName,
                    thumbnailUrl = video.thumbnailUrl
                )
            )
            // Sync to Firestore if signed in
            if (!userId.isNullOrBlank()) {
                try {
                    firestore.collection("users").document(userId)
                        .collection("liked_videos").document(video.id)
                        .set(mapOf(
                            "videoId" to video.id,
                            "title" to video.title,
                            "channelName" to video.channelName,
                            "thumbnailUrl" to video.thumbnailUrl,
                            "timestamp" to System.currentTimeMillis()
                        ))
                } catch (_: Exception) {}
            }
        } else {
            dao.deleteLikedVideo(video.id)
            if (!userId.isNullOrBlank()) {
                try {
                    firestore.collection("users").document(userId)
                        .collection("liked_videos").document(video.id)
                        .delete()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun isLiked(videoId: String): Boolean = withContext(Dispatchers.IO) {
        dao.isLiked(videoId)
    }

    // Playlists
    fun getPlaylists(): List<Playlist> {
        return SampleVideoData.samplePlaylists
    }
}
