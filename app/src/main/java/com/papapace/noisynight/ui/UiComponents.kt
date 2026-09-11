package com.papapace.noisynight.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.papapace.noisynight.NoiseAudioService.NoiseType
import com.papapace.noisynight.R
import java.util.Locale

// Custom Palette matching concept design
val MidnightBlue = Color(0xFF0B1424)
val DarkCardBlue = Color(0xFF16253F)
val GoldenAccent = Color(0xFFFFAE00)
val GoldenDim = Color(0x33FFAE00)
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF8B9FB4)

@Composable
fun NoiseCard(
    type: NoiseType,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = when (type) {
        NoiseType.HEAVY_RAIN -> R.drawable.ic_umbrella
        NoiseType.SOFT_RAIN -> R.drawable.ic_cloud_drizzle
        NoiseType.OCEAN_WAVES -> R.drawable.ic_ocean
        NoiseType.WIND_STORM -> R.drawable.ic_wind
        NoiseType.WHITE -> R.drawable.ic_wind
        NoiseType.GREEN -> R.drawable.ic_trees
        NoiseType.PINK -> R.drawable.ic_heart
        NoiseType.BROWN -> R.drawable.ic_cloud_lightning
        NoiseType.BLUE -> R.drawable.ic_droplet
        NoiseType.PURPLE -> R.drawable.ic_logo_waves
        NoiseType.DEEP_BROWN -> R.drawable.ic_moon
        NoiseType.AMBER -> R.drawable.ic_fire
    }

    val containerColor = if (isSelected) DarkCardBlue else DarkCardBlue.copy(alpha = 0.5f)
    val contentColor = if (isSelected) GoldenAccent else TextWhite
    val borderStroke = if (isSelected) {
        BorderStroke(2.dp, GoldenAccent)
    } else {
        BorderStroke(1.dp, Color(0xFF263959))
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = borderStroke,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp).fillMaxSize()
        ) {
            if (isSelected && isPlaying) {
                // Show Pause Icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(28.dp)
                ) {
                    Surface(color = contentColor, shape = RoundedCornerShape(2.dp), modifier = Modifier.size(8.dp, 22.dp)) {}
                    Surface(color = contentColor, shape = RoundedCornerShape(2.dp), modifier = Modifier.size(8.dp, 22.dp)) {}
                }
            } else {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = type.displayName,
                    tint = contentColor,
                    modifier = if (type == NoiseType.WHITE) Modifier.size(28.dp).rotate(90f) else Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = type.displayName.replace(" Noise", ""),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SleepTimerPanel(
    remainingMs: Long,
    totalMs: Long,
    onSelectDuration: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onShowCustomDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTimerActive = remainingMs > 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = DarkCardBlue
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_alarm_clock),
                    contentDescription = "Sleep Timer",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sleep Timer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isTimerActive) {
                    Text(
                        text = formatRemainingTime(remainingMs),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldenAccent,
                        modifier = Modifier.clickable { onCancelTimer() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quick Select: 8 hours
                val is8hActive = isTimerActive && totalMs == (8 * 60 * 60 * 1000L)
                TimerButton(
                    text = "8h",
                    isActive = is8hActive,
                    onClick = { onSelectDuration(8 * 60) },
                    modifier = Modifier.weight(1f)
                )

                // Quick Select: 12 hours
                val is12hActive = isTimerActive && totalMs == (12 * 60 * 60 * 1000L)
                TimerButton(
                    text = "12h",
                    isActive = is12hActive,
                    onClick = { onSelectDuration(12 * 60) },
                    modifier = Modifier.weight(1f)
                )

                // Custom Select button
                val isCustomActive = isTimerActive && !is8hActive && !is12hActive
                OutlinedButton(
                    onClick = onShowCustomDialog,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isCustomActive) GoldenAccent else TextWhite,
                        containerColor = if (isCustomActive) GoldenDim else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, if (isCustomActive) GoldenAccent else Color(0xFF263959)),
                    modifier = Modifier.size(54.dp, 44.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Custom duration",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isActive) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldenAccent,
                contentColor = MidnightBlue
            ),
            modifier = modifier.height(44.dp)
        ) {
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextWhite,
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, Color(0xFF263959)),
            modifier = modifier.height(44.dp)
        ) {
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}



@Composable
fun DurationPickerDialog(
    onDismiss: () -> Unit,
    onDurationConfirm: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(1) }
    var minutes by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Custom Timer",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        containerColor = DarkCardBlue,
        shape = RoundedCornerShape(24.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                // Visual Duration Display
                Text(
                    text = buildString {
                        if (hours > 0) append("$hours hr ")
                        if (minutes > 0 || hours == 0) append("$minutes min")
                    },
                    color = GoldenAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Senior/Child Friendly Large Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Hours Column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Hours", color = TextMuted, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PickerAdjustButton(text = "-", onClick = { if (hours > 0) hours-- })
                            Text(
                                text = hours.toString(),
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.Center
                            )
                            PickerAdjustButton(text = "+", onClick = { if (hours < 24) hours++ })
                        }
                    }

                    // Minutes Column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Minutes", color = TextMuted, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PickerAdjustButton(text = "-", onClick = {
                                if (minutes >= 15) minutes -= 15
                                else if (minutes > 0) minutes = 0
                            })
                            Text(
                                text = minutes.toString(),
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.Center
                            )
                            PickerAdjustButton(text = "+", onClick = {
                                if (minutes <= 45) minutes += 15
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalMinutes = hours * 60 + minutes
                    if (totalMinutes > 0) {
                        onDurationConfirm(totalMinutes)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldenAccent,
                    contentColor = MidnightBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Timer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextWhite
                ),
                border = BorderStroke(1.dp, Color(0xFF263959)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PickerAdjustButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF263959),
        contentColor = TextWhite,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatRemainingTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
