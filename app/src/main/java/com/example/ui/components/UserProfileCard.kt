package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Country
import com.example.model.UserProfile
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun UserProfileCard(
    userProfile: UserProfile,
    activeCountry: Country?,
    onChangeCountryClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = userProfile.xpProgress,
        animationSpec = tween(400),
        label = "xp_progress"
    )

    val animatedComboProgress by animateFloatAsState(
        targetValue = userProfile.comboProgress,
        animationSpec = tween(150),
        label = "combo_progress"
    )

    if (isCompact) {
        // High-density horizontal status bar (~32dp)
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: User level & title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldCrown)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "L${userProfile.level}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = userProfile.username,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Combo indicator
                    if (userProfile.comboMultiplier > 1) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = FlameOrange,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "x${userProfile.comboMultiplier}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = FlameOrange
                            )
                        }
                    }
                }

                // Right: Active country boost pill (clickable)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        activeCountry?.let { Color(it.colorHex).copy(alpha = 0.8f) } ?: NeonCyan
                    ),
                    modifier = Modifier
                        .clickable(onClick = onChangeCountryClick)
                        .testTag("select_active_country_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = activeCountry?.flag ?: "🌐", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeCountry?.name ?: "Select",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChangeCircle,
                            contentDescription = "Change",
                            tint = NeonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Full expanded profile card
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(18.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top Row: User Avatar, Level & Name, Active Country Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // User Avatar and Level
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                                    )
                                    .border(1.5.dp, GoldCrown, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⚡", fontSize = 22.sp)
                            }
                            // Level Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldCrown)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "L${userProfile.level}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "VIP",
                                    tint = GoldCrown,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = userProfile.title,
                                fontSize = 11.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    // Active Country Boost Pill (Clickable to switch)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceVariantDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            activeCountry?.let { Color(it.colorHex).copy(alpha = 0.8f) } ?: NeonCyan
                        ),
                        modifier = Modifier
                            .clickable(onClick = onChangeCountryClick)
                            .testTag("select_active_country_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeCountry?.flag ?: "🌐",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "ACTIVE BOOST",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = activeCountry?.name ?: "Select",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChangeCircle,
                                contentDescription = "Change country",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Middle: XP Bar with Level Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LEVEL ${userProfile.level} PROGRESS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = "${userProfile.xp} / ${userProfile.xpForNextLevel} XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonCyan,
                    trackColor = SurfaceVariantDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Status Row: Combo multiplier & Total Contributions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Combo Multiplier
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Combo",
                            tint = if (userProfile.comboMultiplier > 1) FlameOrange else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userProfile.comboMultiplier > 1) {
                                "COMBO x${userProfile.comboMultiplier}!"
                            } else {
                                "1x Tap Multiplier"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (userProfile.comboMultiplier > 1) FlameOrange else TextSecondary
                        )

                        if (userProfile.comboMultiplier > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SurfaceVariantDark)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedComboProgress)
                                        .height(4.dp)
                                        .background(FlameOrange)
                                )
                            }
                        }
                    }

                    // Total Contributions Stats
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Total Boosts",
                            tint = NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${Formatters.formatPoints(userProfile.totalBoostsGiven)} pts given",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
