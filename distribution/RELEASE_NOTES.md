# ⚡ Litematica Printer Pro v1.0.0

High-performance, intelligent auto-placement Fabric companion mod for [Litematica](https://github.com/maruohon/litematica) on **Minecraft 26.3** and **1.21.4**.

---

## 🌟 What's New in v1.0.0

- 🎮 **Native Minecraft 26.3 Compatibility**: Fully migrated to modern 26.3 Mojang mappings and Java 25 runtime support.
- ⚡ **Smart Auto-Placement Engine**: High-throughput placement bursts configurable from 1 to 20 blocks per tick.
- 🏗️ **Topological Dependency Resolution**: Always places support blocks before attaching torches, redstone dust, repeaters, comparators, rails, and ladders.
- 🔄 **Carpet / Masa Accurate Placement (v2 & v3)**: Native protocol encoding guarantees correct sub-block rotation (pistons, observers, stairs, slabs, hoppers) without camera twitching.
- 🔭 **Extended 64-Block Reach**: Fully configurable reach distance from 1.0 up to 64.0 blocks with spherical distance candidate early pruning.
- 🎒 **Inventory Buffer Caching**: Instant hotbar swapping directly from main inventory and shulker box container inspection.
- 💬 **Clean Actionbar Notifications**: Replaced bulky, screen-cluttering HUD overlays with clean, temporary Minecraft actionbar notifications above the hotbar.
- ⚙️ **MaLiLib & Mod Menu Integration**: Seamless in-game configuration via `M + C` or Mod Menu.

---

## ⌨️ Controls & Keybindings

| Key | Action |
| :---: | :--- |
| `V` | Toggle auto-placement ON / OFF |
| `B` | Cycle burst speed (1 ➔ 3 ➔ 5 ➔ 10 blocks/tick) |
| `M + C` | Open MaLiLib in-game configuration menu |

---

## 📦 Requirements & Installation

1. Install **Fabric Loader** (`>=0.16.0`, tested with `0.19.5+`) for Minecraft 26.3 or 1.21.4.
2. Ensure you have **Fabric API**, **MaLiLib**, and **Litematica** installed.
3. Drop `litematica-printer-pro-26.3-1.0.0.jar` into your `.minecraft/mods` directory.
4. Launch the game, load your schematic, and press `V` to build!

---

## 🔐 Checksums & Assets

| File | SHA-256 Checksum |
| :--- | :--- |
| `litematica-printer-pro-26.3-1.0.0.jar` | `30ec6edd477de079fde0b2e970a0656229909be2abd08691fdc1ca580c2548fb` |
| `litematica-printer-pro-1.0.0.jar` | `30ec6edd477de079fde0b2e970a0656229909be2abd08691fdc1ca580c2548fb` |
