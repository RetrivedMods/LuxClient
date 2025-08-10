package com.retrivedmods.luxclient.game.module.combat

import com.retrivedmods.luxclient.game.InterceptablePacket
import com.retrivedmods.luxclient.game.Module
import com.retrivedmods.luxclient.game.ModuleCategory
import com.retrivedmods.luxclient.game.entity.Entity
import com.retrivedmods.luxclient.game.entity.EntityUnknown
import com.retrivedmods.luxclient.game.entity.LocalPlayer
import com.retrivedmods.luxclient.game.entity.MobList
import com.retrivedmods.luxclient.game.entity.Player
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)
    private var rangeValue by floatValue("range", 15f, 2f..15f)
    private var attackInterval by intValue("delay", 1, 1..5)
    private var cpsValue by intValue("cps", 50, 1..100)
    private var boost by intValue("packets", 1, 1..10)
    private var targetMode by intValue("Target Mode", 0, 0..2)
    private var switchDelay by intValue("Switch Delay", 500, 100..5000)

    private var lastAttackTime = 0L
    private var lastSwitchTime = 0L
    private var switchIndex = 0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if (packet.tick % attackInterval == 0L && (currentTime - lastAttackTime) >= minAttackDelay) {
                val targets = searchForClosestEntities()
                if (targets.isEmpty()) return

                when (targetMode) {
                    0 -> { // SINGLE
                        val target = targets.first()
                        attackEntity(target)
                    }
                    1 -> { // SWITCH
                        if ((currentTime - lastSwitchTime) >= switchDelay) {
                            switchIndex = (switchIndex + 1) % targets.size
                            lastSwitchTime = currentTime
                        }
                        attackEntity(targets[switchIndex])
                    }
                    2 -> { // MULTI
                        targets.forEach { attackEntity(it) }
                    }
                }

                lastAttackTime = currentTime
            }
        }
    }

    private fun attackEntity(entity: Entity) {
        repeat(boost) {
            session.localPlayer.attack(entity)
        }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) {
                    false
                } else if (playersOnly) {
                    !this.isBot()
                } else {
                    !this.isBot()
                }
            }
            is EntityUnknown -> {
                if (mobsOnly) {
                    isMob()
                } else if (playersOnly) {
                    false
                } else {
                    true
                }
            }
            else -> false
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return false
        return playerList.name.isBlank()
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity -> entity.distance(session.localPlayer) < rangeValue && entity.isTarget() }
            .sortedBy { it.distance(session.localPlayer) }
    }
}
