package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.emulator.ConsoleType
import com.example.emulator.RetroAudioSynth
import com.example.emulator.RetroConsoleEngine
import com.example.emulator.RomParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RetroForge", appName)
    }

    @Test
    fun `test rom parser recognizes console formats`() {
        val nesBytes = byteArrayOf('N'.code.toByte(), 'E'.code.toByte(), 'S'.code.toByte(), 0x1A.toByte(), 2, 1, 0, 0)
        val nesInfo = RomParser.parseRom("SuperMarioBros.nes", nesBytes)
        assertEquals(ConsoleType.NES, nesInfo.consoleType)
        assertTrue(nesInfo.isValidRom)

        val gbInfo = RomParser.parseRom("PokemonRed.gb", ByteArray(512))
        assertEquals(ConsoleType.GB, gbInfo.consoleType)

        val gbaInfo = RomParser.parseRom("ZeldaMinishCap.gba", ByteArray(512))
        assertEquals(ConsoleType.GBA, gbaInfo.consoleType)

        val snesInfo = RomParser.parseRom("ChronoTrigger.sfc", ByteArray(512))
        assertEquals(ConsoleType.SNES, snesInfo.consoleType)

        val genInfo = RomParser.parseRom("SonicTheHedgehog.bin", ByteArray(512))
        assertEquals(ConsoleType.GENESIS, genInfo.consoleType)

        val pspInfo = RomParser.parseRom("RidgeRacer.cso", ByteArray(512))
        assertEquals(ConsoleType.PPSSPP, pspInfo.consoleType)

        val ps2Info = RomParser.parseRom("Tekken5_PS2.iso", ByteArray(512))
        assertEquals(ConsoleType.PS2, ps2Info.consoleType)

        val ps1Info = RomParser.parseRom("CrashBandicoot_PS1.cue", ByteArray(512))
        assertEquals(ConsoleType.PSX, ps1Info.consoleType)
    }

    @Test
    fun `test admob manager loads ad unit correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val adManager = com.example.ads.AdMobManager(context)
        assertEquals("ca-app-pub-1495262574338316/1182179137", adManager.adState.value.adUnitId)
        
        adManager.showPopupAd(com.example.ads.AdUnitType.INTERSTITIAL_GAME_START)
        assertEquals("ca-app-pub-1495262574338316/4962596335", adManager.adState.value.adUnitId)
        assertTrue(adManager.adState.value.isVisible)

        adManager.showRewardedPrestamevip()
        assertEquals("ca-app-pub-1495262574338316/8970040322", adManager.adState.value.adUnitId)
        assertTrue(adManager.adState.value.isRewarded)

        adManager.dismissPopupBanner()
        org.junit.Assert.assertFalse(adManager.adState.value.isVisible)
    }

    @Test
    fun `test emulator engine initializes and runs game`() {
        val synth = RetroAudioSynth()
        val engine = RetroConsoleEngine(synth)

        engine.loadGame("INVADERS", "Space Invaders", ConsoleType.ARCADE)
        val state = engine.uiState.value
        assertEquals("Space Invaders", state.gameTitle)
        assertEquals(3, state.lives)
        assertEquals(0, state.score)
        assertTrue(state.isRunning)

        // Save & load state check
        val serialized = engine.serializeState()
        assertNotNull(serialized)
        assertTrue(serialized.contains(";"))

        engine.deserializeState(serialized)
        assertEquals(state.score, engine.uiState.value.score)

        engine.stop()
    }

    @Test
    fun `test touch layout manager customization and presets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val layoutManager = com.example.controller.TouchLayoutManager(context)

        // Initial layout check
        val initial = layoutManager.layoutState.value
        assertEquals(1.0f, initial.dpad.scale, 0.01f)

        // Custom update
        val updatedDpad = com.example.controller.TouchElementConfig(offsetX = 15f, offsetY = -20f, scale = 1.35f)
        layoutManager.updateElement(com.example.controller.TouchElementId.DPAD, updatedDpad)
        val afterUpdate = layoutManager.layoutState.value
        assertEquals(15f, afterUpdate.dpad.offsetX, 0.01f)
        assertEquals(-20f, afterUpdate.dpad.offsetY, 0.01f)
        assertEquals(1.35f, afterUpdate.dpad.scale, 0.01f)

        // Presets
        layoutManager.applyPreset(com.example.controller.TouchControllerLayout.LOW_THUMBS)
        assertEquals(1.05f, layoutManager.layoutState.value.dpad.scale, 0.01f)

        // Reset
        layoutManager.resetToDefault()
        assertEquals(1.0f, layoutManager.layoutState.value.dpad.scale, 0.01f)
        assertEquals(0f, layoutManager.layoutState.value.dpad.offsetX, 0.01f)
    }

    @Test
    fun `test admob triggers for opening rom, importing rom, and tools`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val adManager = com.example.ads.AdMobManager(context)

        // Opening ROM ad trigger
        adManager.showAdForRomLaunch()
        assertTrue(adManager.adState.value.isVisible)
        assertEquals("ca-app-pub-1495262574338316/4962596335", adManager.adState.value.adUnitId)

        // Importing ROM ad trigger
        adManager.showAdForRomImport()
        assertTrue(adManager.adState.value.isVisible)
        assertEquals("ca-app-pub-1495262574338316/1182179137", adManager.adState.value.adUnitId)

        // Tools and functions ad trigger
        adManager.showAdForToolsAndFunctions()
        assertTrue(adManager.adState.value.isVisible)
        assertEquals("ca-app-pub-1495262574338316/2714752655", adManager.adState.value.adUnitId)

        adManager.dismissPopupBanner()
        org.junit.Assert.assertFalse(adManager.adState.value.isVisible)
    }

    @Test
    fun `test gamepad manager haptic click feedback`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val gamepadManager = com.example.controller.GamepadManager(context)

        // Does not throw exceptions and vibrates properly
        gamepadManager.vibrateHapticClick()
        gamepadManager.vibrate(30)
    }
}
