package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaveStateEntity
import com.example.emulator.DisplayFilter
import com.example.emulator.EmulatorState
import com.example.ui.components.VirtualGamepadOverlay
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmulatorScreen(
    state: EmulatorState,
    frameBitmap: Bitmap?,
    isBluetoothControllerConnected: Boolean,
    saveStates: List<SaveStateEntity>,
    touchLayout: com.example.controller.TouchControllerLayout = com.example.controller.TouchControllerLayout.DEFAULT,
    onSaveTouchLayout: (com.example.controller.TouchControllerLayout) -> Unit = {},
    onPerformHaptic: () -> Unit = {},
    onBackToLibrary: () -> Unit,
    onDpadChange: (up: Boolean, down: Boolean, left: Boolean, right: Boolean) -> Unit,
    onStickChange: (lx: Float, ly: Float) -> Unit,
    onButtonA: (Boolean) -> Unit,
    onButtonB: (Boolean) -> Unit,
    onButtonX: (Boolean) -> Unit,
    onButtonY: (Boolean) -> Unit,
    onButtonL1: (Boolean) -> Unit,
    onButtonR1: (Boolean) -> Unit,
    onButtonL2: (Boolean) -> Unit,
    onButtonR2: (Boolean) -> Unit,
    onButtonStart: (Boolean) -> Unit,
    onButtonSelect: (Boolean) -> Unit,
    onTogglePause: () -> Unit,
    onCycleSpeed: () -> Unit,
    onRewind: () -> Unit,
    onSaveStateSlot: (Int) -> Unit,
    onLoadStateSlot: (Int) -> Unit,
    onCycleFilter: () -> Unit,
    onOpenSettings: () -> Unit,
    onTriggerToolsAd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToLibrary()
    }

    var showSaveStateDialog by remember { mutableStateOf(false) }
    var showTouchControls by remember { mutableStateOf(true) }
    var showLayoutEditorDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06080F))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Ultra-Compact Top Emulator Header Bar (optimized for horizontal landscape gaming)
            Surface(
                color = DarkSurface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button & Title & Console tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(onClick = onBackToLibrary, modifier = Modifier.size(26.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = state.gameTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Surface(
                            color = state.consoleType.primaryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = state.consoleType.shortName,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = state.consoleType.primaryColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = "${state.currentFps} FPS",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldGreen
                        )

                        if (isBluetoothControllerConnected) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "Mando BT",
                                tint = CyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Score, Speed/Lives & Action buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SCORE: ${state.score}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary
                        )

                        Text(
                            text = if (state.consoleType == com.example.emulator.ConsoleType.PPSSPP)
                                "${state.lives} KM/H"
                            else
                                "HP: ${state.lives}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF43F5E)
                        )

                        // Save States
                        IconButton(
                            onClick = {
                                showSaveStateDialog = true
                                onTriggerToolsAd()
                            },
                            modifier = Modifier.size(26.dp).testTag("btn_open_save_states")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Toggle Touch Controls
                        IconButton(
                            onClick = { showTouchControls = !showTouchControls },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = if (showTouchControls) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Táctil",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Display Screen Area (16:9 for PSP/PS2, 4:3 for others, constrained for horizontal landscape)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(state.consoleType.defaultAspectRatio, matchHeightConstraintsFirst = true)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF080B12))
                        .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (frameBitmap != null) {
                        Image(
                            bitmap = frameBitmap.asImageBitmap(),
                            contentDescription = "Pantalla",
                            filterQuality = androidx.compose.ui.graphics.FilterQuality.None,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("emulator_screen_canvas")
                        )
                    }

                    if (state.currentFilter == DisplayFilter.CRT_SCANLINES) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f), Color.Transparent)
                                    )
                                )
                        )
                    }

                    if (state.isPaused) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "PAUSA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // On-Screen Virtual Touch Gamepad Overlay
        if (showTouchControls) {
            VirtualGamepadOverlay(
                consoleType = state.consoleType,
                touchLayout = touchLayout,
                opacity = 0.88f,
                isPaused = state.isPaused,
                speedMultiplier = state.speedMultiplier,
                currentFilter = state.currentFilter,
                onDpadChange = onDpadChange,
                onStickChange = onStickChange,
                onButtonA = onButtonA,
                onButtonB = onButtonB,
                onButtonX = onButtonX,
                onButtonY = onButtonY,
                onButtonL1 = onButtonL1,
                onButtonR1 = onButtonR1,
                onButtonL2 = onButtonL2,
                onButtonR2 = onButtonR2,
                onButtonStart = onButtonStart,
                onButtonSelect = onButtonSelect,
                onTogglePause = onTogglePause,
                onCycleSpeed = onCycleSpeed,
                onRewind = {
                    onRewind()
                    onTriggerToolsAd()
                },
                onQuickSave = {
                    onSaveStateSlot(1)
                    onTriggerToolsAd()
                },
                onQuickLoad = {
                    onLoadStateSlot(1)
                    onTriggerToolsAd()
                },
                onCycleFilter = onCycleFilter,
                onOpenSettings = onOpenSettings,
                onOpenLayoutEditor = {
                    showLayoutEditorDialog = true
                    onTriggerToolsAd()
                },
                onHaptic = onPerformHaptic,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Touch Controller Custom Layout Editor Dialog
    if (showLayoutEditorDialog) {
        com.example.ui.components.TouchLayoutEditorDialog(
            initialLayout = touchLayout,
            consoleType = state.consoleType,
            onSaveLayout = { updatedLayout ->
                onSaveTouchLayout(updatedLayout)
            },
            onDismiss = { showLayoutEditorDialog = false }
        )
    }

    // Save States Dialog
    if (showSaveStateDialog) {
        val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

        AlertDialog(
            onDismissRequest = { showSaveStateDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = CyanPrimary)
                    Text("Ranuras de Guardado (Save States)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    for (slot in 1..4) {
                        val stateEntity = saveStates.firstOrNull { it.slot == slot }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Ranura $slot", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text(
                                        text = if (stateEntity != null) dateFormat.format(Date(stateEntity.timestamp)) else "Vacía",
                                        fontSize = 11.sp,
                                        color = if (stateEntity != null) EmeraldGreen else Color(0xFF64748B)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { onSaveStateSlot(slot); showSaveStateDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Guardar", fontSize = 11.sp, color = CyanPrimary)
                                    }

                                    Button(
                                        onClick = {
                                            if (stateEntity != null) {
                                                onLoadStateSlot(slot)
                                                showSaveStateDialog = false
                                            }
                                        },
                                        enabled = stateEntity != null,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Cargar", fontSize = 11.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSaveStateDialog = false }) {
                    Text("Cerrar", color = CyanPrimary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
