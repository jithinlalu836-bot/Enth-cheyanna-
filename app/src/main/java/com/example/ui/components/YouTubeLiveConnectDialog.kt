package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.auth.GoogleAuthState
import com.example.service.YouTubeConnectionState
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YouTubeLiveConnectDialog(
    connectionState: YouTubeConnectionState,
    googleAuthState: GoogleAuthState,
    savedUrl: String,
    savedApiKey: String,
    onSignInWithGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onConnect: (url: String, apiKey: String) -> Unit,
    onDisconnect: () -> Unit,
    onSimulateTestComment: (author: String, comment: String) -> Unit,
    onDismiss: () -> Unit
) {
    var urlInput by remember { mutableStateOf(savedUrl) }
    var apiKeyInput by remember { mutableStateOf(savedApiKey) }
    var testCommentInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val isConnected = connectionState is YouTubeConnectionState.Connected
    val isConnecting = connectionState is YouTubeConnectionState.Connecting
    val isAuthLoading = googleAuthState is GoogleAuthState.Loading

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, LiveRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LiveRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = LiveRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Connect YouTube Live",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Powered by Google Identity Services",
                                fontSize = 8.5.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Action: Google Identity Services "Connect YouTube" button
                if (googleAuthState is GoogleAuthState.Authenticated) {
                    val profile = googleAuthState.profile
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F1E28),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile.displayName.take(1).uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = NeonGreen,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                    Text(
                                        text = profile.email,
                                        fontSize = 8.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = onSignOutGoogle,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Sign Out",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onSignInWithGoogle,
                        enabled = !isAuthLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("google_identity_connect_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2937)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Authenticating with Google...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                            } else {
                                Text(text = "G", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connect YouTube Account",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Connection & Live Chat Stream ID Status Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (connectionState) {
                        is YouTubeConnectionState.Connected -> Color(0xFF09291E)
                        is YouTubeConnectionState.Connecting -> Color(0xFF2E220D)
                        is YouTubeConnectionState.Error -> Color(0xFF2E0D14)
                        else -> SurfaceVariantDark
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        when (connectionState) {
                            is YouTubeConnectionState.Connected -> NeonGreen
                            is YouTubeConnectionState.Connecting -> GoldCrown
                            is YouTubeConnectionState.Error -> LiveRed
                            else -> SurfaceCardBorder
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (connectionState) {
                            is YouTubeConnectionState.Connected -> {
                                Icon(
                                    imageVector = Icons.Default.Stream,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "LIVE: ${connectionState.streamTitle}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Chat Stream ID: ${connectionState.liveChatId.take(18)}...",
                                        fontSize = 8.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            is YouTubeConnectionState.Connecting -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = GoldCrown
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retrieving YouTube Live Chat Stream ID...",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldCrown
                                )
                            }
                            is YouTubeConnectionState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = LiveRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = connectionState.message,
                                    fontSize = 8.5.sp,
                                    color = LiveRed,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ready to retrieve YouTube Live Chat ID",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // YouTube Live URL input
                Text(
                    text = "YouTube Live URL / Video ID",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = {
                        Text(
                            text = "https://youtube.com/live/your_id or video ID",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { urlInput = it }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("youtube_url_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiveRed,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedContainerColor = SurfaceVariantDark,
                        unfocusedContainerColor = SurfaceVariantDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons: Connect / Disconnect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isConnected) {
                        Button(
                            onClick = onDisconnect,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceVariantDark,
                                contentColor = LiveRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LiveRed)
                        ) {
                            Text(text = "DISCONNECT STREAM", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onConnect(urlInput.trim(), apiKeyInput.trim()) },
                            enabled = !isConnecting && urlInput.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("connect_youtube_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiveRed,
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isConnecting) "FETCHING STREAM ID..." else "RETRIEVE LIVE CHAT",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Comments Section
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💬", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test Comment & Country Points",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val samples = listOf(
                            Pair("Fatima", "🇹🇷 Turkey number one!"),
                            Pair("Aarav", "India 🇮🇳 push to top!"),
                            Pair("Budi", "Indonesia 🇮🇩 mantap!"),
                            Pair("John", "USA 🇺🇸 +1")
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            samples.forEach { (author, text) ->
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = SurfaceCard,
                                    border = androidx.compose.foundation.BorderStroke(0.6.dp, NeonCyan.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        onSimulateTestComment(author, text)
                                    }
                                ) {
                                    Text(
                                        text = text,
                                        fontSize = 8.sp,
                                        color = NeonCyan,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
