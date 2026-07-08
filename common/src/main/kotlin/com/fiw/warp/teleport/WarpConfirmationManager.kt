package com.fiw.warp.teleport

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Tracks pending `/warp accept` / `/warp deny` confirmations. In-memory only. */
object WarpConfirmationManager {

	class Pending(val warpName: String, private val expiresAtMillis: Long) {
		fun isExpired(): Boolean = System.currentTimeMillis() > expiresAtMillis
	}

	private val pending = ConcurrentHashMap<UUID, Pending>()

	fun request(player: UUID, warpName: String, seconds: Int) {
		pending[player] = Pending(warpName, System.currentTimeMillis() + seconds * 1000L)
	}

	/** Looks at the pending request without consuming it. */
	fun peek(player: UUID): Pending? = pending[player]

	/** Removes and returns the pending request, if any. */
	fun consume(player: UUID): Pending? = pending.remove(player)
}
