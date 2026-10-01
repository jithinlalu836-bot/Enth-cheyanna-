package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveEvent
import com.example.model.LiveEventType
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActivityBar(
    latestEvent: LiveEvent?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("activity_bar")
    ) {
        // Pink line above
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.8.dp)
                .background(NeonMagenta)
        )

        // Activity content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C101D))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            if (latestEvent != null) {
                AnimatedContent(
                    targetState = latestEvent,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut()
                        )
                    },
                    label = "activity_text"
                ) { event ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Level badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (event.type == LiveEventType.LIKE_SUBSCRIBE) GoldCrown else NeonMagenta
                                )
                                .padding(horizontal = 4.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = if (event.type == LiveEventType.LIKE_SUBSCRIBE) "VIP" else "Level ${event.level}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        // Username
                        Text(
                            text = "@${event.username}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        // Country Flag & Name
                        Text(
                            text = "${event.countryFlag} ${event.countryName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        // Points earned
                        Text(
                            text = "+${event.points}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (event.type == LiveEventType.LIKE_SUBSCRIBE) GoldCrown else NeonGreen
                        )
                    }
                }
            } else {
                Text(
                    text = "Level 1 @username Country +1",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }

        // Cyan line below
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.8.dp)
                .background(NeonCyan)
        )
    }
}
