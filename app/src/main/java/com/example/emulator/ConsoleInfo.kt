package com.example.emulator

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArcadeNeon
import com.example.ui.theme.GbaIndigo
import com.example.ui.theme.GbGreen
import com.example.ui.theme.GenesisBlue
import com.example.ui.theme.NesRed
import com.example.ui.theme.PsxGrey
import com.example.ui.theme.SnesPurple

enum class ConsoleType(
    val id: String,
    val displayName: String,
    val shortName: String,
    val primaryColor: Color,
    val supportedExtensions: List<String>,
    val defaultAspectRatio: Float,
    val cpuArch: String,
    val resolution: String,
    val isModern3D: Boolean = false,
    val usesPlayStationSymbols: Boolean = false
) {
    PPSSPP(
        id = "PPSSPP",
        displayName = "Sony PlayStation Portable (PSP / PPSSPP)",
        shortName = "PSP",
        primaryColor = Color(0xFF0070D1), // Sony PlayStation Blue
        supportedExtensions = listOf("iso", "cso", "pbp", "prx"),
        defaultAspectRatio = 16f / 9f,
        cpuArch = "MIPS R4000 Allegrex 333MHz",
        resolution = "480 x 272 (Hasta 4K Vulkan/GL)",
        isModern3D = true,
        usesPlayStationSymbols = true
    ),
    PS2(
        id = "PS2",
        displayName = "Sony PlayStation 2",
        shortName = "PS2",
        primaryColor = Color(0xFF00439C), // PS2 Deep Blue
        supportedExtensions = listOf("iso", "bin", "chd", "gz"),
        defaultAspectRatio = 16f / 9f,
        cpuArch = "Emotion Engine (MIPS R5900 @ 294MHz)",
        resolution = "640 x 448 (Hasta 1080p/4K)",
        isModern3D = true,
        usesPlayStationSymbols = true
    ),
    PSX(
        id = "PSX",
        displayName = "Sony PlayStation 1 (PSX)",
        shortName = "PS1",
        primaryColor = PsxGrey,
        supportedExtensions = listOf("iso", "cue", "bin", "chd", "pbp"),
        defaultAspectRatio = 4f / 3f,
        cpuArch = "MIPS R3000A (32-bit @ 33.8MHz)",
        resolution = "320 x 240 (DualShock 3D)",
        isModern3D = true,
        usesPlayStationSymbols = true
    ),
    N64(
        id = "N64",
        displayName = "Nintendo 64",
        shortName = "N64",
        primaryColor = Color(0xFFE52521),
        supportedExtensions = listOf("z64", "n64", "v64"),
        defaultAspectRatio = 4f / 3f,
        cpuArch = "NEC VR4300 (64-bit @ 93.75MHz)",
        resolution = "320 x 240 (Render 3D RCP)",
        isModern3D = true,
        usesPlayStationSymbols = false
    ),
    NDS(
        id = "NDS",
        displayName = "Nintendo DS",
        shortName = "NDS",
        primaryColor = Color(0xFF6B7280),
        supportedExtensions = listOf("nds", "dsi"),
        defaultAspectRatio = 4f / 3f,
        cpuArch = "ARM946E-S + ARM7TDMI",
        resolution = "256 x 192 (Doble pantalla)",
        isModern3D = true,
        usesPlayStationSymbols = false
    ),
    GBA(
        id = "GBA",
        displayName = "Game Boy Advance",
        shortName = "GBA",
        primaryColor = GbaIndigo,
        supportedExtensions = listOf("gba"),
        defaultAspectRatio = 3f / 2f,
        cpuArch = "ARM7TDMI (32-bit)",
        resolution = "240 x 160",
        isModern3D = false,
        usesPlayStationSymbols = false
    ),
    SNES(
        id = "SNES",
        displayName = "Super Nintendo Entertainment System",
        shortName = "SNES",
        primaryColor = SnesPurple,
        supportedExtensions = listOf("sfc", "smc", "fig"),
        defaultAspectRatio = 4f / 3f,
        cpuArch = "Ricoh 5A22 (65C816)",
        resolution = "256 x 224",
        isModern3D = false,
        usesPlayStationSymbols = false
    ),
    GENESIS(
        id = "GENESIS",
        displayName = "Sega Genesis / Mega Drive",
        shortName = "GENESIS",
        primaryColor = GenesisBlue,
        supportedExtensions = listOf("bin", "gen", "md", "smd"),
        defaultAspectRatio = 4f / 3f,
        cpuArch = "Motorola 68000 + Z80",
        resolution = "320 x 224",
        isModern3D = false,
        usesPlayStationSymbols = false
    ),
    NES(
        id = "NES",
        displayName = "Nintendo Entertainment System",
        shortName = "NES",
        primaryColor = NesRed,
        supportedExtensions = listOf("nes", "unf", "fds"),
        defaultAspectRatio = 4f / 3f,
        cpuArch = "Ricoh 2A03 (6502)",
        resolution = "256 x 240",
        isModern3D = false,
        usesPlayStationSymbols = false
    ),
    GB(
        id = "GB",
        displayName = "Game Boy / GBC",
        shortName = "GB/GBC",
        primaryColor = GbGreen,
        supportedExtensions = listOf("gb", "gbc"),
        defaultAspectRatio = 10f / 9f,
        cpuArch = "Sharp LR35902 (Z80)",
        resolution = "160 x 144",
        isModern3D = false,
        usesPlayStationSymbols = false
    ),
    ARCADE(
        id = "ARCADE",
        displayName = "Arcade & Retro Bytecode VM",
        shortName = "ARCADE",
        primaryColor = ArcadeNeon,
        supportedExtensions = listOf("ch8", "rom"),
        defaultAspectRatio = 2f / 1f,
        cpuArch = "COSMAC VIP / 8-Bit VM",
        resolution = "64 x 32 (Scaled)",
        isModern3D = false,
        usesPlayStationSymbols = false
    );

    companion object {
        fun fromExtension(extension: String): ConsoleType {
            val cleanExt = extension.lowercase().removePrefix(".")
            return values().firstOrNull { it.supportedExtensions.contains(cleanExt) } ?: ARCADE
        }

        fun fromString(id: String): ConsoleType {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ARCADE
        }
    }
}

data class RomHeaderInfo(
    val title: String,
    val consoleType: ConsoleType,
    val fileSizeBytes: Long,
    val mapperOrChip: String,
    val headerChecksum: String,
    val isBatteryBacked: Boolean,
    val region: String,
    val isValidRom: Boolean,
    val targetEngine: String = "Internal Core"
)
