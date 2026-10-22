# ⚡ Litematica Printer Pro

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3%20%7C%201.21.4-brightgreen.svg?logo=minecraft&logoColor=white)](https://fabricmc.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.16.0-blue.svg?logo=fabric&logoColor=white)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21%20%7C%2025-orange.svg?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Release](https://img.shields.io/badge/Release-v1.0.0-purple.svg?logo=github&logoColor=white)](https://github.com/slopggobblercobbler81/litematica-printer-pro/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Litematica Printer Pro** is a high-performance, intelligent auto-placement Fabric companion mod for Masa's [Litematica](https://github.com/maruohon/litematica). Built natively for modern Minecraft (with full 26.3 & 1.21.4 compatibility), it delivers rapid, dependable schematic printing without annoying screen clutter or manual orientation head-aches.

---

## 🌟 Highlights

- ⚡ **High-Throughput Placement**: Burst speeds up to 20 blocks/tick with topological support resolution.
- 🎯 **Carpet & Masa Protocol (v2 & v3)**: Flawless orientation for stairs, pistons, observers, and hoppers without camera snapping.
- 🔭 **Extended 64-Block Reach**: Fully configurable reach distance from 1.0 up to 64.0 blocks with spherical candidate pruning.
- 🎒 **Inventory Buffer Caching**: Instant hotbar swapping from main inventory and shulker box container inspection.
- 🧹 **Clean & Unobtrusive**: Zero screen-clutter HUD boxes — all toggle and speed feedback is cleanly displayed in the vanilla actionbar.
- ⚙️ **MaLiLib & Mod Menu Ready**: Directly configurable in-game via `M + C` or Mod Menu.

---

## 🚀 Key Features

### ⚡ Smart Auto-Placement Engine
- High-throughput placement bursts configurable from **1 to 20 blocks per tick**.
- **Topological dependency resolution**: builds bottom-to-top and guarantees solid support blocks are placed before attaching torches, redstone dust, repeaters, comparators, rails, and ladders.
- **Safety boundaries**: Prevents suffocating the player or placing blocks inside the player's collision bounding box.

### 🔄 Accurate Block Rotation Resolution
- Precise sub-block hit-vector calculations for directional blocks including stairs, slabs, observers, pistons, hoppers, dispensers, droppers, and trapdoors.
- Native integration with **Carpet / Masa Accurate Block Placement Protocol (v2 & v3)**, enabling flawless block facings without frantic player camera twisting.

### 🔭 Extended Configurable Reach (1.0 to 64.0 Blocks)
- Configurable spherical reach limit allowing building expansive structures from afar.
- Optimized 3D spherical candidate pruning eliminates unnecessary block tests and maintains solid 60+ FPS.

### 🎒 High-Speed Inventory Buffer Caching
- Caches hotbar and main inventory slots to eliminate scan latency during 20+ bps build runs.
- Seamless automatic hotbar slot swapping directly from main inventory.
- Deep container inspection notifies you when materials are exhausted.

### 🛡️ Server Anti-Cheat & Strict Mode Friendly
- Optional `strictRaycast` line-of-sight validation ensures every placement action conforms to standard server camera visibility requirements.
- Configurable reach boundaries to comply with server anti-cheat plugins.

### 💬 Clean Actionbar Feedback (Zero Screen Clutter)
- Instant feedback in the native Minecraft actionbar above the hotbar when toggling or adjusting speed.
- Completely free of persistent HUD boxes or overlay indicators taking up screen real estate.

---

## 📐 Architecture Diagram

```mermaid
flowchart TD
    subgraph ClientTick ["Fabric Client Tick Loop"]
        A["ClientTickEvents.END_CLIENT_TICK"] --> B["SmartPlacementEngine"]
    end

    subgraph Inspection ["Candidate & Inventory Inspection"]
        B --> C["LitematicaBridge\n(WorldSchematic & Placements)"]
        B --> D["InventoryCacheManager\n(Hotbar / Inv / Shulker Scan)"]
        B --> E["RaycastValidator\n(Line-of-Sight Check)"]
    end

    subgraph Resolution ["Placement & Rotation Resolution"]
        B --> F["BlockRotationHelper\n(Hit Vector & Carpet Protocol)"]
        F --> G["Vanilla Hit / Carpet Sub-Pixel Offset"]
    end

    subgraph Execution ["Interaction Dispatch"]
        D --> H["Hotbar Slot Swap"]
        G --> I["ClientPlayerInteractionManager.interactBlock"]
        I --> J["Server Packet Dispatch & Arm Swing"]
    end

    subgraph Feedback ["Clean Actionbar Feedback"]
        B --> K["Actionbar Overlay Notifications\n(Toggle State & Speed)"]
    end
```

---

## ⌨️ Keybinding Cheatsheet

| Keybinding | Default Key | Action |
| :--- | :---: | :--- |
| **Toggle Printer** | `V` | Toggles automatic schematic block placement on or off. |
| **Cycle Placement Speed** | `B` | Cycles burst throughput through `1` ➔ `3` ➔ `5` ➔ `10` blocks/tick. |
| **Open MaLiLib Config** | `M + C` | Opens the MaLiLib in-game configuration menu. |

*(All keybindings can be customized in the vanilla Controls ➔ Key Binds menu under the `Litematica Printer Pro` category).*

---

## ⚙️ Configuration Options

Configuration can be modified in-game via MaLiLib (`M + C`), Mod Menu, or by editing `.minecraft/config/litematica_printer_pro.json`:

```json
{
  "printerEnabled": false,
  "placementRange": 4.5,
  "blocksPerTick": 3,
  "placementIntervalTicks": 1,
  "smartRotationEnabled": true,
  "carpetAccuratePlacement": true,
  "inventoryBufferCache": true,
  "breakWrongBlocks": false,
  "strictRaycast": false,
  "showHud": false,
  "hudPosX": 10,
  "hudPosY": 10
}
```

### Detailed Option Reference

| Option | Type | Default | Description |
| :--- | :---: | :---: | :--- |
| `printerEnabled` | *boolean* | `false` | Master switch for the auto-placement loop. |
| `placementRange` | *double* | `4.5` | Maximum spherical reach distance from player camera (range: `1.0` to `64.0`). |
| `blocksPerTick` | *integer* | `3` | Maximum blocks placed per client tick (range: `1` to `20`). |
| `placementIntervalTicks` | *integer* | `1` | Delay in ticks between placement cycles (`0` for uncapped every tick). |
| `smartRotationEnabled` | *boolean* | `true` | Calculates optimal hit vectors for directional blocks. |
| `carpetAccuratePlacement` | *boolean* | `true` | Uses the Carpet Accurate Placement protocol v2/v3 for exact block states. |
| `inventoryBufferCache` | *boolean* | `true` | Pre-indexes inventory items for instant hotbar swapping. |
| `breakWrongBlocks` | *boolean* | `false` | Automatically breaks wrong or misplaced blocks obstructing schematic. |
| `strictRaycast` | *boolean* | `false` | Requires unobstructed line-of-sight between player and target block face. |
| `showHud` | *boolean* | `false` | Displays optional on-screen HUD (disabled by default for clean visibility). |

---

## 📦 Installation & Setup

### Requirements
- **Minecraft**: `26.3` (or `1.21.4`)
- **Fabric Loader**: `>=0.16.0` (tested with `0.19.5+`)
- **Fabric API**: Recommended for Fabric client lifecycle events
- **MaLiLib**: Required for Litematica schematic management
- **Litematica**: Schema provider
- **Java**: `21` or `25`

### Quick Start
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.3.
2. Place `fabric-api`, `malilib`, `litematica`, and `litematica-printer-pro-26.3-1.0.0.jar` into your `.minecraft/mods` folder.
3. Launch Minecraft.
4. Load your schematic in Litematica and position it in your world.
5. Press `V` to toggle auto-placement, or `B` to adjust placement speed!

---

## 🛠️ Building from Source

```bash
# Clone the repository
git clone https://github.com/slopggobblercobbler81/litematica-printer-pro.git
cd litematica-printer-pro

# Native Minecraft 26.3 build
python build_native_263.py
```

Packaged JARs will be generated in `build/libs/`.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
