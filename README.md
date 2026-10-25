# ⚡ LiteBuilder (Litematica Printer Pro)

<div align="center">

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/litebuilder?color=00AF5C&label=Modrinth%20Downloads&logo=modrinth)](https://modrinth.com/mod/litebuilder)
[![CurseForge](https://img.shields.io/curseforge/dt/1758829?color=F16436&label=CurseForge%20Downloads&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/litebuilder)
[![GitHub Release](https://img.shields.io/github/v/release/slopggobblercobbler81/litematica-printer-pro?logo=github)](https://github.com/slopggobblercobbler81/litematica-printer-pro/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20--%2026.3-informational)](https://fabricmc.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.16.0-blue)](https://fabricmc.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**An intelligent, high-performance Litematica schematic auto-builder for Fabric.**  
*Features smart directional rotation, automatic note block pitch tuning, redstone state cycling, and Carpet protocol support.*

[📥 Download on Modrinth](https://modrinth.com/mod/litebuilder) • [📦 Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/litebuilder) • [💾 GitHub Releases](https://github.com/slopggobblercobbler81/litematica-printer-pro/releases) • [🐛 Report Bug](https://github.com/slopggobblercobbler81/litematica-printer-pro/issues)

</div>

---

## 🌟 Key Features

### ⚡ Smart Dependency-Ordered Auto-Placement
- **Bottom-to-Top Building**: Places foundation blocks first, ensuring fragile blocks (torches, rails, redstone wire, repeaters, ladders) never drop as items.
- **Configurable Speed & Reach**: Adjust reach up to 64.0 blocks and speeds from 1 to 20 blocks per client tick.

### 🔄 Directional Smart Rotation
- Automatically calculates and spoof-rotates player look angles for directional blocks:
  - **Repeaters & Comparators**: Places facing the exact horizontal schematic direction.
  - **Observers**: Input face correctly aligns with schematic direction.
  - **Pistons & Sticky Pistons**: Head faces the intended direction.
  - **Dispensers, Droppers & Crafters**: Directional outputs match schematic.
  - **Stairs, Slabs & Trapdoors**: Correct half, facing, and corner shapes.
  - **Chests, Furnaces, Barrels, Campfires & Terracotta**: Front face matches schematic.

### 🎵 Automatic Note Block Pitch Tuning
- Detects placed note blocks and automatically cycles right-clicks to match schematic pitch (0 to 24 notes).
- Build complex redstone music boxes and note block songs effortlessly.
- Can be toggled on/off in the config (`tuneNoteBlocks`).

### ⏱️ In-Place Redstone State & Interactive Property Adjustment
- Automatically adjusts in-world block states to match the schematic:
  - **Repeater Delay**: Cycles delay (1–4 ticks) to exact schematic timing.
  - **Comparator Mode**: Toggles between *Compare* and *Subtract* mode.
  - **Daylight Detectors**: Toggles between *Normal* and *Inverted* sensor states.
  - **Trapdoors, Fence Gates & Doors**: Toggles open/closed states.
  - **Levers**: Toggles on/off powered states.

### 🛡️ Anti-Thrashing Inventory Batching
- Batches candidates by item ID to place all blocks of the same item in continuous bursts without mid-tick slot thrashing.
- Synchronizes active hotbar slots to the server via network packets before placing, eliminating ghost placements and item misplacement under rapid speeds.
- Fast-swaps needed items from your main inventory buffer automatically.

### 🌐 Carpet Protocol & Vanilla Fallback
- Supports **Carpet Accurate Placement Protocol v2 and v3** when playing on Carpet servers.
- **Smart Vanilla Fallback**: Automatically verifies protocol availability to prevent invalid coordinate packet rejections on vanilla/fabric servers.

### 🧹 Break Wrong Blocks (Optional)
- When enabled (`breakWrongBlocks = true`), automatically breaks obstructing or incorrectly rotated blocks so they can be re-placed with the correct orientation.

---

## 🎮 Keybindings

| Action | Default Key | Description |
| :--- | :---: | :--- |
| **Toggle Placement** | `V` | Toggles automatic schematic building on or off. |
| **Cycle Speed** | `B` | Cycles placement speed (1, 3, 5, or 10 blocks per tick). |
| **Configuration Menu** | `M + C` | Opens the MaLiLib in-game configuration menu. |

*Keybindings can be remapped in Options > Controls > Key Binds under the LiteBuilder category.*

---

## ⚙️ Configuration

Settings can be changed in-game through `M + C` (or Mod Menu), or in `.minecraft/config/litebuilder.json`:

| Option | Type | Default | Description |
| :--- | :---: | :---: | :--- |
| `printerEnabled` | boolean | `false` | Master toggle for automatic placement. |
| `placementRange` | double | `4.5` | Reach distance in blocks (1.0 - 64.0). |
| `blocksPerTick` | integer | `3` | Maximum blocks placed per client tick (1 - 20). |
| `placementIntervalTicks` | integer | `1` | Delay in ticks between placement cycles. |
| `smartRotationEnabled` | boolean | `true` | Resolves face hit angles for directional blocks. |
| `carpetAccuratePlacement` | boolean | `true` | Sends Carpet-compatible placement protocol packets when supported. |
| `tuneNoteBlocks` | boolean | `true` | Automatically tunes note blocks to match schematic pitch. |
| `inventoryBufferCache` | boolean | `true` | Caches inventory slots to minimize lookup overhead. |
| `breakWrongBlocks` | boolean | `false` | Breaks incorrectly placed blocks obstructing the schematic. |
| `strictRaycast` | boolean | `false` | Enforces line-of-sight checks before placing blocks. |
| `showHud` | boolean | `false` | Displays placement speed, queue depth, and status overlay. |

---

## 📦 Installation & Dependencies

1. Install [Fabric Loader](https://fabricmc.net/) (>= 0.16.0).
2. Download and drop the following mods into your `.minecraft/mods` folder:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [MaLiLib](https://modrinth.com/mod/malilib)
   - [Litematica](https://modrinth.com/mod/litematica)
   - [LiteBuilder](https://modrinth.com/mod/litebuilder)
3. Launch Minecraft, load a schematic using Litematica, and press **V** to start building!

---

## 🧩 Supported Versions

- **Minecraft 1.20.x**: `1.20.1`, `1.20.2`, `1.20.4`, `1.20.6`
- **Minecraft 1.21.x**: `1.21`, `1.21.1`, `1.21.2`, `1.21.3`, `1.21.4`
- **Minecraft 26.x**: `26.3` (and snapshots)
- **Loaders**: Fabric, Quilt

---

## 🔨 Building from Source

```bash
git clone https://github.com/slopggobblercobbler81/litematica-printer-pro.git
cd litematica-printer-pro
python build_native_263.py
```

Packaged jar files will be created in `build/libs/`.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
