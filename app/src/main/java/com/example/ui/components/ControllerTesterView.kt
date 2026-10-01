package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.ConnectedControllerInfo
import com.example.controller.ControllerInputState
import com.example.controller.ControllerProfile
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletSecondary

@Composable
fun ControllerTesterView(
    connectedControllers: List<ConnectedControllerInfo>,
    inputState: ControllerInputState,
    lightbarColor: Color,
    onSelectProfile: (ControllerProfile) -> Unit,
    onChangeLightbarColor: (Color) -> Unit,
    onTestVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProfile = inputState.activeProfile
    val isPlayStation = activeProfile.usesPlayStationSymbols

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Modern Controller Profile Switcher Bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PERFIL DE MANDO GAMER ACTIVO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = CyanPrimary
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ControllerProfile.values()) { profile ->
                    val isSelected = activeProfile == profile
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectProfile(profile) },
                        label = {
                            Text(
                                text = profile.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = profile.primaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = profile.primaryAccent,
                            containerColor = DarkSurfaceElevated,
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = profile.primaryAccent,
                            borderColor = DarkBorder
                        )
                    )
                }
            }
        }

        // Status Card with DualSense Lightbar
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, lightbarColor.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(lightbarColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = lightbarColor
                            )
                        }

                        Column {
                            Text(
                                text = if (connectedControllers.isNotEmpty())
                                    connectedControllers.first().name
                                else
                                    activeProfile.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (connectedControllers.isNotEmpty())
                                    "Conectado • Bluetooth HID Zero-Lag"
                                else
                                    "Modo visualización / Emparejamiento listo",
                                fontSize = 12.sp,
                                color = if (connectedControllers.isNotEmpty()) EmeraldGreen else Color(0xFF94A3B8)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onTestVibration,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = lightbarColor)
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rumble", fontSize = 12.sp)
                    }
                }

                // DualSense Simulated Lightbar Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = lightbarColor, modifier = Modifier.size(16.dp))
                        Text("Barra de luz LED DualSense:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }

                    // Color palette pills
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Color(0xFF0070D1), // PS Blue
                            Color(0xFF00E5FF), // Cyan
                            Color(0xFFF59E0B), // Orange
                            Color(0xFFEF4444), // Red
                            Color(0xFF10B981), // Green
                            Color(0xFFA855F7)  // Violet
                        ).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(if (lightbarColor == color) 2.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { onChangeLightbarColor(color) }
                            )
                        }
                    }
                }
            }
        }

        // Modern Visual Diagnostic Gamepad
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TABLERO DIAGNÓSTICO EN TIEMPO REAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CyanPrimary
                )

                // Triggers & Bumpers (L1/L2 and R1/R2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TriggerStatusColumn("L1 / L2 (Gatillo)", inputState.btnL1, inputState.btnL2, inputState.triggerL)
                    TriggerStatusColumn("R1 / R2 (Gatillo)", inputState.btnR1, inputState.btnR2, inputState.triggerR)
                }

                // Middle: D-Pad, Center Touchpad/Menu, Face Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // D-Pad
                    DiagnosticDpad(
                        up = inputState.dpadUp,
                        down = inputState.dpadDown,
                        left = inputState.dpadLeft,
                        right = inputState.dpadRight
                    )

                    // DualSense Touchpad & Share / Options
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Simulated Touchpad
                        Surface(
                            color = if (inputState.btnTouchpad) lightbarColor.copy(alpha = 0.5f) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (inputState.btnTouchpad) lightbarColor else Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                Text(
                                    text = if (isPlayStation) "TOUCHPAD" else "GUÍA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PillIndicator(if (isPlayStation) "SHARE" else "BACK", inputState.btnSelect)
                            PillIndicator(if (isPlayStation) "OPTIONS" else "START", inputState.btnStart)
                        }
                    }

                    // Face Buttons (PlayStation △ ◯ ✕ ▢ or Xbox ABXY)
                    DiagnosticFaceButtons(
                        isPlayStation = isPlayStation,
                        a = inputState.btnA,
                        b = inputState.btnB,
                        x = inputState.btnX,
                        y = inputState.btnY
                    )
                }

                // Dual Analog Sticks Visual Scopes (L3 & R3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnalogStickScope(
                        label = "Stick Izquierdo (L3)",
                        x = inputState.leftStickX,
                        y = inputState.leftStickY,
                        isClick = inputState.btnThumbL
                    )
                    AnalogStickScope(
                        label = "Stick Derecho (R3)",
                        x = inputState.rightStickX,
                        y = inputState.rightStickY,
                        isClick = inputState.btnThumbR
                    )
                }

                // Last Key Detected
                if (inputState.lastKeyCode != 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Último evento detectado:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "${inputState.lastKeyName} [${inputState.lastKeyCode}]",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggerStatusColumn(label: String, b1: Boolean, b2: Boolean, triggerVal: Float) {
    Column(modifier = Modifier.width(130.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PillIndicator("BUMPER", b1)
            PillIndicator("GATILLO", b2)
        }
        LinearProgressIndicator(
            progress = { triggerVal },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CyanPrimary,
            trackColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun DiagnosticDpad(up: Boolean, down: Boolean, left: Boolean, right: Boolean) {
    Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.width(84.dp).height(28.dp).background(Color(0xFF1E2433), RoundedCornerShape(4.dp)))
        Box(modifier = Modifier.width(28.dp).height(84.dp).background(Color(0xFF1E2433), RoundedCornerShape(4.dp)))

        if (up) Box(modifier = Modifier.size(28.dp).align(Alignment.TopCenter).background(CyanPrimary, RoundedCornerShape(4.dp)))
        if (down) Box(modifier = Modifier.size(28.dp).align(Alignment.BottomCenter).background(CyanPrimary, RoundedCornerShape(4.dp)))
        if (left) Box(modifier = Modifier.size(28.dp).align(Alignment.CenterStart).background(CyanPrimary, RoundedCornerShape(4.dp)))
        if (right) Box(modifier = Modifier.size(28.dp).align(Alignment.CenterEnd).background(CyanPrimary, RoundedCornerShape(4.dp)))
    }
}

@Composable
private fun DiagnosticFaceButtons(isPlayStation: Boolean, a: Boolean, b: Boolean, x: Boolean, y: Boolean) {
    val northLabel = if (isPlayStation) "△" else "Y"
    val northColor = if (isPlayStation) Color(0xFF10B981) else Color(0xFFF59E0B)

    val eastLabel = if (isPlayStation) "◯" else "B"
    val eastColor = if (isPlayStation) Color(0xFFEF4444) else Color(0xFFEF4444)

    val southLabel = if (isPlayStation) "✕" else "A"
    val southColor = if (isPlayStation) Color(0xFF38BDF8) else Color(0xFF10B981)

    val westLabel = if (isPlayStation) "▢" else "X"
    val westColor = if (isPlayStation) Color(0xFFEC4899) else Color(0xFF38BDF8)

    Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
        MiniIndicator(northLabel, y, northColor, Modifier.align(Alignment.TopCenter))
        MiniIndicator(westLabel, x, westColor, Modifier.align(Alignment.CenterStart))
        MiniIndicator(eastLabel, b, eastColor, Modifier.align(Alignment.CenterEnd))
        MiniIndicator(southLabel, a, southColor, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun MiniIndicator(label: String, active: Boolean, activeColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (active) activeColor else Color(0xFF1E293B))
            .border(1.dp, if (active) Color.White else Color(0xFF334155), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun PillIndicator(label: String, active: Boolean) {
    Surface(
        color = if (active) CyanPrimary else Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Color.White else Color(0xFF334155))
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) Color.Black else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun AnalogStickScope(label: String, x: Float, y: Float, isClick: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(if (isClick) VioletSecondary.copy(alpha = 0.3f) else Color(0xFF1E2433))
                .border(1.dp, if (isClick) VioletSecondary else Color(0xFF334155), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                drawLine(Color(0xFF334155), Offset(0f, cy), Offset(size.width, cy), 1f)
                drawLine(Color(0xFF334155), Offset(cx, 0f), Offset(cx, size.height), 1f)
                val px = cx + (x * (cx - 10))
                val py = cy + (y * (cy - 10))
                drawCircle(CyanPrimary, radius = 6.dp.toPx(), center = Offset(px, py))
            }
        }
        Text(
            text = "X: ${"%.2f".format(x)} Y: ${"%.2f".format(y)}",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF94A3B8)
        )
    }
}
