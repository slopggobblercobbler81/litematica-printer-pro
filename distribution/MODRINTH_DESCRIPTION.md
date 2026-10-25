# LiteBuilder

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

- V: Toggle automatic placement on or off.
- B: Cycle placement speed (1, 3, 5, or 10 blocks per tick).
- M + C: Open the MaLiLib in-game configuration menu.

Keybindings can be remapped in Options > Controls > Key Binds under the LiteBuilder category.

## Settings

Settings can be changed in the in-game menu or in `.minecraft/config/litebuilder.json`:

- printerEnabled (default: false): Master toggle for automatic placement.
- placementRange (default: 4.5, range: 1.0 - 64.0): Reach distance in blocks from the player.
- blocksPerTick (default: 3, range: 1 - 20): Maximum blocks placed per client tick.
- placementIntervalTicks (default: 1): Delay in ticks between placement cycles.
- smartRotationEnabled (default: true): Resolves face hit angles for directional blocks.
- carpetAccuratePlacement (default: true): Sends Carpet-compatible placement protocol packets when supported.
- inventoryBufferCache (default: true): Caches inventory slots to minimize lookup overhead during fast placement.
- breakWrongBlocks (default: false): Breaks incorrectly placed blocks obstructing the schematic.
- strictRaycast (default: false): Enforces line-of-sight checks before placing blocks.

## Dependencies

- Fabric Loader (>= 0.16.0)
- Fabric API
- MaLiLib
- Litematica

## Supported Versions

Minecraft 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, and 26.3.

## License

This project is licensed under the MIT License.
