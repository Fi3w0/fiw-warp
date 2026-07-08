package com.fiw.warp.fabric

import com.fiw.warp.FiwWarp
import com.fiw.warp.command.FiwWarpCommands
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult

object FiwWarpFabric : ModInitializer {
	override fun onInitialize() {
		FiwWarp.init(FabricLoader.getInstance().configDir)

		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			FiwWarpCommands.register(dispatcher)
		}

		ServerTickEvents.END_SERVER_TICK.register { server -> FiwWarp.tick(server) }
		ServerLifecycleEvents.SERVER_STOPPING.register { FiwWarp.save() }

		PlayerBlockBreakEvents.AFTER.register { _, player, _, _, _ ->
			(player as? ServerPlayer)?.let(FiwWarp::onBlockBreak)
		}

		AttackEntityCallback.EVENT.register { player, _, _, _, _ ->
			(player as? ServerPlayer)?.let(FiwWarp::onAttack)
			InteractionResult.PASS
		}
	}
}
