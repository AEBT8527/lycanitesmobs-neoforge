# Lycanites Mobs — NeoForge Port (1.21.1 & 26.1.2)

> **Unofficial NeoForge port** of **Lycanites Mobs `0.0.9-alpha`** (schism-maintained source), converted from the Forge 1.20.1 codebase.

This repository holds two independent NeoForge ports of Lycanites Mobs — one per Minecraft version — on separate branches:

| Branch | Minecraft | NeoForge | Java | Parchment | Status |
|--------|-----------|----------|------|-----------|--------|
| **[`neoforge-1.21.1`](../../tree/neoforge-1.21.1)** *(default)* | 1.21.1 | 21.1.217 | 21 | 2024.11.17 | ✅ Stable — full gameplay verified |
| [`neoforge-26.1.2`](../../tree/neoforge-26.1.2) | 26.1.2 | 26.1.2.76 | 25 | — | ▶️ Playable — polish ongoing |

Each branch is a complete, buildable source tree for that Minecraft version. The `26.1.2` branch continues from the finished `1.21.1` port.

---

## Why this port exists

These ports are part of a broader effort to bring the mods that large modpacks are built around onto **NeoForge**, up to **Minecraft 26.1.2** — the same effort behind the 26.1.2 ports of Citadel, Alex's Mobs and Alex's Caves. The aim is to prove out **26.1.2 NeoForge as a viable new baseline — a new standard — for modpacks**, so pack authors are not held back on older Minecraft versions.

The official Lycanites Mobs release line currently stops at **Minecraft 1.20.1**, so these ports fill a version gap rather than duplicating anything upstream.

*(This is the port maintainer's motivation, not a statement on behalf of the original author.)*

---

## Credits & License

Lycanites Mobs is created by **Lycanite (Richard Nicholson)** — [lycanitesmobs.com](https://lycanitesmobs.com) · [GitLab](https://gitlab.com/Lycanite/LycanitesMobs) · [Modrinth](https://modrinth.com/mod/lycanites-mobs). These ports are built on the community **schism**-maintained source of `0.0.9-alpha`, converted from the Forge 1.20.1 codebase.

This is an **unofficial port** — not affiliated with, nor endorsed by, the original author. All credit for the mod's design, models, and content belongs to Lycanite and the schism contributors.

**License: LGPL-3.0-only**, as declared by the original author. As that licence requires, these ports stay **LGPL-3.0-only**, ship the full licence text (see [`LICENSE`](LICENSE), with the GPL base it references in [`COPYING`](COPYING)), and their complete corresponding source is public in this repository.

---

## What is Lycanites Mobs?

A large creature-and-combat expansion adding:

- **122 elemental creatures** across biomes, dimensions, and depths — tameable pets, mountable beasts, and bosses.
- **73 mob events** (world-wide invasions and boss summons) and **7 procedural dungeons**.
- **47 JSON-driven spawners** with a flexible condition/trigger/location system.
- An **equipment forging** system (parts → tools/weapons), **beastiary** knowledge progression, elemental fluids/blocks, and a summoning-staff/pedestal minion system.

Almost all content is data-driven (JSON under `assets`/`common`), so creatures, spawns, events and dungeons can be tuned without recompiling.

---

## The two ports

### `neoforge-1.21.1` — Forge 1.20.1 → NeoForge 1.21.1

A complete loader + vanilla-API conversion (848 Java source files):

- **Loader migration:** Forge → NeoForge APIs; `SimpleChannel` → the payload network system (all 22 message classes preserved behind a multiplexed `LycanitesPayload`); Capabilities → **Data Attachments**; `@Mod` constructor + event-bus wiring; `DeferredHolder`/`BuiltInRegistries` registration.
- **MC 1.20.1 → 1.21.1 vanilla migration:** `MobType` removed → tag-based classification; event renames (tick/damage/sleep events); `ResourceLocation.parse`; `EntityDimensions` accessor methods; `ItemAbility`/`IShearable` changes; GUI overlay → `RegisterGuiLayersEvent`.
- **Renderer:** the custom OBJ creature renderer rewritten for the 1.21.1 render pipeline (VBO/MeshData).
- **Verified in-game:** all 122 creatures construct and spawn, all mob events run to completion, natural spawning works, and all textures/models load. Considered stable.

### `neoforge-26.1.2` — 1.21.1 → Minecraft 26.1.2

A large MC-API migration layered on top of the NeoForge conversion (≈1230 compile errors ground to zero, then runtime bring-up):

- **Rendering:** whole stack ported to 26's **state/submit** entity rendering and the **`extract*`** GUI pipeline (Screen/Button/list/tooltip rewrites; `GuiGraphics.drawString` → `textRenderer().accept`; 2D `Matrix3x2f` GUI poses; 9-arg normalized-UV blit). OBJ meshes emit as vanilla render-type **quads** (triangle → degenerate-quad), fluids registered via `RegisterFluidModelsEvent`, equipment items drawn through a **`SpecialModelRenderer`**, spawn-egg tints via `ItemTintSources`.
- **Registration/data:** every Block/Item now calls `Properties.setId(...)` before construction; item-list building deferred until data components bind; `BlockEntityType` via constructor; `EntityType.Builder.build(ResourceKey)`.
- **Save/load & world:** codec-based `SavedData`, entity & block-entity **ValueIO** (`ValueInput`/`ValueOutput`), weather/time, `TeleportTransition`, `EntityReference` owner sync.
- **Networking:** bidirectional payload handlers registered for both sides (client validation).
- **RLCraft tuning applied:** creature stats, spawner rates, and variant/level scaling matched to the RLCraft 2.10 Lycanites config, with dungeon boss levels rescaled to a sane cap (≤ ~12).

---

## Building

```bash
# neoforge-1.21.1  →  JAVA_HOME = JDK 21
# neoforge-26.1.2  →  JAVA_HOME = JDK 25
./gradlew build
# output → build/libs/lycanitesmobs-0.0.9-alpha.jar
```

Both branches use the NeoForge **moddev** Gradle plugin. The `26.1.2` branch requires **Java 25** (mandated by MC 26.x) and moddev `2.0.141`.

### Repo layout

```
src/main/java/com/lycanitesmobs/   # mod source
src/main/resources/
├── assets/lycanitesmobs/          # textures, models, item defs, lang
├── common/lycanitesmobs/          # data-driven creatures / spawners / events / dungeons
├── data/lycanitesmobs/            # tags, biome modifiers, loot
└── META-INF/neoforge.mods.toml
```

Build output (`build/`, `.gradle/`, `run/`) is git-ignored; only source is tracked.

---

## Notes

- This port targets single-player and dedicated servers. Runtime was smoke-tested on a dedicated NeoForge server via RCON (creature summons, mob events, damage, saving).
- Some client-only polish on `26.1.2` is still in progress (GUI entity previews, the fear light-dimming effect); these are marked with TODOs in the source and don't affect gameplay.
