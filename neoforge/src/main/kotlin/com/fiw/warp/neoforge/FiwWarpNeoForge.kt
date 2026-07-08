package com.fiw.warp.neoforge

import com.fiw.warp.Constants
import com.fiw.warp.FiwWarp
import com.fiw.warp.command.FiwWarpCommands
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

@Mod(Constants.MOD_ID)
object FiwWarpNeoForge {
	init {
		FiwWarp.init(FMLPaths.CONFIGDIR.get())
		NeoForge.EVENT_BUS.register(this)
	}

	@SubscribeEvent
	fun onRegisterCommands(event: RegisterCommandsEvent) {
		FiwWarpCommands.register(event.dispatcher)
	}

	@SubscribeEvent
	fun onServerTick(event: ServerTickEvent.Post) {
		FiwWarp.tick(event.server)
	}

	@SubscribeEvent
	fun onBlockBreak(event: BlockEvent.BreakEvent) {
		(event.player as? ServerPlayer)?.let(FiwWarp::onBlockBreak)
	}

	@SubscribeEvent
	fun onAttack(event: AttackEntityEvent) {
		(event.entity as? ServerPlayer)?.let(FiwWarp::onAttack)
	}

	@SubscribeEvent
	fun onServerStopping(event: ServerStoppingEvent) {
		FiwWarp.save()
	}
}
