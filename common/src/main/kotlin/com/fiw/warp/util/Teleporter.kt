package com.fiw.warp.util

import com.fiw.warp.FiwWarp
import com.fiw.warp.data.WarpLocation
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Relative
import java.util.EnumSet

/** Performs the actual teleport plus the optional particle/sound effects. */
object Teleporter {

	private val NO_RELATIVE: Set<Relative> = EnumSet.noneOf(Relative::class.java)

	fun teleport(player: ServerPlayer, level: ServerLevel, target: WarpLocation) {
		val cfg = FiwWarp.config
		if (cfg.effectsEnabled) {
			if (cfg.vanishParticlesEnabled) vanishBurst(player.level(), player.x, player.y, player.z)
			else portalBurst(player.level(), player.x, player.y, player.z)
		}

		player.teleportTo(level, target.x, target.y, target.z, NO_RELATIVE, target.yaw, target.pitch, false)

		if (cfg.effectsEnabled) portalBurst(player.level(), player.x, player.y, player.z)
	}

	/** Subtle particles ticked during the warmup so players can see it charging. */
	fun warmupEffect(player: ServerPlayer) {
		player.level().sendParticles(
			ParticleTypes.PORTAL, player.x, player.y + 1.0, player.z, 6, 0.4, 0.6, 0.4, 0.02,
		)
	}

	private fun portalBurst(level: ServerLevel, x: Double, y: Double, z: Double) {
		level.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 40, 0.4, 1.0, 0.4, 0.2)
		level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.0f)
	}

	/** A bigger "poof" burst on top of the portal swirl, so the player visibly vanishes. */
	private fun vanishBurst(level: ServerLevel, x: Double, y: Double, z: Double) {
		level.sendParticles(ParticleTypes.POOF, x, y + 1.0, z, 25, 0.5, 0.6, 0.5, 0.05)
		portalBurst(level, x, y, z)
	}
}
