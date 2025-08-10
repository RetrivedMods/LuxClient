package com.retrivedmods.luxclient.game.module.misc

import com.retrivedmods.luxclient.game.InterceptablePacket
import com.retrivedmods.luxclient.game.Module
import com.retrivedmods.luxclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket

class AntiKickModule : Module("antikick", ModuleCategory.Misc) {

override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (!isEnabled) false  
        if (packet is DisconnectPacket) {
           false
        }
        true
    }
}
