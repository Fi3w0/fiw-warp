package com.fiw.warp.data

import com.fiw.warp.FiwWarp
import com.fiw.warp.util.Json
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Per-player named homes, persisted to `config/fiwwarp/homes.json`. */
class HomeStore(private val dir: Path) {

	private val homes = ConcurrentHashMap<UUID, ConcurrentHashMap<String, WarpLocation>>()
	private val file: Path get() = dir.resolve("homes.json")

	fun load() {
		if (!Files.exists(file)) return
		try {
			Files.newBufferedReader(file).use { reader ->
				val type = object : TypeToken<Map<String, Map<String, WarpLocation>>>() {}.type
				val raw: Map<String, Map<String, WarpLocation>>? = Json.GSON.fromJson(reader, type)
				homes.clear()
				raw?.forEach { (uuid, byName) ->
					runCatching { UUID.fromString(uuid) }.getOrNull()?.let { id ->
						homes[id] = ConcurrentHashMap(byName)
					}
				}
			}
		} catch (e: Exception) {
			FiwWarp.logger.error("Failed to load homes.json", e)
		}
	}

	fun save() {
		try {
			Files.createDirectories(dir)
			val raw = homes.entries.associate { it.key.toString() to it.value }
			Files.newBufferedWriter(file).use { Json.GSON.toJson(raw, it) }
		} catch (e: Exception) {
			FiwWarp.logger.error("Failed to save homes.json", e)
		}
	}

	fun get(player: UUID, name: String): WarpLocation? = homes[player]?.get(name.lowercase())

	fun names(player: UUID): List<String> = homes[player]?.keys?.sorted() ?: emptyList()

	fun count(player: UUID): Int = homes[player]?.size ?: 0

	fun has(player: UUID, name: String): Boolean = homes[player]?.containsKey(name.lowercase()) == true

	fun set(player: UUID, name: String, location: WarpLocation) {
		homes.computeIfAbsent(player) { ConcurrentHashMap() }[name.lowercase()] = location
		save()
	}

	fun remove(player: UUID, name: String): Boolean {
		val removed = homes[player]?.remove(name.lowercase()) != null
		if (removed) save()
		return removed
	}

	/** Renames a home. Returns false if [from] is missing or [to] already exists. */
	fun rename(player: UUID, from: String, to: String): Boolean {
		val map = homes[player] ?: return false
		val fromKey = from.lowercase()
		val toKey = to.lowercase()
		if (!map.containsKey(fromKey) || map.containsKey(toKey)) return false
		map[toKey] = map.remove(fromKey)!!
		save()
		return true
	}
}
