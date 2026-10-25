# ⚡ LiteBuilder (Litematica Printer Pro)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20--%2026.3-informational)](https://fabricmc.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.16.0-blue)](https://fabricmc.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://github.com/slopggobblercobbler81/litematica-printer-pro/blob/main/LICENSE)

**LiteBuilder** is an intelligent, high-performance client-side Fabric companion mod for [Litematica](https://modrinth.com/mod/litematica). It automatically places blocks from your inventory to construct loaded schematics seamlessly as you move around—with full support for directional block rotation, redstone state adjustment, note block pitch tuning, and server anti-cheat compliance.

---

## 🌟 Key Features

### ⚡ Smart Auto-Placement Engine
- **Support-Aware Ordering**: Builds bottom-to-top and prioritizes foundation blocks before placing dependent blocks (torches, redstone wire, repeaters, rails, ladders).
- **High-Speed Execution**: Configurable placement speeds from 1 up to 20 blocks per tick with configurable tick intervals.
- **Reach Distance up to 64 Blocks**: Build massive structures without constantly readjusting your position.

### 🔄 Directional Smart Rotation
- Automatically calculates and spoof-rotates player view angles for directional blocks:
  - **Repeaters & Comparators** (correct horizontal facing)
  - **Observers** (observing face points toward schematic direction)
  - **Pistons & Sticky Pistons** (wooden head faces correct direction)
  - **Dispensers, Droppers & Crafters**
  - **Stairs, Slabs & Trapdoors** (half, facing, and shape)
  - **Chests, Furnaces, Barrels, Campfires & Terracotta**

### 🎵 In-Place Note Block Pitch Tuning
- Detects placed Note Blocks and automatically right-clicks them to match the schematic pitch (0 to 24 notes).
- Build complete redstone song machines and music engines without manual tuning!
- Can be toggled on/off in the configuration (`tuneNoteBlocks`).

### ⏱️ Redstone State & Interactive Property Adjustment
- Automatically tunes in-world block states to match the schematic:
  - **Repeater Delay**: Cycles delay (1–4 ticks) to exact schematic timing.
  - **Comparator Mode**: Toggles between *Compare* and *Subtract* mode.
  - **Daylight Detectors**: Toggles between *Normal* and *Inverted* sensor states.
  - **Trapdoors, Fence Gates & Doors**: Sets open/closed states.
  - **Levers**: Sets on/off powered states.

### 🛡️ Anti-Thrashing Inventory Batching
- Groups candidates by item ID to place all blocks of the same item in continuous bursts.
- Synchronizes active hotbar slots to the server before interaction packets, eliminating ghost block placements and wrong-block swaps under rapid speeds.
- Fast-swaps needed items from your main inventory buffer automatically.

### 🌐 Carpet Protocol & Vanilla Fallback
- Supports **Carpet Accurate Placement Protocol v2 and v3** when playing on Carpet servers.
- **Smart Vanilla Fallback**: Automatically verifies protocol availability to prevent invalid coordinate packet rejections on vanilla/fabric servers.

### 🧹 Break Wrong Blocks (Optional)
- When enabled (`breakWrongBlocks = true`), automatically breaks obstructing or incorrectly rotated blocks so they can be re-placed with the correct orientation.

---

## 🎮 Keybindings & Controls

| Action | Default Key | Description |
| :--- | :---: | :--- |
| **Toggle Placement** | `V` | Turns automatic schematic building on or off. |
| **Cycle Speed** | `B` | Cycles placement speed (1, 3, 5, or 10 blocks/tick). |
| **Config Menu** | `M + C` | Opens the in-game MaLiLib configuration screen. |

*All keybindings can be customized in Options > Controls > Key Binds under the LiteBuilder category.*

---

## ⚙️ Configuration

Open the config menu with `M + C` or through **Mod Menu**, or edit `.minecraft/config/litebuilder.json`:

| Setting | Type | Default | Description |
| :--- | :---: | :---: | :--- |
| `printerEnabled` | boolean | `false` | Master toggle for schematic auto-placement. |
| `placementRange` | double | `4.5` | Reach distance in blocks (1.0 to 64.0). |
| `blocksPerTick` | integer | `3` | Maximum blocks placed in a single client tick (1 to 20). |
| `placementIntervalTicks` | integer | `1` | Tick delay between successive placement burst executions. |
| `smartRotationEnabled` | boolean | `true` | Calculates exact orientation vectors for directional blocks. |
| `carpetAccuratePlacement` | boolean | `true` | Enables Carpet/Masa accurate placement protocol when available. |
| `tuneNoteBlocks` | boolean | `true` | Automatically tunes note blocks to match schematic pitch. |
| `inventoryBufferCache` | boolean | `true` | Fast-swaps needed items into the hotbar from main inventory. |
| `breakWrongBlocks` | boolean | `false` | Breaks incorrectly placed or obstructing blocks. |
| `strictRaycast` | boolean | `false` | Enforces camera line-of-sight check before placement. |
| `showHud` | boolean | `false` | Displays placement speed, queue depth, and status overlay. |

---

## 📦 Dependencies

LiteBuilder requires the following client-side mods:
- [Fabric Loader](https://fabricmc.net/) (>= 0.16.0)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MaLiLib](https://modrinth.com/mod/malilib)
- [Litematica](https://modrinth.com/mod/litematica)

---

## 🧩 Supported Minecraft Versions

- **Minecraft 1.20.x**: `1.20.1`, `1.20.2`, `1.20.4`, `1.20.6`
- **Minecraft 1.21.x**: `1.21`, `1.21.1`, `1.21.2`, `1.21.3`, `1.21.4`
- **Minecraft 26.x**: `26.3` (and snapshots)
- **Loaders**: Fabric, Quilt

---

## 📜 Links & Source

- **Source Code**: [GitHub Repository](https://github.com/slopggobblercobbler81/litematica-printer-pro)
- **Issue Tracker**: [GitHub Issues](https://github.com/slopggobblercobbler81/litematica-printer-pro/issues)
- **License**: [MIT License](https://github.com/slopggobblercobbler81/litematica-printer-pro/blob/main/LICENSE)
