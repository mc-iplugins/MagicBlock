# MagicBlock

<div align="center">

[![Minecraft](https://img.shields.io/badge/Minecraft-1.18+-green.svg)](https://www.minecraft.net/)
[![Spigot](https://img.shields.io/badge/Spigot-支持-orange.svg)](https://www.spigotmc.org/)
[![Paper](https://img.shields.io/badge/Paper-支持-blue.svg)](https://papermc.io/)
[![Folia](https://img.shields.io/badge/Folia-支持-purple.svg)](https://papermc.io/software/folia)
[![Version](https://img.shields.io/badge/版本-3.2.1.0-brightgreen.svg)](https://github.com/Syferie/MagicBlock)

**一个功能强大的 Minecraft 魔法方块与魔法食物插件**

[English](README.md) | 简体中文

</div>

---

## 📖 简介

MagicBlock 是一个功能丰富的 Minecraft 服务器插件，允许玩家使用具有**有限使用次数**的魔法方块和魔法食物。这些特殊物品可以绑定到玩家，通过直观的 GUI 界面进行管理，非常适合生存服、RPG 服等各类服务器使用。

---

## 🖥️ 支持的服务器

| 服务端类型 | 支持状态 | 最低版本 |
|-----------|---------|---------|
| Spigot    | ✅ 完全支持 | 1.18+ |
| Paper     | ✅ 完全支持 | 1.18+ |
| Folia     | ✅ 完全支持 | 1.18+ |
| Purpur    | ✅ 完全支持 | 1.18+ |

**可选依赖**：[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - 用于变量支持

---

## ✨ 功能列表

### 🧱 魔法方块系统
- 可配置使用次数的方块
- 方块绑定系统 - 绑定后只有绑定者可使用
- 直观的 GUI 选择界面
- 方块搜索功能
- 收藏夹系统
- 防刷机制保护

### 🍖 魔法食物系统
- 可重复使用的食物物品
- 自定义食物效果（恢复、药水效果等）
- 独立的使用次数系统

### 🎨 自定义功能
- 多语言支持（中文/英文）
- 高度可配置的 GUI 界面
- 自定义方块名称翻译
- 自定义 Lore 显示
- 权限组系统

---

## 🔐 权限节点

### 管理员权限
| 权限节点 | 说明 | 默认 |
|---------|------|------|
| `magicblock.admin` | 包含所有权限 | OP |

### 基础权限
| 权限节点 | 说明 | 默认 |
|---------|------|------|
| `magicblock.use` | 放置和交互魔法方块 | 所有玩家 |
| `magicblock.break` | 破坏魔法方块 | 所有玩家 |
| `magicblock.list` | 查看已绑定的方块列表 | 所有玩家 |

### 管理权限
| 权限节点 | 说明 | 默认 |
|---------|------|------|
| `magicblock.get` | 获取魔法方块 | OP |
| `magicblock.give` | 给予其他玩家魔法方块 | OP |
| `magicblock.reload` | 重载插件配置 | OP |
| `magicblock.settimes` | 设置方块使用次数 | OP |
| `magicblock.addtimes` | 增加方块使用次数 | OP |
| `magicblock.getfood` | 获取魔法食物 | OP |

### 权限组权限
| 权限节点 | 说明 |
|---------|------|
| `magicblock.group.<组名>` | 访问特定权限组的材料 |

> 例如：`magicblock.group.vip-material` 允许玩家使用配置文件中 `vip-material` 组的方块

---

## 📝 命令列表

主命令：`/magicblock` 或 `/mb`

| 命令 | 说明 | 权限 |
|-----|------|------|
| `/mb help` | 显示帮助信息 | 无 |
| `/mb list` | 打开已绑定方块列表 GUI | `magicblock.list` |
| `/mb get <材料> [数量] [次数]` | 获取指定材料的魔法方块 | `magicblock.get` |
| `/mb give <玩家> <材料> [数量] [次数]` | 给予玩家魔法方块 | `magicblock.give` |
| `/mb getfood <食物> [数量]` | 获取魔法食物 | `magicblock.getfood` |
| `/mb settimes <次数>` | 设置手持魔法方块的使用次数 | `magicblock.settimes` |
| `/mb addtimes <次数>` | 增加手持魔法方块的使用次数 | `magicblock.addtimes` |
| `/mb reload` | 重载插件配置 | `magicblock.reload` |

**参数说明**：
- `<材料>` - Minecraft 材料名称，如 `DIAMOND_BLOCK`
- `[数量]` - 可选，默认为 1
- `[次数]` - 可选，设置为 `-1` 表示无限使用次数

---

## 🎮 使用方法

### 魔法方块操作
| 操作 | 效果 |
|-----|------|
| 潜行 + 右键点击 | 绑定方块 |
| 潜行 + 左键点击 | 打开方块选择 GUI |
| 直接放置 | 放置方块（消耗1次使用次数） |

### GUI 界面操作
| 操作 | 效果 |
|-----|------|
| 左键点击方块 | 选择该方块类型 |
| 右键点击方块 | 收藏/取消收藏 |
| 点击搜索按钮 | 搜索特定方块 |
| 点击翻页按钮 | 浏览更多方块 |

### 绑定列表操作
| 操作 | 效果 |
|-----|------|
| 左键点击 | 取回绑定的方块 |
| 双击右键 | 从列表中隐藏（不解除绑定） |

---

## 📊 PlaceholderAPI 变量

安装 PlaceholderAPI 后可使用以下变量：

| 变量 | 说明 |
|-----|------|
| `%magicblock_block_uses%` | 玩家使用方块的总次数 |
| `%magicblock_food_uses%` | 玩家使用食物的总次数 |
| `%magicblock_remaining_uses%` | 手持方块的剩余使用次数 |
| `%magicblock_max_uses%` | 手持方块的最大使用次数 |
| `%magicblock_uses_progress%` | 使用进度（百分比） |
| `%magicblock_progress_bar%` | 使用进度条 |
| `%magicblock_has_block%` | 是否持有魔法方块 |
| `%magicblock_has_food%` | 是否持有魔法食物 |

---

## 📁 配置文件

| 文件 | 说明 |
|-----|------|
| `config.yml` | 主配置文件（语言、GUI、方块设置等） |
| `foodconf.yml` | 魔法食物配置 |
| `lang_zh_CN.yml` | 中文语言文件 |
| `lang_en.yml` | 英文语言文件 |

---

## 💬 支持

如有问题或建议，请通过以下方式联系：

- **GitHub Issues**: [提交问题](https://github.com/Syferie/MagicBlock/issues)
- **QQ 交流群**: [134484522](https://qm.qq.com/q/134484522)

> **Bug 反馈提示**：请提供详细的复现步骤，否则可能无法修复。

---

## 📜 许可协议

本插件采用修改版 MIT 许可证：

- ✅ 允许在任何服务器上免费使用
- ✅ 允许修改源代码
- ✅ 允许分发修改后的版本
- ❌ 禁止商业销售
- ❌ 二次开发需保留原作者信息

---

<div align="center">

**© 2024-2025 Syferie. All Rights Reserved.**

</div>
