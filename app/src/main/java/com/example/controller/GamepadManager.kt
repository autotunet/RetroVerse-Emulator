package com.example.controller

import android.content.Context
import android.hardware.input.InputManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

enum class ControllerProfile(
    val id: String,
    val displayName: String,
    val brand: String,
    val primaryAccent: Color,
    val usesPlayStationSymbols: Boolean
) {
    DUALSENSE(
        id = "DUALSENSE",
        displayName = "PlayStation 5 DualSense",
        brand = "Sony",
        primaryAccent = Color(0xFF0070D1),
        usesPlayStationSymbols = true
    ),
    DUALSHOCK4(
        id = "DUALSHOCK4",
        displayName = "PlayStation 4 DualShock 4",
        brand = "Sony",
        primaryAccent = Color(0xFF2979FF),
        usesPlayStationSymbols = true
    ),
    XBOX_SERIES(
        id = "XBOX_SERIES",
        displayName = "Xbox Wireless / Series X|S",
        brand = "Microsoft",
        primaryAccent = Color(0xFF107C10),
        usesPlayStationSymbols = false
    ),
    SWITCH_PRO(
        id = "SWITCH_PRO",
        displayName = "Nintendo Switch Pro / Joy-Con",
        brand = "Nintendo",
        primaryAccent = Color(0xFFE60012),
        usesPlayStationSymbols = false
    ),
    MOBILE_GRIP(
        id = "MOBILE_GRIP",
        displayName = "Razer Kishi / Backbone / GameSir",
        brand = "Mobile Gamer",
        primaryAccent = Color(0xFF00E5FF),
        usesPlayStationSymbols = false
    ),
    GENERIC(
        id = "GENERIC",
        displayName = "Mando Bluetooth HID Genérico",
        brand = "Universal",
        primaryAccent = Color(0xFFA855F7),
        usesPlayStationSymbols = false
    )
}

data class ConnectedControllerInfo(
    val id: Int,
    val name: String,
    val vendorId: Int,
    val productId: Int,
    val isBluetooth: Boolean,
    val hasVibration: Boolean,
    val detectedProfile: ControllerProfile
)

data class ControllerInputState(
    // Buttons (ABXY or PS Symbols: Cross, Circle, Square, Triangle)
    val btnA: Boolean = false, // Cross on PS
    val btnB: Boolean = false, // Circle on PS
    val btnX: Boolean = false, // Square on PS
    val btnY: Boolean = false, // Triangle on PS
    val btnL1: Boolean = false,
    val btnR1: Boolean = false,
    val btnL2: Boolean = false,
    val btnR2: Boolean = false,
    val btnSelect: Boolean = false, // Share / Create
    val btnStart: Boolean = false, // Options / Menu
    val btnThumbL: Boolean = false, // L3
    val btnThumbR: Boolean = false, // R3
    val btnTouchpad: Boolean = false, // DualSense Touchpad click
    // D-Pad
    val dpadUp: Boolean = false,
    val dpadDown: Boolean = false,
    val dpadLeft: Boolean = false,
    val dpadRight: Boolean = false,
    // Analog Sticks (-1.0 to 1.0)
    val leftStickX: Float = 0f,
    val leftStickY: Float = 0f,
    val rightStickX: Float = 0f,
    val rightStickY: Float = 0f,
    // Analog Triggers (0.0 to 1.0)
    val triggerL: Float = 0f,
    val triggerR: Float = 0f,
    // Last pressed key code & name for remapping
    val lastKeyCode: Int = 0,
    val lastKeyName: String = "",
    // Active Profile
    val activeProfile: ControllerProfile = ControllerProfile.DUALSENSE
)

class GamepadManager(private val context: Context) : InputManager.InputDeviceListener {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager

    private val _connectedControllers = MutableStateFlow<List<ConnectedControllerInfo>>(emptyList())
    val connectedControllers: StateFlow<List<ConnectedControllerInfo>> = _connectedControllers.asStateFlow()

    private val _inputState = MutableStateFlow(ControllerInputState())
    val inputState: StateFlow<ControllerInputState> = _inputState.asStateFlow()

    // Configurable Settings
    var deadzoneThreshold: Float = 0.15f
    var vibrationIntensity: Float = 1.0f

    // DualSense Simulated Lightbar Color (RGB Hex)
    val lightbarColor = MutableStateFlow(Color(0xFF0070D1))

    init {
        inputManager?.registerInputDeviceListener(this, null)
        refreshConnectedControllers()
    }

    fun cleanup() {
        inputManager?.unregisterInputDeviceListener(this)
    }

    fun setProfile(profile: ControllerProfile) {
        _inputState.value = _inputState.value.copy(activeProfile = profile)
        lightbarColor.value = profile.primaryAccent
    }

    fun setLightbarColor(color: Color) {
        lightbarColor.value = color
    }

    fun refreshConnectedControllers() {
        val list = mutableListOf<ConnectedControllerInfo>()
        val deviceIds = InputDevice.getDeviceIds()
        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
                    (sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)

            if (isGamepad && !device.isVirtual) {
                val hasVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    device.vibratorManager.defaultVibrator.hasVibrator()
                } else {
                    @Suppress("DEPRECATION")
                    device.vibrator.hasVibrator()
                }

                val devName = device.name ?: "Controlador Bluetooth"
                val profile = detectProfileFromName(devName)

                list.add(
                    ConnectedControllerInfo(
                        id = id,
                        name = devName,
                        vendorId = device.vendorId,
                        productId = device.productId,
                        isBluetooth = true,
                        hasVibration = hasVibrator,
                        detectedProfile = profile
                    )
                )
            }
        }
        _connectedControllers.value = list

        if (list.isNotEmpty()) {
            setProfile(list.first().detectedProfile)
        }
    }

    private fun detectProfileFromName(name: String): ControllerProfile {
        val lower = name.lowercase()
        return when {
            lower.contains("dualsense") || lower.contains("ps5") -> ControllerProfile.DUALSENSE
            lower.contains("dualshock") || lower.contains("ps4") || lower.contains("wireless controller") -> ControllerProfile.DUALSHOCK4
            lower.contains("xbox") || lower.contains("microsoft") -> ControllerProfile.XBOX_SERIES
            lower.contains("switch") || lower.contains("pro controller") || lower.contains("joy-con") -> ControllerProfile.SWITCH_PRO
            lower.contains("kishi") || lower.contains("backbone") || lower.contains("gamesir") || lower.contains("razer") -> ControllerProfile.MOBILE_GRIP
            else -> ControllerProfile.DUALSENSE // Default modern gamer profile
        }
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        refreshConnectedControllers()
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        refreshConnectedControllers()
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        refreshConnectedControllers()
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val isGamepadSource = (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
                (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) ||
                (event.source and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD)

        if (!isGamepadSource) return false

        val isDown = event.action == KeyEvent.ACTION_DOWN
        val keyCode = event.keyCode
        val keyName = KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")

        var cur = _inputState.value
        cur = cur.copy(lastKeyCode = keyCode, lastKeyName = keyName)

        cur = when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_DPAD_CENTER -> cur.copy(btnA = isDown)
            KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK -> cur.copy(btnB = isDown)
            KeyEvent.KEYCODE_BUTTON_X -> cur.copy(btnX = isDown)
            KeyEvent.KEYCODE_BUTTON_Y -> cur.copy(btnY = isDown)
            KeyEvent.KEYCODE_BUTTON_L1 -> cur.copy(btnL1 = isDown)
            KeyEvent.KEYCODE_BUTTON_R1 -> cur.copy(btnR1 = isDown)
            KeyEvent.KEYCODE_BUTTON_L2 -> cur.copy(btnL2 = isDown)
            KeyEvent.KEYCODE_BUTTON_R2 -> cur.copy(btnR2 = isDown)
            KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU -> cur.copy(btnStart = isDown)
            KeyEvent.KEYCODE_BUTTON_SELECT -> cur.copy(btnSelect = isDown)
            KeyEvent.KEYCODE_BUTTON_THUMBL -> cur.copy(btnThumbL = isDown)
            KeyEvent.KEYCODE_BUTTON_THUMBR -> cur.copy(btnThumbR = isDown)
            KeyEvent.KEYCODE_BUTTON_MODE -> cur.copy(btnTouchpad = isDown)
            KeyEvent.KEYCODE_DPAD_UP -> cur.copy(dpadUp = isDown)
            KeyEvent.KEYCODE_DPAD_DOWN -> cur.copy(dpadDown = isDown)
            KeyEvent.KEYCODE_DPAD_LEFT -> cur.copy(dpadLeft = isDown)
            KeyEvent.KEYCODE_DPAD_RIGHT -> cur.copy(dpadRight = isDown)
            else -> cur
        }

        _inputState.value = cur
        return true
    }

    fun handleMotionEvent(event: MotionEvent): Boolean {
        if ((event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) ||
            (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD)
        ) {
            val deadzone = deadzoneThreshold

            // Left Stick (Movement / NuB)
            var lx = event.getAxisValue(MotionEvent.AXIS_X)
            var ly = event.getAxisValue(MotionEvent.AXIS_Y)
            if (abs(lx) < deadzone) lx = 0f
            if (abs(ly) < deadzone) ly = 0f

            // Right Stick (Camera / Look)
            var rx = event.getAxisValue(MotionEvent.AXIS_Z)
            var ry = event.getAxisValue(MotionEvent.AXIS_RZ)
            if (abs(rx) < deadzone) rx = 0f
            if (abs(ry) < deadzone) ry = 0f

            // D-Pad Hat
            val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
            val hatLeft = hatX < -0.5f
            val hatRight = hatX > 0.5f
            val hatUp = hatY < -0.5f
            val hatDown = hatY > 0.5f

            // Triggers (L2 / R2)
            val lTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER).coerceAtLeast(event.getAxisValue(MotionEvent.AXIS_BRAKE))
            val rTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER).coerceAtLeast(event.getAxisValue(MotionEvent.AXIS_GAS))

            val dpadLeft = hatLeft || lx < -0.5f
            val dpadRight = hatRight || lx > 0.5f
            val dpadUp = hatUp || ly < -0.5f
            val dpadDown = hatDown || ly > 0.5f

            _inputState.value = _inputState.value.copy(
                leftStickX = lx,
                leftStickY = ly,
                rightStickX = rx,
                rightStickY = ry,
                triggerL = lTrigger,
                triggerR = rTrigger,
                dpadLeft = dpadLeft,
                dpadRight = dpadRight,
                dpadUp = dpadUp,
                dpadDown = dpadDown,
                btnL2 = lTrigger > 0.3f || _inputState.value.btnL2,
                btnR2 = rTrigger > 0.3f || _inputState.value.btnR2
            )
            return true
        }
        return false
    }

    fun vibrate(durationMs: Long = 80) {
        val effectiveMs = (durationMs * vibrationIntensity).toLong().coerceAtLeast(10)
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(effectiveMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(effectiveMs)
            }
        } catch (ignored: Exception) {
        }
    }

    fun vibrateHapticClick() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (180 * vibrationIntensity).toInt().coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(25, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (ignored: Exception) {
        }
    }
}
