package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CountryItem
import com.example.model.LiveEvent
import com.example.model.LiveEventType
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun AdminPanelDialog(
    isScoringActive: Boolean,
    onToggleScoring: (Boolean) -> Unit,
    pointsPerComment: Long,
    onChangePointsPerComment: (Long) -> Unit,
    superChatPointsPerDollar: Long,
    onChangeSuperChatPoints: (Long) -> Unit,
    giftPoints: Long,
    onChangeGiftPoints: (Long) -> Unit,
    roundNumber: Int,
    onStartNewRound: () -> Unit,
    onAddExtraTime: () -> Unit,
    isSimulationRunning: Boolean,
    onToggleSimulation: () -> Unit,
    simulationSpeed: Long,
    onChangeSimulationSpeed: (Long) -> Unit,
    isTransparentBackground: Boolean,
    onToggleTransparentBackground: (Boolean) -> Unit,
    roundTimerSeconds: Int?,
    onSetRoundTimerMinutes: (Int?) -> Unit,
    countries: List<CountryItem>,
    onManualAdjustScore: (String, Long) -> Unit,
    blockedViewers: Set<String>,
    onBlockViewer: (String) -> Unit,
    onUnblockViewer: (String) -> Unit,
    eventHistory: List<LiveEvent>,
    onResetScores: () -> Unit,
    onTriggerTestComment: () -> Unit,
    onTriggerTestLikeSub: () -> Unit,
    onTriggerTestSuperChat: () -> Unit,
    onTriggerTestGift: () -> Unit,
    onOpenYouTubeConnect: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Rules & Round", "Manual Scoring", "Viewers", "Live History")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldCrown),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldCrown.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = GoldCrown,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Control Panel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceVariantDark,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) NeonCyan else TextMuted
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> RulesAndRoundTab(
                            isScoringActive = isScoringActive,
                            onToggleScoring = onToggleScoring,
                            pointsPerComment = pointsPerComment,
                            onChangePointsPerComment = onChangePointsPerComment,
                            superChatPointsPerDollar = superChatPointsPerDollar,
                            onChangeSuperChatPoints = onChangeSuperChatPoints,
                            giftPoints = giftPoints,
                            onChangeGiftPoints = onChangeGiftPoints,
                            roundNumber = roundNumber,
                            onStartNewRound = onStartNewRound,
                            onAddExtraTime = onAddExtraTime,
                            roundTimerSeconds = roundTimerSeconds,
                            onSetRoundTimerMinutes = onSetRoundTimerMinutes,
                            isSimulationRunning = isSimulationRunning,
                            onToggleSimulation = onToggleSimulation,
                            simulationSpeed = simulationSpeed,
                            onChangeSimulationSpeed = onChangeSimulationSpeed,
                            isTransparentBackground = isTransparentBackground,
                            onToggleTransparentBackground = onToggleTransparentBackground,
                            onTriggerTestComment = onTriggerTestComment,
                            onTriggerTestLikeSub = onTriggerTestLikeSub,
                            onTriggerTestSuperChat = onTriggerTestSuperChat,
                            onTriggerTestGift = onTriggerTestGift,
                            onOpenYouTubeConnect = onOpenYouTubeConnect
                        )
                        1 -> ManualScoringTab(
                            countries = countries,
                            onManualAdjustScore = onManualAdjustScore,
                            onResetScores = onResetScores
                        )
                        2 -> ViewersAntiSpamTab(
                            blockedViewers = blockedViewers,
                            onBlockViewer = onBlockViewer,
                            onUnblockViewer = onUnblockViewer
                        )
                        3 -> LiveHistoryTab(
                            eventHistory = eventHistory,
                            onBlockViewer = onBlockViewer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RulesAndRoundTab(
    isScoringActive: Boolean,
    onToggleScoring: (Boolean) -> Unit,
    pointsPerComment: Long,
    onChangePointsPerComment: (Long) -> Unit,
    superChatPointsPerDollar: Long,
    onChangeSuperChatPoints: (Long) -> Unit,
    giftPoints: Long,
    onChangeGiftPoints: (Long) -> Unit,
    roundNumber: Int,
    onStartNewRound: () -> Unit,
    onAddExtraTime: () -> Unit,
    roundTimerSeconds: Int?,
    onSetRoundTimerMinutes: (Int?) -> Unit,
    isSimulationRunning: Boolean,
    onToggleSimulation: () -> Unit,
    simulationSpeed: Long,
    onChangeSimulationSpeed: (Long) -> Unit,
    isTransparentBackground: Boolean,
    onToggleTransparentBackground: (Boolean) -> Unit,
    onTriggerTestComment: () -> Unit,
    onTriggerTestLikeSub: () -> Unit,
    onTriggerTestSuperChat: () -> Unit,
    onTriggerTestGift: () -> Unit,
    onOpenYouTubeConnect: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        // Scoring Active Switch
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceVariantDark,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Scoring Engine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isScoringActive) "Active (processing points)" else "Paused (scores frozen)",
                            fontSize = 8.5.sp,
                            color = if (isScoringActive) NeonGreen else FlameOrange
                        )
                    }
                    Switch(
                        checked = isScoringActive,
                        onCheckedChange = onToggleScoring,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonGreen
                        )
                    )
                }
            }
        }

        // Scoring Rules & Multipliers
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceVariantDark,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Scoring Configuration",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldCrown
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Comment points
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Comment Points:", fontSize = 10.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1L, 2L, 5L, 10L).forEach { pts ->
                                Button(
                                    onClick = { onChangePointsPerComment(pts) },
                                    modifier = Modifier.height(24.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (pointsPerComment == pts) NeonCyan else SurfaceCard,
                                        contentColor = if (pointsPerComment == pts) Color.Black else TextSecondary
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text(text = "+$pts", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Super Chat points per dollar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Super Chat ($/pts):", fontSize = 10.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(500L, 1000L, 2000L).forEach { multiplier ->
                                Button(
                                    onClick = { onChangeSuperChatPoints(multiplier) },
                                    modifier = Modifier.height(24.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (superChatPointsPerDollar == multiplier) GoldCrown else SurfaceCard,
                                        contentColor = if (superChatPointsPerDollar == multiplier) Color.Black else TextSecondary
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text(text = "$multiplier/$$", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Gift points
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Gift Points:", fontSize = 10.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(25L, 50L, 100L).forEach { pts ->
                                Button(
                                    onClick = { onChangeGiftPoints(pts) },
                                    modifier = Modifier.height(24.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (giftPoints == pts) NeonMagenta else SurfaceCard,
                                        contentColor = if (giftPoints == pts) Color.White else TextSecondary
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text(text = "+$pts", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Round Controls
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceVariantDark,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Round Management (Round #$roundNumber)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Button(
                            onClick = onStartNewRound,
                            modifier = Modifier.height(26.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldCrown,
                                contentColor = Color.Black
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(text = "NEW ROUND", fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onAddExtraTime,
                            modifier = Modifier.weight(1f).height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceCard,
                                contentColor = NeonGreen
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(text = "+1 MIN", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        listOf(Pair("3 min", 3), Pair("5 min", 5), Pair("10 min", 10)).forEach { (label, min) ->
                            Button(
                                onClick = { onSetRoundTimerMinutes(min) },
                                modifier = Modifier.weight(1f).height(28.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceCard,
                                    contentColor = NeonCyan
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Test Triggers
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceVariantDark,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Instant Event Testing",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onTriggerTestSuperChat,
                            modifier = Modifier.weight(1f).height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A1E06),
                                contentColor = GoldCrown
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(text = "⚡ $5 SuperChat", fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = onTriggerTestGift,
                            modifier = Modifier.weight(1f).height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A0620),
                                contentColor = NeonMagenta
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(text = "🎁 Test Gift", fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onTriggerTestComment,
                            modifier = Modifier.weight(1f).height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF06281E),
                                contentColor = NeonGreen
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(text = "💬 Test Comment", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onTriggerTestLikeSub,
                            modifier = Modifier.weight(1f).height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF281E06),
                                contentColor = FlameOrange
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(text = "🔔 Like/Sub", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // OBS Transparent Background & YouTube Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenYouTubeConnect,
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiveRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "LINK YOUTUBE", fontSize = 9.sp, fontWeight = FontWeight.Black)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "OBS Overlay", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Switch(
                            checked = isTransparentBackground,
                            onCheckedChange = onToggleTransparentBackground,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonCyan
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualScoringTab(
    countries: List<CountryItem>,
    onManualAdjustScore: (String, Long) -> Unit,
    onResetScores: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(countries.firstOrNull()) }

    Column(modifier = Modifier.fillMaxHeight()) {
        // Quick Selector for Country
        Text(
            text = "Select Country to Adjust:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(countries) { country ->
                val isSelected = selectedCountry?.id == country.id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF1E2844) else SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 1.2.dp else 0.6.dp,
                        if (isSelected) NeonCyan else SurfaceCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCountry = country }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = country.flag, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = country.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Formatters.formatScore(country.score),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldCrown
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Adjustment buttons
                            listOf(Pair("+10", 10L), Pair("+50", 50L), Pair("+500", 500L), Pair("-10", -10L)).forEach { (label, delta) ->
                                Button(
                                    onClick = { onManualAdjustScore(country.id, delta) },
                                    modifier = Modifier
                                        .padding(start = 3.dp)
                                        .height(24.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (delta > 0) Color(0xFF0F3A2E) else Color(0xFF3A0F1E),
                                        contentColor = if (delta > 0) NeonGreen else LiveRed
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onResetScores,
            modifier = Modifier.fillMaxWidth().height(32.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3B0B17),
                contentColor = LiveRed
            )
        ) {
            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "RESET ALL SCORES TO 0", fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ViewersAntiSpamTab(
    blockedViewers: Set<String>,
    onBlockViewer: (String) -> Unit,
    onUnblockViewer: (String) -> Unit
) {
    var viewerToBlock by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxHeight()) {
        Text(
            text = "Anti-Spam & Viewer Moderation",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewerToBlock,
                onValueChange = { viewerToBlock = it },
                placeholder = { Text("Username or Channel ID", fontSize = 10.sp, color = TextMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LiveRed,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    if (viewerToBlock.isNotBlank()) {
                        onBlockViewer(viewerToBlock.trim())
                        viewerToBlock = ""
                    }
                },
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiveRed,
                    contentColor = Color.White
                )
            ) {
                Text(text = "BLOCK", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Blocked Viewers (${blockedViewers.size}):",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (blockedViewers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No viewers currently blocked.",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(blockedViewers.toList()) { blocked ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SurfaceVariantDark,
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, LiveRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = blocked, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { onUnblockViewer(blocked) },
                                modifier = Modifier.height(24.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceCard,
                                    contentColor = NeonGreen
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                            ) {
                                Text(text = "UNBLOCK", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveHistoryTab(
    eventHistory: List<LiveEvent>,
    onBlockViewer: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Text(
            text = "Recent Chat & Super Chat Feed (${eventHistory.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (eventHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Waiting for live comments...", fontSize = 10.sp, color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(eventHistory) { event ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (event.type) {
                            LiveEventType.SUPER_CHAT -> Color(0xFF231804)
                            LiveEventType.GIFT -> Color(0xFF22081C)
                            else -> SurfaceVariantDark
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            0.6.dp,
                            when (event.type) {
                                LiveEventType.SUPER_CHAT -> GoldCrown
                                LiveEventType.GIFT -> NeonMagenta
                                else -> SurfaceCardBorder
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${event.countryFlag} @${event.username}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (event.type == LiveEventType.SUPER_CHAT) GoldCrown else TextPrimary
                                    )
                                    if (event.userId.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "(${event.userId})", fontSize = 8.5.sp, color = TextMuted)
                                    }
                                }
                                Text(
                                    text = event.message,
                                    fontSize = 9.5.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+${event.points}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (event.type == LiveEventType.SUPER_CHAT) GoldCrown else NeonGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { onBlockViewer(event.username) },
                                    modifier = Modifier.height(20.dp),
                                    shape = RoundedCornerShape(3.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF381017),
                                        contentColor = LiveRed
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text(text = "BLOCK", fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
