package com.fiw.warp.config

import com.fiw.warp.FiwWarp
import com.fiw.warp.util.Json
import java.nio.file.Files
import java.nio.file.Path

/**
 * The main config. Lives at `config/fiwwarp/config.json` on the server and can be
 * edited by admins. Missing fields are filled with defaults and re-written on load.
 */
class FiwWarpConfig {
	/** Master switch for the home system (/home, /sethome, /delhome, /edithome, /homes). */
	var enableHomes: Boolean = true

	/** Master switch for the warp system (/warp, /warps, /setwarp, /delwarp, /editwarp). */
	var enableWarps: Boolean = true

	/** Enable the /back command (return to where you teleported from). */
	var enableBack: Boolean = true

	/** Seconds the player must stand still before the teleport fires. 0 = instant. */
	var warmupSeconds: Int = 5

	/** Seconds a player must wait after using /home before using it again. 0 = none. */
	var homeCooldownSeconds: Int = 0

	/** Seconds a player must wait after using /warp before using it again. 0 = none. */
	var warpCooldownSeconds: Int = 0

	/** Seconds a player must wait after using /back before using it again. 0 = none. */
	var backCooldownSeconds: Int = 0

	/** Cancel the pending teleport if the player moves. */
	var cancelOnMove: Boolean = true

	/** Cancel the pending teleport if the player takes damage (gets punched). */
	var cancelOnDamage: Boolean = true

	/** Cancel the pending teleport if the player attacks something (punches). */
	var cancelOnAttack: Boolean = true

	/** Cancel the pending teleport if the player breaks a block. */
	var cancelOnBlockBreak: Boolean = true

	/** Maximum number of named homes a player may have. */
	var maxHomes: Int = 3

	/** Whether high-permission players skip warmup and cooldown. */
	var allowBypass: Boolean = true

	/** Permission level required to bypass warmup/cooldown. */
	var bypassPermissionLevel: Int = 2

	/** Permission level required for /setwarp, /delwarp, /editwarp and /fiwwarp reload. */
	var adminPermissionLevel: Int = 2

	/** Allow teleporting between dimensions. */
	var crossDimension: Boolean = true

	/** Play teleport particles and sound. Turn off if you don't like the effects. */
	var effectsEnabled: Boolean = true

	/** Extra "poof" burst at the origin point (on top of the portal swirl) so it looks like the player vanished. */
	var vanishParticlesEnabled: Boolean = true

	/** Give a short Blindness effect on instant teleports (no warmup), purely cosmetic. */
	var instantTeleportBlindnessEnabled: Boolean = true

	/** Duration of the instant-teleport Blindness effect, in seconds. */
	var instantTeleportBlindnessSeconds: Int = 2

	/** Whether non-admin players may create their own warps with /setwarp. */
	var warpsPlayersCanCreate: Boolean = false

	/** Max warps a non-admin player may own when [warpsPlayersCanCreate] is enabled. Admins are unlimited. */
	var maxWarpsPerPlayer: Int = 1

	/** Require /warp accept before a warp teleport starts, like a teleport request. */
	var warpConfirmationEnabled: Boolean = true

	/** Seconds a player has to /warp accept or /warp deny before the request expires. */
	var warpConfirmationSeconds: Int = 15

	/** Every player-facing message, customizable per-string. */
	var messages: Messages = Messages()

	fun save(dir: Path) {
		try {
			Files.createDirectories(dir)
			Files.newBufferedWriter(dir.resolve(FILE)).use { Json.GSON.toJson(this, it) }
		} catch (e: Exception) {
			FiwWarp.logger.error("Failed to write config.json", e)
		}
	}

	companion object {
		private const val FILE = "config.json"

		fun load(dir: Path): FiwWarpConfig {
			val file = dir.resolve(FILE)
			val config = try {
				if (Files.exists(file)) {
					Files.newBufferedReader(file).use {
						Json.GSON.fromJson(it, FiwWarpConfig::class.java)
					} ?: FiwWarpConfig()
				} else {
					FiwWarpConfig()
				}
			} catch (e: Exception) {
				FiwWarp.logger.error("Failed to read config.json, using defaults", e)
				FiwWarpConfig()
			}
			// Re-save so new/missing fields get written back with their defaults.
			config.save(dir)
			return config
		}
	}
}
