package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val console: String, // NES, GB, GBC, GBA, SNES, GENESIS, PSX, ARCADE
    val filePath: String,
    val fileFormat: String, // .nes, .gb, .gba, .sfc, .ch8, etc.
    val fileSize: Long,
    val isBuiltIn: Boolean = false,
    val isFavorite: Boolean = false,
    val playTimeMinutes: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val cartridgeColor: Long = 0xFF00E5FF
)

@Entity(tableName = "save_states")
data class SaveStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val slot: Int, // 1 to 5, or 0 for QuickSave
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val stateData: String
)

@Entity(tableName = "controller_mappings")
data class ControllerMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceName: String,
    val emulatorAction: String, // DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, BTN_A, BTN_B, BTN_X, BTN_Y, BTN_L, BTN_R, BTN_START, BTN_SELECT, BTN_TURBO_A, BTN_TURBO_B, FAST_FORWARD, REWIND
    val keyCode: Int
)
