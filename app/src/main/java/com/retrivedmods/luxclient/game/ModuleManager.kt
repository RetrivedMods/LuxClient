package com.retrivedmods.luxclient.game

import com.retrivedmods.luxclient.game.module.misc.AntiKickModule
import com.retrivedmods.luxclient.application.AppContext
import com.retrivedmods.luxclient.game.module.combat.AntiCrystalModule
import com.retrivedmods.luxclient.game.module.combat.AntiKnockbackModule
import com.retrivedmods.luxclient.game.module.combat.KillauraModule
import com.retrivedmods.luxclient.game.module.misc.DesyncModule
import com.retrivedmods.luxclient.game.module.misc.NoClipModule
import com.retrivedmods.luxclient.game.module.visual.ESPModule
import com.retrivedmods.luxclient.game.module.misc.PlayerTracerModule
import com.retrivedmods.luxclient.game.module.misc.PositionLoggerModule
import com.retrivedmods.luxclient.game.module.misc.TimeShiftModule
import com.retrivedmods.luxclient.game.module.misc.WeatherControllerModule
import com.retrivedmods.luxclient.game.module.motion.AirJumpModule
import com.retrivedmods.luxclient.game.module.motion.AntiAFKModule
import com.retrivedmods.luxclient.game.module.motion.AutoWalkModule
import com.retrivedmods.luxclient.game.module.motion.BhopModule
import com.retrivedmods.luxclient.game.module.motion.FlyModule
import com.retrivedmods.luxclient.game.module.motion.HighJumpModule
import com.retrivedmods.luxclient.game.module.motion.JetPackModule
import com.retrivedmods.luxclient.game.module.motion.MotionFlyModule
import com.retrivedmods.luxclient.game.module.motion.MotionVarModule
import com.retrivedmods.luxclient.game.module.motion.SpeedModule
import com.retrivedmods.luxclient.game.module.motion.SprintModule
import com.retrivedmods.luxclient.game.module.visual.FreeCameraModule
import com.retrivedmods.luxclient.game.module.visual.NoHurtCameraModule
import com.retrivedmods.luxclient.game.module.visual.ZoomModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject


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
            add(NoClipModule())
            add(SpeedModule())
            add(JetPackModule())
            add(HighJumpModule())
            add(AntiKnockbackModule())
            add(BhopModule())
            add(SprintModule())
            add(NoHurtCameraModule())
            add(AutoWalkModule())
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

}
