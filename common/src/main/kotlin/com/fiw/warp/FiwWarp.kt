package com.fiw.warp

import com.fiw.warp.config.FiwWarpConfig
import com.fiw.warp.data.HomeStore
import com.fiw.warp.data.WarpStore
import com.fiw.warp.teleport.TeleportManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**
 * Loader-agnostic entry point. Each loader (Fabric / NeoForge) wires its events
 * into this object; all the actual logic lives in :common.
 */
object FiwWarp {
	val logger: org.slf4j.Logger = LoggerFactory.getLogger(Constants.MOD_NAME)

	lateinit var config: FiwWarpConfig
		private set
	lateinit var homes: HomeStore
		private set
	lateinit var warps: WarpStore
		private set

	private lateinit var dataDir: Path
	private var initialized = false

	/** [configRoot] is the platform config directory (e.g. `.../config`). */
	fun init(configRoot: Path) {
		if (initialized) return
		initialized = true

		dataDir = configRoot.resolve(Constants.MOD_ID)
		config = FiwWarpConfig.load(dataDir)
		homes = HomeStore(dataDir).also { it.load() }
		warps = WarpStore(dataDir).also { it.load() }

		logger.info(
			"{} ready (warmup={}s, homeCooldown={}s, warpCooldown={}s, maxHomes={})",
			Constants.MOD_NAME, config.warmupSeconds, config.homeCooldownSeconds, config.warpCooldownSeconds, config.maxHomes,
		)
	}

	fun reload() {
		config = FiwWarpConfig.load(dataDir)
		logger.info("{} config reloaded", Constants.MOD_NAME)
	}

	fun tick(server: MinecraftServer) = TeleportManager.tick(server)

	fun onBlockBreak(player: ServerPlayer) = TeleportManager.onBlockBreak(player)

	fun onAttack(player: ServerPlayer) = TeleportManager.onAttack(player)

	fun save() {
		if (!initialized) return
		homes.save()
		warps.save()
	}
}
