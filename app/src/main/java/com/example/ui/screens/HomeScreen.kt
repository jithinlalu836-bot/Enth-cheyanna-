package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ChampionLiveCommentBar
import com.example.ui.components.CommentDialog
import com.example.ui.components.CountrySelectDialog
import com.example.ui.components.FloatingParticleOverlay
import com.example.ui.components.GlobalLeaderboardGrid
import com.example.ui.components.HeaderSection
import com.example.ui.components.LiveChatTicker
import com.example.ui.components.LiveCommentsFeedDialog
import com.example.ui.components.PodiumSection
import com.example.ui.components.SingleScreenFlagGrid
import com.example.ui.components.UserProfileCard
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BoostViewModel

@Composable
fun HomeScreen(
    viewModel: BoostViewModel,
    modifier: Modifier = Modifier
) {
    val countries by viewModel.countries.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val liveMessages by viewModel.liveMessages.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedContinent by viewModel.selectedContinent.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val isSimulationActive by viewModel.isLiveSimulationEnabled.collectAsState()

    val haptic = LocalHapticFeedback.current

    var showCommentDialog by remember { mutableStateOf(false) }
    var showAllCommentsDialog by remember { mutableStateOf(false) }
    var showCountrySelectDialog by remember { mutableStateOf(false) }

    // User requested: "Made the flags fit in a single screen"
    // By default, enable Single Screen Arena mode so all flags fit simultaneously on the screen!
    var isSingleScreenMode by remember { mutableStateOf(true) }

    val activeCountry = countries.find { it.id == userProfile.activeCountryId }
        ?: countries.firstOrNull()

    // Pulse animation for the mega boost FAB
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_pulse"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        containerColor = BackgroundDark,
        floatingActionButton = {
            if (!isSingleScreenMode) {
                // Floating Mega Boost Action Button in Scrollable Feed mode
                Surface(
                    modifier = Modifier
                        .scale(if (userProfile.comboMultiplier > 1) fabScale else 1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.boostActiveCountry(basePoints = 100L)
                        }
                        .testTag("floating_mega_boost_button")
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (userProfile.comboMultiplier > 1) FlameOrange else NeonCyan
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    if (userProfile.comboMultiplier > 1) {
                                        listOf(Color(0xFF8B2500), FlameOrange)
                                    } else {
                                        listOf(Color(0xFF0F3854), NeonCyan)
                                    }
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = activeCountry?.flag ?: "⚡",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TAP BOOST +${100 * userProfile.comboMultiplier}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = if (userProfile.comboMultiplier > 1) Color.White else Color.Black
                                    )
                                    if (userProfile.comboMultiplier > 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = GoldCrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Supercharging ${activeCountry?.name ?: "Nation"}",
                                    fontSize = 10.sp,
                                    color = if (userProfile.comboMultiplier > 1) GoldCrown else Color.Black.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSingleScreenMode) {
                // ========================================================
                // SINGLE SCREEN ARENA MODE (All Flags Fit in One Screen!)
                // ========================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Header with Title & Action Cards (Comment, Like & Sub, Turbo Tap)
                    HeaderSection(
                        onCommentClick = { showCommentDialog = true },
                        onLikeSubscribeClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.likeAndSubscribeAction()
                        },
                        onTurboClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.boostActiveCountry(basePoints = 100L)
                        },
                        isGridView = isGridView,
                        onToggleView = { viewModel.toggleViewMode() },
                        isSimulationActive = isSimulationActive,
                        onToggleSimulation = { viewModel.toggleLiveSimulation() },
                        isCompact = true,
                        screenModeLabel = "Fit Screen",
                        onToggleScreenMode = { isSingleScreenMode = false }
                    )

                    // 2. User Status & Active Country Pill Bar
                    UserProfileCard(
                        userProfile = userProfile,
                        activeCountry = activeCountry,
                        onChangeCountryClick = { showCountrySelectDialog = true },
                        isCompact = true
                    )

                    // 3. *** LIVE COMMENT SHOWING OPTION (Positioned ABOVE the top champion!) ***
                    ChampionLiveCommentBar(
                        latestMessage = liveMessages.firstOrNull(),
                        onOpenCommentDialog = { showCommentDialog = true },
                        onOpenAllCommentsDialog = { showAllCommentsDialog = true }
                    )

                    // 4. Compact Top 3 Champions Podium
                    PodiumSection(
                        topCountries = countries.take(3),
                        onCountryBoostClick = { countryId ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.boostCountry(
                                countryId = countryId,
                                basePoints = 50L,
                                isUserAction = true
                            )
                        },
                        isCompact = true
                    )

                    // 5. Global Flag Grid Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GLOBAL FLAGS ARENA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "${countries.size} Flags • Tap to Boost",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                    }

                    // 6. Single Screen Flag Grid (Fills available vertical space so ALL flags fit together)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        SingleScreenFlagGrid(
                            countries = countries,
                            activeCountryId = userProfile.activeCountryId,
                            onCountryBoostClick = { countryId ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.boostCountry(
                                    countryId = countryId,
                                    basePoints = 50L,
                                    isUserAction = true
                                )
                            }
                        )
                    }

                    // 7. Bottom Boost Action Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (userProfile.comboMultiplier > 1) FlameOrange else NeonCyan.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Current active nation info
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeCountry?.flag ?: "⚡",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "BOOSTING: ${activeCountry?.name ?: "Nation"}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${countries.find { it.id == userProfile.activeCountryId }?.score?.let { com.example.util.Formatters.formatPoints(it) } ?: "0"} pts • Rank #${activeCountry?.currentRank ?: 1}",
                                        fontSize = 9.sp,
                                        color = NeonCyan
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // 1-Tap Mega Boost Button
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.boostActiveCountry(basePoints = 100L)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userProfile.comboMultiplier > 1) FlameOrange else NeonCyan,
                                    contentColor = if (userProfile.comboMultiplier > 1) Color.White else Color.Black
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("floating_mega_boost_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "Boost",
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "BOOST +${100 * userProfile.comboMultiplier}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ========================================================
                // EXTENDED SCROLLABLE FEED MODE (Expanded Cards & Full Chat)
                // ========================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    // Hero Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.battle_banner_1790787326771),
                                contentDescription = "Esports Battle Arena",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Dark Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                BackgroundDark.copy(alpha = 0.7f),
                                                BackgroundDark
                                            )
                                        )
                                    )
                            )
                            // Arena Status text overlay
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonPurple.copy(alpha = 0.85f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "SEASON 5 BATTLE ROYALE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    // Header Section
                    item {
                        HeaderSection(
                            onCommentClick = { showCommentDialog = true },
                            onLikeSubscribeClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.likeAndSubscribeAction()
                            },
                            onTurboClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.boostActiveCountry(basePoints = 100L)
                            },
                            isGridView = isGridView,
                            onToggleView = { viewModel.toggleViewMode() },
                            isSimulationActive = isSimulationActive,
                            onToggleSimulation = { viewModel.toggleLiveSimulation() },
                            isCompact = false,
                            screenModeLabel = "Feed View",
                            onToggleScreenMode = { isSingleScreenMode = true }
                        )
                    }

                    // User Profile & Status Card
                    item {
                        UserProfileCard(
                            userProfile = userProfile,
                            activeCountry = activeCountry,
                            onChangeCountryClick = { showCountrySelectDialog = true },
                            isCompact = false
                        )
                    }

                    // *** Live Comment Bar positioned ABOVE the top champion ***
                    item {
                        ChampionLiveCommentBar(
                            latestMessage = liveMessages.firstOrNull(),
                            onOpenCommentDialog = { showCommentDialog = true },
                            onOpenAllCommentsDialog = { showAllCommentsDialog = true },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Top 3 Podium Section
                    item {
                        PodiumSection(
                            topCountries = countries.take(3),
                            onCountryBoostClick = { countryId ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.boostCountry(
                                    countryId = countryId,
                                    basePoints = 50L,
                                    isUserAction = true
                                )
                            },
                            isCompact = false
                        )
                    }

                    // Live Chat Feed Ticker
                    item {
                        LiveChatTicker(
                            messages = liveMessages,
                            onSendComment = { text ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.commentAction(text)
                            }
                        )
                    }

                    // Global Leaderboard Grid / List
                    item {
                        GlobalLeaderboardGrid(
                            countries = countries,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                            selectedContinent = selectedContinent,
                            onContinentSelect = { viewModel.onContinentSelected(it) },
                            isGridView = isGridView,
                            onCountryBoostClick = { countryId ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.boostCountry(
                                    countryId = countryId,
                                    basePoints = 50L,
                                    isUserAction = true
                                )
                            },
                            activeCountryId = userProfile.activeCountryId
                        )
                    }
                }
            }

            // Floating particle animation overlay on top
            FloatingParticleOverlay(particles = particles)

            // Dialogs
            if (showCommentDialog) {
                CommentDialog(
                    activeCountry = activeCountry,
                    userLevel = userProfile.level,
                    onDismiss = { showCommentDialog = false },
                    onSendComment = { comment ->
                        viewModel.commentAction(comment)
                    }
                )
            }

            if (showAllCommentsDialog) {
                LiveCommentsFeedDialog(
                    messages = liveMessages,
                    onDismiss = { showAllCommentsDialog = false },
                    onSendComment = { comment ->
                        viewModel.commentAction(comment)
                    }
                )
            }

            if (showCountrySelectDialog) {
                CountrySelectDialog(
                    countries = countries,
                    activeCountryId = userProfile.activeCountryId,
                    onCountrySelected = { countryId ->
                        viewModel.setActiveCountry(countryId)
                    },
                    onDismiss = { showCountrySelectDialog = false }
                )
            }
        }
    }
}
