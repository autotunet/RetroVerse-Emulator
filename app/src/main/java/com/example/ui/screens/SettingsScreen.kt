package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emulator.DisplayFilter
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentFilter: DisplayFilter,
    onFilterChange: (DisplayFilter) -> Unit,
    graphicsBackend: String,
    onGraphicsBackendChange: (String) -> Unit,
    renderScaling: String,
    onRenderScalingChange: (String) -> Unit,
    isAudioMuted: Boolean,
    onToggleAudioMute: (Boolean) -> Unit,
    touchOpacity: Float,
    onTouchOpacityChange: (Float) -> Unit,
    autoHideTouchControls: Boolean,
    onToggleAutoHideTouch: (Boolean) -> Unit,
    hapticFeedbackEnabled: Boolean,
    onToggleHapticFeedback: (Boolean) -> Unit,
    touchLayout: com.example.controller.TouchControllerLayout = com.example.controller.TouchControllerLayout.DEFAULT,
    onSaveTouchLayout: (com.example.controller.TouchControllerLayout) -> Unit = {},
    onResetTouchLayout: () -> Unit = {},
    onApplyTouchPreset: (com.example.controller.TouchControllerLayout) -> Unit = {},
    autoPopupAds: Boolean = true,
    onToggleAutoPopupAds: (Boolean) -> Unit = {},
    vipUnlocked: Boolean = false,
    onTriggerAdUnit: (com.example.ads.AdUnitType) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var showLayoutEditorDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
        // Compact Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = CyanPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Ajustes del Sistema",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Gráficos, Audio, Mandos y Publicidad",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (vipUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                    ) {
                        Text(
                            text = "VIP ACTIVO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Graphics API Backend (Vulkan / OpenGL)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = CyanPrimary)
                        Text(
                            text = "API de Renderizado Gráfico (Backend)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    val backends = listOf("Vulkan (Direct)", "OpenGL ES 3.2", "Software Multi-Thread")
                    backends.forEach { b ->
                        val isSelected = graphicsBackend == b
                        FilterChip(
                            selected = isSelected,
                            onClick = { onGraphicsBackendChange(b) },
                            label = { Text(b, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary,
                                containerColor = Color(0xFF1E2433),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = CyanPrimary,
                                borderColor = DarkBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Internal Resolution Upscaler (PPSSPP & PS2)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DisplaySettings, contentDescription = null, tint = VioletSecondary)
                        Text(
                            text = "Reescalado de Resolución Interna (PPSSPP / PS2)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    val scalings = listOf("1x Nativa (Original)", "2x HD (720p)", "3x Full HD (1080p)", "4x 4K UHD")
                    scalings.forEach { s ->
                        val isSelected = renderScaling == s
                        FilterChip(
                            selected = isSelected,
                            onClick = { onRenderScalingChange(s) },
                            label = { Text(s, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletSecondary.copy(alpha = 0.2f),
                                selectedLabelColor = VioletSecondary,
                                containerColor = Color(0xFF1E2433),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = VioletSecondary,
                                borderColor = DarkBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Display Shaders / Video Filters
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DisplaySettings, contentDescription = null, tint = CyanPrimary)
                        Text(
                            text = "Filtro de Pantalla / Shader de Video",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    DisplayFilter.values().forEach { filter ->
                        val isSelected = currentFilter == filter
                        val filterName = when (filter) {
                            DisplayFilter.PSP_LCD_WIDE -> "PSP 16:9 LCD Widescreen (Matriz moderna)"
                            DisplayFilter.FXAA_SMOOTH -> "FXAA Anti-Aliasing (Suavizado de bordes 3D)"
                            DisplayFilter.CRT_SCANLINES -> "CRT Scanlines (Líneas de fósforo arcade)"
                            DisplayFilter.LCD_GRID -> "LCD Grid (Matriz de puntos Game Boy clásica)"
                            DisplayFilter.CRISP_PIXEL -> "Píxel Perfecto (Nearest Neighbor 1:1)"
                            DisplayFilter.RETRO_AMBER -> "Fósforo Ámbar (Monitores 1980)"
                            DisplayFilter.CYBER_NEON -> "Cyber Neon (Gama neón púrpura)"
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filterName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary,
                                containerColor = Color(0xFF1E2433),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = CyanPrimary,
                                borderColor = DarkBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Audio Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = VioletSecondary)
                            Column {
                                Text(
                                    text = "Sonido y Efectos de Audio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Sintetizador en tiempo real de baja latencia",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = !isAudioMuted,
                            onCheckedChange = { onToggleAudioMute(!it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VioletSecondary,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }
        }

        // On-Screen Touch Controls Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Gamepad, contentDescription = null, tint = EmeraldGreen)
                        Text(
                            text = "Controles Táctiles y Hápticos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Opacidad de botones", fontSize = 13.sp, color = TextPrimary)
                            Text("${(touchOpacity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Slider(
                            value = touchOpacity,
                            onValueChange = onTouchOpacityChange,
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ocultar táctil al conectar mando", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                            Text("Pantalla limpia al usar mando Bluetooth", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = autoHideTouchControls,
                            onCheckedChange = onToggleAutoHideTouch,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldGreen)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vibración táctil (Háptica)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                            Text("Respuesta de vibración al pulsar botones en pantalla", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = onToggleHapticFeedback,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldGreen)
                        )
                    }

                    // Custom Touch Layout Editor Section
                    Surface(
                        color = Color(0xFF1E2433),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                    Text("Distribución de Botones", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        onTriggerAdUnit(com.example.ads.AdUnitType.INTERSTITIAL_TOOLS)
                                        showLayoutEditorDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF070B14)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_open_layout_editor_settings")
                                ) {
                                    Text("Editar Controles", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text("Mueve y redimensiona la cruceta D-Pad, sticks y botones para que se adapten a tu estilo de juego.", fontSize = 11.sp, color = TextSecondary)

                            // Quick Presets
                            Text("Distribuciones rápidas:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        onApplyTouchPreset(com.example.controller.TouchControllerLayout.DEFAULT)
                                        onTriggerAdUnit(com.example.ads.AdUnitType.INTERSTITIAL_TOOLS)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Text("Estándar", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Surface(
                                    onClick = {
                                        onApplyTouchPreset(com.example.controller.TouchControllerLayout.LOW_THUMBS)
                                        onTriggerAdUnit(com.example.ads.AdUnitType.INTERSTITIAL_TOOLS)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Text("Pulgares Bajos", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Surface(
                                    onClick = {
                                        onApplyTouchPreset(com.example.controller.TouchControllerLayout.COMPACT)
                                        onTriggerAdUnit(com.example.ads.AdUnitType.INTERSTITIAL_TOOLS)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Text("Compacto", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Surface(
                                    onClick = {
                                        onApplyTouchPreset(com.example.controller.TouchControllerLayout.TABLET_EXPANDED)
                                        onTriggerAdUnit(com.example.ads.AdUnitType.INTERSTITIAL_TOOLS)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Text("Tablet", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Google AdMob Automatic Pop-Up & Interstitials Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0070D1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF38BDF8))
                            Column {
                                Text(
                                    text = "Publicidad Emergente Google AdMob",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Anuncios automáticos al iniciar, pausar y guardar",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = autoPopupAds,
                            onCheckedChange = onToggleAutoPopupAds,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0070D1)
                            )
                        )
                    }

                    // Special Prestamevip Rewarded Ad Button
                    Surface(
                        color = Color(0xFF78350F).copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFB300)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Savings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(
                                        text = "Prestamevip • Recompensa VIP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFFB300)
                                    )
                                    Text(
                                        text = if (vipUnlocked) "✓ VIP Activado: Shaders 4K y Rebobinado Ilimitado" else "Ver anuncio para desbloquear Shaders 4K y Rebobinado VIP",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                            }

                            Button(
                                onClick = { onTriggerAdUnit(com.example.ads.AdUnitType.REWARDED_PRESTAMEVIP) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("VER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "UNIDADES DE ANUNCIOS CONFIGURADAS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CyanPrimary
                    )

                    // All 5 Interstitial / Rewarded Units listing
                    listOf(
                        com.example.ads.AdUnitType.REWARDED_PRESTAMEVIP,
                        com.example.ads.AdUnitType.INTERSTITIAL_MAIN,
                        com.example.ads.AdUnitType.INTERSTITIAL_GAME_START,
                        com.example.ads.AdUnitType.INTERSTITIAL_SAVE_STATE,
                        com.example.ads.AdUnitType.INTERSTITIAL_PAUSE,
                        com.example.ads.AdUnitType.BANNER_FOOTER
                    ).forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E2433), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(unit.idName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text(unit.adUnitId, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                            }

                            Button(
                                onClick = { onTriggerAdUnit(unit) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("PROBAR", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Modern Consoles Supported Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanPrimary)
                        Text(
                            text = "Formatos y Consolas Soportadas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "• Sony PSP (PPSSPP): .iso, .cso, .pbp, .prx\n" +
                                "• Sony PlayStation 2: .iso, .bin, .chd, .gz\n" +
                                "• Sony PlayStation 1: .iso, .cue, .bin, .chd, .pbp\n" +
                                "• Nintendo 64: .z64, .n64, .v64\n" +
                                "• Nintendo DS: .nds, .dsi\n" +
                                "• Game Boy Advance: .gba\n" +
                                "• Super Nintendo: .sfc, .smc\n" +
                                "• Sega Genesis / Mega Drive: .bin, .gen, .md\n" +
                                "• Nintendo NES: .nes, .unf, .fds\n" +
                                "• Game Boy / Color: .gb, .gbc\n" +
                                "• Archivos Comprimidos: .zip, .7z",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    if (showLayoutEditorDialog) {
        com.example.ui.components.TouchLayoutEditorDialog(
            initialLayout = touchLayout,
            onSaveLayout = { updated ->
                onSaveTouchLayout(updated)
            },
            onDismiss = { showLayoutEditorDialog = false }
        )
    }
}
}
