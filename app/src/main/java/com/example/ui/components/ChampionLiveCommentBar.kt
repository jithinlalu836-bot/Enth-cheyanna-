package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveChatMessage
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
fun ChampionLiveCommentBar(
    latestMessage: LiveChatMessage?,
    onOpenCommentDialog: () -> Unit,
    onOpenAllCommentsDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onOpenAllCommentsDialog)
            .testTag("champion_live_comment_bar"),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0F172A),
                            SurfaceCard,
                            Color(0xFF131D33)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Live Comment Label & Pulse indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = dotAlpha))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Comments",
                    tint = NeonCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LIVE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGreen,
                    letterSpacing = 0.5.sp
                )
            }

            // Center: Animated Latest Comment display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                if (latestMessage != null) {
                    AnimatedContent(
                        targetState = latestMessage,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut()
                            )
                        },
                        label = "comment_ticker"
                    ) { message ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = message.countryFlag,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${message.userName}: ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (message.isUser) NeonCyan else TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = message.message,
                                fontSize = 9.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${Formatters.formatPoints(message.boostAmount)}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Be the first to comment & boost your country!",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }

            // Right side: Comment Options
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Option 1: Quick Add Comment button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, NeonCyan),
                    modifier = Modifier
                        .clickable(onClick = onOpenCommentDialog)
                        .testTag("champion_comment_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = "Add cheer",
                            tint = NeonCyan,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+CHEER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    }
                }

                // Option 2: View All Comments button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                    modifier = Modifier
                        .clickable(onClick = onOpenAllCommentsDialog)
                        .testTag("champion_view_all_comments_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "View all comments",
                            tint = TextSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "FEED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
