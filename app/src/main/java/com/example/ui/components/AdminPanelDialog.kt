package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

@Composable
fun AdminPanelDialog(
    isSimulationRunning: Boolean,
    onToggleSimulation: () -> Unit,
    simulationSpeed: Long,
    onChangeSimulationSpeed: (Long) -> Unit,
    isTransparentBackground: Boolean,
    onToggleTransparentBackground: (Boolean) -> Unit,
    roundTimerSeconds: Int?,
    onSetRoundTimerMinutes: (Int?) -> Unit,
    onResetScores: () -> Unit,
    onTriggerTestComment: () -> Unit,
    onTriggerTestLikeSub: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldCrown),
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
                            text = "Admin & OBS Settings",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Transparent background for OBS Browser Source
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
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
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "OBS Transparent Background",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Removes solid dark background for overlays",
                                    fontSize = 8.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isTransparentBackground,
                            onCheckedChange = onToggleTransparentBackground,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("obs_transparent_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section 2: Round Timer
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = GoldCrown,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Round Timer: ${if (roundTimerSeconds != null) "${roundTimerSeconds / 60}m ${roundTimerSeconds % 60}s" else "Disabled"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(Pair("3 min", 3), Pair("5 min", 5), Pair("10 min", 10)).forEach { (label, min) ->
                                Button(
                                    onClick = { onSetRoundTimerMinutes(min) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp),
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
                            Button(
                                onClick = { onSetRoundTimerMinutes(null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceCard,
                                    contentColor = LiveRed
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(text = "Off", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section 3: Mock Event Simulator Controls
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mock Event Simulator",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Button(
                                onClick = onToggleSimulation,
                                modifier = Modifier.height(26.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSimulationRunning) LiveRed else NeonGreen,
                                    contentColor = if (isSimulationRunning) Color.White else Color.Black
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isSimulationRunning) "PAUSE" else "START",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Speed: ${(simulationSpeed / 1000f)}s per event",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(Pair("Fast (0.7s)", 700L), Pair("Normal (1.4s)", 1400L), Pair("Slow (2.5s)", 2500L)).forEach { (label, speed) ->
                                Button(
                                    onClick = { onChangeSimulationSpeed(speed) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (simulationSpeed == speed) NeonCyan else SurfaceCard,
                                        contentColor = if (simulationSpeed == speed) Color.Black else TextSecondary
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Manual Trigger Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onTriggerTestComment,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceCard,
                                    contentColor = NeonGreen
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(text = "+1 Comment", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = onTriggerTestLikeSub,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceCard,
                                    contentColor = GoldCrown
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(text = "+400 Like & Sub", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 4: Reset All Scores
                Button(
                    onClick = {
                        onResetScores()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("reset_scores_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiveRed.copy(alpha = 0.2f),
                        contentColor = LiveRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LiveRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RESET ALL SCORES TO ZERO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
