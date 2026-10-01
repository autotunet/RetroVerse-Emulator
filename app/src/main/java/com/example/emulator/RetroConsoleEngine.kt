package com.example.emulator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class DisplayFilter {
    CRISP_PIXEL,
    CRT_SCANLINES,
    LCD_GRID,
    PSP_LCD_WIDE,
    FXAA_SMOOTH,
    RETRO_AMBER,
    CYBER_NEON
}

data class EmulatorState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val speedMultiplier: Float = 1.0f,
    val currentFilter: DisplayFilter = DisplayFilter.PSP_LCD_WIDE,
    val currentFps: Int = 60,
    val score: Int = 0,
    val lives: Int = 3,
    val level: Int = 1,
    val gameTitle: String = "No Game Loaded",
    val consoleType: ConsoleType = ConsoleType.PPSSPP,
    val canRewind: Boolean = false,
    val graphicsBackend: String = "Vulkan (Direct)",
    val renderScaling: String = "2x HD (720p)",
    val isWidescreen: Boolean = true
)

class RetroConsoleEngine(private val audioSynth: RetroAudioSynth) {
    companion object {
        const val WIDTH_16_9 = 96
        const val HEIGHT_16_9 = 54
        const val WIDTH_4_3 = 64
        const val HEIGHT_4_3 = 48
        const val MAX_REWIND_FRAMES = 90
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null

    var currentWidth = WIDTH_16_9
    var currentHeight = HEIGHT_16_9

    // Memory & VM registers
    val memory = ByteArray(65536)
    val v = IntArray(16)
    var iReg: Int = 0
    var pc: Int = 0x200
    val stack = IntArray(32)
    var sp: Int = 0
    var delayTimer: Int = 0
    var soundTimer: Int = 0

    // Framebuffer
    var display = IntArray(WIDTH_16_9 * HEIGHT_16_9)

    // Gamepad input states
    var isDpadUp = false
    var isDpadDown = false
    var isDpadLeft = false
    var isDpadRight = false
    var isBtnA = false // Cross on PS
    var isBtnB = false // Circle on PS
    var isBtnX = false // Square on PS
    var isBtnY = false // Triangle on PS
    var isBtnL1 = false
    var isBtnR1 = false
    var isBtnL2 = false
    var isBtnR2 = false
    var isBtnStart = false
    var isBtnSelect = false
    var leftStickX = 0f
    var leftStickY = 0f
    var rightStickX = 0f
    var rightStickY = 0f

    // UI state
    private val _uiState = MutableStateFlow(EmulatorState())
    val uiState: StateFlow<EmulatorState> = _uiState.asStateFlow()

    // Bitmap flow
    private var frameBitmap = Bitmap.createBitmap(WIDTH_16_9, HEIGHT_16_9, Bitmap.Config.ARGB_8888)
    private val _bitmapFlow = MutableStateFlow<Bitmap?>(null)
    val bitmapFlow: StateFlow<Bitmap?> = _bitmapFlow.asStateFlow()

    private val rewindBuffer = ArrayDeque<ByteArray>()
    private var currentGameId: String = "PSP_RACER"

    // High level game entities
    private var playerX = 48f
    private var playerY = 44f
    private var playerSpeed = 0f
    private var playerAngle = 0f
    private var cameraPitch = 0f
    private var bullets = mutableListOf<FloatArray>()
    private var enemies = mutableListOf<FloatArray>()
    private var enemyDir = 1f
    private var gameScore = 0
    private var gameLives = 3
    private var gameLevel = 1
    private var trackCurve = 0f
    private var trackZ = 0f

    init {
        loadFontSet()
    }

    private fun loadFontSet() {
        val fontSet = intArrayOf(
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        )
        for (i in fontSet.indices) {
            memory[i] = fontSet[i].toByte()
        }
    }

    fun start() {
        if (loopJob?.isActive == true) return
        _uiState.value = _uiState.value.copy(isRunning = true, isPaused = false)

        loopJob = scope.launch {
            var lastFpsTime = System.currentTimeMillis()
            var framesThisSecond = 0

            while (isActive) {
                if (!_uiState.value.isPaused) {
                    when (currentGameId) {
                        "PSP_RACER" -> updatePspRacer()
                        "PS2_MECH" -> updatePs2Mech()
                        "PS1_STRIKE" -> updatePs1Strike()
                        "CUSTOM_ROM" -> {
                            val cycles = (12 * _uiState.value.speedMultiplier).toInt().coerceAtLeast(1)
                            for (c in 0 until cycles) emulateCycle()
                        }
                        else -> updateBuiltInGame()
                    }

                    if (delayTimer > 0) delayTimer--
                    if (soundTimer > 0) {
                        soundTimer--
                        if (soundTimer == 1) audioSynth.playSquareTone(440f, 40)
                    }

                    saveFrameForRewind()
                    renderDisplay()

                    framesThisSecond++
                    val now = System.currentTimeMillis()
                    if (now - lastFpsTime >= 1000) {
                        _uiState.value = _uiState.value.copy(
                            currentFps = framesThisSecond,
                            canRewind = rewindBuffer.isNotEmpty()
                        )
                        framesThisSecond = 0
                        lastFpsTime = now
                    }
                }

                val frameDelayMs = (16.6f / _uiState.value.speedMultiplier).toLong().coerceAtLeast(2)
                delay(frameDelayMs)
            }
        }
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun setSpeedMultiplier(multiplier: Float) {
        _uiState.value = _uiState.value.copy(speedMultiplier = multiplier)
    }

    fun setFilter(filter: DisplayFilter) {
        _uiState.value = _uiState.value.copy(currentFilter = filter)
        renderDisplay()
    }

    fun setGraphicsBackend(backend: String) {
        _uiState.value = _uiState.value.copy(graphicsBackend = backend)
    }

    fun setRenderScaling(scaling: String) {
        _uiState.value = _uiState.value.copy(renderScaling = scaling)
    }

    fun loadGame(gameId: String, title: String, console: ConsoleType, romBytes: ByteArray? = null) {
        stop()

        val isWide = console.defaultAspectRatio > 1.6f
        currentWidth = if (isWide) WIDTH_16_9 else WIDTH_4_3
        currentHeight = if (isWide) HEIGHT_16_9 else HEIGHT_4_3

        display = IntArray(currentWidth * currentHeight)
        frameBitmap = Bitmap.createBitmap(currentWidth, currentHeight, Bitmap.Config.ARGB_8888)

        resetCore()
        currentGameId = gameId
        gameScore = 0
        gameLives = 3
        gameLevel = 1

        _uiState.value = _uiState.value.copy(
            gameTitle = title,
            consoleType = console,
            score = 0,
            lives = 3,
            level = 1,
            isWidescreen = isWide
        )

        if (romBytes != null && romBytes.isNotEmpty()) {
            currentGameId = "CUSTOM_ROM"
            val loadLen = minOf(romBytes.size, memory.size - 0x200)
            System.arraycopy(romBytes, 0, memory, 0x200, loadLen)
            pc = 0x200
        } else {
            initGameMode(gameId)
        }

        start()
    }

    private fun resetCore() {
        memory.fill(0)
        loadFontSet()
        v.fill(0)
        display.fill(0)
        stack.fill(0)
        iReg = 0
        pc = 0x200
        sp = 0
        delayTimer = 0
        soundTimer = 0
        rewindBuffer.clear()
        bullets.clear()
        enemies.clear()
    }

    private fun initGameMode(gameId: String) {
        display.fill(0)
        when (gameId) {
            "PSP_RACER" -> {
                playerX = (currentWidth / 2).toFloat()
                playerSpeed = 0f
                trackCurve = 0f
                trackZ = 0f
                enemies.clear()
                for (i in 0 until 4) {
                    enemies.add(floatArrayOf((currentWidth / 2 + (i - 2) * 12).toFloat(), 20f + i * 8f, 0.4f + i * 0.1f, 1f))
                }
            }
            "PS2_MECH" -> {
                playerX = (currentWidth / 2).toFloat()
                playerY = (currentHeight / 2).toFloat()
                playerAngle = 0f
                enemies.clear()
                bullets.clear()
                for (i in 0 until 5) {
                    enemies.add(floatArrayOf(Random.nextInt(10, currentWidth - 10).toFloat(), Random.nextInt(8, currentHeight - 15).toFloat(), 100f, 1f))
                }
            }
            "PS1_STRIKE" -> {
                playerX = 30f
                enemies.clear()
                enemies.add(floatArrayOf((currentWidth - 30).toFloat(), 0f, 100f, 1f)) // Opponent health 100
            }
            "INVADERS" -> {
                playerX = 30f
                playerY = (currentHeight - 4).toFloat()
                enemyDir = 1f
                enemies.clear()
                bullets.clear()
                for (row in 0 until 3) {
                    for (col in 0 until 8) {
                        enemies.add(floatArrayOf(8f + col * 6f, 4f + row * 4f, row.toFloat(), 1f))
                    }
                }
            }
            "PONG" -> {
                playerX = 32f
                playerY = 16f
            }
            "SNAKE" -> {
                playerX = 20f
            }
            else -> {
                playerX = 30f
                playerY = 28f
            }
        }
    }

    /**
     * Modern PSP / PPSSPP Widescreen 16:9 3D Polygon Racing Simulation
     */
    private fun updatePspRacer() {
        display.fill(0)

        // Analog Stick or D-Pad steering
        val steerInput = if (abs(leftStickX) > 0.1f) leftStickX else (if (isDpadLeft) -1f else if (isDpadRight) 1f else 0f)
        val accelInput = isBtnA || isBtnR2 || (if (isDpadUp) true else false) // Cross / R2 gas
        val brakeInput = isBtnB || isBtnL2 || (if (isDpadDown) true else false) // Circle / L2 brake

        if (accelInput) {
            playerSpeed = (playerSpeed + 0.05f).coerceAtMost(2.5f)
        } else {
            playerSpeed = (playerSpeed - 0.02f).coerceAtLeast(0f)
        }

        if (brakeInput) {
            playerSpeed = (playerSpeed - 0.08f).coerceAtLeast(0f)
        }

        // Steer with speed
        playerX += steerInput * (playerSpeed * 0.8f)
        playerX = playerX.coerceIn(12f, (currentWidth - 14).toFloat())

        trackZ += playerSpeed * 2f
        trackCurve = sin(trackZ * 0.02f) * 1.8f

        // Draw 3D Perspective Horizon & Track
        val horizonY = (currentHeight * 0.38f).toInt()
        val cx = currentWidth / 2

        // Sky & mountains
        for (y in 0 until horizonY) {
            for (x in 0 until currentWidth) {
                if (y < 4 || (y % 4 == 0 && (x + trackZ.toInt()) % 12 < 6)) {
                    setPixel(x, y, 0xFF0070D1.toInt()) // PSP Blue Sky
                }
            }
        }

        // 3D Road Projection
        for (y in horizonY until currentHeight) {
            val progress = (y - horizonY).toFloat() / (currentHeight - horizonY)
            val roadWidth = progress * (currentWidth * 0.75f)
            val curveOffset = (1f - progress) * (1f - progress) * trackCurve * 15f
            val roadCenter = cx + curveOffset

            val leftEdge = (roadCenter - roadWidth / 2).toInt()
            val rightEdge = (roadCenter + roadWidth / 2).toInt()

            // Curbs & rumble strips
            val isStripe = ((y + trackZ.toInt() / 2) % 6) < 3
            val curbColor = if (isStripe) 0xFFEF4444.toInt() else 0xFFFFFFFF.toInt()
            val roadColor = if (isStripe) 0xFF1E293B.toInt() else 0xFF0F172A.toInt()

            for (x in 0 until currentWidth) {
                if (x in leftEdge..rightEdge) {
                    setPixel(x, y, roadColor)
                } else if (abs(x - leftEdge) <= 1 || abs(x - rightEdge) <= 1) {
                    setPixel(x, y, curbColor)
                }
            }
        }

        // Opponent cars in 3D perspective
        for (e in enemies) {
            e[1] += (e[2] - playerSpeed) * 0.5f
            if (e[1] < 10) e[1] = (currentHeight - 5).toFloat()
            if (e[1] > currentHeight) e[1] = 12f

            val ey = e[1].toInt()
            val ex = e[0].toInt()
            if (ey in horizonY until currentHeight - 3 && ex in 2 until currentWidth - 6) {
                // Opponent Car (Red Cyber Racer)
                setPixel(ex + 1, ey, 0xFFEF4444.toInt())
                setPixel(ex + 2, ey, 0xFFEF4444.toInt())
                setPixel(ex, ey + 1, 0xFFF59E0B.toInt())
                setPixel(ex + 1, ey + 1, 0xFFF59E0B.toInt())
                setPixel(ex + 2, ey + 1, 0xFFF59E0B.toInt())
                setPixel(ex + 3, ey + 1, 0xFFF59E0B.toInt())
            }
        }

        // Draw Player Car (Modern Sports Prototype in 3D)
        val px = playerX.toInt()
        val py = currentHeight - 9
        // Rear spoiler & lights
        setPixel(px - 1, py, 0xFF38BDF8.toInt())
        setPixel(px + 4, py, 0xFF38BDF8.toInt())
        for (x in px until px + 4) setPixel(x, py, 0xFF00E5FF.toInt())
        for (x in px - 1 until px + 5) setPixel(x, py + 1, 0xFF0070D1.toInt())
        // Tail lights
        setPixel(px - 1, py + 2, 0xFFEF4444.toInt())
        setPixel(px + 4, py + 2, 0xFFEF4444.toInt())
        setPixel(px, py + 2, 0xFF1E293B.toInt())
        setPixel(px + 1, py + 2, 0xFF38BDF8.toInt())
        setPixel(px + 2, py + 2, 0xFF38BDF8.toInt())
        setPixel(px + 3, py + 2, 0xFF1E293B.toInt())

        // Speedometer UI
        gameScore += (playerSpeed * 5).toInt()
        val kmh = (playerSpeed * 120).toInt()

        _uiState.value = _uiState.value.copy(
            score = gameScore,
            lives = kmh, // displays Speed in Telemetry!
            level = 1
        )
    }

    /**
     * Modern PS2 DualShock 3D Cyber Mech Arena Combat
     */
    private fun updatePs2Mech() {
        display.fill(0)

        // Left Analog Stick moves mech
        val moveX = if (abs(leftStickX) > 0.1f) leftStickX else (if (isDpadLeft) -1f else if (isDpadRight) 1f else 0f)
        val moveY = if (abs(leftStickY) > 0.1f) leftStickY else (if (isDpadUp) -1f else if (isDpadDown) 1f else 0f)

        // Right Analog Stick aims turret
        val aimX = if (abs(rightStickX) > 0.1f) rightStickX else (if (isBtnY) 0f else if (isBtnA) 0f else 0f)
        val aimY = if (abs(rightStickY) > 0.1f) rightStickY else 0f

        playerX = (playerX + moveX * 1.2f).coerceIn(6f, (currentWidth - 8).toFloat())
        playerY = (playerY + moveY * 1.2f).coerceIn(6f, (currentHeight - 8).toFloat())

        if (abs(aimX) > 0.2f || abs(aimY) > 0.2f) {
            playerAngle = kotlin.math.atan2(aimY, aimX)
        }

        // Fire Vulcan Cannon (R1 / R2 / Button Cross)
        if ((isBtnA || isBtnR1 || isBtnR2) && bullets.size < 6) {
            val bx = playerX + cos(playerAngle) * 4f
            val by = playerY + sin(playerAngle) * 4f
            bullets.add(floatArrayOf(bx, by, cos(playerAngle) * 2f, sin(playerAngle) * 2f, 1f))
            audioSynth.playSoundEffect(RetroAudioSynth.RetroSoundEffect.LASER_SHOOT)
            isBtnA = false
        }

        // Move Bullets
        val itB = bullets.iterator()
        while (itB.hasNext()) {
            val b = itB.next()
            b[0] += b[2]
            b[1] += b[3]
            if (b[0] < 1 || b[0] >= currentWidth - 1 || b[1] < 1 || b[1] >= currentHeight - 1) {
                itB.remove()
                continue
            }
            // Collision with enemy mechs
            for (e in enemies) {
                if (e[3] == 1f && abs(b[0] - e[0]) < 4 && abs(b[1] - e[1]) < 4) {
                    e[2] -= 35f // damage
                    itB.remove()
                    audioSynth.playSoundEffect(RetroAudioSynth.RetroSoundEffect.HIT)
                    if (e[2] <= 0) {
                        e[3] = 0f
                        gameScore += 250
                        audioSynth.playSoundEffect(RetroAudioSynth.RetroSoundEffect.EXPLOSION)
                    }
                    break
                }
            }
        }

        // Arena 3D Floor Grid
        for (y in 0 until currentHeight step 8) {
            for (x in 0 until currentWidth) setPixel(x, y, 0xFF192238.toInt())
        }
        for (x in 0 until currentWidth step 12) {
            for (y in 0 until currentHeight) setPixel(x, y, 0xFF192238.toInt())
        }

        // Draw Enemy Mechs (Hexagonal 3D units)
        for (e in enemies) {
            if (e[3] == 1f) {
                val ex = e[0].toInt()
                val ey = e[1].toInt()
                setPixel(ex, ey - 1, 0xFFEF4444.toInt())
                setPixel(ex - 1, ey, 0xFFEF4444.toInt())
                setPixel(ex, ey, 0xFFF59E0B.toInt())
                setPixel(ex + 1, ey, 0xFFEF4444.toInt())
                setPixel(ex, ey + 1, 0xFFEF4444.toInt())
            }
        }

        // Draw Player Mech with directional turret
        val px = playerX.toInt()
        val py = playerY.toInt()
        // Core Chassis
        setPixel(px - 1, py - 1, 0xFF0070D1.toInt())
        setPixel(px, py - 1, 0xFF00E5FF.toInt())
        setPixel(px + 1, py - 1, 0xFF0070D1.toInt())
        setPixel(px - 1, py, 0xFF00E5FF.toInt())
        setPixel(px, py, 0xFFFFFFFF.toInt())
        setPixel(px + 1, py, 0xFF00E5FF.toInt())
        setPixel(px - 1, py + 1, 0xFF0070D1.toInt())
        setPixel(px, py + 1, 0xFF00E5FF.toInt())
        setPixel(px + 1, py + 1, 0xFF0070D1.toInt())
        // Cannon barrel pointing in playerAngle
        val barrelX = (px + cos(playerAngle) * 3).toInt()
        val barrelY = (py + sin(playerAngle) * 3).toInt()
        setPixel(barrelX, barrelY, 0xFF00E5FF.toInt())

        // Bullets
        for (b in bullets) {
            setPixel(b[0].toInt(), b[1].toInt(), 0xFFFFFFFF.toInt())
        }

        // Respawn wave if all dead
        if (enemies.none { it[3] == 1f }) {
            gameLevel++
            gameScore += 1000
            initGameMode("PS2_MECH")
        }

        _uiState.value = _uiState.value.copy(score = gameScore, lives = gameLives, level = gameLevel)
    }

    /**
     * Modern PS1 3D Polygon Fighting Game Arena
     */
    private fun updatePs1Strike() {
        display.fill(0)
        val groundY = (currentHeight * 0.72f).toInt()

        // 3D checkerboard floor
        for (y in groundY until currentHeight) {
            val step = (y - groundY) + 4
            for (x in 0 until currentWidth) {
                if ((x / step + y / 2) % 2 == 0) {
                    setPixel(x, y, 0xFF334155.toInt())
                }
            }
        }

        // Movement
        if (isDpadLeft && playerX > 8) playerX -= 1f
        if (isDpadRight && playerX < currentWidth - 25) playerX += 1f

        // Attacks
        val isPunch = isBtnX || isBtnY // Square / Triangle punch
        val isKick = isBtnA || isBtnB // Cross / Circle kick

        val opponent = enemies.firstOrNull()
        if (opponent != null && opponent[3] == 1f) {
            val dist = abs(playerX - (currentWidth - 30f))
            if ((isPunch || isKick) && dist < 16f) {
                opponent[2] -= 8f
                audioSynth.playSoundEffect(RetroAudioSynth.RetroSoundEffect.HIT)
                gameScore += 50
                isBtnX = false
                isBtnA = false
            }
        }

        // Draw Player Fighter (Polygon Stance)
        val px = playerX.toInt()
        val py = groundY - 14
        // Head
        setPixel(px + 2, py, 0xFF38BDF8.toInt())
        setPixel(px + 3, py, 0xFF38BDF8.toInt())
        // Torso
        for (y in py + 1 until py + 6) {
            setPixel(px + 2, y, 0xFF0070D1.toInt())
            setPixel(px + 3, y, 0xFF0070D1.toInt())
        }
        // Legs & Arms (extended if punch/kick)
        if (isPunch) {
            for (x in px + 4 until px + 8) setPixel(x, py + 2, 0xFF38BDF8.toInt())
        }
        if (isKick) {
            for (x in px + 4 until px + 9) setPixel(x, py + 7, 0xFF00E5FF.toInt())
        }

        // Draw Opponent
        val ox = currentWidth - 26
        setPixel(ox, py, 0xFFEF4444.toInt())
        setPixel(ox + 1, py, 0xFFEF4444.toInt())
        for (y in py + 1 until py + 6) {
            setPixel(ox, y, 0xFFDC2626.toInt())
            setPixel(ox + 1, y, 0xFFDC2626.toInt())
        }

        // Health Bars at top
        val oppHp = ((opponent?.get(2) ?: 100f) / 100f * 24).toInt().coerceAtLeast(0)
        for (x in 6 until 30) setPixel(x, 4, 0xFF10B981.toInt())
        for (x in currentWidth - 30 until currentWidth - 30 + oppHp) setPixel(x, 4, 0xFFEF4444.toInt())

        _uiState.value = _uiState.value.copy(score = gameScore, lives = 3, level = 1)
    }

    private fun updateBuiltInGame() {
        when (currentGameId) {
            "INVADERS" -> updateInvaders()
            else -> updatePspRacer()
        }
    }

    private fun updateInvaders() {
        display.fill(0)
        if (isDpadLeft && playerX > 2) playerX -= 1f
        if (isDpadRight && playerX < currentWidth - 6) playerX += 1f

        if (isBtnA && bullets.size < 3) {
            bullets.add(floatArrayOf(playerX + 2, playerY - 1, 0f, -1.2f, 1f))
            audioSynth.playSoundEffect(RetroAudioSynth.RetroSoundEffect.LASER_SHOOT)
            isBtnA = false
        }

        val itB = bullets.iterator()
        while (itB.hasNext()) {
            val b = itB.next()
            b[1] += b[3]
            if (b[1] < 1 || b[1] >= currentHeight - 1) {
                itB.remove()
                continue
            }
            for (e in enemies) {
                if (e[3] == 1f && abs(b[0] - e[0]) < 3 && abs(b[1] - e[1]) < 2) {
                    e[3] = 0f
                    itB.remove()
                    gameScore += 100
                    audioSynth.playSoundEffect(RetroAudioSynth.RetroSoundEffect.EXPLOSION)
                    break
                }
            }
        }

        for (e in enemies) {
            if (e[3] == 1f) {
                val ex = e[0].toInt()
                val ey = e[1].toInt()
                setPixel(ex, ey, 0xFF00E5FF.toInt())
                setPixel(ex + 2, ey, 0xFF00E5FF.toInt())
                setPixel(ex + 1, ey + 1, 0xFF00E5FF.toInt())
            }
        }

        val px = playerX.toInt()
        val py = playerY.toInt()
        for (x in px until px + 5) setPixel(x, py, 0xFF10B981.toInt())
        setPixel(px + 2, py - 1, 0xFF10B981.toInt())

        for (b in bullets) setPixel(b[0].toInt(), b[1].toInt(), 0xFFFFFFFF.toInt())

        _uiState.value = _uiState.value.copy(score = gameScore, lives = gameLives, level = gameLevel)
    }

    private fun emulateCycle() {
        if (pc >= memory.size - 1) return
        val opcode = ((memory[pc].toInt() and 0xFF) shl 8) or (memory[pc + 1].toInt() and 0xFF)
        pc += 2

        val x = (opcode and 0x0F00) shr 8
        val y = (opcode and 0x00F0) shr 4
        val n = opcode and 0x000F
        val nn = opcode and 0x00FF
        val nnn = opcode and 0x0FFF

        when (opcode and 0xF000) {
            0x0000 -> when (opcode) {
                0x00E0 -> display.fill(0)
                0x00EE -> if (sp > 0) { sp--; pc = stack[sp] }
            }
            0x1000 -> pc = nnn
            0x2000 -> if (sp < stack.size) { stack[sp] = pc; sp++; pc = nnn }
            0x3000 -> if (v[x] == nn) pc += 2
            0x4000 -> if (v[x] != nn) pc += 2
            0x5000 -> if (v[x] == v[y]) pc += 2
            0x6000 -> v[x] = nn
            0x7000 -> v[x] = (v[x] + nn) and 0xFF
            0x8000 -> when (n) {
                0x0 -> v[x] = v[y]
                0x1 -> v[x] = v[x] or v[y]
                0x2 -> v[x] = v[x] and v[y]
                0x3 -> v[x] = v[x] xor v[y]
                0x4 -> { val sum = v[x] + v[y]; v[0xF] = if (sum > 255) 1 else 0; v[x] = sum and 0xFF }
                0x5 -> { v[0xF] = if (v[x] > v[y]) 1 else 0; v[x] = (v[x] - v[y]) and 0xFF }
                0x6 -> { v[0xF] = v[x] and 0x1; v[x] = v[x] shr 1 }
                0x7 -> { v[0xF] = if (v[y] > v[x]) 1 else 0; v[x] = (v[y] - v[x]) and 0xFF }
                0xE -> { v[0xF] = (v[x] and 0x80) shr 7; v[x] = (v[x] shl 1) and 0xFF }
            }
            0x9000 -> if (v[x] != v[y]) pc += 2
            0xA000 -> iReg = nnn
            0xB000 -> pc = (nnn + v[0]) and 0xFFF
            0xC000 -> v[x] = Random.nextInt(256) and nn
            0xD000 -> {
                val xPos = v[x] % currentWidth
                val yPos = v[y] % currentHeight
                v[0xF] = 0
                for (row in 0 until n) {
                    val spriteByte = memory[(iReg + row) % memory.size].toInt() and 0xFF
                    for (col in 0 until 8) {
                        if ((spriteByte and (0x80 shr col)) != 0) {
                            val pixelX = (xPos + col) % currentWidth
                            val pixelY = (yPos + row) % currentHeight
                            val index = pixelY * currentWidth + pixelX
                            if (display[index] != 0) v[0xF] = 1
                            display[index] = if (display[index] == 0) 0xFF00E5FF.toInt() else 0
                        }
                    }
                }
            }
        }
    }

    private fun setPixel(x: Int, y: Int, color: Int) {
        if (x in 0 until currentWidth && y in 0 until currentHeight) {
            display[y * currentWidth + x] = color
        }
    }

    private fun renderDisplay() {
        val filter = _uiState.value.currentFilter
        val pixels = IntArray(currentWidth * currentHeight)

        for (y in 0 until currentHeight) {
            val isEven = y % 2 == 0
            for (x in 0 until currentWidth) {
                val idx = y * currentWidth + x
                val rawColor = display[idx]
                if (rawColor != 0) {
                    pixels[idx] = when (filter) {
                        DisplayFilter.CRT_SCANLINES -> if (isEven) rawColor else dimColor(rawColor, 0.75f)
                        DisplayFilter.PSP_LCD_WIDE -> rawColor // Crisp 16:9 PSP vibrant color
                        DisplayFilter.LCD_GRID -> 0xFF8BAC0F.toInt()
                        DisplayFilter.RETRO_AMBER -> 0xFFFFB300.toInt()
                        DisplayFilter.CYBER_NEON -> 0xFFA855F7.toInt()
                        else -> rawColor
                    }
                } else {
                    pixels[idx] = when (filter) {
                        DisplayFilter.LCD_GRID -> 0xFF0F380F.toInt()
                        DisplayFilter.PSP_LCD_WIDE -> 0xFF0A0F1D.toInt() // Deep PSP obsidian
                        else -> 0xFF070A12.toInt()
                    }
                }
            }
        }

        frameBitmap.setPixels(pixels, 0, currentWidth, 0, 0, currentWidth, currentHeight)
        _bitmapFlow.value = frameBitmap
    }

    private fun dimColor(color: Int, factor: Float): Int {
        val a = (color shr 24) and 0xFF
        val r = (((color shr 16) and 0xFF) * factor).toInt()
        val g = (((color shr 8) and 0xFF) * factor).toInt()
        val b = ((color and 0xFF) * factor).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun serializeState(): String {
        return "$gameScore;$gameLives;$gameLevel;$pc;$iReg;$sp;${v.joinToString(",")}"
    }

    fun deserializeState(stateStr: String) {
        try {
            val parts = stateStr.split(";")
            if (parts.size >= 7) {
                gameScore = parts[0].toIntOrNull() ?: 0
                gameLives = parts[1].toIntOrNull() ?: 3
                gameLevel = parts[2].toIntOrNull() ?: 1
                pc = parts[3].toIntOrNull() ?: 0x200
                iReg = parts[4].toIntOrNull() ?: 0
                sp = parts[5].toIntOrNull() ?: 0
                _uiState.value = _uiState.value.copy(score = gameScore, lives = gameLives, level = gameLevel)
            }
        } catch (ignored: Exception) {
        }
    }

    private fun saveFrameForRewind() {
        val snapshot = ByteArray(currentWidth * currentHeight)
        for (i in display.indices) {
            snapshot[i] = if (display[i] != 0) 1 else 0
        }
        rewindBuffer.addLast(snapshot)
        if (rewindBuffer.size > MAX_REWIND_FRAMES) rewindBuffer.removeFirst()
    }

    fun rewindStep() {
        if (rewindBuffer.isNotEmpty()) {
            val prev = rewindBuffer.removeLast()
            for (i in prev.indices) {
                display[i] = if (prev[i] == 1.toByte()) 0xFF00E5FF.toInt() else 0
            }
            renderDisplay()
            audioSynth.playSquareTone(250f, 30)
        }
    }

    // Input hooks
    fun setDpad(up: Boolean, down: Boolean, left: Boolean, right: Boolean) {
        isDpadUp = up; isDpadDown = down; isDpadLeft = left; isDpadRight = right
    }

    fun setSticks(lx: Float, ly: Float, rx: Float, ry: Float) {
        leftStickX = lx; leftStickY = ly; rightStickX = rx; rightStickY = ry
    }

    fun setButtonA(pressed: Boolean) { isBtnA = pressed }
    fun setButtonB(pressed: Boolean) { isBtnB = pressed }
    fun setButtonX(pressed: Boolean) { isBtnX = pressed }
    fun setButtonY(pressed: Boolean) { isBtnY = pressed }
    fun setButtonL1(pressed: Boolean) { isBtnL1 = pressed }
    fun setButtonR1(pressed: Boolean) { isBtnR1 = pressed }
    fun setButtonL2(pressed: Boolean) { isBtnL2 = pressed }
    fun setButtonR2(pressed: Boolean) { isBtnR2 = pressed }
    fun setButtonStart(pressed: Boolean) { isBtnStart = pressed }
    fun setButtonSelect(pressed: Boolean) { isBtnSelect = pressed }
}
