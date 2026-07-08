# Changelog

All notable changes to Fiw Warp are documented here. This file is tracked in version control —
the release pipeline extracts the matching version section as GitHub release notes on tag push.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the
project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2026-07-08

### Added
- `enable_homes` and `enable_warps` config switches to turn the home and warp systems on or off
  independently (the `back` system already had `enable_back`). Disabled systems hide all of
  their commands and respond to `/fiwwarp reload`.
- Player-created warps: `warps_players_can_create` lets non-admins use `/setwarp` up to
  `max_warps_per_player` each (admins stay unlimited). Each warp remembers its creator —
  non-admins can only `/delwarp`/`/editwarp` their own; admins can manage any warp.
- Warp confirmation: `/warp <name>` now prompts a clickable **[Accept]**/**[Deny]** message
  before the warmup starts (like a teleport request), controlled by `warp_confirmation_enabled`
  and `warp_confirmation_seconds`. New `/warp accept` and `/warp deny` commands. Set
  `warp_confirmation_enabled` to `false` for the old instant behavior.
- Separate cooldowns per command: `home_cooldown_seconds`, `warp_cooldown_seconds`, and
  `back_cooldown_seconds` replace the single `cooldown_seconds`, each ticking independently.
- Visual polish for teleports: `vanish_particles_enabled` adds an extra "poof" burst at the
  spot a player teleports away from, and `instant_teleport_blindness_enabled` gives a short,
  purely cosmetic Blindness effect (`instant_teleport_blindness_seconds`, default 2s) on any
  instant teleport (no warmup), so instant hops feel less jarring.
- Every player-facing message is now customizable via a `messages` object in `config.json` —
  reword any text the mod sends, with `{placeholder}` tokens for dynamic values.

### Changed
- `cooldown_seconds` was replaced by `home_cooldown_seconds` / `warp_cooldown_seconds` /
  `back_cooldown_seconds`. If you had a custom cooldown set, re-apply it under the matching key.
- Built jar files are now named `fiw-warp-fabric-*.jar` / `fiw-warp-neoforge-*.jar`
  (the loader mod id stays `fiwwarp`).
- License changed to MIT.

### Notes
- `warps.json` from 1.0.0 loads and migrates automatically — existing warps keep working with
  no owner (admin-managed), same as before.
- Don't name a warp `accept` or `deny`; those are reserved `/warp` subcommands. `/setwarp` and
  `/editwarp` now reject those names, and the server logs a warning on startup if a warp with
  a reserved name already exists from before the upgrade.

## [1.0.0] - 2026-06-08

### Added
- Multiloader (Fabric + NeoForge) support for Minecraft 1.21.11, written in Kotlin.
- Per-player named homes with a configurable limit: `/home`, `/sethome`, `/delhome`,
  `/edithome`, `/homes`.
- Global, admin-managed warps: `/warp`, `/warps`, `/setwarp`, `/delwarp`, `/editwarp`.
- `/back` to return to the last teleport origin.
- `/fiwwarp reload` to reload the config at runtime.
- Configurable warmup ("stand still") teleport, default 5 seconds, cancelled by moving,
  taking damage, attacking, or breaking a block (each individually toggleable).
- Optional post-teleport cooldown, default disabled.
- Permission-level bypass so admins can teleport instantly and ignore cooldown.
- Teleport particles and sound, toggleable via `effects_enabled`.
- Main config plus `homes.json` / `warps.json` stored in `config/fiwwarp/` on the server.
