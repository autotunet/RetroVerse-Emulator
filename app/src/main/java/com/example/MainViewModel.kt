package com.example

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.controller.ControllerProfile
import com.example.controller.GamepadManager
import com.example.data.AppDatabase
import com.example.data.GameEntity
import com.example.data.GameRepository
import com.example.data.SaveStateEntity
import com.example.emulator.ConsoleType
import com.example.emulator.DisplayFilter
import com.example.emulator.RetroAudioSynth
import com.example.emulator.RetroConsoleEngine
import com.example.emulator.RomParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    LIBRARY,
    CONTROLLERS,
    EMULATOR,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "retroforge_database"
    ).build()

    val repository = GameRepository(db.gameDao())

    val audioSynth = RetroAudioSynth()
    val emulatorEngine = RetroConsoleEngine(audioSynth)
    val gamepadManager = GamepadManager(application.applicationContext)
    val touchLayoutManager = com.example.controller.TouchLayoutManager(application.applicationContext)
    val touchLayout = touchLayoutManager.layoutState
    val adMobManager = com.example.ads.AdMobManager(application.applicationContext)
    val adState = adMobManager.adState

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.LIBRARY)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Current active game
    private val _currentGame = MutableStateFlow<GameEntity?>(null)
    val currentGame: StateFlow<GameEntity?> = _currentGame.asStateFlow()

    // Save states for current game
    private val _currentSaveStates = MutableStateFlow<List<SaveStateEntity>>(emptyList())
    val currentSaveStates: StateFlow<List<SaveStateEntity>> = _currentSaveStates.asStateFlow()

    // Settings
    val currentFilter = MutableStateFlow(DisplayFilter.PSP_LCD_WIDE)
    val isAudioMuted = MutableStateFlow(false)
    val touchOpacity = MutableStateFlow(0.85f)
    val autoHideTouchControls = MutableStateFlow(true)
    val hapticFeedbackEnabled = MutableStateFlow(true)
    val graphicsBackend = MutableStateFlow("Vulkan (Direct)")
    val renderScaling = MutableStateFlow("2x HD (720p)")

    // Games list from Room
    val gamesList: StateFlow<List<GameEntity>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var playSessionJob: Job? = null
    private var controllerSyncJob: Job? = null

    init {
        viewModelScope.launch {
            checkAndSeedBuiltInGames()
        }
        startControllerSyncLoop()
    }

    private fun startControllerSyncLoop() {
        controllerSyncJob = viewModelScope.launch(Dispatchers.Default) {
            gamepadManager.inputState.collect { input ->
                // Feed physical gamepad inputs directly into the active emulator engine!
                emulatorEngine.setDpad(
                    up = input.dpadUp,
                    down = input.dpadDown,
                    left = input.dpadLeft,
                    right = input.dpadRight
                )
                emulatorEngine.setSticks(
                    lx = input.leftStickX,
                    ly = input.leftStickY,
                    rx = input.rightStickX,
                    ry = input.rightStickY
                )
                emulatorEngine.setButtonA(input.btnA)
                emulatorEngine.setButtonB(input.btnB)
                emulatorEngine.setButtonX(input.btnX)
                emulatorEngine.setButtonY(input.btnY)
                emulatorEngine.setButtonL1(input.btnL1)
                emulatorEngine.setButtonR1(input.btnR1)
                emulatorEngine.setButtonL2(input.btnL2)
                emulatorEngine.setButtonR2(input.btnR2)
                emulatorEngine.setButtonStart(input.btnStart)
                emulatorEngine.setButtonSelect(input.btnSelect)

                // Optional haptic vibration on button down
                if (hapticFeedbackEnabled.value && (input.btnA || input.btnB || input.btnX || input.btnY || input.btnR2)) {
                    gamepadManager.vibrate(30)
                }
            }
        }
    }

    private suspend fun checkAndSeedBuiltInGames() {
        val initialGames = listOf(
            // Modern PSP / PPSSPP Widescreen 3D Game
            GameEntity(
                title = "Ridge Racer GP Portable (PSP / PPSSPP)",
                console = ConsoleType.PPSSPP.id,
                filePath = "builtin://psp_racer",
                fileFormat = ".cso",
                fileSize = 184549376,
                isBuiltIn = true,
                isFavorite = true,
                cartridgeColor = 0xFF0070D1
            ),
            // Modern PS2 3D DualShock Mech Combat
            GameEntity(
                title = "Armored Cyber Mech 2026 (PS2)",
                console = ConsoleType.PS2.id,
                filePath = "builtin://ps2_mech",
                fileFormat = ".iso",
                fileSize = 734003200,
                isBuiltIn = true,
                isFavorite = true,
                cartridgeColor = 0xFF00439C
            ),
            // Modern PS1 DualShock 3D Fighter
            GameEntity(
                title = "Tekken Strike Arena 3D (PS1)",
                console = ConsoleType.PSX.id,
                filePath = "builtin://ps1_strike",
                fileFormat = ".pbp",
                fileSize = 471859200,
                isBuiltIn = true,
                isFavorite = true,
                cartridgeColor = 0xFF5C6F84
            ),
            // Retro Classics
            GameEntity(
                title = "Space Invaders 1978",
                console = ConsoleType.ARCADE.id,
                filePath = "builtin://invaders",
                fileFormat = ".ch8",
                fileSize = 4096,
                isBuiltIn = true,
                cartridgeColor = 0xFF00E5FF
            ),
            GameEntity(
                title = "Neon Snake 8-Bit",
                console = ConsoleType.GB.id,
                filePath = "builtin://snake",
                fileFormat = ".gb",
                fileSize = 8192,
                isBuiltIn = true,
                cartridgeColor = 0xFF8BAC0F
            ),
            GameEntity(
                title = "Brix Breaker Classic",
                console = ConsoleType.NES.id,
                filePath = "builtin://brix",
                fileFormat = ".nes",
                fileSize = 16384,
                isBuiltIn = true,
                cartridgeColor = 0xFFE52521
            ),
            GameEntity(
                title = "Tetromino Arcade",
                console = ConsoleType.GB.id,
                filePath = "builtin://tetris",
                fileFormat = ".gb",
                fileSize = 32768,
                isBuiltIn = true,
                cartridgeColor = 0xFF4D40A4
            ),
            GameEntity(
                title = "Retro Racer GP",
                console = ConsoleType.GENESIS.id,
                filePath = "builtin://racer",
                fileFormat = ".bin",
                fileSize = 65536,
                isBuiltIn = true,
                cartridgeColor = 0xFF0066CC
            )
        )

        withContext(Dispatchers.IO) {
            val existing = db.gameDao().getGameById(1)
            if (existing == null) {
                repository.insertGames(initialGames)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setControllerProfile(profile: ControllerProfile) {
        gamepadManager.setProfile(profile)
    }

    fun setLightbarColor(color: Color) {
        gamepadManager.setLightbarColor(color)
    }

    fun setGraphicsBackend(backend: String) {
        graphicsBackend.value = backend
        emulatorEngine.setGraphicsBackend(backend)
    }

    fun setRenderScaling(scaling: String) {
        renderScaling.value = scaling
        emulatorEngine.setRenderScaling(scaling)
    }

    fun launchGame(game: GameEntity, showAd: Boolean = true) {
        _currentGame.value = game
        val console = ConsoleType.fromString(game.console)

        val gameId = when {
            game.filePath.contains("psp_racer") -> "PSP_RACER"
            game.filePath.contains("ps2_mech") -> "PS2_MECH"
            game.filePath.contains("ps1_strike") -> "PS1_STRIKE"
            game.filePath.contains("invaders") -> "INVADERS"
            game.filePath.contains("pong") -> "PONG"
            game.filePath.contains("snake") -> "SNAKE"
            game.filePath.contains("brix") -> "BRIX"
            game.filePath.contains("tetris") -> "TETRIS"
            game.filePath.contains("racer") -> "RACER"
            else -> "CUSTOM_ROM"
        }

        emulatorEngine.loadGame(gameId, game.title, console)
        _currentScreen.value = AppScreen.EMULATOR

        // Automatic Ad for opening/launching ROM games
        if (showAd) {
            adMobManager.showAdForRomLaunch()
        }

        startPlaySessionTracker(game.id)
        loadSaveStatesForCurrentGame(game.id)
    }

    fun showAdForRomOpening() {
        adMobManager.showPopupAd(com.example.ads.AdUnitType.INTERSTITIAL_IMPORT_ROM)
    }

    fun dismissPopupAd() {
        adMobManager.dismissPopupBanner()
    }

    fun showTestPopupAd(adUnit: com.example.ads.AdUnitType = com.example.ads.AdUnitType.INTERSTITIAL_MAIN) {
        adMobManager.showPopupAd(adUnit)
    }

    fun showRewardedPrestamevip() {
        adMobManager.showRewardedPrestamevip()
    }

    fun toggleAutoPopup(enabled: Boolean) {
        adMobManager.toggleAutoPopup(enabled)
    }

    private fun loadSaveStatesForCurrentGame(gameId: Long) {
        viewModelScope.launch {
            repository.getSaveStatesForGame(gameId).collect { states ->
                _currentSaveStates.value = states
            }
        }
    }

    private fun startPlaySessionTracker(gameId: Long) {
        playSessionJob?.cancel()
        playSessionJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(60000)
                repository.recordPlaySession(gameId, 1, System.currentTimeMillis())
            }
        }
    }

    fun stopEmulation() {
        playSessionJob?.cancel()
        playSessionJob = null
        emulatorEngine.stop()
        _currentGame.value = null
        _currentScreen.value = AppScreen.LIBRARY

        // Trigger Main Interstitial on session end: ca-app-pub-1495262574338316/1182179137
        adMobManager.showPopupAd(com.example.ads.AdUnitType.INTERSTITIAL_MAIN)
    }

    fun togglePause() {
        emulatorEngine.pause()
        if (emulatorEngine.uiState.value.isPaused) {
            // Trigger Pause Interstitial: ca-app-pub-1495262574338316/3503687777
            adMobManager.showPopupAd(com.example.ads.AdUnitType.INTERSTITIAL_PAUSE)
        }
    }

    fun toggleFavorite(gameId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(gameId)
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            repository.deleteGame(game)
        }
    }

    fun importRomFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                var fileName = "ROM_${System.currentTimeMillis()}"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
                val headerInfo = RomParser.parseRom(fileName, bytes)

                val newGame = GameEntity(
                    title = headerInfo.title,
                    console = headerInfo.consoleType.id,
                    filePath = uri.toString(),
                    fileFormat = "." + fileName.substringAfterLast('.', "iso").lowercase(),
                    fileSize = bytes.size.toLong(),
                    isBuiltIn = false,
                    cartridgeColor = headerInfo.consoleType.primaryColor.value.toLong()
                )

                val newId = repository.insertGame(newGame)
                val savedGame = newGame.copy(id = newId)

                withContext(Dispatchers.Main) {
                    // Show ad for importing ROM files
                    adMobManager.showAdForRomImport()
                    launchGame(savedGame, showAd = false)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    fun saveState(slot: Int) {
        val game = _currentGame.value ?: return
        val stateData = emulatorEngine.serializeState()
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSaveState(
                SaveStateEntity(
                    gameId = game.id,
                    slot = slot,
                    timestamp = System.currentTimeMillis(),
                    description = "Slot $slot",
                    stateData = stateData
                )
            )
        }
        audioSynth.playSquareTone(880f, 60)
        gamepadManager.vibrate(50)

        // Show ad for tools and functions (Save State)
        adMobManager.showAdForToolsAndFunctions()
    }

    fun loadState(slot: Int) {
        val game = _currentGame.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.getSaveState(game.id, slot)
            if (state != null) {
                withContext(Dispatchers.Main) {
                    emulatorEngine.deserializeState(state.stateData)
                    audioSynth.playSquareTone(660f, 60)
                    gamepadManager.vibrate(50)
                    // Show ad for tools and functions (Load State)
                    adMobManager.showAdForToolsAndFunctions()
                }
            }
        }
    }

    fun cycleSpeed() {
        val current = emulatorEngine.uiState.value.speedMultiplier
        val next = when (current) {
            1.0f -> 2.0f
            2.0f -> 4.0f
            4.0f -> 8.0f
            else -> 1.0f
        }
        emulatorEngine.setSpeedMultiplier(next)
        adMobManager.showAdForToolsAndFunctions()
    }

    fun cycleFilter() {
        val allFilters = DisplayFilter.values()
        val currentIdx = allFilters.indexOf(currentFilter.value)
        val next = allFilters[(currentIdx + 1) % allFilters.size]
        setFilter(next)
        adMobManager.showAdForToolsAndFunctions()
    }

    fun setFilter(filter: DisplayFilter) {
        currentFilter.value = filter
        emulatorEngine.setFilter(filter)
    }

    fun toggleAudioMute(muted: Boolean) {
        isAudioMuted.value = muted
        audioSynth.setMuted(muted)
    }

    fun testVibration() {
        gamepadManager.vibrate(220)
    }

    fun showAdForTools() {
        adMobManager.showAdForToolsAndFunctions()
    }

    fun saveTouchLayout(layout: com.example.controller.TouchControllerLayout) {
        touchLayoutManager.saveLayout(layout)
        adMobManager.showAdForToolsAndFunctions()
    }

    fun resetTouchLayout() {
        touchLayoutManager.resetToDefault()
    }

    fun applyTouchPreset(preset: com.example.controller.TouchControllerLayout) {
        touchLayoutManager.applyPreset(preset)
    }

    override fun onCleared() {
        super.onCleared()
        controllerSyncJob?.cancel()
        playSessionJob?.cancel()
        gamepadManager.cleanup()
        emulatorEngine.stop()
    }
}
