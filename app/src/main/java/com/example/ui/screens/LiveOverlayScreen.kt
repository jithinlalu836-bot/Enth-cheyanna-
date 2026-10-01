package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.YouTubeConnectionState
import com.example.ui.components.ActionCards
import com.example.ui.components.ActivityBar
import com.example.ui.components.AdminPanelDialog
import com.example.ui.components.CoinBurst
import com.example.ui.components.CommentBanner
import com.example.ui.components.CountdownTimerBar
import com.example.ui.components.FlagGrid
import com.example.ui.components.Header
import com.example.ui.components.TopChatters
import com.example.ui.components.YouTubeLiveConnectDialog
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.viewmodel.LiveOverlayViewModel

@Composable
fun LiveOverlayScreen(
    viewModel: LiveOverlayViewModel,
    modifier: Modifier = Modifier
) {
    val countries by viewModel.countries.collectAsState()
    val topChatters by viewModel.topChatters.collectAsState()
    val latestEvent by viewModel.latestEvent.collectAsState()
    val coinBursts by viewModel.coinBursts.collectAsState()
    val isSimulationRunning by viewModel.isSimulationRunning.collectAsState()
    val simulationSpeed by viewModel.simulationSpeed.collectAsState()
    val isTransparentBackground by viewModel.isTransparentBackground.collectAsState()
    val roundTimerSeconds by viewModel.roundTimeRemaining.collectAsState()
    val totalRoundDuration by viewModel.totalRoundDuration.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val isRoundFinished by viewModel.isRoundFinished.collectAsState()
    val youTubeConnectionState by viewModel.youTubeConnectionState.collectAsState()
    val savedYouTubeUrl by viewModel.savedYouTubeUrl.collectAsState()
    val savedYouTubeApiKey by viewModel.savedYouTubeApiKey.collectAsState()
    val googleAuthState by viewModel.googleAuthState.collectAsState()
    val activeCommentBanner by viewModel.activeCommentBanner.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current
    var showAdminDialog by remember { mutableStateOf(false) }
    var showYouTubeDialog by remember { mutableStateOf(false) }

    // Theme: Dark navy/purple gradient background (or transparent if OBS mode enabled)
    val backgroundBrush = if (isTransparentBackground) {
        Brush.verticalGradient(
            listOf(
                Color(0x22050814),
                Color(0x330B0F20)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFF090C1A),
                Color(0xFF0F142A),
                Color(0xFF140D24),
                Color(0xFF090C1A)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Main Mobile-First Portrait (9:16) Column layout from top to bottom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header: "✦ BOOST YOUR COUNTRY ✦" in bold white with a soft glow
            Header(
                isSimulationRunning = isSimulationRunning,
                onToggleSimulation = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.toggleSimulation()
                },
                isYouTubeConnected = youTubeConnectionState is YouTubeConnectionState.Connected,
                onOpenYouTubeConnect = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showYouTubeDialog = true
                },
                roundTimeRemainingSeconds = roundTimerSeconds
            )

            // Prominent Round Countdown Timer
            CountdownTimerBar(
                remainingSeconds = roundTimerSeconds,
                totalDurationSeconds = totalRoundDuration,
                isTimerRunning = isTimerRunning,
                isRoundFinished = isRoundFinished,
                winningCountry = countries.firstOrNull(),
                onTogglePause = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.toggleTimerPause()
                },
                onAddExtraTime = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.addExtraTime(60)
                },
                onRestartRound = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.startCountdownTimer(300)
                }
            )

            // 2. Two action cards side by side
            // - "Comment +1 / Lv 5 +5 Point" (green glowing border, chat bubble icon)
            // - "Like & Subscribe +400 Points" (dark card, yellow bell icon)
            ActionCards(
                onCommentClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.triggerCommentAction()
                },
                onLikeSubscribeClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.triggerLikeSubscribeAction()
                }
            )

            // 3. Latest-activity bar: "Level 1 @username Country +1", with a pink line above and a cyan line below
            ActivityBar(latestEvent = latestEvent)

            // 4. "Top Chatter" panel (gold border, dark background): "Top 5" label at top right and a horizontally scrollable row of cards
            TopChatters(chatters = topChatters)

            // 5. Country leaderboard grid: 9 columns of flag tiles with the score under each flag
            // - Sorted by score, highest first
            // - Top 3 get crown, silver, bronze medal, #1 flag has yellow highlight border
            // - Countries with 0 points dimmed at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                FlagGrid(
                    countries = countries,
                    onCountryClick = { country ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onCountryClick(country)
                    }
                )
            }
        }

        // 6. A small floating settings gear on the right edge
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-4).dp)
                .size(34.dp)
                .clip(CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showAdminDialog = true
                }
                .testTag("floating_settings_gear_button"),
            shape = CircleShape,
            color = Color(0xFF161F38),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldCrown)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Admin Settings",
                    tint = GoldCrown,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Floating Gold Coins Burst & Popups animation overlay
        CoinBurst(bursts = coinBursts)

        // Admin Panel & OBS Settings Dialog
        if (showAdminDialog) {
            AdminPanelDialog(
                isSimulationRunning = isSimulationRunning,
                onToggleSimulation = { viewModel.toggleSimulation() },
                simulationSpeed = simulationSpeed,
                onChangeSimulationSpeed = { viewModel.setSimulationSpeed(it) },
                isTransparentBackground = isTransparentBackground,
                onToggleTransparentBackground = { viewModel.toggleTransparentBackground(it) },
                roundTimerSeconds = roundTimerSeconds,
                onSetRoundTimerMinutes = { viewModel.setRoundTimerMinutes(it) },
                onResetScores = { viewModel.resetAllScores() },
                onTriggerTestComment = { viewModel.triggerCommentAction() },
                onTriggerTestLikeSub = { viewModel.triggerLikeSubscribeAction() },
                onDismiss = { showAdminDialog = false }
            )
        }

        // YouTube Live Chat Connection Dialog
        if (showYouTubeDialog) {
            YouTubeLiveConnectDialog(
                connectionState = youTubeConnectionState,
                googleAuthState = googleAuthState,
                savedUrl = savedYouTubeUrl,
                savedApiKey = savedYouTubeApiKey,
                onSignInWithGoogle = {
                    activity?.let {
                        viewModel.signInAndConnectYouTube(it, savedYouTubeUrl)
                    }
                },
                onSignOutGoogle = { viewModel.signOutGoogle() },
                onConnect = { url, key -> viewModel.connectYouTube(url, key) },
                onDisconnect = { viewModel.disconnectYouTube() },
                onSimulateTestComment = { author, comment -> viewModel.simulateTestComment(author, comment) },
                onDismiss = { showYouTubeDialog = false }
            )
        }
    }
}
