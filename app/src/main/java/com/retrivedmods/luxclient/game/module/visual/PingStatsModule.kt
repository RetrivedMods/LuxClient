package com.retrivedmods.luxclient.game.module.visual

import com.retrivedmods.luxclient.game.InterceptablePacket
import com.retrivedmods.luxclient.game.Module
import com.retrivedmods.luxclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.ServerStatsPacket

class PingStatsModule : Module("PingStats", ModuleCategory.Misc) {

    private var lastDisplayTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is ServerStatsPacket) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastDisplayTime >= 1000) { // update every 1s
                val serverTime = packet.serverTime
                val ping = packet.networkTime
                session.displayClientMessage("§7[§bStats§7] §fPing: §a${ping.toInt()}ms §f| Server TPS time: §e${"%.2f".format(serverTime)}ms")
                lastDisplayTime = currentTime
            }
        }
    }

    override fun onDisabled() {
        lastDisplayTime = 0L
    }
}