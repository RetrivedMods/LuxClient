package com.retrivedmods.luxclient.game

import android.content.Context
import android.net.Uri
import com.retrivedmods.luxclient.game.module.misc.AntiKickModule
import com.retrivedmods.luxclient.application.AppContext
import com.retrivedmods.luxclient.game.module.combat.AntiCrystalModule
import com.retrivedmods.luxclient.game.module.combat.AntiKnockbackModule
import com.retrivedmods.luxclient.game.module.combat.AutoClickerModule
import com.retrivedmods.luxclient.game.module.combat.HitboxModule
import com.retrivedmods.luxclient.game.module.combat.KillauraModule
import com.retrivedmods.luxclient.game.module.combat.OpFightBotModule
import com.retrivedmods.luxclient.game.module.combat.TPAuraModule
import com.retrivedmods.luxclient.game.module.misc.DesyncModule
import com.retrivedmods.luxclient.game.module.motion.NoClipModule
import com.retrivedmods.luxclient.game.module.visual.ESPModule
import com.retrivedmods.luxclient.game.module.misc.PlayerTracerModule
import com.retrivedmods.luxclient.game.module.misc.PositionLoggerModule
import com.retrivedmods.luxclient.game.module.misc.TimeShiftModule
import com.retrivedmods.luxclient.game.module.misc.WeatherControllerModule
import com.retrivedmods.luxclient.game.module.motion.AirJumpModule
import com.retrivedmods.luxclient.game.module.motion.AntiAFKModule
import com.retrivedmods.luxclient.game.module.motion.BhopModule
import com.retrivedmods.luxclient.game.module.motion.FlyModule
import com.retrivedmods.luxclient.game.module.motion.HighJumpModule
import com.retrivedmods.luxclient.game.module.motion.JetPackModule
import com.retrivedmods.luxclient.game.module.motion.MotionFlyModule
import com.retrivedmods.luxclient.game.module.motion.MotionVarModule
import com.retrivedmods.luxclient.game.module.motion.PlayerTPModule
import com.retrivedmods.luxclient.game.module.motion.SpeedModule
import com.retrivedmods.luxclient.game.module.motion.SprintModule
import com.retrivedmods.luxclient.game.module.visual.DamageTextModule
import com.retrivedmods.luxclient.game.module.visual.FreeCameraModule
import com.retrivedmods.luxclient.game.module.visual.NightVisionModule
import com.retrivedmods.luxclient.game.module.visual.NoHurtCameraModule
import com.retrivedmods.luxclient.game.module.visual.PingStatsModule
import com.retrivedmods.luxclient.game.module.visual.PlayerJoinModule
import com.retrivedmods.luxclient.game.module.visual.ZoomModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File


object ModuleManager {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        with(_modules) {
            add(AntiKickModule())
            add(FlyModule())
            add(ZoomModule())
            add(AirJumpModule())
            add(ESPModule())
            add(DamageTextModule())
            add(PlayerJoinModule())
            add(NightVisionModule())
            add(AutoClickerModule())
            add(HitboxModule())
            add(PlayerTPModule())
            add(NoClipModule())
            add(SpeedModule())
            add(JetPackModule())
            add(HighJumpModule())
            add(AntiKnockbackModule())
            add(BhopModule())
            add(SprintModule())
            add(NoHurtCameraModule())
            add(OpFightBotModule())
            add(PingStatsModule())
            add(TPAuraModule())
            add(AntiAFKModule())
            add(DesyncModule())
            add(PositionLoggerModule())
            add(MotionFlyModule())
            add(FreeCameraModule())
            add(KillauraModule())
            add(AntiCrystalModule())
            add(TimeShiftModule())
            add(WeatherControllerModule())
            add(MotionVarModule())
            add(PlayerTracerModule())
        }
    }

    fun saveConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }

        config.writeText(json.encodeToString(jsonObject))
    }

    fun loadConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) {
            return
        }

        val jsonString = config.readText()
        if (jsonString.isEmpty()) {
            return
        }

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val modules = jsonObject["modules"]!!.jsonObject
        _modules.forEach { module ->
            (modules[module.name] as? JsonObject)?.let {
                module.fromJson(it)
            }
        }
    }

    fun exportConfig(): String {
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }
        return json.encodeToString(jsonObject)
    }

    fun importConfig(configStr: String) {
        try {
            val jsonObject = json.parseToJsonElement(configStr).jsonObject
            val modules = jsonObject["modules"]?.jsonObject ?: return

            _modules.forEach { module ->
                modules[module.name]?.let {
                    if (it is JsonObject) {
                        module.fromJson(it)
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid config format")
        }
    }

    fun exportConfigToFile(context: Context, fileName: String): Boolean {
        return try {
            val configsDir = context.getExternalFilesDir("configs")
            configsDir?.mkdirs()

            val configFile = File(configsDir, "$fileName.json")
            configFile.writeText(exportConfig())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importConfigFromFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val configStr = input.bufferedReader().readText()
                importConfig(configStr)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}