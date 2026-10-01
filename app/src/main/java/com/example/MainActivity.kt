package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ControllersScreen
import com.example.ui.screens.EmulatorScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    /**
     * Intercept physical Bluetooth / USB controller key events.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (viewModel.gamepadManager.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Intercept physical Bluetooth / USB controller analog sticks and triggers.
     */
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (viewModel.gamepadManager.handleMotionEvent(event)) {
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val games by viewModel.gamesList.collectAsStateWithLifecycle()
    val emulatorState by viewModel.emulatorEngine.uiState.collectAsStateWithLifecycle()
    val frameBitmap by viewModel.emulatorEngine.bitmapFlow.collectAsStateWithLifecycle()
    val connectedControllers by viewModel.gamepadManager.connectedControllers.collectAsStateWithLifecycle()
    val inputState by viewModel.gamepadManager.inputState.collectAsStateWithLifecycle()
    val saveStates by viewModel.currentSaveStates.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()
    val isAudioMuted by viewModel.isAudioMuted.collectAsStateWithLifecycle()
    val touchOpacity by viewModel.touchOpacity.collectAsStateWithLifecycle()
    val autoHideTouch by viewModel.autoHideTouchControls.collectAsStateWithLifecycle()
    val hapticFeedback by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val graphicsBackend by viewModel.graphicsBackend.collectAsStateWithLifecycle()
    val renderScaling by viewModel.renderScaling.collectAsStateWithLifecycle()
    val lightbarColor by viewModel.gamepadManager.lightbarColor.collectAsStateWithLifecycle()
    val touchLayout by viewModel.touchLayout.collectAsStateWithLifecycle()
    val adState by viewModel.adState.collectAsStateWithLifecycle()

    val isRunningGame = emulatorState.isRunning
    val isEmulatorScreen = currentScreen == AppScreen.EMULATOR

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    // Automatic screen orientation: Rotate to landscape when entering emulator, restore when exiting
    androidx.compose.runtime.DisposableEffect(currentScreen) {
        if (currentScreen == AppScreen.EMULATOR) {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = if (isEmulatorScreen) WindowInsets(0, 0, 0, 0) else androidx.compose.material3.ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            // Hide bottom bar when inside Emulator screen to give full screen gaming experience
            if (!isEmulatorScreen) {
                Surface(
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBar(
                        containerColor = DarkSurface,
                        tonalElevation = 0.dp
                    ) {
                        // 1. Biblioteca
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.LIBRARY,
                            onClick = { viewModel.navigateTo(AppScreen.LIBRARY) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.LIBRARY) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                                    contentDescription = "Biblioteca"
                                )
                            },
                            label = { Text("Juegos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = navItemColors(),
                            modifier = Modifier.testTag("nav_library")
                        )

                        // 2. Mandos Bluetooth
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.CONTROLLERS,
                            onClick = { viewModel.navigateTo(AppScreen.CONTROLLERS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (connectedControllers.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldGreen)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.CONTROLLERS) Icons.Filled.Bluetooth else Icons.Outlined.Bluetooth,
                                        contentDescription = "Mandos"
                                    )
                                }
                            },
                            label = { Text("Mandos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = navItemColors(),
                            modifier = Modifier.testTag("nav_controllers")
                        )

                        // 3. Emulador
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.EMULATOR,
                            onClick = {
                                if (isRunningGame) {
                                    viewModel.navigateTo(AppScreen.EMULATOR)
                                } else if (games.isNotEmpty()) {
                                    viewModel.launchGame(games.first())
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (isRunningGame) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(CyanPrimary)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.EMULATOR) Icons.Filled.Gamepad else Icons.Outlined.Gamepad,
                                        contentDescription = "Emulador"
                                    )
                                }
                            },
                            label = {
                                Text(
                                    if (isRunningGame) "En Juego" else "Emulador",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = navItemColors(),
                            modifier = Modifier.testTag("nav_emulator")
                        )

                        // 4. Ajustes
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.SETTINGS,
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Ajustes"
                                )
                            },
                            label = { Text("Ajustes", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = navItemColors(),
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.LIBRARY -> LibraryScreen(
                    games = games,
                    onLaunchGame = { viewModel.launchGame(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDeleteGame = { viewModel.deleteGame(it) },
                    onImportRomUri = { viewModel.importRomFromUri(it) },
                    onTriggerOpenRomAd = { viewModel.showAdForRomOpening() }
                )

                AppScreen.CONTROLLERS -> ControllersScreen(
                    connectedControllers = connectedControllers,
                    inputState = inputState,
                    lightbarColor = lightbarColor,
                    onSelectProfile = { viewModel.setControllerProfile(it) },
                    onChangeLightbarColor = { viewModel.setLightbarColor(it) },
                    onRefreshControllers = { viewModel.gamepadManager.refreshConnectedControllers() },
                    onTestVibration = { viewModel.testVibration() },
                    onBack = { viewModel.navigateTo(AppScreen.LIBRARY) }
                )

                AppScreen.EMULATOR -> EmulatorScreen(
                    state = emulatorState,
                    frameBitmap = frameBitmap,
                    isBluetoothControllerConnected = connectedControllers.isNotEmpty(),
                    saveStates = saveStates,
                    touchLayout = touchLayout,
                    onSaveTouchLayout = { viewModel.saveTouchLayout(it) },
                    onPerformHaptic = {
                        if (hapticFeedback) {
                            viewModel.gamepadManager.vibrateHapticClick()
                        }
                    },
                    onBackToLibrary = { viewModel.stopEmulation() },
                    onDpadChange = { up, down, left, right ->
                        viewModel.emulatorEngine.setDpad(up, down, left, right)
                        if (hapticFeedback && (up || down || left || right)) {
                            viewModel.gamepadManager.vibrate(20)
                        }
                    },
                    onStickChange = { lx, ly ->
                        viewModel.emulatorEngine.setSticks(lx, ly, 0f, 0f)
                    },
                    onButtonA = { pressed ->
                        viewModel.emulatorEngine.setButtonA(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonB = { pressed ->
                        viewModel.emulatorEngine.setButtonB(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonX = { pressed ->
                        viewModel.emulatorEngine.setButtonX(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonY = { pressed ->
                        viewModel.emulatorEngine.setButtonY(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonL1 = { pressed ->
                        viewModel.emulatorEngine.setButtonL1(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonR1 = { pressed ->
                        viewModel.emulatorEngine.setButtonR1(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonL2 = { pressed ->
                        viewModel.emulatorEngine.setButtonL2(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonR2 = { pressed ->
                        viewModel.emulatorEngine.setButtonR2(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonStart = { pressed ->
                        viewModel.emulatorEngine.setButtonStart(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onButtonSelect = { pressed ->
                        viewModel.emulatorEngine.setButtonSelect(pressed)
                        if (hapticFeedback && pressed) viewModel.gamepadManager.vibrate(25)
                    },
                    onTogglePause = { viewModel.togglePause() },
                    onCycleSpeed = { viewModel.cycleSpeed() },
                    onRewind = { viewModel.emulatorEngine.rewindStep() },
                    onSaveStateSlot = { slot -> viewModel.saveState(slot) },
                    onLoadStateSlot = { slot -> viewModel.loadState(slot) },
                    onCycleFilter = { viewModel.cycleFilter() },
                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onTriggerToolsAd = { viewModel.showAdForTools() }
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    currentFilter = currentFilter,
                    onFilterChange = { viewModel.setFilter(it) },
                    graphicsBackend = graphicsBackend,
                    onGraphicsBackendChange = { viewModel.setGraphicsBackend(it) },
                    renderScaling = renderScaling,
                    onRenderScalingChange = { viewModel.setRenderScaling(it) },
                    isAudioMuted = isAudioMuted,
                    onToggleAudioMute = { viewModel.toggleAudioMute(it) },
                    touchOpacity = touchOpacity,
                    onTouchOpacityChange = { viewModel.touchOpacity.value = it },
                    autoHideTouchControls = autoHideTouch,
                    onToggleAutoHideTouch = { viewModel.autoHideTouchControls.value = it },
                    hapticFeedbackEnabled = hapticFeedback,
                    onToggleHapticFeedback = { viewModel.hapticFeedbackEnabled.value = it },
                    touchLayout = touchLayout,
                    onSaveTouchLayout = { viewModel.saveTouchLayout(it) },
                    onResetTouchLayout = { viewModel.resetTouchLayout() },
                    onApplyTouchPreset = { viewModel.applyTouchPreset(it) },
                    autoPopupAds = adState.autoPopupEnabled,
                    onToggleAutoPopupAds = { viewModel.toggleAutoPopup(it) },
                    vipUnlocked = adState.vipUnlocked,
                    onTriggerAdUnit = { viewModel.showTestPopupAd(it) },
                    onBack = { viewModel.navigateTo(AppScreen.LIBRARY) }
                )
            }

            // Automatic Pop-Up Banner Ad Dialog (ca-app-pub-1495262574338316/2803163788)
            com.example.ui.components.PopupBannerAdDialog(
                adState = adState,
                onDismiss = { viewModel.dismissPopupAd() },
                onActionClick = { viewModel.dismissPopupAd() }
            )
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = CyanPrimary,
    selectedTextColor = CyanPrimary,
    indicatorColor = CyanPrimary.copy(alpha = 0.15f),
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextMuted
)
