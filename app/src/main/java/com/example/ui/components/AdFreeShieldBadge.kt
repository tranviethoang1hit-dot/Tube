package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.YouTubeRed

@Composable
fun AdFreeShieldBadge(
    modifier: Modifier = Modifier,
    adBlockedCount: Int = 0
) {
    Box(
        modifier = modifier
            .testTag("ad_free_shield_badge")
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF201205),
                        Color(0xFF2D1600),
                        Color(0xFF1F0A00)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(PremiumGold, YouTubeRed)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Ad-Free Shield",
                tint = PremiumGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "VIP PREMIUM • 100% AD-FREE",
                color = PremiumGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            if (adBlockedCount > 0) {
                Text(
                    text = "($adBlockedCount ads blocked)",
                    color = Color(0xFFFFB300),
                    fontSize = 10.sp
                )
            }
        }
    }
}
