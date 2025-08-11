package com.retrivedmods.luxclient.game.module.visual

import com.retrivedmods.luxclient.game.InterceptablePacket
import com.retrivedmods.luxclient.game.Module
import com.retrivedmods.luxclient.game.ModuleCategory
import com.retrivedmods.luxclient.game.data.Effect
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class NightVisionModule : Module("nightVision", ModuleCategory.Visual) {

    private val amplifierValue by intValue("amplifier", 1, 1..5)

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.REMOVE
                effectId = Effect.NIGHT_VISION
            })
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (session.localPlayer.tickExists % 20 == 0L) {
                session.clientBound(MobEffectPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    event = MobEffectPacket.Event.ADD
                    effectId = Effect.NIGHT_VISION
                    amplifier = amplifierValue
                    isParticles = false
                    duration = 360000
                })
            }
        }
    }
}