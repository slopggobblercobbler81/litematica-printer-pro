# LiteBuilder v1.0.0

LiteBuilder is a client-side Fabric mod that adds automatic block placement for schematics loaded through Litematica. It places blocks matching the schematic outline as you move around, with support for directional block rotation protocols, configurable reach, and inventory restocking.

## Features & Highlights in v1.0.0

- **Intelligent Placement Engine**: Automatic dependency-ordered placement respecting gravity and support blocks.
- **Directional Smart Rotation**: Accurate orientation resolution for repeaters, comparators, observers, pistons, dispensers, droppers, crafters, stairs, and chests.
- **In-Place Block State Tuning**:
  - Repeater delay cycling (1–4 ticks).
  - Comparator mode toggling (Compare vs Subtract).
  - Daylight Detector mode toggling (Normal vs Inverted).
  - Note Block pitch tuning (0–24 notes) with dedicated toggle switch.
  - Interactive states for trapdoors, doors, fence gates, and levers.
- **Carpet Protocol & Vanilla Fallback**: Automatic detection for Carpet Accurate Placement Protocol with safe surface hit-vector fallback on vanilla servers.
- **Anti-Thrashing Inventory Cache**: Item batching and carrier-slot synchronization to prevent rapid swapping misplacements under high speed.
- **Configurable Reach & Speed**: Reach distance configuration up to 64.0 blocks and 1-20 blocks per tick.
- **In-Game Configuration**: Full MaLiLib GUI (M + C) and Mod Menu integration with instant live property synchronization.

## Keybindings

- **V**: Toggle placement on or off.
- **B**: Cycle placement speed (1, 3, 5, 10 blocks/tick).
- **M + C**: Open configuration menu.

## Checksums (SHA-256)

- `litebuilder-1.0.0.jar`: `c638b06e719d7fe985a2144adf969f7b3326a6654d98aada79b9439f5eb3366d`
- `litebuilder-26.3-1.0.0.jar`: `c638b06e719d7fe985a2144adf969f7b3326a6654d98aada79b9439f5eb3366d`
- `litematica-printer-pro-1.0.0.jar`: `c638b06e719d7fe985a2144adf969f7b3326a6654d98aada79b9439f5eb3366d`
- `litematica-printer-pro-26.3-1.0.0.jar`: `c638b06e719d7fe985a2144adf969f7b3326a6654d98aada79b9439f5eb3366d`
