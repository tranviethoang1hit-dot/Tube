package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "VIP Premium User",
    val email: String = "premium.user@tubepremium.app",
    val photoUrl: String = "",
    val isPremium: Boolean = true,
    val premiumPlan: String = "YouTube Premium Family",
    val memberSince: String = "Sep 2024",
    val backgroundPlaybackEnabled: Boolean = true,
    val smartAdBlockCount: Int = 1428,
    val offlineStorageUsageMb: Double = 345.5,
    val preferredQuality: String = "1080p60 HDR",
    val sleepTimerMinutes: Int = 0
)
