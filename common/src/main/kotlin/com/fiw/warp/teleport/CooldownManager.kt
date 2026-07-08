package com.fiw.warp.teleport

import java.util.EnumMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Tracks the post-teleport cooldown per player, separately for each [TeleportKind]. In-memory only. */
object CooldownManager {

	private val until = ConcurrentHashMap<UUID, EnumMap<TeleportKind, Long>>()

	fun remainingSeconds(player: UUID, kind: TeleportKind): Long {
		val end = until[player]?.get(kind) ?: return 0
		val remaining = end - System.currentTimeMillis()
		return if (remaining <= 0) 0 else (remaining + 999) / 1000
	}

	fun apply(player: UUID, kind: TeleportKind, seconds: Int) {
		if (seconds <= 0) {
			until[player]?.remove(kind)
		} else {
			until.computeIfAbsent(player) { EnumMap(TeleportKind::class.java) }[kind] =
				System.currentTimeMillis() + seconds * 1000L
		}
	}
}
