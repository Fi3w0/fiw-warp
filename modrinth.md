# Fiw Warp

**Lightweight, server-side teleportation for Fabric and NeoForge.**

Fiw Warp gives your players homes and your server warps, with a fair, configurable warmup so
teleporting isn't an instant escape button — and an optional cooldown to stop spam. It's
server-side only, registers no networking, and ships no client code, so it stays out of the way
and keeps your server light.

> Works on **Fabric** and **NeoForge** for **Minecraft 1.21.11** from the same mod.

## ✨ Features

- 🏠 **Multiple named homes** per player (configurable limit)
- 🌐 **Warps** — admin-only by default, or let players create their own with a per-player limit
- ✅ **Warp confirmation** — clickable Accept/Deny prompt before a `/warp` teleport starts
  (optional)
- ⏳ **Warmup teleport** — stand still for a few seconds (default 5); moving, getting hit,
  attacking, or breaking a block cancels it (all toggleable)
- ❄️ **Separate cooldowns** for `/home`, `/warp`, and `/back` (off by default)
- ⚡ **Instant teleport for admins** via permission level
- 🎆 **Particles + sound** on teleport, with an extra vanish burst and a short cosmetic
  Blindness effect on instant teleports — or turn any of it off
- 💬 **Every message customizable** — reword anything the mod says, right in the config
- 🪶 **Tiny footprint**, plain-JSON storage, no Architectury dependency

## 🎮 Commands

- `/home [name]`, `/sethome [name]`, `/delhome [name]`, `/edithome <name> <newname>`, `/homes`
- `/warp [name]`, `/warp accept`, `/warp deny`, `/warps`
- `/back`
- **Warp management:** `/setwarp <name>`, `/delwarp <name>`, `/editwarp <name> <newname>` —
  admin-only by default, or open to everyone (own warps only) if configured
- **Admin:** `/fiwwarp reload`

## ⚙️ Configuration

Everything is configurable in `config/fiwwarp/config.json` — warmup time, per-command cooldowns,
what cancels a teleport, home/warp limits, cross-dimension toggle, particle/blindness effects,
warp confirmation, admin permission level, every message the mod sends, and more. Edit it and
run `/fiwwarp reload`.

**Mix and match the systems.** Each of the **home**, **warp**, and **back** systems can be turned
on or off independently — e.g. run a homes-only SMP by setting `enable_warps` and `enable_back`
to `false`. Disabled systems hide their commands entirely.

## 📦 Requirements

- **Fabric:** Fabric API + Fabric Language Kotlin
- **NeoForge:** Kotlin for Forge

## 🧩 Compatibility

Server-side only. Clients do not need the mod installed to join a server running it.
