package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import com.example.model.Chatter
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun TopChatters(
    chatters: List<Chatter>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("top_chatter_panel"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F1424),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldCrown)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        ) {
            // Header: Title on left, "Top 5" label at top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TOP CHATTER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.8.sp
                    )
                }

                // "Top 5" badge at top right
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldCrown)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Top 5",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Horizontally scrollable row of cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                itemsIndexed(chatters.take(5)) { index, chatter ->
                    ChatterCard(rank = index + 1, chatter = chatter)
                }
            }
        }
    }
}

@Composable
private fun ChatterCard(
    rank: Int,
    chatter: Chatter
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariantDark,
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            if (rank == 1) GoldCrown else SurfaceCardBorder
        ),
        modifier = Modifier.width(96.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar frame with initials & Flag badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(chatter.avatarColorHex))
                        .border(
                            1.dp,
                            if (rank == 1) GoldCrown else Color.White.copy(alpha = 0.6f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chatter.initials,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Country Flag Badge overlay
                Text(
                    text = chatter.countryFlag,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(bottom = 0.dp)
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            Column {
                Text(
                    text = "@${chatter.username}",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${chatter.chatCount} chats",
                    fontSize = 7.5.sp,
                    color = TextSecondary
                )
                Text(
                    text = "+${Formatters.formatPoints(chatter.pointsEarned)} pts",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldCrown
                )
            }
        }
    }
}
