package com.fiw.warp.data

import com.fiw.warp.FiwWarp
import com.fiw.warp.util.Json
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Global, server-wide warps, persisted to `config/fiwwarp/warps.json`. */
class WarpStore(private val dir: Path) {

	private val warps = ConcurrentHashMap<String, WarpEntry>()
	private val file: Path get() = dir.resolve("warps.json")

	fun load() {
		if (!Files.exists(file)) return
		try {
			Files.newBufferedReader(file).use { reader ->
				val type = object : TypeToken<Map<String, JsonObject>>() {}.type
				val raw: Map<String, JsonObject>? = Json.GSON.fromJson(reader, type)
				warps.clear()
				raw?.forEach { (name, obj) ->
					// Pre-1.1 warps.json stored the location fields directly; migrate those in place.
					val entry = if (obj.has("location")) {
						Json.GSON.fromJson(obj, WarpEntry::class.java)
					} else {
						WarpEntry(Json.GSON.fromJson(obj, WarpLocation::class.java))
					}
					warps[name.lowercase()] = entry
				}
			}
		} catch (e: Exception) {
			FiwWarp.logger.error("Failed to load warps.json", e)
		}
	}

	fun save() {
		try {
			Files.createDirectories(dir)
			Files.newBufferedWriter(file).use { Json.GSON.toJson(warps, it) }
		} catch (e: Exception) {
			FiwWarp.logger.error("Failed to save warps.json", e)
		}
	}

	fun get(name: String): WarpLocation? = warps[name.lowercase()]?.location

	fun owner(name: String): UUID? = warps[name.lowercase()]?.owner

	fun names(): List<String> = warps.keys.sorted()

	fun has(name: String): Boolean = warps.containsKey(name.lowercase())

	fun countByOwner(owner: UUID): Int = warps.values.count { it.owner == owner }

	/** Creates or updates a warp. On update, the original owner is kept regardless of [owner]. */
	fun set(name: String, location: WarpLocation, owner: UUID?) {
		val key = name.lowercase()
		val existingOwner = warps[key]?.owner
		warps[key] = WarpEntry(location, existingOwner ?: owner)
		save()
	}

	fun remove(name: String): Boolean {
		val removed = warps.remove(name.lowercase()) != null
		if (removed) save()
		return removed
	}

	/** Renames a warp. Returns false if [from] is missing or [to] already exists. */
	fun rename(from: String, to: String): Boolean {
		val fromKey = from.lowercase()
		val toKey = to.lowercase()
		if (!warps.containsKey(fromKey) || warps.containsKey(toKey)) return false
		warps[toKey] = warps.remove(fromKey)!!
		save()
		return true
	}
}
