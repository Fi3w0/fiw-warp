package com.fiw.warp

object Constants {
	const val MOD_ID = "fiwwarp"
	const val MOD_NAME = "Fiw Warp"

	/** Warp names that can't be used — they collide with reserved `/warp` subcommands. */
	val RESERVED_WARP_NAMES = setOf("accept", "deny")
}
