package com.fiw.warp.command

import com.fiw.warp.Constants
import com.fiw.warp.FiwWarp
import com.fiw.warp.data.WarpLocation
import com.fiw.warp.teleport.BackManager
import com.fiw.warp.teleport.TeleportKind
import com.fiw.warp.teleport.TeleportManager
import com.fiw.warp.teleport.WarpConfirmationManager
import com.fiw.warp.util.Perms
import com.fiw.warp.util.fmt
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * Registers every command. Brigadier + the command source are vanilla, so this is
 * shared across both loaders untouched.
 */
object FiwWarpCommands {

	private val WARP_NAMES = SuggestionProvider<CommandSourceStack> { _, builder ->
		FiwWarp.warps.names().forEach(builder::suggest)
		builder.buildFuture()
	}

	private val HOME_NAMES = SuggestionProvider<CommandSourceStack> { ctx, builder ->
		ctx.source.player?.let { FiwWarp.homes.names(it.uuid).forEach(builder::suggest) }
		builder.buildFuture()
	}

	fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		registerHomes(dispatcher)
		registerWarps(dispatcher)
		registerBack(dispatcher)
		registerAdmin(dispatcher)
	}

	// ---- Homes (available to everyone, scoped to the player) ----------------

	private fun registerHomes(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			Commands.literal("home")
				.requireHomes()
				.executes { runHome(it, null) }
				.then(
					Commands.argument("name", StringArgumentType.word())
						.suggests(HOME_NAMES)
						.executes { runHome(it, StringArgumentType.getString(it, "name")) },
				),
		)

		dispatcher.register(
			Commands.literal("sethome")
				.requireHomes()
				.executes { setHome(it, "home") }
				.then(
					Commands.argument("name", StringArgumentType.word())
						.executes { setHome(it, StringArgumentType.getString(it, "name")) },
				),
		)

		dispatcher.register(
			Commands.literal("delhome")
				.requireHomes()
				.executes { delHome(it, "home") }
				.then(
					Commands.argument("name", StringArgumentType.word())
						.suggests(HOME_NAMES)
						.executes { delHome(it, StringArgumentType.getString(it, "name")) },
				),
		)

		dispatcher.register(
			Commands.literal("edithome")
				.requireHomes()
				.then(
					Commands.argument("name", StringArgumentType.word())
						.suggests(HOME_NAMES)
						.then(
							Commands.argument("newname", StringArgumentType.word())
								.executes {
									editHome(
										it,
										StringArgumentType.getString(it, "name"),
										StringArgumentType.getString(it, "newname"),
									)
								},
						),
				),
		)

		dispatcher.register(Commands.literal("homes").requireHomes().executes(::listHomes))
	}

	private fun runHome(ctx: CommandContext<CommandSourceStack>, name: String?): Int {
		val player = requirePlayer(ctx) ?: return 0
		val msg = FiwWarp.config.messages
		val homes = FiwWarp.homes
		val resolved: String? = when {
			name != null -> name
			homes.has(player.uuid, "home") -> "home"
			homes.count(player.uuid) == 1 -> homes.names(player.uuid).first()
			else -> null
		}
		if (resolved == null) {
			val names = homes.names(player.uuid)
			if (names.isEmpty()) player.sendSystemMessage(Component.literal(msg.homeNoneSetSpecify))
			else player.sendSystemMessage(Component.literal(msg.homeSpecifyOneOf.fmt("names" to names.joinToString(", "))))
			return 0
		}
		val location = homes.get(player.uuid, resolved)
		if (location == null) {
			player.sendSystemMessage(Component.literal(msg.homeNotFound.fmt("name" to resolved)))
			return 0
		}
		TeleportManager.request(player, location, "home '$resolved'", TeleportKind.HOME)
		return 1
	}

	private fun setHome(ctx: CommandContext<CommandSourceStack>, name: String): Int {
		val player = requirePlayer(ctx) ?: return 0
		val cfg = FiwWarp.config
		val homes = FiwWarp.homes
		if (!homes.has(player.uuid, name) && homes.count(player.uuid) >= cfg.maxHomes) {
			player.sendSystemMessage(Component.literal(cfg.messages.homeLimitReached.fmt("max" to cfg.maxHomes)))
			return 0
		}
		homes.set(player.uuid, name, WarpLocation.of(player))
		player.sendSystemMessage(Component.literal(cfg.messages.homeSet.fmt("name" to name.lowercase())))
		return 1
	}

	private fun delHome(ctx: CommandContext<CommandSourceStack>, name: String): Int {
		val player = requirePlayer(ctx) ?: return 0
		val msg = FiwWarp.config.messages
		return if (FiwWarp.homes.remove(player.uuid, name)) {
			player.sendSystemMessage(Component.literal(msg.homeDeleted.fmt("name" to name.lowercase())))
			1
		} else {
			player.sendSystemMessage(Component.literal(msg.homeNotFound.fmt("name" to name)))
			0
		}
	}

	private fun editHome(ctx: CommandContext<CommandSourceStack>, from: String, to: String): Int {
		val player = requirePlayer(ctx) ?: return 0
		val msg = FiwWarp.config.messages
		val homes = FiwWarp.homes
		if (!homes.has(player.uuid, from)) {
			player.sendSystemMessage(Component.literal(msg.homeNotFound.fmt("name" to from)))
			return 0
		}
		if (homes.has(player.uuid, to)) {
			player.sendSystemMessage(Component.literal(msg.homeRenameConflict.fmt("to" to to.lowercase())))
			return 0
		}
		homes.rename(player.uuid, from, to)
		player.sendSystemMessage(Component.literal(msg.homeRenamed.fmt("from" to from.lowercase(), "to" to to.lowercase())))
		return 1
	}

	private fun listHomes(ctx: CommandContext<CommandSourceStack>): Int {
		val player = requirePlayer(ctx) ?: return 0
		val cfg = FiwWarp.config
		val names = FiwWarp.homes.names(player.uuid)
		if (names.isEmpty()) player.sendSystemMessage(Component.literal(cfg.messages.homesListEmpty))
		else player.sendSystemMessage(
			Component.literal(
				cfg.messages.homesList.fmt(
					"count" to names.size,
					"max" to cfg.maxHomes,
					"names" to names.joinToString(", "),
				),
			),
		)
		return 1
	}

	// ---- Warps (use is public; creating/managing depends on config) ---------

	private fun registerWarps(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			Commands.literal("warp")
				.requireWarps()
				.executes(::listWarps)
				.then(Commands.literal("accept").executes(::acceptWarp))
				.then(Commands.literal("deny").executes(::denyWarp))
				.then(
					Commands.argument("name", StringArgumentType.word())
						.suggests(WARP_NAMES)
						.executes { runWarp(it, StringArgumentType.getString(it, "name")) },
				),
		)

		dispatcher.register(Commands.literal("warps").requireWarps().executes(::listWarps))
	}

	private fun runWarp(ctx: CommandContext<CommandSourceStack>, name: String): Int {
		val player = requirePlayer(ctx) ?: return 0
		val cfg = FiwWarp.config
		val location = FiwWarp.warps.get(name)
		if (location == null) {
			player.sendSystemMessage(Component.literal(cfg.messages.warpNotFound.fmt("name" to name)))
			return 0
		}

		if (cfg.warpConfirmationEnabled) {
			val key = name.lowercase()
			val previous = WarpConfirmationManager.peek(player.uuid)
			if (previous != null && !previous.isExpired()) {
				player.sendSystemMessage(Component.literal(cfg.messages.warpConfirmSuperseded.fmt("name" to previous.warpName)))
			}
			WarpConfirmationManager.request(player.uuid, key, cfg.warpConfirmationSeconds)

			val accept = Component.literal(cfg.messages.warpConfirmAcceptButton)
				.withStyle { it.withClickEvent(ClickEvent.RunCommand("/warp accept")) }
			val deny = Component.literal(cfg.messages.warpConfirmDenyButton)
				.withStyle { it.withClickEvent(ClickEvent.RunCommand("/warp deny")) }

			player.sendSystemMessage(
				Component.literal(cfg.messages.warpConfirmPrompt.fmt("name" to key, "seconds" to cfg.warpConfirmationSeconds))
					.append(" ").append(accept).append(" ").append(deny),
			)
		} else {
			TeleportManager.request(player, location, "warp '${name.lowercase()}'", TeleportKind.WARP)
		}
		return 1
	}

	private fun acceptWarp(ctx: CommandContext<CommandSourceStack>): Int {
		val player = requirePlayer(ctx) ?: return 0
		val msg = FiwWarp.config.messages
		val pending = WarpConfirmationManager.consume(player.uuid)
		if (pending == null) {
			player.sendSystemMessage(Component.literal(msg.warpConfirmNone))
			return 0
		}
		if (pending.isExpired()) {
			player.sendSystemMessage(Component.literal(msg.warpConfirmExpired.fmt("name" to pending.warpName)))
			return 0
		}
		val location = FiwWarp.warps.get(pending.warpName)
		if (location == null) {
			player.sendSystemMessage(Component.literal(msg.warpConfirmGone))
			return 0
		}
		TeleportManager.request(player, location, "warp '${pending.warpName}'", TeleportKind.WARP)
		return 1
	}

	private fun denyWarp(ctx: CommandContext<CommandSourceStack>): Int {
		val player = requirePlayer(ctx) ?: return 0
		val msg = FiwWarp.config.messages
		val pending = WarpConfirmationManager.consume(player.uuid)
		if (pending == null) {
			player.sendSystemMessage(Component.literal(msg.warpConfirmNone))
			return 0
		}
		if (pending.isExpired()) {
			player.sendSystemMessage(Component.literal(msg.warpConfirmExpired.fmt("name" to pending.warpName)))
			return 0
		}
		player.sendSystemMessage(Component.literal(msg.warpConfirmDenied.fmt("name" to pending.warpName)))
		return 1
	}

	private fun listWarps(ctx: CommandContext<CommandSourceStack>): Int {
		val msg = FiwWarp.config.messages
		val names = FiwWarp.warps.names()
		val message =
			if (names.isEmpty()) Component.literal(msg.warpsListEmpty)
			else Component.literal(msg.warpsList.fmt("names" to names.joinToString(", ")))
		ctx.source.sendSystemMessage(message)
		return 1
	}

	// ---- /back --------------------------------------------------------------

	private fun registerBack(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			Commands.literal("back").requires { FiwWarp.config.enableBack }.executes { ctx ->
				val player = requirePlayer(ctx) ?: return@executes 0
				val location = BackManager.get(player.uuid)
				if (location == null) {
					player.sendSystemMessage(Component.literal(FiwWarp.config.messages.backNone))
					return@executes 0
				}
				TeleportManager.request(player, location, "previous location", TeleportKind.BACK)
				1
			},
		)
	}

	// ---- Admin: warp management + reload ------------------------------------

	private fun registerAdmin(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			Commands.literal("setwarp").requireWarpManage()
				.then(
					Commands.argument("name", StringArgumentType.word())
						.executes { setWarp(it, StringArgumentType.getString(it, "name")) },
				),
		)

		dispatcher.register(
			Commands.literal("delwarp").requireWarpManage()
				.then(
					Commands.argument("name", StringArgumentType.word())
						.suggests(WARP_NAMES)
						.executes { delWarp(it, StringArgumentType.getString(it, "name")) },
				),
		)

		dispatcher.register(
			Commands.literal("editwarp").requireWarpManage()
				.then(
					Commands.argument("name", StringArgumentType.word())
						.suggests(WARP_NAMES)
						.then(
							Commands.argument("newname", StringArgumentType.word())
								.executes {
									editWarp(
										it,
										StringArgumentType.getString(it, "name"),
										StringArgumentType.getString(it, "newname"),
									)
								},
						),
				),
		)

		dispatcher.register(
			Commands.literal("fiwwarp").requireAdmin()
				.then(
					Commands.literal("reload").executes { ctx ->
						FiwWarp.reload()
						ctx.source.sendSystemMessage(Component.literal(FiwWarp.config.messages.configReloaded))
						1
					},
				),
		)
	}

	private fun setWarp(ctx: CommandContext<CommandSourceStack>, name: String): Int {
		val player = requirePlayer(ctx) ?: return 0
		val cfg = FiwWarp.config
		val warps = FiwWarp.warps
		val admin = isAdmin(ctx.source)
		val existed = warps.has(name)

		if (!existed && name.lowercase() in Constants.RESERVED_WARP_NAMES) {
			player.sendSystemMessage(Component.literal(cfg.messages.warpReservedName))
			return 0
		}

		if (existed) {
			val owner = warps.owner(name)
			if (!admin && owner != player.uuid) {
				player.sendSystemMessage(Component.literal(cfg.messages.warpNotOwner))
				return 0
			}
		} else if (!admin && warps.countByOwner(player.uuid) >= cfg.maxWarpsPerPlayer) {
			player.sendSystemMessage(Component.literal(cfg.messages.warpLimitReached.fmt("max" to cfg.maxWarpsPerPlayer)))
			return 0
		}

		warps.set(name, WarpLocation.of(player), player.uuid)
		val text = if (existed) cfg.messages.warpUpdated else cfg.messages.warpCreated
		player.sendSystemMessage(Component.literal(text.fmt("name" to name.lowercase())))
		return 1
	}

	private fun delWarp(ctx: CommandContext<CommandSourceStack>, name: String): Int {
		val msg = FiwWarp.config.messages
		val warps = FiwWarp.warps
		if (!warps.has(name)) {
			ctx.source.sendFailure(Component.literal(msg.warpNotFound.fmt("name" to name)))
			return 0
		}
		if (!isAdmin(ctx.source) && warps.owner(name) != ctx.source.player?.uuid) {
			ctx.source.sendFailure(Component.literal(msg.warpNotOwner))
			return 0
		}
		warps.remove(name)
		ctx.source.sendSystemMessage(Component.literal(msg.warpDeleted.fmt("name" to name.lowercase())))
		return 1
	}

	private fun editWarp(ctx: CommandContext<CommandSourceStack>, from: String, to: String): Int {
		val msg = FiwWarp.config.messages
		val warps = FiwWarp.warps
		if (!warps.has(from)) {
			ctx.source.sendFailure(Component.literal(msg.warpNotFound.fmt("name" to from)))
			return 0
		}
		if (!isAdmin(ctx.source) && warps.owner(from) != ctx.source.player?.uuid) {
			ctx.source.sendFailure(Component.literal(msg.warpNotOwner))
			return 0
		}
		if (to.lowercase() in Constants.RESERVED_WARP_NAMES) {
			ctx.source.sendFailure(Component.literal(msg.warpReservedName))
			return 0
		}
		if (warps.has(to)) {
			ctx.source.sendFailure(Component.literal(msg.warpRenameConflict.fmt("to" to to.lowercase())))
			return 0
		}
		warps.rename(from, to)
		ctx.source.sendSystemMessage(Component.literal(msg.warpRenamed.fmt("from" to from.lowercase(), "to" to to.lowercase())))
		return 1
	}

	// ---- helpers ------------------------------------------------------------

	private fun isAdmin(source: CommandSourceStack): Boolean =
		Perms.atLeast(source.permissions(), FiwWarp.config.adminPermissionLevel)

	private fun LiteralArgumentBuilder<CommandSourceStack>.requireHomes(): LiteralArgumentBuilder<CommandSourceStack> =
		requires { FiwWarp.config.enableHomes }

	private fun LiteralArgumentBuilder<CommandSourceStack>.requireWarps(): LiteralArgumentBuilder<CommandSourceStack> =
		requires { FiwWarp.config.enableWarps }

	private fun LiteralArgumentBuilder<CommandSourceStack>.requireAdmin(): LiteralArgumentBuilder<CommandSourceStack> =
		requires { isAdmin(it) }

	/** Admins can always manage warps; players only when [FiwWarpConfig.warpsPlayersCanCreate] is on. */
	private fun LiteralArgumentBuilder<CommandSourceStack>.requireWarpManage(): LiteralArgumentBuilder<CommandSourceStack> =
		requires { FiwWarp.config.enableWarps && (isAdmin(it) || FiwWarp.config.warpsPlayersCanCreate) }

	private fun requirePlayer(ctx: CommandContext<CommandSourceStack>): ServerPlayer? {
		val player = ctx.source.player
		if (player == null) ctx.source.sendFailure(Component.literal("This command can only be used by a player."))
		return player
	}
}
