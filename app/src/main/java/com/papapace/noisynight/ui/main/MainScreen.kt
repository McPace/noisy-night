package com.papapace.noisynight.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.papapace.noisynight.NoiseAudioService
import com.papapace.noisynight.NoiseAudioService.NoiseType
import com.papapace.noisynight.R
import com.papapace.noisynight.ui.DarkCardBlue
import com.papapace.noisynight.ui.DurationPickerDialog
import com.papapace.noisynight.ui.GoldenAccent
import com.papapace.noisynight.ui.MidnightBlue
import com.papapace.noisynight.ui.NoiseCard
import com.papapace.noisynight.ui.SleepTimerPanel
import com.papapace.noisynight.ui.TextMuted
import com.papapace.noisynight.ui.TextWhite

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    service: NoiseAudioService,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { 
            NoisyNightTopBar(
                onInfoClick = { onItemClick(com.papapace.noisynight.Info) }
            ) 
        },
        containerColor = MidnightBlue,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            PlayerTabContent(service = service)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoisyNightTopBar(onInfoClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo_waves),
                    contentDescription = "Logo",
                    tint = GoldenAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Noisy Night",
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Information",
                    tint = TextMuted
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MidnightBlue,
            titleContentColor = TextWhite
        )
    )
}

@Composable
fun PlayerTabContent(service: NoiseAudioService) {
    val isPlaying by service.isPlaying.collectAsState()
    val currentNoise by service.currentNoise.collectAsState()
    val timerRemainingMs by service.timerRemainingMs.collectAsState()
    val timerTotalMs by service.timerTotalMs.collectAsState()

    var showCustomTimerDialog by remember { mutableStateOf(false) }

    val onClickNoise: (NoiseType) -> Unit = { type ->
        if (currentNoise == type && isPlaying) {
            service.pauseNoise()
        } else {
            service.selectNoise(type)
            service.playNoise()
        }
    }

    if (showCustomTimerDialog) {
        DurationPickerDialog(
            onDismiss = { showCustomTimerDialog = false },
            onDurationConfirm = { minutes ->
                service.startTimer(minutes)
                // Autoplay if not playing when setting a timer
                if (!isPlaying) service.playNoise()
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(), 
        verticalArrangement = Arrangement.SpaceBetween, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dynamic Noise Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoiseCard(type = NoiseType.PURPLE, isSelected = currentNoise == NoiseType.PURPLE, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.PURPLE) }, modifier = Modifier.weight(1f))
                NoiseCard(type = NoiseType.BLUE, isSelected = currentNoise == NoiseType.BLUE, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.BLUE) }, modifier = Modifier.weight(1f))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoiseCard(type = NoiseType.WHITE, isSelected = currentNoise == NoiseType.WHITE, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.WHITE) }, modifier = Modifier.weight(1f))
                NoiseCard(type = NoiseType.GREEN, isSelected = currentNoise == NoiseType.GREEN, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.GREEN) }, modifier = Modifier.weight(1f))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoiseCard(type = NoiseType.PINK, isSelected = currentNoise == NoiseType.PINK, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.PINK) }, modifier = Modifier.weight(1f))
                NoiseCard(type = NoiseType.AMBER, isSelected = currentNoise == NoiseType.AMBER, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.AMBER) }, modifier = Modifier.weight(1f))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoiseCard(type = NoiseType.BROWN, isSelected = currentNoise == NoiseType.BROWN, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.BROWN) }, modifier = Modifier.weight(1f))
                NoiseCard(type = NoiseType.DEEP_BROWN, isSelected = currentNoise == NoiseType.DEEP_BROWN, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.DEEP_BROWN) }, modifier = Modifier.weight(1f))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoiseCard(type = NoiseType.HEAVY_RAIN, isSelected = currentNoise == NoiseType.HEAVY_RAIN, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.HEAVY_RAIN) }, modifier = Modifier.weight(1f))
                NoiseCard(type = NoiseType.SOFT_RAIN, isSelected = currentNoise == NoiseType.SOFT_RAIN, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.SOFT_RAIN) }, modifier = Modifier.weight(1f))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoiseCard(type = NoiseType.OCEAN_WAVES, isSelected = currentNoise == NoiseType.OCEAN_WAVES, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.OCEAN_WAVES) }, modifier = Modifier.weight(1f))
                NoiseCard(type = NoiseType.WIND_STORM, isSelected = currentNoise == NoiseType.WIND_STORM, isPlaying = isPlaying, onClick = { onClickNoise(NoiseType.WIND_STORM) }, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Timer Panel
        SleepTimerPanel(
            remainingMs = timerRemainingMs,
            totalMs = timerTotalMs,
            onSelectDuration = { minutes ->
                service.startTimer(minutes)
                if (!isPlaying) service.playNoise()
            },
            onCancelTimer = { service.cancelTimer() },
            onShowCustomDialog = { showCustomTimerDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Information", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldenAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlue
                )
            )
        },
        containerColor = MidnightBlue,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Battery optimization card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_alarm_clock),
                            contentDescription = "Battery",
                            tint = GoldenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Unrestricted Battery Usage",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To ensure the noise generator plays reliably all night without being terminated by Android's background limits, check that battery settings are set to 'Unrestricted'. Click here to open App Settings.",
                        color = TextWhite,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // About This App card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "About",
                            tint = GoldenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "About This App",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Remember when apps were just apps? No one was trying to harvest your data, track your habits, or track your family. This app is a return to those simpler days.\n\nI was searching for a basic white noise generator and couldn't find a single one that didn't force a login or demand payment to remove ads. My family just needed a plain, straightforward app—and I figured yours might need one too. This app does not request special permissions, contains zero ads, collects zero data, and requires no login.\n\nI'm not making a dime off this, so if it helps you out, please share it with your friends and leave a positive review!\n\n— Papa Pace",
                        color = TextWhite,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Tiny size info
            Text(
                text = "App Version 1.0 (Procedural Audio Synth)",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}
