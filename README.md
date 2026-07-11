# Lycanites Mobs — NeoForge Port (1.21.1 & 26.1.2)

> **Unofficial NeoForge port** of **Lycanites Mobs 0.0.9-alpha** (schism source), ported from Forge 1.20.1.

This repository holds two NeoForge ports of Lycanites Mobs, one per Minecraft version, on separate branches:

| Branch | Minecraft | NeoForge | Java | Status |
|--------|-----------|----------|------|--------|
| [`neoforge-1.21.1`](../../tree/neoforge-1.21.1) | 1.21.1 | 21.1.x | 21 | Stable, gameplay-tested |
| [`neoforge-26.1.2`](../../tree/neoforge-26.1.2) | 26.1.2 | 26.1.2.76 | 25 | Playable; visual/gameplay polish ongoing |

## Credits & License

Lycanites Mobs is created by **Lycanite**. This is a community port of the schism-maintained source and is provided for personal use only. Please respect the original author's licensing terms — **keep this repository private and do not redistribute builds.**

## About

Lycanites Mobs adds 100+ elemental creatures, bosses, mob events, dungeons, an equipment-forging system, and a beastiary. This port brings the schism 1.20.1 Forge source to NeoForge.

## The two ports

### `neoforge-1.21.1`
Full Forge 1.20.1 → NeoForge 1.21.1 conversion:
- Loader migration (Forge → NeoForge APIs, payload network system, data attachments replacing capabilities).
- MC 1.20.1 → 1.21.1 vanilla API migration.
- OBJ creature renderer rewritten for 1.21.1's render pipeline.
- Runtime-verified: all 122 creatures spawn, all mob events run, natural spawning and textures confirmed in-game.

### `neoforge-26.1.2`
The 1.21.1 port carried forward to Minecraft 26.1.2 — a large MC-API migration on top of the NeoForge conversion. Highlights:
- Whole render stack ported to 26's state/submit entity rendering and `extract*` GUI pipeline (Button/Screen/list/tooltip rewrites), OBJ meshes emitted as vanilla render-type quads.
- Data-driven item components: every Block/Item now sets its registry id before construction; deferred item-list building until components bind.
- Networking, SavedData (codec), block-entity & entity ValueIO save/load, weather/time, fluids, spawn-egg tints, and equipment special-model rendering all migrated to 26 APIs.
- **RLCraft-style tuning applied** (creature stats, spawner rates, variant/level scaling) matching the RLCraft 2.10 Lycanites config, with dungeon boss levels rescaled to a sane cap.

## Building

```bash
# neoforge-1.21.1 branch → JAVA_HOME = JDK 21
# neoforge-26.1.2 branch → JAVA_HOME = JDK 25
./gradlew build
# → build/libs/lycanitesmobs-0.0.9-alpha.jar
```
