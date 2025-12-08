# MagicBlock

<div align="center">

[![Minecraft](https://img.shields.io/badge/Minecraft-1.18+-green.svg)](https://www.minecraft.net/)
[![Spigot](https://img.shields.io/badge/Spigot-Supported-orange.svg)](https://www.spigotmc.org/)
[![Paper](https://img.shields.io/badge/Paper-Supported-blue.svg)](https://papermc.io/)
[![Folia](https://img.shields.io/badge/Folia-Supported-purple.svg)](https://papermc.io/software/folia)
[![Version](https://img.shields.io/badge/Version-3.2.1.0-brightgreen.svg)](https://github.com/Syferie/MagicBlock)

**A powerful Minecraft plugin for Magic Blocks and Magic Food**

English | [简体中文](README_zh.md)

</div>

---

## 📖 Introduction

MagicBlock is a feature-rich Minecraft server plugin that allows players to use **magic blocks** and **magic food** with limited usage counts. These special items can be bound to players and managed through an intuitive GUI interface, making it perfect for survival servers, RPG servers, and more.

---

## 🖥️ Supported Servers

| Server Type | Support Status | Minimum Version |
|-------------|----------------|-----------------|
| Spigot      | ✅ Fully Supported | 1.18+ |
| Paper       | ✅ Fully Supported | 1.18+ |
| Folia       | ✅ Fully Supported | 1.18+ |
| Purpur      | ✅ Fully Supported | 1.18+ |

**Optional Dependency**: [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - For placeholder support

---

## ✨ Features

### 🧱 Magic Block System
- Configurable usage counts for blocks
- Block binding system - Only the bound player can use the block
- Intuitive GUI selection interface
- Block search functionality
- Favorites system
- Anti-duplication protection

### 🍖 Magic Food System
- Reusable food items
- Custom food effects (healing, potion effects, etc.)
- Independent usage count system

### 🎨 Customization
- Multi-language support (English/Chinese)
- Highly configurable GUI interface
- Custom block name translations
- Custom lore display
- Permission group system

---

## 🔐 Permissions

### Administrator Permission
| Permission Node | Description | Default |
|-----------------|-------------|---------|
| `magicblock.admin` | Includes all permissions | OP |

### Basic Permissions
| Permission Node | Description | Default |
|-----------------|-------------|---------|
| `magicblock.use` | Place and interact with magic blocks | All Players |
| `magicblock.break` | Break magic blocks | All Players |
| `magicblock.list` | View bound blocks list | All Players |

### Management Permissions
| Permission Node | Description | Default |
|-----------------|-------------|---------|
| `magicblock.get` | Get magic blocks | OP |
| `magicblock.give` | Give magic blocks to other players | OP |
| `magicblock.reload` | Reload plugin configuration | OP |
| `magicblock.settimes` | Set block usage count | OP |
| `magicblock.addtimes` | Add block usage count | OP |
| `magicblock.getfood` | Get magic food | OP |

### Group Permissions
| Permission Node | Description |
|-----------------|-------------|
| `magicblock.group.<group-name>` | Access materials from specific permission groups |

> Example: `magicblock.group.vip-material` allows players to use blocks from the `vip-material` group in config

---

## 📝 Commands

Main command: `/magicblock` or `/mb`

| Command | Description | Permission |
|---------|-------------|------------|
| `/mb help` | Display help information | None |
| `/mb list` | Open bound blocks list GUI | `magicblock.list` |
| `/mb get <material> [amount] [uses]` | Get magic block of specified material | `magicblock.get` |
| `/mb give <player> <material> [amount] [uses]` | Give magic block to a player | `magicblock.give` |
| `/mb getfood <food> [amount]` | Get magic food | `magicblock.getfood` |
| `/mb settimes <uses>` | Set usage count for held magic block | `magicblock.settimes` |
| `/mb addtimes <uses>` | Add usage count to held magic block | `magicblock.addtimes` |
| `/mb reload` | Reload plugin configuration | `magicblock.reload` |

**Parameter Notes**:
- `<material>` - Minecraft material name, e.g., `DIAMOND_BLOCK`
- `[amount]` - Optional, defaults to 1
- `[uses]` - Optional, set to `-1` for infinite uses

---

## 🎮 Usage Guide

### Magic Block Operations
| Action | Effect |
|--------|--------|
| Sneak + Right Click | Bind the block |
| Sneak + Left Click | Open block selection GUI |
| Place normally | Place block (consumes 1 use) |

### GUI Operations
| Action | Effect |
|--------|--------|
| Left click on block | Select that block type |
| Right click on block | Add/remove from favorites |
| Click search button | Search for specific blocks |
| Click page buttons | Browse more blocks |

### Bound List Operations
| Action | Effect |
|--------|--------|
| Left click | Retrieve bound block |
| Double right click | Hide from list (doesn't unbind) |

---

## 📊 PlaceholderAPI Variables

The following placeholders are available when PlaceholderAPI is installed:

| Placeholder | Description |
|-------------|-------------|
| `%magicblock_block_uses%` | Total block uses by player |
| `%magicblock_food_uses%` | Total food uses by player |
| `%magicblock_remaining_uses%` | Remaining uses of held block |
| `%magicblock_max_uses%` | Maximum uses of held block |
| `%magicblock_uses_progress%` | Usage progress (percentage) |
| `%magicblock_progress_bar%` | Usage progress bar |
| `%magicblock_has_block%` | Whether player has magic block |
| `%magicblock_has_food%` | Whether player has magic food |

---

## 📁 Configuration Files

| File | Description |
|------|-------------|
| `config.yml` | Main configuration (language, GUI, block settings, etc.) |
| `foodconf.yml` | Magic food configuration |
| `lang_zh_CN.yml` | Chinese language file |
| `lang_en.yml` | English language file |

---

## 💬 Support

For issues or suggestions, please contact us through:

- **GitHub Issues**: [Submit an issue](https://github.com/Syferie/MagicBlock/issues)
- **QQ Group**: [134484522](https://qm.qq.com/q/134484522)

> **Bug Report Tips**: Please provide detailed reproduction steps, otherwise the bug may not be fixable.

---

## 📜 License

This plugin uses a modified MIT License:

- ✅ Free to use on any server
- ✅ Allowed to modify source code
- ✅ Allowed to distribute modified versions
- ❌ Commercial sales prohibited
- ❌ Must retain original author information in derivative works

---

<div align="center">

**© 2024-2025 Syferie. All Rights Reserved.**

</div>
