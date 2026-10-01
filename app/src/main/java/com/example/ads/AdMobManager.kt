package com.example.ads

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AdUnitType(
    val idName: String,
    val adUnitId: String,
    val title: String,
    val description: String,
    val callToAction: String,
    val isRewarded: Boolean = false
) {
    REWARDED_PRESTAMEVIP(
        idName = "Prestamevip Recompensa VIP",
        adUnitId = "ca-app-pub-1495262574338316/8970040322",
        title = "Prestamevip • Solicitud de Crédito Rápido",
        description = "¡Consigue tu crédito personal al instante con Prestamevip! Sin papeleos complejos y con aprobación en minutos. Mira el anuncio completo para desbloquear Shaders 4K y Rebobinado VIP.",
        callToAction = "SOLICITAR EN PRESTAMEVIP",
        isRewarded = true
    ),
    INTERSTITIAL_MAIN(
        idName = "Intersticial General",
        adUnitId = "ca-app-pub-1495262574338316/1182179137",
        title = "Mando Gamer Inalámbrico DualSense",
        description = "Siente el poder de la retroalimentación háptica y gatillos adaptables en tus juegos de PSP, PS2 y PS1 en RetroForge.",
        callToAction = "VER OFERTAS GAMER"
    ),
    INTERSTITIAL_GAME_START(
        idName = "Lanzador de Juegos",
        adUnitId = "ca-app-pub-1495262574338316/4962596335",
        title = "Razer Kishi V2 & Backbone One",
        description = "Convierte tu teléfono en una consola portátil de última generación con cero latencia para emuladores.",
        callToAction = "DESCUBRIR MANDO"
    ),
    INTERSTITIAL_IMPORT_ROM(
        idName = "Importador de ROMs",
        adUnitId = "ca-app-pub-1495262574338316/1182179137",
        title = "Almacenamiento Cloud y ROMs Retro",
        description = "Archivo ROM importado con éxito. Disfruta de tus títulos favoritos con aceleración por hardware Vulkan y OpenGL.",
        callToAction = "CARGAR JUEGO"
    ),
    INTERSTITIAL_TOOLS(
        idName = "Herramientas y Funciones",
        adUnitId = "ca-app-pub-1495262574338316/2714752655",
        title = "Herramientas Pro y Ranuras de Guardado",
        description = "Acceso a ranuras de guardado rápido, rebobinado en vivo, shaders CRT/LCD y personalización total de controles táctiles.",
        callToAction = "CONTINUAR"
    ),
    INTERSTITIAL_SAVE_STATE(
        idName = "Guardado de Partida",
        adUnitId = "ca-app-pub-1495262574338316/2714752655",
        title = "Tarjeta MicroSD Ultra 512GB para ROMs",
        description = "Guarda miles de juegos de PS2, PSP y PS1 sin quedarte sin espacio. Velocidad extrema A2 para cargas instantáneas.",
        callToAction = "COMPRAR AHORA"
    ),
    INTERSTITIAL_PAUSE(
        idName = "Pausa del Emulador",
        adUnitId = "ca-app-pub-1495262574338316/3503687777",
        title = "VPN Gamer de Alta Velocidad",
        description = "Juega online con latencia mínima, protección total de tus descargas y acceso a servidores de todo el mundo.",
        callToAction = "PROBAR GRATIS"
    ),
    BANNER_FOOTER(
        idName = "Banner AdMob Inferior",
        adUnitId = "ca-app-pub-1495262574338316/2803163788",
        title = "Google AdMob Publicidad",
        description = "Banner fijo compatible con AdMob.",
        callToAction = "MÁS INFORMACIÓN"
    )
}

data class PopupBannerAdState(
    val isVisible: Boolean = false,
    val currentAdUnit: AdUnitType = AdUnitType.INTERSTITIAL_MAIN,
    val adUnitId: String = AdUnitType.INTERSTITIAL_MAIN.adUnitId,
    val title: String = AdUnitType.INTERSTITIAL_MAIN.title,
    val description: String = AdUnitType.INTERSTITIAL_MAIN.description,
    val callToAction: String = AdUnitType.INTERSTITIAL_MAIN.callToAction,
    val sponsorName: String = "Google AdMob • Anuncio Patrocinado",
    val countdownSeconds: Int = 5,
    val canSkip: Boolean = false,
    val isRewarded: Boolean = false,
    val autoPopupEnabled: Boolean = true,
    val vipUnlocked: Boolean = false
)

class AdMobManager(private val context: Context) {
    companion object {
        const val AUTO_POPUP_INTERVAL_SEC = 150 // 2.5 minutes
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var countdownJob: Job? = null
    private var autoPopupIntervalJob: Job? = null

    private val _adState = MutableStateFlow(PopupBannerAdState())
    val adState: StateFlow<PopupBannerAdState> = _adState.asStateFlow()

    init {
        startAutoPopupTimer()
    }

    /**
     * Starts periodic automatic pop-up banner timer.
     */
    fun startAutoPopupTimer() {
        autoPopupIntervalJob?.cancel()
        autoPopupIntervalJob = scope.launch {
            while (isActive) {
                delay(AUTO_POPUP_INTERVAL_SEC * 1000L)
                if (_adState.value.autoPopupEnabled && !_adState.value.isVisible) {
                    showPopupAd(AdUnitType.INTERSTITIAL_MAIN)
                }
            }
        }
    }

    /**
     * Shows a specific AdMob pop-up interstitial unit with countdown.
     */
    fun showPopupAd(adUnit: AdUnitType) {
        countdownJob?.cancel()
        val duration = if (adUnit.isRewarded) 6 else 4

        _adState.value = _adState.value.copy(
            isVisible = true,
            currentAdUnit = adUnit,
            adUnitId = adUnit.adUnitId,
            title = adUnit.title,
            description = adUnit.description,
            callToAction = adUnit.callToAction,
            isRewarded = adUnit.isRewarded,
            countdownSeconds = duration,
            canSkip = false
        )

        countdownJob = scope.launch {
            var remaining = duration
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _adState.value = _adState.value.copy(
                    countdownSeconds = remaining,
                    canSkip = remaining <= 0
                )
            }
            // If rewarded ad completed, grant VIP status!
            if (adUnit.isRewarded) {
                _adState.value = _adState.value.copy(vipUnlocked = true)
            }
        }
    }

    /**
     * Shows the Prestamevip Rewarded Ad unit.
     */
    fun showRewardedPrestamevip() {
        showPopupAd(AdUnitType.REWARDED_PRESTAMEVIP)
    }

    /**
     * Shows ad when launching an emulated ROM game.
     */
    fun showAdForRomLaunch() {
        if (!_adState.value.vipUnlocked) {
            showPopupAd(AdUnitType.INTERSTITIAL_GAME_START)
        }
    }

    /**
     * Shows ad when importing an external ROM file.
     */
    fun showAdForRomImport() {
        if (!_adState.value.vipUnlocked) {
            showPopupAd(AdUnitType.INTERSTITIAL_IMPORT_ROM)
        }
    }

    /**
     * Shows ad when using emulator tools and functions (save states, rewinds, fast forward, shaders).
     */
    fun showAdForToolsAndFunctions() {
        if (!_adState.value.vipUnlocked) {
            showPopupAd(AdUnitType.INTERSTITIAL_TOOLS)
        }
    }

    /**
     * Dismisses the current pop-up banner.
     */
    fun dismissPopupBanner() {
        countdownJob?.cancel()
        _adState.value = _adState.value.copy(isVisible = false)
    }

    /**
     * Toggles automatic popups on/off.
     */
    fun toggleAutoPopup(enabled: Boolean) {
        _adState.value = _adState.value.copy(autoPopupEnabled = enabled)
        if (enabled) startAutoPopupTimer() else autoPopupIntervalJob?.cancel()
    }
}
