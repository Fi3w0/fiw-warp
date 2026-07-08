package com.fiw.warp.teleport

import com.fiw.warp.FiwWarp
import com.fiw.warp.config.FiwWarpConfig
import com.fiw.warp.data.WarpLocation
import com.fiw.warp.util.Perms
import com.fiw.warp.util.Teleporter
import com.fiw.warp.util.fmt
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles the warmup ("stand still for N seconds") and cooldown rules.
 * Movement and damage are detected cheaply in [tick]; attacks and block breaks
 * are reported by the loaders via [onAttack] / [onBlockBreak].
 */
object TeleportManager {

	private class Pending(
		val target: WarpLocation,
		val label: String,
		val kind: TeleportKind,
		var ticksLeft: Int,
		val startX: Double,
		val startY: Double,
		val startZ: Double,
		val startHealth: Float,
	)

	private val pending = ConcurrentHashMap<UUID, Pending>()

	/** Starts a teleport, applying cooldown/warmup. Sends feedback to the player. */
	fun request(player: ServerPlayer, target: WarpLocation, label: String, kind: TeleportKind) {
		val cfg = FiwWarp.config
		val msg = cfg.messages
		val uuid = player.uuid
		val bypass = cfg.allowBypass && Perms.atLeast(player.permissions(), cfg.bypassPermissionLevel)

		if (!bypass) {
			val wait = CooldownManager.remainingSeconds(uuid, kind)
			if (wait > 0) {
				player.sendSystemMessage(Component.literal(msg.cooldownWait.fmt("seconds" to wait)))
				return
			}
		}

		if (pending.containsKey(uuid)) {
			player.sendSystemMessage(Component.literal(msg.alreadyPending))
			return
		}

		val warmup = if (bypass) 0 else cfg.warmupSeconds.coerceAtLeast(0)
		if (warmup == 0) {
			complete(player, target, label, kind, bypass, instant = true)
			return
		}

		pending[uuid] = Pending(
			target = target,
			label = label,
			kind = kind,
			ticksLeft = warmup * 20,
			startX = player.x, startY = player.y, startZ = player.z,
			startHealth = player.health,
		)
		player.sendSystemMessage(Component.literal(msg.warmupStarted.fmt("label" to label, "seconds" to warmup)))
	}

	fun onAttack(player: ServerPlayer) {
		if (FiwWarp.config.cancelOnAttack) cancel(player, FiwWarp.config.messages.cancelledAttack)
	}

	fun onBlockBreak(player: ServerPlayer) {
		if (FiwWarp.config.cancelOnBlockBreak) cancel(player, FiwWarp.config.messages.cancelledBlockBreak)
	}

	private fun cancel(player: ServerPlayer, message: String) {
		if (pending.remove(player.uuid) != null) {
			player.sendSystemMessage(Component.literal(message))
		}
	}

	fun tick(server: MinecraftServer) {
		if (pending.isEmpty()) return
		val cfg = FiwWarp.config
		val iterator = pending.entries.iterator()
		while (iterator.hasNext()) {
			val entry = iterator.next()
			val player = server.playerList.getPlayer(entry.key)
			if (player == null) {
				iterator.remove()
				continue
			}
			val p = entry.value

			if (cfg.cancelOnMove && hasMoved(player, p)) {
				iterator.remove()
				player.sendSystemMessage(Component.literal(cfg.messages.cancelledMoved))
				continue
			}
			if (cfg.cancelOnDamage && player.health < p.startHealth) {
				iterator.remove()
				player.sendSystemMessage(Component.literal(cfg.messages.cancelledDamage))
				continue
			}

			p.ticksLeft--
			if (p.ticksLeft <= 0) {
				iterator.remove()
				val bypass = cfg.allowBypass && Perms.atLeast(player.permissions(), cfg.bypassPermissionLevel)
				complete(player, p.target, p.label, p.kind, bypass, instant = false)
			} else if (cfg.effectsEnabled && p.ticksLeft % 10 == 0) {
				Teleporter.warmupEffect(player)
			}
		}
	}

	private fun hasMoved(player: ServerPlayer, p: Pending): Boolean {
		val dx = player.x - p.startX
		val dy = player.y - p.startY
		val dz = player.z - p.startZ
		return dx * dx + dy * dy + dz * dz > MOVE_THRESHOLD_SQ
	}

	private fun complete(
		player: ServerPlayer,
		target: WarpLocation,
		label: String,
		kind: TeleportKind,
		bypass: Boolean,
		instant: Boolean,
	) {
		val cfg = FiwWarp.config
		val msg = cfg.messages
		val level = target.level(player.level().server)
		if (level == null) {
			player.sendSystemMessage(Component.literal(msg.dimensionUnavailable))
			return
		}
		if (!cfg.crossDimension && !bypass && level != player.level()) {
			player.sendSystemMessage(Component.literal(msg.crossDimensionDisabled))
			return
		}

		BackManager.record(player)
		Teleporter.teleport(player, level, target)
		player.sendSystemMessage(Component.literal(msg.teleported.fmt("label" to label)))

		if (instant && cfg.instantTeleportBlindnessEnabled) {
			val ticks = cfg.instantTeleportBlindnessSeconds.coerceAtLeast(0) * 20
			if (ticks > 0) player.addEffect(MobEffectInstance(MobEffects.BLINDNESS, ticks, 0, false, false))
		}

		if (!bypass) CooldownManager.apply(player.uuid, kind, cooldownSecondsFor(kind, cfg))
	}

	private fun cooldownSecondsFor(kind: TeleportKind, cfg: FiwWarpConfig): Int = when (kind) {
		TeleportKind.HOME -> cfg.homeCooldownSeconds
		TeleportKind.WARP -> cfg.warpCooldownSeconds
		TeleportKind.BACK -> cfg.backCooldownSeconds
	}

	private const val MOVE_THRESHOLD_SQ = 0.05 * 0.05
}
