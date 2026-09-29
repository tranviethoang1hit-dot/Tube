package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentItem(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isChannelOwner: Boolean = false,
    val isVipMember: Boolean = false
)
