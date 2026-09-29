package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TubeDao {
    // Downloads
    @Query("SELECT * FROM downloaded_videos ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(video: DownloadedVideoEntity)

    @Query("DELETE FROM downloaded_videos WHERE id = :videoId")
    suspend fun deleteDownload(videoId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_videos WHERE id = :videoId)")
    suspend fun isDownloaded(videoId: String): Boolean

    // History
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 100")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()

    // Liked
    @Query("SELECT * FROM liked_videos ORDER BY likedAt DESC")
    fun getLikedVideos(): Flow<List<LikedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedVideo(liked: LikedVideoEntity)

    @Query("DELETE FROM liked_videos WHERE videoId = :videoId")
    suspend fun deleteLikedVideo(videoId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_videos WHERE videoId = :videoId)")
    suspend fun isLiked(videoId: String): Boolean

    // Playlists
    @Query("SELECT * FROM saved_playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<SavedPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: SavedPlaylistEntity)

    @Query("DELETE FROM saved_playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)
}
