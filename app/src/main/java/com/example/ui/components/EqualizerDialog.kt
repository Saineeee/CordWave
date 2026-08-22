package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AudioEffectConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDialog(
    config: AudioEffectConfig,
    onConfigChange: (AudioEffectConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(config.isEnabled) }
    var selectedPreset by remember { mutableStateOf(config.presetIndex) }
    var bands by remember { mutableStateOf(config.bands) }
    var bassBoost by remember { mutableStateOf(config.bassBoostStrength.toFloat()) }
    var virtualizer by remember { mutableStateOf(config.virtualizerStrength.toFloat()) }
    var tempo by remember { mutableStateOf(config.tempo) }
    var pitch by remember { mutableStateOf(config.pitch) }
    var replayGain by remember { mutableStateOf(config.replayGainEnabled) }

    val presetNames = listOf("Flat", "Bass Boost", "Rock", "Pop", "Jazz", "Electronic", "Vocal", "Classical")
    val presetValues = listOf(
        listOf(0, 0, 0, 0, 0),         // Flat
        listOf(600, 300, 0, 100, 200),  // Bass Boost
        listOf(400, 200, -100, 200, 500),// Rock
        listOf(-100, 200, 500, 100, -200),// Pop
        listOf(300, 200, -100, 200, 400),// Jazz
        listOf(500, 300, 0, 200, 400),  // Electronic
        listOf(-200, 100, 500, 200, -100),// Vocal
        listOf(400, 200, -200, 200, 300) // Classical
    )

    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    fun emitChange() {
        onConfigChange(
            AudioEffectConfig(
                isEnabled = isEnabled,
                presetIndex = selectedPreset,
                bands = bands,
                bassBoostStrength = bassBoost.toInt(),
                virtualizerStrength = virtualizer.toInt(),
                tempo = tempo,
                pitch = pitch,
                replayGainEnabled = replayGain
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("equalizer_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Audio Equalizer & FX",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            emitChange()
                        },
                        modifier = Modifier.testTag("eq_master_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets horizontal list
                Text(
                    text = "PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetNames.forEachIndexed { idx, name ->
                        FilterChip(
                            selected = selectedPreset == idx,
                            onClick = {
                                selectedPreset = idx
                                bands = presetValues[idx]
                                emitChange()
                            },
                            label = { Text(name, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5-band Graphic Equalizer Sliders
                Text(
                    text = "5-BAND EQUALIZER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    bandLabels.forEachIndexed { index, label ->
                        val currentLevel = bands.getOrElse(index) { 0 }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${if (currentLevel > 0) "+" else ""}${currentLevel / 100}dB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Slider(
                                value = currentLevel.toFloat(),
                                onValueChange = { newVal ->
                                    val updated = bands.toMutableList()
                                    if (index < updated.size) {
                                        updated[index] = newVal.toInt()
                                    }
                                    bands = updated
                                    emitChange()
                                },
                                valueRange = -1000f..1000f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                enabled = isEnabled
                            )

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bass Boost & Virtualizer & ReplayGain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bass Boost (${(bassBoost / 10).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = bassBoost,
                            onValueChange = {
                                bassBoost = it
                                emitChange()
                            },
                            valueRange = 0f..1000f,
                            enabled = isEnabled
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3D Virtualizer (${(virtualizer / 10).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = virtualizer,
                            onValueChange = {
                                virtualizer = it
                                emitChange()
                            },
                            valueRange = 0f..1000f,
                            enabled = isEnabled
                        )
                    }
                }

                // Speed / Tempo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Speed (${String.format("%.2fx", tempo)})",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = tempo,
                            onValueChange = {
                                tempo = it
                                emitChange()
                            },
                            valueRange = 0.5f..2.0f
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Checkbox(
                            checked = replayGain,
                            onCheckedChange = {
                                replayGain = it
                                emitChange()
                            }
                        )
                        Text(
                            text = "ReplayGain Norm.",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("eq_done_button")
                ) {
                    Text("Done")
                }
            }
        }
    }
}
