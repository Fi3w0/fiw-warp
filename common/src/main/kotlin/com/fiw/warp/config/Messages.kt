package com.fiw.warp.config

/**
 * Every player-facing string the mod sends, as a template. Supports `{placeholder}` tokens
 * (see [com.fiw.warp.util.fmt]); admins can reword any of them in `config.json` without
 * touching code. Lives at `config/fiwwarp/config.json` under the `messages` object.
 */
class Messages {

	// ---- Teleport (warmup, cooldown, completion) -----------------------------

	var cooldownWait: String = "§cYou must wait §f{seconds}s§c before teleporting again."
	var alreadyPending: String = "§cYou already have a teleport in progress."
	var warmupStarted: String = "§eTeleporting to §f{label}§e in §f{seconds}s§e — don't move."
	var cancelledMoved: String = "§cTeleport cancelled: you moved."
	var cancelledDamage: String = "§cTeleport cancelled: you took damage."
	var cancelledAttack: String = "§cTeleport cancelled: you attacked."
	var cancelledBlockBreak: String = "§cTeleport cancelled: you broke a block."
	var dimensionUnavailable: String = "§cThat destination's dimension isn't available."
	var crossDimensionDisabled: String = "§cCross-dimension teleporting is disabled."
	var teleported: String = "§aTeleported to §f{label}§a."

	// ---- Homes ----------------------------------------------------------------

	var homeNoneSetSpecify: String = "§cYou have no homes. Use §f/sethome§c."
	var homeSpecifyOneOf: String = "§cSpecify a home: §f{names}"
	var homeNotFound: String = "§cNo home named §f{name}§c."
	var homeLimitReached: String = "§cYou reached the home limit (§f{max}§c)."
	var homeSet: String = "§aHome §f{name}§a set."
	var homeDeleted: String = "§aHome §f{name}§a deleted."
	var homeRenamed: String = "§aRenamed home §f{from}§a to §f{to}§a."
	var homeRenameConflict: String = "§cYou already have a home named §f{to}§c."
	var homesList: String = "§eHomes (§f{count}§e/§f{max}§e): §f{names}"
	var homesListEmpty: String = "§7You have no homes."

	// ---- Warps ------------------------------------------------------------------

	var warpNotFound: String = "§cNo warp named §f{name}§c."
	var warpsListEmpty: String = "§7There are no warps yet."
	var warpsList: String = "§eWarps: §f{names}"
	var warpCreated: String = "§aWarp §f{name}§a created."
	var warpUpdated: String = "§aWarp §f{name}§a updated."
	var warpDeleted: String = "§aWarp §f{name}§a deleted."
	var warpRenamed: String = "§aRenamed warp §f{from}§a to §f{to}§a."
	var warpRenameConflict: String = "§cA warp named §f{to}§c already exists."
	var warpLimitReached: String = "§cYou reached the warp limit (§f{max}§c)."
	var warpNotOwner: String = "§cYou can only manage warps you created."
	var warpReservedName: String = "§cThat name is reserved and can't be used for a warp."

	/** Sent before the accept/deny buttons; `{name}` and `{seconds}` are available. */
	var warpConfirmPrompt: String = "§eTeleporting to warp §f{name}§e. Confirm within §f{seconds}s§e:"
	var warpConfirmAcceptButton: String = "§a[Accept]"
	var warpConfirmDenyButton: String = "§c[Deny]"
	var warpConfirmNone: String = "§cYou have no pending warp request."
	var warpConfirmExpired: String = "§cYour warp request to §f{name}§c expired. Use §f/warp {name}§c again."
	var warpConfirmDenied: String = "§7Warp request to §f{name}§7 cancelled."
	var warpConfirmSuperseded: String = "§7Your pending request for warp §f{name}§7 was replaced."
	var warpConfirmGone: String = "§cThat warp no longer exists."

	// ---- Back -------------------------------------------------------------------

	var backNone: String = "§cNo previous location to return to."

	// ---- Admin ------------------------------------------------------------------

	var configReloaded: String = "§aFiw Warp config reloaded."
}
