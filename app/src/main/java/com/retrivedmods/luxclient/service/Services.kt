package com.retrivedmods.luxclient.service

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.graphics.PixelFormat
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.retrivedmods.luxclient.game.AccountManager
import com.retrivedmods.luxclient.game.GameSession
import com.retrivedmods.luxclient.game.ModuleManager
import com.retrivedmods.luxclient.game.module.visual.ESPModule
import com.retrivedmods.luxclient.model.CaptureModeModel
import com.retrivedmods.luxclient.overlay.OverlayManager
import com.retrivedmods.luxclient.render.RenderOverlayView
import com.radiantbyte.novarelay.NovaRelay
import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.definition.Definitions
import com.radiantbyte.novarelay.listener.AutoCodecPacketListener
import com.radiantbyte.novarelay.listener.GamingPacketHandler
import com.radiantbyte.novarelay.listener.OnlineLoginPacketListener
import com.radiantbyte.novarelay.util.captureGamePacket
import java.io.File
import kotlin.concurrent.thread

@Suppress("MemberVisibilityCanBePrivate")
object Services {

    private val handler = Handler(Looper.getMainLooper())

    private var novaRelay: NovaRelay? = null
    private var thread: Thread? = null

    private var renderView: RenderOverlayView? = null
    private var windowManager: WindowManager? = null

    var isActive by mutableStateOf(false)

    fun toggle(context: Context, captureModeModel: CaptureModeModel) {
        if (!isActive) {
            on(context, captureModeModel)
            return
        }

        off()
    }

    private fun on(context: Context, captureModeModel: CaptureModeModel) {
        if (thread != null) {
            return
        }

        File(context.cacheDir, "token_cache.json")

        isActive = true
        handler.post {
            OverlayManager.show(context)
        }

        setupOverlay(context)

        thread = thread(
            name = "NovaRelayThread",
            priority = Thread.MAX_PRIORITY
        ) {
            runCatching {
                ModuleManager.loadConfig()
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                context.toast("Load configuration error: ${it.message}")
            }

            runCatching {
                Definitions.loadBlockPalette()
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                context.toast("Load block palette error: ${it.message}")
            }

            val selectedAccount = AccountManager.selectedAccount

            runCatching {
                novaRelay = captureGamePacket(
                    remoteAddress = NovaAddress(
                        captureModeModel.serverHostName,
                        captureModeModel.serverPort
                    )
                ) {
                    initModules(this)

                    listeners.add(AutoCodecPacketListener(this))
                    selectedAccount?.let { OnlineLoginPacketListener(this, it) }
                        ?.let { listeners.add(it) }
                    listeners.add(GamingPacketHandler(this))
                }
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                context.toast("Start NovaRelay error: ${it.stackTraceToString()}")
            }
        }
    }

    private fun off() {
        thread(name = "NovaRelayThread") {
            ModuleManager.saveConfig()
            handler.post {
                OverlayManager.dismiss()
            }
            removeOverlay()
            isActive = false
            thread?.interrupt()
            thread = null
        }
    }

    private fun Context.toast(message: String) {
        handler.post {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun initModules(novaRelaySession: NovaRelaySession) {
        val session = GameSession(novaRelaySession)
        novaRelaySession.listeners.add(session)

        for (module in ModuleManager.modules) {
            module.session = session
        }
        Log.e("Services", "Init session")
    }

    private fun setupOverlay(context: Context) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alpha = 0.8f
                flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                setFitInsetsTypes(0)
                setFitInsetsSides(0)
            }
        }

        renderView = RenderOverlayView(context)
        ESPModule.setRenderView(renderView!!)

        handler.post {
            try {
                windowManager?.addView(renderView, params)
            } catch (e: Exception) {
                e.printStackTrace()
                context.toast("Failed to add overlay view: ${e.message}")
            }
        }
    }

    private fun removeOverlay() {
        renderView?.let { view ->
            windowManager?.removeView(view)
            renderView = null
        }
    }
}
