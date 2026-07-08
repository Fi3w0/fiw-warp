# Fiw Warp

A lightweight, **server-side** teleport mod for Minecraft **1.21.11**, written in Kotlin and
built for **both Fabric and NeoForge** from a single codebase.

Set homes, define server warps, and teleport around — with a configurable warmup ("stand
still") and an optional cooldown so teleporting isn't instant or spammy. The mod registers no
networking and ships no client code, so vanilla-ish clients can connect to a server running it
without installing anything.

## Features

- 🏠 **Multiple named homes** per player, with a configurable limit.
- 🌐 **Warps** — admin-only by default, or let players create their own (with a per-player
  limit; admins stay unlimited).
- ✅ **Warp confirmation** — `/warp <name>` prompts a clickable Accept/Deny confirmation before
  the warmup starts, like a teleport request. Fully optional — turn it off for instant behavior.
- ⏳ **Warmup teleport** — by default you must stand still for **5 seconds**; moving, taking
  damage, attacking, or breaking a block cancels it. Each of these is individually toggleable.
- ❄️ **Separate cooldowns** for `/home`, `/warp`, and `/back`, each independently configurable
  (default **0** = none).
- ⚡ **Instant teleport** for admins (permission-level bypass, configurable).
- ✨ **Teleport particles + sound**, with an extra "vanish" burst so it looks like the player
  really disappeared, and a short cosmetic Blindness effect on instant teleports — all toggleable.
- 💬 **Every message customizable** — every piece of text the mod sends can be reworded in the
  config, no code or resource pack required.
- 🪶 **Lightweight** — pending teleports are only ticked when they exist; data is plain JSON.
- 🧩 **Multiloader** — Fabric + NeoForge, no Architectury API dependency.

## Commands

| Command | Who | Description |
| --- | --- | --- |
| `/home [name]` | everyone | Teleport to a home (defaults to `home`, or your only home). |
| `/sethome [name]` | everyone | Set a home at your position (defaults to `home`). |
| `/delhome [name]` | everyone | Delete a home. |
| `/edithome <name> <newname>` | everyone | Rename a home. |
| `/homes` | everyone | List your homes. |
| `/warp [name]` | everyone | Teleport to a warp (no name = list). Prompts a confirmation unless disabled. |
| `/warp accept` / `/warp deny` | everyone | Confirm or cancel a pending `/warp` request. |
| `/warps` | everyone | List all warps. |
| `/back` | everyone | Return to where you last teleported from (if enabled). |
| `/setwarp <name>` | admin, or any player if `warps_players_can_create` is on | Create/update a warp at your position. |
| `/delwarp <name>` | admin, or the warp's creator | Delete a warp. |
| `/editwarp <name> <newname>` | admin, or the warp's creator | Rename a warp. |
| `/fiwwarp reload` | admin | Reload the config from disk. |

"admin" means a player at or above `admin_permission_level` (default op level 2).

> Don't name a warp `accept` or `deny` — those are reserved as `/warp` subcommands and would
> shadow a warp with that name.

### Player-created warps

By default only admins can create warps. Set `warps_players_can_create` to `true` to let
everyone use `/setwarp` (up to `max_warps_per_player` each — admins are always unlimited).
Each warp remembers who created it: non-admins can only `/delwarp` or `/editwarp` their own,
while admins can manage any warp, including ones other players made.

### Warp confirmation

When `warp_confirmation_enabled` is `true` (the default), running `/warp <name>` doesn't
teleport immediately — it sends a clickable **[Accept]** / **[Deny]** prompt that expires after
`warp_confirmation_seconds`. This gives players a chance to back out before the warmup even
starts. Set it to `false` to skip straight to the warmup, like `/home` does.

## Configuration

The main config lives on the server at `config/fiwwarp/config.json` and is created with
defaults on first run. Homes and warps are stored next to it in `homes.json` and `warps.json`.

```jsonc
{
  "enable_homes": true,                     // turn the whole home system on/off
  "enable_warps": true,                     // turn the whole warp system on/off
  "enable_back": true,                      // enable /back
  "warmup_seconds": 5,                      // stand-still time before teleport; 0 = instant
  "home_cooldown_seconds": 0,               // wait after /home; 0 = no cooldown
  "warp_cooldown_seconds": 0,               // wait after /warp; 0 = no cooldown
  "back_cooldown_seconds": 0,               // wait after /back; 0 = no cooldown
  "cancel_on_move": true,                   // moving cancels the warmup
  "cancel_on_damage": true,                 // getting punched cancels the warmup
  "cancel_on_attack": true,                 // punching something cancels the warmup
  "cancel_on_block_break": true,            // breaking a block cancels the warmup
  "max_homes": 3,                           // homes allowed per player
  "allow_bypass": true,                     // let high-perm players skip warmup/cooldown
  "bypass_permission_level": 2,             // permission level that bypasses
  "admin_permission_level": 2,              // permission level for /setwarp etc.
  "cross_dimension": true,                  // allow teleporting between dimensions
  "effects_enabled": true,                  // particles + sound on teleport
  "vanish_particles_enabled": true,         // extra "poof" burst so it looks like you vanished
  "instant_teleport_blindness_enabled": true, // short Blindness effect on instant teleports
  "instant_teleport_blindness_seconds": 2,  // duration of that Blindness effect
  "warps_players_can_create": false,        // let non-admins /setwarp their own warps
  "max_warps_per_player": 1,                // per-player warp limit (admins are unlimited)
  "warp_confirmation_enabled": true,        // require /warp accept before teleporting
  "warp_confirmation_seconds": 15,          // time to accept/deny before it expires
  "messages": { "...": "every player-facing string — see below" }
}
```

Edit the file and run `/fiwwarp reload` (or restart the server) to apply changes.

### Customizing messages

Every message the mod sends lives under the `messages` object in `config.json`, one string per
situation (e.g. `home_set`, `warp_confirm_prompt`, `cooldown_wait`), generated with defaults on
first run. They support `{placeholder}` tokens that get filled in, and use literal `§` color
codes — for example:

```jsonc
"messages": {
  "home_set": "§aHome §f{name}§a set.",
  "cooldown_wait": "§cSlow down — wait §f{seconds}s§c."
}
```

Edit whichever ones you want to change; the rest keep their defaults.

### Enabling only the systems you want

Each system can be turned off independently. For example, an SMP that wants **homes only** —
no warps, no `/back`:

```jsonc
{
  "enable_homes": true,
  "enable_warps": false,
  "enable_back": false
}
```

Disabled systems hide all of their commands (they won't show up or run), and the toggles
respond to `/fiwwarp reload` without a restart.

## Project structure

```
common/     Shared logic — config, storage, commands, teleport rules (loader-agnostic)
fabric/     Fabric entry point + fabric.mod.json
neoforge/   NeoForge entry point + neoforge.mods.toml
```

The `common` module is compiled against Mojang mappings and bundled into each loader jar, so
there is no Architectury runtime dependency.

## Building

Requires a JDK 21+.

```bash
./gradlew build
```

Artifacts land in:

- `fabric/build/libs/fiw-warp-fabric-<version>.jar`
- `neoforge/build/libs/fiw-warp-neoforge-<version>.jar`

Drop the matching jar into your server's `mods/` folder. On Fabric you also need
[Fabric API](https://modrinth.com/mod/fabric-api) and
[Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin); on NeoForge you need
[Kotlin for Forge](https://modrinth.com/mod/kotlin-for-forge).

All loader/library versions are pinned in `gradle.properties`.

## License

MIT — see [LICENSE](LICENSE).
