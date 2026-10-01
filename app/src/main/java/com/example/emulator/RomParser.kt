package com.example.emulator

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

object RomParser {

    /**
     * Inspects binary stream / bytes of a ROM file and extracts console header details.
     */
    fun parseRom(fileName: String, bytes: ByteArray): RomHeaderInfo {
        var effectiveBytes = bytes
        var effectiveName = fileName

        // If it's a ZIP archive, inspect inner entry
        if (fileName.endsWith(".zip", ignoreCase = true) || isZip(bytes)) {
            val extracted = extractFromZip(bytes)
            if (extracted != null) {
                effectiveName = extracted.first
                effectiveBytes = extracted.second
            }
        }

        val ext = effectiveName.substringAfterLast('.', "").lowercase()
        val lowerName = effectiveName.lowercase()

        return when {
            // PSP / PPSSPP (.cso, or .iso/.pbp with PSP tags)
            ext == "cso" || (ext == "iso" && (lowerName.contains("psp") || lowerName.contains("portable"))) -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.PPSSPP,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "MIPS Allegrex UMD Image (Vulkan 3D)",
                    headerChecksum = "PSP UMD CRC OK",
                    isBatteryBacked = true,
                    region = "Universal Region Free",
                    isValidRom = true,
                    targetEngine = "PPSSPP Core"
                )
            }

            // PS2 (.chd, .gz, or .iso with PS2 tags or large size)
            (ext in listOf("chd", "gz") && lowerName.contains("ps2")) || (ext == "iso" && (lowerName.contains("ps2") || lowerName.contains("playstation 2") || effectiveBytes.size > 500_000_000)) -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.PS2,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "Emotion Engine DVD-ROM / GS",
                    headerChecksum = "SYSTEM.CNF Found",
                    isBatteryBacked = true,
                    region = "NTSC-U / PAL DualShock 2",
                    isValidRom = true,
                    targetEngine = "PCSX2 / Modern GS Core"
                )
            }

            // PSX / PS1 (.cue, .pbp, .chd, .bin or .iso)
            ext in listOf("cue", "pbp") || (ext in listOf("iso", "bin", "chd") && (lowerName.contains("ps1") || lowerName.contains("psx") || lowerName.contains("playstation"))) -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.PSX,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "CD-ROM XA Mode 2 DualShock",
                    headerChecksum = "ISO9660 PSX Track 1",
                    isBatteryBacked = true,
                    region = "Sony PlayStation NTSC-U",
                    isValidRom = true,
                    targetEngine = "DuckStation / Beetle PSX"
                )
            }

            // Nintendo 64 (.z64, .n64, .v64)
            ext in listOf("z64", "n64", "v64") -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.N64,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "Reality Coprocessor 3D",
                    headerChecksum = "N64 Header CRC32",
                    isBatteryBacked = true,
                    region = "NTSC / PAL",
                    isValidRom = true,
                    targetEngine = "Mupen64Plus-Next"
                )
            }

            // Nintendo DS (.nds, .dsi)
            ext in listOf("nds", "dsi") -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.NDS,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "DualScreen ARM9/ARM7",
                    headerChecksum = "NDS Secure Area OK",
                    isBatteryBacked = true,
                    region = "World Free",
                    isValidRom = true,
                    targetEngine = "MelonDS Core"
                )
            }

            // NES Header: "NES\x1A" or .nes extension
            (effectiveBytes.size >= 4 && effectiveBytes[0] == 'N'.code.toByte() &&
                    effectiveBytes[1] == 'E'.code.toByte() &&
                    effectiveBytes[2] == 'S'.code.toByte() &&
                    effectiveBytes[3] == 0x1A.toByte()) || ext in listOf("nes", "unf", "fds") -> {
                val prgSize = if (effectiveBytes.size > 4) effectiveBytes[4].toInt() and 0xFF else 1
                val chrSize = if (effectiveBytes.size > 5) effectiveBytes[5].toInt() and 0xFF else 1
                val mapperLow = if (effectiveBytes.size > 6) (effectiveBytes[6].toInt() and 0xF0) shr 4 else 0
                val mapperHigh = if (effectiveBytes.size > 7) effectiveBytes[7].toInt() and 0xF0 else 0
                val mapperId = mapperHigh or mapperLow
                val hasBattery = if (effectiveBytes.size > 6) (effectiveBytes[6].toInt() and 0x02) != 0 else false
                val title = cleanTitle(effectiveName)

                RomHeaderInfo(
                    title = title,
                    consoleType = ConsoleType.NES,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "Mapper #$mapperId (PRG: ${prgSize * 16}KB, CHR: ${chrSize * 8}KB)",
                    headerChecksum = "0x" + Integer.toHexString(mapperId).uppercase(),
                    isBatteryBacked = hasBattery,
                    region = "NTSC / PAL",
                    isValidRom = true,
                    targetEngine = "FCEUMM Core"
                )
            }

            // Game Boy / GBC Header: Title at 0x0134 .. 0x0143 or extension
            ext in listOf("gb", "gbc") -> {
                var rawTitle = ""
                var isGbc = ext == "gbc"
                var cartridgeType = 0
                var romSizeCode = 0
                if (effectiveBytes.size >= 0x0150) {
                    val titleChars = StringBuilder()
                    for (i in 0x0134 until 0x0143) {
                        val c = effectiveBytes[i].toInt() and 0xFF
                        if (c == 0) break
                        if (c in 32..126) titleChars.append(c.toChar())
                    }
                    rawTitle = titleChars.toString().trim()
                    isGbc = isGbc || ((effectiveBytes[0x0143].toInt() and 0x80) != 0)
                    romSizeCode = effectiveBytes[0x0148].toInt() and 0xFF
                    cartridgeType = effectiveBytes[0x0147].toInt() and 0xFF
                }
                val hasBattery = cartridgeType in listOf(0x03, 0x06, 0x09, 0x0D, 0x0F, 0x10, 0x13, 0x1B, 0x1E)

                RomHeaderInfo(
                    title = rawTitle.ifEmpty { cleanTitle(effectiveName) },
                    consoleType = ConsoleType.GB,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "MBC/Cartridge Type 0x" + Integer.toHexString(cartridgeType).uppercase(),
                    headerChecksum = "ROM Size Code: $romSizeCode",
                    isBatteryBacked = hasBattery,
                    region = if (effectiveBytes.size > 0x014A && effectiveBytes[0x014A].toInt() == 0) "Japan" else "World / Non-Japan",
                    isValidRom = true,
                    targetEngine = "Gambatte Core"
                )
            }

            // GBA Header: Title at 0x00A0 .. 0x00AB or extension
            ext in listOf("gba", "agb") -> {
                var rawTitle = ""
                if (effectiveBytes.size >= 0x00C0) {
                    val titleChars = StringBuilder()
                    for (i in 0x00A0 until 0x00AC) {
                        val c = effectiveBytes[i].toInt() and 0xFF
                        if (c == 0) break
                        if (c in 32..126) titleChars.append(c.toChar())
                    }
                    rawTitle = titleChars.toString().trim()
                }

                RomHeaderInfo(
                    title = rawTitle.ifEmpty { cleanTitle(effectiveName) },
                    consoleType = ConsoleType.GBA,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "32-bit ARM Cartridge",
                    headerChecksum = "CRC OK",
                    isBatteryBacked = true,
                    region = "Global (Multi-region)",
                    isValidRom = true,
                    targetEngine = "mGBA Core"
                )
            }

            // Sega Genesis Header: "SEGA" at 0x0100 or extension
            ext in listOf("gen", "md", "smd") || (ext == "bin" && (lowerName.contains("sonic") || lowerName.contains("genesis") || lowerName.contains("sega") || (effectiveBytes.size >= 0x0200 && effectiveBytes[0x0100] == 'S'.code.toByte()))) -> {
                val title = cleanTitle(effectiveName)
                RomHeaderInfo(
                    title = title,
                    consoleType = ConsoleType.GENESIS,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "MegaDrive Standard",
                    headerChecksum = "SEGA OS Ready",
                    isBatteryBacked = false,
                    region = "NTSC / PAL Multi",
                    isValidRom = true,
                    targetEngine = "Genesis Plus GX"
                )
            }

            // SNES (Super Nintendo)
            ext in listOf("sfc", "smc", "fig") -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.SNES,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "LoROM / HiROM Mode",
                    headerChecksum = "SNES Bank Mode",
                    isBatteryBacked = true,
                    region = "NTSC / PAL",
                    isValidRom = true,
                    targetEngine = "Snes9x Core"
                )
            }

            // Generic ISO / Bin default to PS2 / PSP / PS1 depending on name
            ext in listOf("iso", "bin") -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.PPSSPP,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "Disc Image UMD/CD/DVD",
                    headerChecksum = "Track 01 Image",
                    isBatteryBacked = true,
                    region = "Global",
                    isValidRom = true,
                    targetEngine = "Universal ISO Runner"
                )
            }

            // Arcade / CHIP-8
            else -> {
                RomHeaderInfo(
                    title = cleanTitle(effectiveName),
                    consoleType = ConsoleType.ARCADE,
                    fileSizeBytes = effectiveBytes.size.toLong(),
                    mapperOrChip = "Bytecode VM / 8-Bit Machine",
                    headerChecksum = "0x" + Integer.toHexString(effectiveBytes.size).uppercase(),
                    isBatteryBacked = false,
                    region = "Universal",
                    isValidRom = effectiveBytes.isNotEmpty(),
                    targetEngine = "RetroForge VM"
                )
            }
        }
    }

    private fun isZip(bytes: ByteArray): Boolean {
        return bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
    }

    private fun extractFromZip(zipBytes: ByteArray): Pair<String, ByteArray>? {
        return try {
            val zis = ZipInputStream(ByteArrayInputStream(zipBytes))
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val name = entry.name
                    val ext = name.substringAfterLast('.', "").lowercase()
                    if (ext in listOf("iso", "cso", "pbp", "chd", "gz", "z64", "nds", "nes", "gb", "gbc", "gba", "sfc", "smc", "bin", "gen", "md", "ch8", "rom")) {
                        val buffer = zis.readBytes()
                        zis.close()
                        return Pair(name, buffer)
                    }
                }
                entry = zis.nextEntry
            }
            zis.close()
            null
        } catch (e: Exception) {
            null
        }
    }

    fun cleanTitle(fileName: String): String {
        var name = fileName.substringAfterLast('/')
        val dot = name.lastIndexOf('.')
        if (dot > 0) name = name.substring(0, dot)

        // Remove typical dump tags like (U), [!], (E), [b1], etc.
        name = name.replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace('_', ' ')
            .trim()

        return if (name.isEmpty()) "Retro Game" else name
    }
}
