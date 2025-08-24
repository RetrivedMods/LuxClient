package com.retrivedmods.luxclient.game.module.combat

import com.retrivedmods.luxclient.game.Module
import com.retrivedmods.luxclient.game.ModuleCategory
import com.retrivedmods.luxclient.game.InterceptablePacket
import com.retrivedmods.luxclient.game.entity.Entity
import com.retrivedmods.luxclient.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.*
import kotlin.random.Random

class OpFightBotModule : Module("OpFightBot", ModuleCategory.Combat) {

    private var playersOnly by boolValue("Players Only", false)
    private var filterInvisible by boolValue("Filter Invisible", true)

    private var mode by intValue("Mode", 1, 0..2) // 0=random, 1=strafe, 2=behind
    private var range by floatValue("Range", 2.5f, 1.5f..5.0f)
    private var passive by boolValue("Passive", false)

    private var hSpeed by floatValue("Horizontal Speed", 5.0f, 1.0f..7.0f)
    private var vSpeed by floatValue("Vertical Speed", 4.0f, 1.0f..7.0f)
    private var strafeSpeed by intValue("Strafe Speed", 20, 10..90)

    private var attack by boolValue("Attack", true)
    private var cps by intValue("CPS", 5, 1..20)
    private var packets by intValue("Packets", 1, 1..10)

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        val player = session.localPlayer
        val playerPos = player.vec3Position

        val target = session.level.entityMap.values
            .filter { it != player }
            .filter { !playersOnly || it is Player }
            .filter { !isEntityInvisible(it) }
            .minByOrNull { it.vec3Position.distanceSquared(playerPos) }
            ?: return

        val distance = playerPos.distance(target.vec3Position)
        val targetPos = target.vec3Position

        if (distance < range) {
            // --- Strafe or orbit around target ---
            val angle = when (mode) {
                0 -> Random.nextDouble() * 360.0
                1 -> (player.tickExists * strafeSpeed) % 360.0
                2 -> target.vec3Rotation.y + 180.0
                else -> 0.0
            }

            val rad = Math.toRadians(angle)
            val newPos = Vector3f.from(
                (targetPos.x - sin(rad) * range).toFloat(),
                (targetPos.y + 0.5f).toFloat(),
                (targetPos.z + cos(rad) * range).toFloat()
            )

            // --- Correct yaw/pitch ---
            val dx = targetPos.x - playerPos.x
            val dy = targetPos.y - playerPos.y
            val dz = targetPos.z - playerPos.z
            val horizDist = sqrt(dx * dx + dz * dz)

            val yaw = (Math.toDegrees(atan2(-dx, dz).toDouble()) % 360).toFloat()
            val pitch = (Math.toDegrees((-atan2(dy, horizDist)).toDouble()) % 360).toFloat()

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = newPos
                rotation = Vector3f.from(pitch, yaw, yaw) // pitch, yaw, headYaw
                mode = MovePlayerPacket.Mode.NORMAL
                isOnGround = true
                tick = player.tickExists
            })

            // --- Attack ---
            if (attack && (currentTime - lastAttackTime) >= (1000L / cps)) {
                repeat(packets) {
                    player.attack(target)
                }
                lastAttackTime = currentTime
            }
        } else if (!passive) {
            // --- Move toward target if too far ---
            val dx = targetPos.x - playerPos.x
            val dz = targetPos.z - playerPos.z
            val dir = atan2(dz, dx)

            val newPos = Vector3f.from(
                playerPos.x + cos(dir) * hSpeed,
                targetPos.y.coerceIn(playerPos.y - vSpeed, playerPos.y + vSpeed),
                playerPos.z + sin(dir) * hSpeed
            )

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = newPos
                rotation = player.vec3Rotation
                mode = MovePlayerPacket.Mode.NORMAL
                isOnGround = true
                tick = player.tickExists
            })
        }
    }

    private fun isEntityInvisible(entity: Entity): Boolean {
        if (!filterInvisible) return false
        if (entity.vec3Position.y < -30) return true

        val flags = entity.metadata[EntityDataTypes.FLAGS]
        if (flags is Number && (flags.toLong() and (1L shl 5)) != 0L) return true

        val name = entity.metadata[EntityDataTypes.NAME] as? String ?: ""
        return name.contains("invisible", ignoreCase = true) || name.isEmpty()
    }

    private fun Vector3f.distanceSquared(other: Vector3f): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    private fun Vector3f.distance(other: Vector3f): Float =
        sqrt(distanceSquared(other))

    private fun Vector3f.horizontalDistance(other: Vector3f): Float {
        val dx = x - other.x
        val dz = z - other.z
        return sqrt(dx * dx + dz * dz)
    }
}
