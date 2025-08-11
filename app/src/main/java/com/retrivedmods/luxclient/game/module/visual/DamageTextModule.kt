package com.retrivedmods.luxclient.game.module.visual

import com.retrivedmods.luxclient.game.InterceptablePacket
import com.retrivedmods.luxclient.game.Module
import com.retrivedmods.luxclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import com.retrivedmods.luxclient.game.entity.Player

class DamageTextModule : Module("DamageText", ModuleCategory.Visual) {


    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is EntityEventPacket && packet.type == EntityEventType.HURT) {
            val entityId = packet.runtimeEntityId


            if (entityId == session.localPlayer.runtimeEntityId) return


            val entity = session.level.entityMap[entityId]


            if (entity is Player) {
                val playerName = entity.username

                val stateText = "$playerName§r §cEnemy Damaged"
                val status = "§f$stateText"
                val message = " $status"



                session.displayClientMessage(message)
            }
        }
    }
}