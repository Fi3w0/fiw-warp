package com.fiw.warp.data

import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

/** A serializable teleport destination. Dimension is stored as e.g. "minecraft:overworld". */
data class WarpLocation(
	val dimension: String,
	val x: Double,
	val y: Double,
	val z: Double,
	val yaw: Float,
	val pitch: Float,
) {
	fun level(server: MinecraftServer): ServerLevel? {
		val id = Identifier.tryParse(dimension) ?: return null
		return server.getLevel(ResourceKey.create(Registries.DIMENSION, id))
	}

	companion object {
		fun of(player: ServerPlayer): WarpLocation {
			val level = player.level()
			return WarpLocation(
				dimension = level.dimension().identifier().toString(),
				x = player.x,
				y = player.y,
				z = player.z,
				yaw = player.yRot,
				pitch = player.xRot,
			)
		}
	}
}
