package com.fiw.warp.util

import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionSet
import net.minecraft.server.permissions.Permissions

/**
 * 1.21.11 replaced the old integer `hasPermission(level)` checks with a [PermissionSet]
 * of named [Permission]s. We map the familiar op levels (0-4) onto the vanilla command
 * permissions so config stays as simple integers.
 */
object Perms {

	private fun forLevel(level: Int): Permission? = when {
		level <= 0 -> null // everyone
		level == 1 -> Permissions.COMMANDS_MODERATOR
		level == 2 -> Permissions.COMMANDS_GAMEMASTER
		level == 3 -> Permissions.COMMANDS_ADMIN
		else -> Permissions.COMMANDS_OWNER
	}

	/** True if [set] grants at least the given op [level]. */
	fun atLeast(set: PermissionSet, level: Int): Boolean {
		val perm = forLevel(level) ?: return true
		return set.hasPermission(perm)
	}
}
