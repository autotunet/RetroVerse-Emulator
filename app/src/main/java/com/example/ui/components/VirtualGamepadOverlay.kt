package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.TouchControllerLayout
import com.example.emulator.ConsoleType
import com.example.emulator.DisplayFilter
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletSecondary
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VirtualGamepadOverlay(
    consoleType: ConsoleType,
    touchLayout: TouchControllerLayout = TouchControllerLayout.DEFAULT,
    opacity: Float = 0.85f,
    isPaused: Boolean,
    speedMultiplier: Float,
    currentFilter: DisplayFilter,
    onDpadChange: (up: Boolean, down: Boolean, left: Boolean, right: Boolean) -> Unit,
    onStickChange: (lx: Float, ly: Float) -> Unit = { _, _ -> },
    onButtonA: (Boolean) -> Unit, // Cross on PS
    onButtonB: (Boolean) -> Unit, // Circle on PS
    onButtonX: (Boolean) -> Unit, // Square on PS
    onButtonY: (Boolean) -> Unit, // Triangle on PS
    onButtonL1: (Boolean) -> Unit,
    onButtonR1: (Boolean) -> Unit,
    onButtonL2: (Boolean) -> Unit = {},
    onButtonR2: (Boolean) -> Unit = {},
    onButtonStart: (Boolean) -> Unit,
    onButtonSelect: (Boolean) -> Unit,
    onTogglePause: () -> Unit,
    onCycleSpeed: () -> Unit,
    onRewind: () -> Unit,
    onQuickSave: () -> Unit,
    onQuickLoad: () -> Unit,
    onCycleFilter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLayoutEditor: () -> Unit = {},
    onHaptic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPlayStation = consoleType == ConsoleType.PPSSPP || consoleType == ConsoleType.PS2 || consoleType == ConsoleType.PSX

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(opacity)
    ) {
        // 1. Left Shoulder Triggers (L1 / L2) - at Top-Start, comfortably below the top bar
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 36.dp)
                .offset { IntOffset(touchLayout.shoulders.offsetX.dp.roundToPx(), touchLayout.shoulders.offsetY.dp.roundToPx()) }
                .scale(touchLayout.shoulders.scale),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShoulderTriggerButton(label = "L2", onPress = onButtonL2, isTrigger = true, onHaptic = onHaptic)
            ShoulderTriggerButton(label = "L1", onPress = onButtonL1, isTrigger = false, onHaptic = onHaptic)
        }

        // 2. Right Shoulder Triggers (R1 / R2) - at Top-End, comfortably below the top bar
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 36.dp)
                .offset { IntOffset(touchLayout.shoulders.offsetX.dp.roundToPx(), touchLayout.shoulders.offsetY.dp.roundToPx()) }
                .scale(touchLayout.shoulders.scale),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShoulderTriggerButton(label = "R1", onPress = onButtonR1, isTrigger = false, onHaptic = onHaptic)
            ShoulderTriggerButton(label = "R2", onPress = onButtonR2, isTrigger = true, onHaptic = onHaptic)
        }

        // 3. Quick Menu Bar - floating at Top-Center, neatly below the top bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 34.dp)
                .offset { IntOffset(touchLayout.quickMenu.offsetX.dp.roundToPx(), touchLayout.quickMenu.offsetY.dp.roundToPx()) }
                .scale(touchLayout.quickMenu.scale)
        ) {
            QuickMenuBar(
                isPaused = isPaused,
                speedMultiplier = speedMultiplier,
                currentFilter = currentFilter,
                onTogglePause = onTogglePause,
                onCycleSpeed = onCycleSpeed,
                onRewind = onRewind,
                onQuickSave = onQuickSave,
                onQuickLoad = onQuickLoad,
                onCycleFilter = onCycleFilter,
                onOpenSettings = onOpenSettings,
                onOpenLayoutEditor = onOpenLayoutEditor
            )
        }

        // 4. Left Controls: D-Pad & 3D Analog Stick
        var activeLeftMode by remember { mutableStateOf(if (consoleType == ConsoleType.PPSSPP) "STICK" else "DPAD") }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 10.dp)
                .offset { IntOffset(touchLayout.dpad.offsetX.dp.roundToPx(), touchLayout.dpad.offsetY.dp.roundToPx()) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Mode switch pill for 3D consoles
            if (consoleType.isModern3D) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activeLeftMode = if (activeLeftMode == "DPAD") "STICK" else "DPAD" }
                ) {
                    Text(
                        text = if (activeLeftMode == "DPAD") "Cruceta (Toca ➜ Stick)" else "Stick 3D (Toca ➜ D-Pad)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (consoleType.isModern3D && activeLeftMode == "STICK") {
                VirtualThumbstick(
                    onStickMove = onStickChange,
                    modifier = Modifier
                        .offset { IntOffset(touchLayout.analogStick.offsetX.dp.roundToPx(), touchLayout.analogStick.offsetY.dp.roundToPx()) }
                        .scale(touchLayout.analogStick.scale)
                        .testTag("virtual_left_stick")
                )
            } else {
                VirtualDpad(
                    onDirectionChange = onDpadChange,
                    onHaptic = onHaptic,
                    modifier = Modifier
                        .scale(touchLayout.dpad.scale)
                        .testTag("virtual_dpad")
                )
            }
        }

        // 5. Center: Select & Start
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .offset { IntOffset(touchLayout.startSelect.offsetX.dp.roundToPx(), touchLayout.startSelect.offsetY.dp.roundToPx()) }
                .scale(touchLayout.startSelect.scale)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton(
                    label = if (isPlayStation) "SELECT" else "SELECT",
                    onPress = onButtonSelect,
                    onHaptic = onHaptic,
                    modifier = Modifier.testTag("btn_select")
                )
                PillButton(
                    label = if (isPlayStation) "START" else "START",
                    onPress = onButtonStart,
                    onHaptic = onHaptic,
                    modifier = Modifier.testTag("btn_start")
                )
            }
        }

        // 6. Right Side: Action Buttons Cluster
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 10.dp)
                .offset { IntOffset(touchLayout.actionButtons.offsetX.dp.roundToPx(), touchLayout.actionButtons.offsetY.dp.roundToPx()) }
        ) {
            ActionButtonsCluster(
                consoleType = consoleType,
                isPlayStation = isPlayStation,
                onButtonA = onButtonA,
                onButtonB = onButtonB,
                onButtonX = onButtonX,
                onButtonY = onButtonY,
                onHaptic = onHaptic,
                modifier = Modifier
                    .scale(touchLayout.actionButtons.scale)
                    .testTag("action_buttons_cluster")
            )
        }
    }
}

@Composable
fun VirtualThumbstick(
    onStickMove: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 36f

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.5.dp, Color(0xFF334155), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = kotlin.math.sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        knobOffset = if (dist <= maxRadius) {
                            newOffset
                        } else {
                            Offset((newOffset.x / dist) * maxRadius, (newOffset.y / dist) * maxRadius)
                        }
                        onStickMove(knobOffset.x / maxRadius, knobOffset.y / maxRadius)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onStickMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onStickMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Center Nub / Thumb Grip
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0F172A))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}

@Composable
private fun QuickMenuBar(
    isPaused: Boolean,
    speedMultiplier: Float,
    currentFilter: DisplayFilter,
    onTogglePause: () -> Unit,
    onCycleSpeed: () -> Unit,
    onRewind: () -> Unit,
    onQuickSave: () -> Unit,
    onQuickLoad: () -> Unit,
    onCycleFilter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLayoutEditor: () -> Unit = {}
) {
    Surface(
        color = DarkSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283553)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = onTogglePause, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Pausa",
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onCycleSpeed)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = null, tint = VioletSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${speedMultiplier.toInt()}x", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            IconButton(onClick = onRewind, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Restore, contentDescription = "Rewind", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onQuickSave, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Save, contentDescription = "Quick Save", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onCycleFilter, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Tv, contentDescription = "Shaders", tint = CyanPrimary, modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onOpenLayoutEditor, modifier = Modifier.size(32.dp).testTag("btn_quick_layout_editor")) {
                Icon(Icons.Default.TouchApp, contentDescription = "Editar Controles", tint = CyanPrimary, modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onOpenSettings, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun VirtualDpad(
    onDirectionChange: (up: Boolean, down: Boolean, left: Boolean, right: Boolean) -> Unit,
    onHaptic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var upPressed by remember { mutableStateOf(false) }
    var downPressed by remember { mutableStateOf(false) }
    var leftPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }

    fun updateTouch(offset: Offset, isInside: Boolean) {
        if (!isInside) {
            upPressed = false; downPressed = false; leftPressed = false; rightPressed = false
            onDirectionChange(false, false, false, false)
            return
        }
        val centerX = 65f
        val centerY = 65f
        val dx = offset.x - centerX
        val dy = offset.y - centerY
        val dist = kotlin.math.sqrt(dx * dx + dy * dy)

        val prevUp = upPressed
        val prevDown = downPressed
        val prevLeft = leftPressed
        val prevRight = rightPressed

        if (dist < 12f) {
            upPressed = false; downPressed = false; leftPressed = false; rightPressed = false
        } else {
            val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            rightPressed = angle in -60f..60f
            downPressed = angle in 30f..150f
            leftPressed = angle >= 120f || angle <= -120f
            upPressed = angle in -150f..-30f
        }

        val hasNewDirection = (upPressed && !prevUp) ||
                              (downPressed && !prevDown) ||
                              (leftPressed && !prevLeft) ||
                              (rightPressed && !prevRight)
        if (hasNewDirection) {
            onHaptic()
        }

        onDirectionChange(upPressed, downPressed, leftPressed, rightPressed)
    }

    Box(
        modifier = modifier
            .size(130.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> updateTouch(offset, true) },
                    onDrag = { change, _ ->
                        updateTouch(change.position, true)
                        change.consume()
                    },
                    onDragEnd = { updateTouch(Offset.Zero, false) },
                    onDragCancel = { updateTouch(Offset.Zero, false) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E2433).copy(alpha = 0.9f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E2433).copy(alpha = 0.9f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = if (upPressed) CyanPrimary else Color(0xFF94A3B8),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).size(26.dp)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = if (downPressed) CyanPrimary else Color(0xFF94A3B8),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).size(26.dp)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = null,
            tint = if (leftPressed) CyanPrimary else Color(0xFF94A3B8),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp).size(26.dp)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = if (rightPressed) CyanPrimary else Color(0xFF94A3B8),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(26.dp)
        )
    }
}

@Composable
fun ActionButtonsCluster(
    consoleType: ConsoleType,
    isPlayStation: Boolean,
    onButtonA: (Boolean) -> Unit,
    onButtonB: (Boolean) -> Unit,
    onButtonX: (Boolean) -> Unit,
    onButtonY: (Boolean) -> Unit,
    onHaptic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTwoBtn = consoleType == ConsoleType.NES || consoleType == ConsoleType.GB

    if (isTwoBtn) {
        Row(
            modifier = modifier.padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundArcadeButton(label = "B", color = Color(0xFFEF4444), onPress = onButtonB, onHaptic = onHaptic)
            RoundArcadeButton(label = "A", color = Color(0xFFEF4444), onPress = onButtonA, onHaptic = onHaptic)
        }
    } else {
        // Modern Diamond Layout: If PlayStation -> △, ◯, ✕, ▢. If Xbox/Switch -> Y, B, A, X
        val northLabel = if (isPlayStation) "△" else "Y"
        val northColor = if (isPlayStation) Color(0xFF10B981) else Color(0xFFF59E0B)

        val eastLabel = if (isPlayStation) "◯" else "B"
        val eastColor = if (isPlayStation) Color(0xFFEF4444) else Color(0xFFEF4444)

        val southLabel = if (isPlayStation) "✕" else "A"
        val southColor = if (isPlayStation) Color(0xFF38BDF8) else Color(0xFF10B981)

        val westLabel = if (isPlayStation) "▢" else "X"
        val westColor = if (isPlayStation) Color(0xFFEC4899) else Color(0xFF38BDF8)

        Box(
            modifier = modifier.size(140.dp).padding(end = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            RoundArcadeButton(label = westLabel, color = westColor, onPress = onButtonX, onHaptic = onHaptic, modifier = Modifier.align(Alignment.CenterStart))
            RoundArcadeButton(label = northLabel, color = northColor, onPress = onButtonY, onHaptic = onHaptic, modifier = Modifier.align(Alignment.TopCenter))
            RoundArcadeButton(label = southLabel, color = southColor, onPress = onButtonA, onHaptic = onHaptic, modifier = Modifier.align(Alignment.BottomCenter))
            RoundArcadeButton(label = eastLabel, color = eastColor, onPress = onButtonB, onHaptic = onHaptic, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RoundArcadeButton(
    label: String,
    color: Color,
    onPress: (Boolean) -> Unit,
    onHaptic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(50.dp)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onPress(true)
                        onHaptic()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onPress(false)
                        true
                    }
                    else -> false
                }
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (isPressed) listOf(color, color.copy(alpha = 0.5f))
                    else listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.35f))
                )
            )
            .border(2.dp, if (isPressed) Color.White else color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShoulderTriggerButton(
    label: String,
    onPress: (Boolean) -> Unit,
    isTrigger: Boolean,
    onHaptic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        color = if (isPressed) CyanPrimary.copy(alpha = 0.7f) else Color(0xFF1E293B).copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPressed) CyanPrimary else Color(0xFF475569)),
        modifier = modifier
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onPress(true)
                        onHaptic()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onPress(false)
                        true
                    }
                    else -> false
                }
            }
    ) {
        Box(
            modifier = Modifier.width(if (isTrigger) 44.dp else 48.dp).height(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) Color.Black else Color.White
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PillButton(
    label: String,
    onPress: (Boolean) -> Unit,
    onHaptic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        color = if (isPressed) Color.White.copy(alpha = 0.8f) else Color(0xFF334155).copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B)),
        modifier = modifier
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onPress(true)
                        onHaptic()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onPress(false)
                        true
                    }
                    else -> false
                }
            }
    ) {
        Box(modifier = Modifier.width(52.dp).height(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = if (isPressed) Color.Black else Color(0xFFE2E8F0)
            )
        }
    }
}
