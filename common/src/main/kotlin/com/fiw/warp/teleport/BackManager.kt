package com.fiw.warp.teleport

import com.fiw.warp.data.WarpLocation
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Remembers the spot a player teleported away from, for /back. In-memory only. */
object BackManager {

	private val previous = ConcurrentHashMap<UUID, WarpLocation>()

	fun record(player: ServerPlayer) {
		previous[player.uuid] = WarpLocation.of(player)
	}

	fun get(player: UUID): WarpLocation? = previous[player]
}
