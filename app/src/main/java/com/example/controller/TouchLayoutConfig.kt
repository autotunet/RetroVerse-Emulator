package com.example.controller

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TouchElementId(val displayName: String, val description: String) {
    DPAD("Cruceta D-Pad", "Direcciones arriba, abajo, izquierda y derecha"),
    ACTION_BUTTONS("Botones de Acción", "Botones A, B, X, Y (o ✕, ○, □, △)"),
    ANALOG_STICK("Stick Analógico", "Palanca analógica para PSP y PS2"),
    SHOULDERS("Gatillos L1/L2/R1/R2", "Botones de hombro superiores"),
    START_SELECT("Select & Start", "Botones de menú y pausa del sistema"),
    QUICK_MENU("Barra Rápida Flotante", "Guardado rápido, velocidad y shaders")
}

data class TouchElementConfig(
    val offsetX: Float = 0f, // in dp
    val offsetY: Float = 0f, // in dp
    val scale: Float = 1.0f  // 0.6f .. 1.6f
)

data class TouchControllerLayout(
    val dpad: TouchElementConfig = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 1.0f),
    val actionButtons: TouchElementConfig = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 1.0f),
    val analogStick: TouchElementConfig = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 1.0f),
    val shoulders: TouchElementConfig = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 1.0f),
    val startSelect: TouchElementConfig = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 1.0f),
    val quickMenu: TouchElementConfig = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 1.0f)
) {
    fun getElement(element: TouchElementId): TouchElementConfig = when (element) {
        TouchElementId.DPAD -> dpad
        TouchElementId.ACTION_BUTTONS -> actionButtons
        TouchElementId.ANALOG_STICK -> analogStick
        TouchElementId.SHOULDERS -> shoulders
        TouchElementId.START_SELECT -> startSelect
        TouchElementId.QUICK_MENU -> quickMenu
    }

    fun updateElement(element: TouchElementId, config: TouchElementConfig): TouchControllerLayout = when (element) {
        TouchElementId.DPAD -> copy(dpad = config)
        TouchElementId.ACTION_BUTTONS -> copy(actionButtons = config)
        TouchElementId.ANALOG_STICK -> copy(analogStick = config)
        TouchElementId.SHOULDERS -> copy(shoulders = config)
        TouchElementId.START_SELECT -> copy(startSelect = config)
        TouchElementId.QUICK_MENU -> copy(quickMenu = config)
    }

    companion object {
        val DEFAULT = TouchControllerLayout()

        val LOW_THUMBS = TouchControllerLayout(
            dpad = TouchElementConfig(offsetX = 4f, offsetY = 20f, scale = 1.05f),
            actionButtons = TouchElementConfig(offsetX = -4f, offsetY = 20f, scale = 1.05f),
            analogStick = TouchElementConfig(offsetX = 10f, offsetY = 20f, scale = 1.0f),
            shoulders = TouchElementConfig(offsetX = 0f, offsetY = -4f, scale = 1.0f),
            startSelect = TouchElementConfig(offsetX = 0f, offsetY = 14f, scale = 0.95f),
            quickMenu = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 1.0f)
        )

        val COMPACT = TouchControllerLayout(
            dpad = TouchElementConfig(offsetX = 8f, offsetY = 0f, scale = 0.85f),
            actionButtons = TouchElementConfig(offsetX = -8f, offsetY = 0f, scale = 0.85f),
            analogStick = TouchElementConfig(offsetX = 4f, offsetY = 0f, scale = 0.85f),
            shoulders = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 0.85f),
            startSelect = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 0.85f),
            quickMenu = TouchElementConfig(offsetX = 0f, offsetY = 0f, scale = 0.85f)
        )

        val TABLET_EXPANDED = TouchControllerLayout(
            dpad = TouchElementConfig(offsetX = 20f, offsetY = -10f, scale = 1.25f),
            actionButtons = TouchElementConfig(offsetX = -20f, offsetY = -10f, scale = 1.25f),
            analogStick = TouchElementConfig(offsetX = 24f, offsetY = -10f, scale = 1.2f),
            shoulders = TouchElementConfig(offsetX = 0f, offsetY = 6f, scale = 1.15f),
            startSelect = TouchElementConfig(offsetX = 0f, offsetY = -6f, scale = 1.1f),
            quickMenu = TouchElementConfig(offsetX = 0f, offsetY = 4f, scale = 1.1f)
        )
    }
}

class TouchLayoutManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("touch_controller_layout_prefs", Context.MODE_PRIVATE)

    private val _layoutState = MutableStateFlow(loadLayout())
    val layoutState: StateFlow<TouchControllerLayout> = _layoutState.asStateFlow()

    private fun loadLayout(): TouchControllerLayout {
        fun loadElement(prefix: String, default: TouchElementConfig): TouchElementConfig {
            val ox = prefs.getFloat("${prefix}_ox", default.offsetX)
            val oy = prefs.getFloat("${prefix}_oy", default.offsetY)
            val scale = prefs.getFloat("${prefix}_scale", default.scale)
            return TouchElementConfig(ox, oy, scale.coerceIn(0.6f, 1.6f))
        }

        return TouchControllerLayout(
            dpad = loadElement("dpad", TouchElementConfig()),
            actionButtons = loadElement("buttons", TouchElementConfig()),
            analogStick = loadElement("stick", TouchElementConfig()),
            shoulders = loadElement("shoulders", TouchElementConfig()),
            startSelect = loadElement("start_select", TouchElementConfig()),
            quickMenu = loadElement("quick_menu", TouchElementConfig())
        )
    }

    fun saveLayout(layout: TouchControllerLayout) {
        prefs.edit().apply {
            fun saveElement(prefix: String, config: TouchElementConfig) {
                putFloat("${prefix}_ox", config.offsetX)
                putFloat("${prefix}_oy", config.offsetY)
                putFloat("${prefix}_scale", config.scale)
            }
            saveElement("dpad", layout.dpad)
            saveElement("buttons", layout.actionButtons)
            saveElement("stick", layout.analogStick)
            saveElement("shoulders", layout.shoulders)
            saveElement("start_select", layout.startSelect)
            saveElement("quick_menu", layout.quickMenu)
            apply()
        }
        _layoutState.value = layout
    }

    fun updateElement(element: TouchElementId, config: TouchElementConfig) {
        val updated = _layoutState.value.updateElement(element, config)
        saveLayout(updated)
    }

    fun applyPreset(preset: TouchControllerLayout) {
        saveLayout(preset)
    }

    fun resetToDefault() {
        saveLayout(TouchControllerLayout.DEFAULT)
    }
}
