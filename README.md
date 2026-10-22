# LiteBuilder

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20--%2026.3-informational)](https://fabricmc.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.16.0-blue)](https://fabricmc.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

LiteBuilder is a client-side Fabric mod that adds automatic block placement for schematics loaded through Litematica. It places blocks matching the schematic outline as you move around, with support for directional block rotation protocols, configurable reach, and inventory restocking.

## Features

- Schematic placement: Automatically places blocks from your hotbar or inventory matching your loaded Litematica schematic.
- Directional block rotation: Correctly places stairs, slabs, observers, pistons, hoppers, and trapdoors using appropriate hit angles.
- Accurate placement protocol: Supports the Carpet / Masa Accurate Block Placement protocol (v2 and v3) when available on servers, allowing accurate block rotation without needing camera adjustments.
- Configurable reach: Placement distance can be adjusted from 1.0 up to 64.0 blocks in the settings.
- Inventory restocking: Automatically swaps required items into your hotbar from your main inventory as you build.
- Support-aware placement: Builds bottom-to-top and prioritizes solid support blocks before placing dependent blocks like torches, rails, ladders, and redstone components.
- Actionbar notifications: Speed changes and toggle status are displayed above the hotbar, leaving your main screen clear.
- In-game configuration: Configurable through MaLiLib's in-game config menu (M + C) or Mod Menu.

## Controls

| Key | Default | Description |
| :--- | :---: | :--- |
| Toggle Placement | `V` | Toggles automatic placement on or off. |
| Cycle Speed | `B` | Cycles placement speed (1, 3, 5, or 10 blocks per tick). |
| Configuration Menu | `M + C` | Opens the MaLiLib in-game configuration screen. |

Keybindings can be remapped in Options > Controls > Key Binds under the LiteBuilder category.

## Settings

Settings can be changed in the in-game menu or in `.minecraft/config/litebuilder.json`:

| Option | Type | Default | Description |
| :--- | :---: | :---: | :--- |
| `printerEnabled` | boolean | `false` | Master toggle for automatic placement. |
| `placementRange` | double | `4.5` | Reach distance in blocks (1.0 - 64.0). |
| `blocksPerTick` | integer | `3` | Maximum blocks placed per client tick (1 - 20). |
| `placementIntervalTicks` | integer | `1` | Delay in ticks between placement cycles. |
| `smartRotationEnabled` | boolean | `true` | Resolves face hit angles for directional blocks. |
| `carpetAccuratePlacement` | boolean | `true` | Sends Carpet-compatible placement protocol packets when supported. |
| `inventoryBufferCache` | boolean | `true` | Caches inventory slots to minimize lookup overhead. |
| `breakWrongBlocks` | boolean | `false` | Breaks incorrectly placed blocks obstructing the schematic. |
| `strictRaycast` | boolean | `false` | Enforces line-of-sight checks before placing blocks. |
| `showHud` | boolean | `false` | Optional on-screen HUD (disabled by default for clean visibility). |

## Dependencies

- Fabric Loader (>= 0.16.0)
- Fabric API
- MaLiLib
- Litematica

## Supported Versions

Minecraft 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, and 26.3.

## Building from Source

```bash
git clone https://github.com/slopggobblercobbler81/litematica-printer-pro.git
cd litematica-printer-pro
python build_native_263.py
```

Packaged jar files will be created in `build/libs/`.

## License

This project is licensed under the MIT License.
