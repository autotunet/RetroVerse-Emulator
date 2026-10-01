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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.ConnectedControllerInfo
import com.example.controller.ControllerInputState
import com.example.controller.ControllerProfile
import com.example.ui.components.ControllerTesterView
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSecondary

@Composable
fun ControllersScreen(
    connectedControllers: List<ConnectedControllerInfo>,
    inputState: ControllerInputState,
    lightbarColor: Color,
    onSelectProfile: (ControllerProfile) -> Unit,
    onChangeLightbarColor: (Color) -> Unit,
    onRefreshControllers: () -> Unit,
    onTestVibration: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var deadzone by remember { mutableStateOf(0.15f) }
    var rumbleIntensity by remember { mutableStateOf(1.0f) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                        color = Color(0xFF003087).copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Mandos & Joysticks",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (connectedControllers.isEmpty()) "Sin mandos conectados" else "${connectedControllers.size} mando(s) activo(s)",
                            fontSize = 11.sp,
                            color = if (connectedControllers.isEmpty()) TextSecondary else EmeraldGreen
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onTestVibration,
                        modifier = Modifier.size(36.dp).testTag("btn_test_vibration")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Test vibración",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onRefreshControllers,
                        modifier = Modifier.size(36.dp).testTag("btn_refresh_controllers")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Escanear mandos",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Live Diagnostic and Visual Tester
        item {
            ControllerTesterView(
                connectedControllers = connectedControllers,
                inputState = inputState,
                lightbarColor = lightbarColor,
                onSelectProfile = onSelectProfile,
                onChangeLightbarColor = onChangeLightbarColor,
                onTestVibration = onTestVibration
            )
        }

        // Joystick Calibration (Deadzone & Rumble)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanPrimary)
                        Text(
                            text = "Calibración de Sticks y Motores Hápticos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    // Deadzone
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Zona Muerta (Anti-Drift de Sticks)", fontSize = 13.sp, color = TextPrimary)
                            Text("${(deadzone * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Slider(
                            value = deadzone,
                            onValueChange = { deadzone = it },
                            valueRange = 0.05f..0.35f,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )
                    }

                    // Rumble Intensity
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Intensidad de Vibración Rumble", fontSize = 13.sp, color = TextPrimary)
                            Text("${(rumbleIntensity * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VioletSecondary)
                        }
                        Slider(
                            value = rumbleIntensity,
                            onValueChange = { rumbleIntensity = it },
                            valueRange = 0.2f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = VioletSecondary, activeTrackColor = VioletSecondary)
                        )
                    }
                }
            }
        }

        // Modern Gaming Controller Guides
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanPrimary)
                        Text(
                            text = "Guía de Emparejamiento por Mando",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    PairingStepItem(
                        step = "PS5 / PS4",
                        title = "Sony PlayStation DualSense & DualShock 4",
                        description = "Mantén pulsados el botón 'Create / Share' + el 'Botón PS' durante 3 segundos hasta que la barra de luz parpadee rápidamente."
                    )

                    PairingStepItem(
                        step = "XBOX",
                        title = "Xbox Series X|S / Xbox One Wireless",
                        description = "Enciende el mando con el botón Xbox y mantén pulsado el botón de sincronización frontal ())) durante 3 segundos hasta que el logo parpadee rápido."
                    )

                    PairingStepItem(
                        step = "SWITCH",
                        title = "Nintendo Switch Pro Controller & Joy-Cons",
                        description = "Mantén presionado el botón circular de sincronización junto al puerto USB-C hasta que las 4 luces inferiores se muevan de lado a lado."
                    )

                    PairingStepItem(
                        step = "MÓVIL",
                        title = "Razer Kishi, Backbone One, GameSir",
                        description = "Conéctalo vía USB-C o Bluetooth. Son reconocidos de inmediato sin necesidad de apps adicionales mediante Android Gamepad API."
                    )
                }
            }
        }
    }
}

@Composable
private fun PairingStepItem(step: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = CyanPrimary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
        ) {
            Text(
                text = step,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Text(text = description, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}
