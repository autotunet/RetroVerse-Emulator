package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.controller.TouchControllerLayout
import com.example.controller.TouchElementConfig
import com.example.controller.TouchElementId
import com.example.emulator.ConsoleType
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletSecondary
import kotlin.math.roundToInt

@Composable
fun TouchLayoutEditorDialog(
    initialLayout: TouchControllerLayout,
    consoleType: ConsoleType = ConsoleType.PPSSPP,
    onSaveLayout: (TouchControllerLayout) -> Unit,
    onDismiss: () -> Unit
) {
    var workingLayout by remember { mutableStateOf(initialLayout) }
    var selectedElement by remember { mutableStateOf(TouchElementId.DPAD) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B14).copy(alpha = 0.95f))
        ) {
            // Background Grid Lines for positioning guidance
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 40.dp.toPx()
                val stroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f))
                var x = 0f
                while (x < size.width) {
                    drawLine(Color(0xFF1E293B).copy(alpha = 0.35f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(Color(0xFF1E293B).copy(alpha = 0.35f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }
            }

            // Interactive Elements on Screen (Draggable and Selectable)
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. D-PAD Element
                DraggableLayoutElement(
                    elementId = TouchElementId.DPAD,
                    isSelected = selectedElement == TouchElementId.DPAD,
                    config = workingLayout.dpad,
                    alignment = Alignment.BottomStart,
                    defaultPadding = PaddingValues(start = 24.dp, bottom = 24.dp),
                    onSelect = { selectedElement = TouchElementId.DPAD },
                    onDrag = { dx, dy ->
                        val current = workingLayout.dpad
                        workingLayout = workingLayout.copy(
                            dpad = current.copy(offsetX = current.offsetX + dx, offsetY = current.offsetY + dy)
                        )
                    }
                ) {
                    // Preview representation of D-Pad
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(workingLayout.dpad.scale),
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
                    }
                }

                // 2. ANALOG STICK Element
                DraggableLayoutElement(
                    elementId = TouchElementId.ANALOG_STICK,
                    isSelected = selectedElement == TouchElementId.ANALOG_STICK,
                    config = workingLayout.analogStick,
                    alignment = Alignment.BottomStart,
                    defaultPadding = PaddingValues(start = 160.dp, bottom = 24.dp),
                    onSelect = { selectedElement = TouchElementId.ANALOG_STICK },
                    onDrag = { dx, dy ->
                        val current = workingLayout.analogStick
                        workingLayout = workingLayout.copy(
                            analogStick = current.copy(offsetX = current.offsetX + dx, offsetY = current.offsetY + dy)
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(workingLayout.analogStick.scale)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                            .border(1.5.dp, Color(0xFF334155), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.3f))
                                .border(1.5.dp, CyanPrimary, CircleShape)
                        )
                    }
                }

                // 3. ACTION BUTTONS Element (Cluster)
                DraggableLayoutElement(
                    elementId = TouchElementId.ACTION_BUTTONS,
                    isSelected = selectedElement == TouchElementId.ACTION_BUTTONS,
                    config = workingLayout.actionButtons,
                    alignment = Alignment.BottomEnd,
                    defaultPadding = PaddingValues(end = 24.dp, bottom = 24.dp),
                    onSelect = { selectedElement = TouchElementId.ACTION_BUTTONS },
                    onDrag = { dx, dy ->
                        val current = workingLayout.actionButtons
                        workingLayout = workingLayout.copy(
                            actionButtons = current.copy(offsetX = current.offsetX + dx, offsetY = current.offsetY + dy)
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(workingLayout.actionButtons.scale)
                    ) {
                        // Four buttons preview
                        Box(modifier = Modifier.size(46.dp).align(Alignment.TopCenter).clip(CircleShape).background(Color(0xFF1E2433)).border(1.dp, Color(0xFF334155), CircleShape), contentAlignment = Alignment.Center) {
                            Text("△/Y", color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.size(46.dp).align(Alignment.CenterStart).clip(CircleShape).background(Color(0xFF1E2433)).border(1.dp, Color(0xFF334155), CircleShape), contentAlignment = Alignment.Center) {
                            Text("□/X", color = Color(0xFFEC4899), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.size(46.dp).align(Alignment.CenterEnd).clip(CircleShape).background(Color(0xFF1E2433)).border(1.dp, Color(0xFF334155), CircleShape), contentAlignment = Alignment.Center) {
                            Text("○/B", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.size(46.dp).align(Alignment.BottomCenter).clip(CircleShape).background(Color(0xFF1E2433)).border(1.dp, Color(0xFF334155), CircleShape), contentAlignment = Alignment.Center) {
                            Text("✕/A", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. SHOULDERS Element (L/R)
                DraggableLayoutElement(
                    elementId = TouchElementId.SHOULDERS,
                    isSelected = selectedElement == TouchElementId.SHOULDERS,
                    config = workingLayout.shoulders,
                    alignment = Alignment.TopCenter,
                    defaultPadding = PaddingValues(top = 10.dp),
                    onSelect = { selectedElement = TouchElementId.SHOULDERS },
                    onDrag = { dx, dy ->
                        val current = workingLayout.shoulders
                        workingLayout = workingLayout.copy(
                            shoulders = current.copy(offsetX = current.offsetX + dx, offsetY = current.offsetY + dy)
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .scale(workingLayout.shoulders.scale)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E2433), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))) {
                            Text("L1 / L2", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E2433), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))) {
                            Text("R1 / R2", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5. START / SELECT Element
                DraggableLayoutElement(
                    elementId = TouchElementId.START_SELECT,
                    isSelected = selectedElement == TouchElementId.START_SELECT,
                    config = workingLayout.startSelect,
                    alignment = Alignment.BottomCenter,
                    defaultPadding = PaddingValues(bottom = 20.dp),
                    onSelect = { selectedElement = TouchElementId.START_SELECT },
                    onDrag = { dx, dy ->
                        val current = workingLayout.startSelect
                        workingLayout = workingLayout.copy(
                            startSelect = current.copy(offsetX = current.offsetX + dx, offsetY = current.offsetY + dy)
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .scale(workingLayout.startSelect.scale)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1E2433), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))) {
                            Text("SELECT", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1E2433), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))) {
                            Text("START", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top Header: Title, Presets and Close
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = DarkSurface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Personalizar Distribución Táctil", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Arrastra para mover o usa el panel inferior para redimensionar", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Bottom Floating Adjustment Toolbar & Presets
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = DarkSurface.copy(alpha = 0.97f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Element Selector Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TouchElementId.entries) { elem ->
                            val isSel = selectedElement == elem
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedElement = elem },
                                label = { Text(elem.displayName, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanPrimary,
                                    containerColor = DarkSurfaceElevated,
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    selectedBorderColor = CyanPrimary,
                                    borderColor = DarkBorder,
                                    borderWidth = 1.dp
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    val currentElemConfig = workingLayout.getElement(selectedElement)

                    // Scale Slider and Position Controls for Selected Element
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Scale / Size Slider
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tamaño: ${(currentElemConfig.scale * 100).roundToInt()}%", fontSize = 11.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                                Text("Pos: (${currentElemConfig.offsetX.toInt()}dp, ${currentElemConfig.offsetY.toInt()}dp)", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Slider(
                                value = currentElemConfig.scale,
                                onValueChange = { newScale ->
                                    workingLayout = workingLayout.updateElement(
                                        selectedElement,
                                        currentElemConfig.copy(scale = newScale)
                                    )
                                },
                                valueRange = 0.6f..1.6f,
                                steps = 10,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanPrimary,
                                    activeTrackColor = CyanPrimary,
                                    inactiveTrackColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.height(28.dp).testTag("slider_element_scale")
                            )
                        }

                        // Fine-tuning Position D-pad arrows
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    workingLayout = workingLayout.updateElement(
                                        selectedElement,
                                        currentElemConfig.copy(offsetX = currentElemConfig.offsetX - 6f)
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Izquierda", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        workingLayout = workingLayout.updateElement(
                                            selectedElement,
                                            currentElemConfig.copy(offsetY = currentElemConfig.offsetY - 6f)
                                        )
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Arriba", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = {
                                        workingLayout = workingLayout.updateElement(
                                            selectedElement,
                                            currentElemConfig.copy(offsetY = currentElemConfig.offsetY + 6f)
                                        )
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Abajo", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            IconButton(
                                onClick = {
                                    workingLayout = workingLayout.updateElement(
                                        selectedElement,
                                        currentElemConfig.copy(offsetX = currentElemConfig.offsetX + 6f)
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Derecha", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Presets & Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Presets Row
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PresetChip("Estándar") { workingLayout = TouchControllerLayout.DEFAULT }
                            PresetChip("Pulgares Bajos") { workingLayout = TouchControllerLayout.LOW_THUMBS }
                            PresetChip("Compacto") { workingLayout = TouchControllerLayout.COMPACT }
                            PresetChip("Tablet") { workingLayout = TouchControllerLayout.TABLET_EXPANDED }
                        }

                        // Save & Reset Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { workingLayout = TouchControllerLayout.DEFAULT },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("btn_reset_layout")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restablecer", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    onSaveLayout(workingLayout)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF070B14)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("btn_save_touch_layout")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DraggableLayoutElement(
    elementId: TouchElementId,
    isSelected: Boolean,
    config: TouchElementConfig,
    alignment: Alignment,
    defaultPadding: PaddingValues,
    onSelect: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(defaultPadding),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(config.offsetX.dp.roundToPx(), config.offsetY.dp.roundToPx()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onSelect() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x / density, dragAmount.y / density)
                        }
                    )
                }
                .clickable(onClick = onSelect)
                .then(
                    if (isSelected) {
                        Modifier
                            .border(2.dp, CyanPrimary, RoundedCornerShape(12.dp))
                            .background(CyanPrimary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    } else {
                        Modifier
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    }
                )
        ) {
            content()

            // Glowing label chip on selected element
            if (isSelected) {
                Surface(
                    color = CyanPrimary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-14).dp)
                ) {
                    Text(
                        text = elementId.displayName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF070B14),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
