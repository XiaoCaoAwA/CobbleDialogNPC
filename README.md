# CobbleDialogNPC 对话配置文件编写教程

## 📖 概述

CobbleDialogNPC 插件支持两种对话配置格式：
- **单页对话格式** (`dialogue` 格式) - 适合简单的对话
- **多页对话格式** (`pages` 格式) - 适合复杂的多页面对话

所有配置文件都应放置在 `src/main/resources/dialog/` 目录下，文件名以 `.json` 结尾。

## 🎯 基本结构

每个对话配置文件都包含以下基本字段：

```json
{
  "title": "对话标题",
  "description": "对话描述",
  // 对话内容（二选一）
  "dialogue": { ... },  // 单页格式
  "pages": [ ... ]      // 多页格式
}
```

## 📄 单页对话格式 (dialogue)

### 基本结构

```json
{
  "title": "欢迎对话",
  "description": "简单的欢迎对话",
  "dialogue": {
    "speaker": "NPC名称",
    "text": "对话文本内容",
    "options": [
      {
        "text": "选项文本",
        "action": { ... },     // 执行动作
        "response": "回复文本"  // 或显示回复
      }
    ]
  }
}
```

### 选项类型

#### 1. 动作选项 (action)
执行特定命令或操作：

```json
{
  "text": "给我礼物",
  "action": {
    "type": "console",
    "commands": [
      "give {player} minecraft:diamond 1",
      "tell {player} §a你获得了钻石！"
    ]
  }
}
```

#### 2. 回复选项 (response)
显示NPC的回复文本：

```json
{
  "text": "你好吗？",
  "response": "我很好，谢谢你的关心！"
}
```

#### 3. 关闭选项
直接关闭对话：

```json
{
  "text": "再见",
  "action": "close"
}
```

## 📚 多页对话格式 (pages)

### 基本结构

```json
{
  "title": "多页对话",
  "description": "复杂的多页面对话",
  "pages": [
    {
      "id": "main",
      "speaker": "NPC名称",
      "text": "主页面文本",
      "inputs": [
        {
          "text": "选项文本",
          "action": { ... },    // 执行动作
          "next": "page_id"     // 跳转到其他页面
        }
      ]
    },
    {
      "id": "page_id",
      "speaker": "NPC名称",
      "text": "其他页面文本",
      "inputs": [ ... ]
    }
  ]
}
```

### 页面跳转

使用 `next` 字段实现页面间跳转：

```json
{
  "text": "下一页",
  "next": "next_page"
}
```

### 组合动作

可以同时执行动作和跳转页面：

```json
{
  "text": "执行并跳转",
  "action": {
    "type": "tell",
    "commands": ["§a执行成功！"]
  },
  "next": "next_page"
}
```

## ⚡ 动作类型 (Action Types)

### 1. tell - 私聊消息
向玩家发送私聊消息：

```json
{
  "type": "tell",
  "commands": [
    "§a欢迎来到服务器！",
    "§b你的名字是：{player}"
  ]
}
```

### 2. console - 控制台命令
以控制台身份执行命令：

```json
{
  "type": "console",
  "commands": [
    "give {player} minecraft:diamond 3",
    "tp {player} 0 100 0"
  ]
}
```

### 3. broadcast - 广播消息
向全服玩家广播消息：

```json
{
  "type": "broadcast",
  "commands": [
    "§6{player} 完成了特殊任务！"
  ]
}
```

### 4. op - OP权限命令
以OP权限执行命令：

```json
{
  "type": "op",
  "commands": [
    "weather clear",
    "time set day"
  ]
}
```

### 5. close - 关闭对话
直接关闭对话窗口：

```json
{
  "action": "close"
}
```

## 🔧 占位符系统

支持以下占位符：

- `{player}` - 玩家名称
- `{p}` - 玩家名称（简写）
- 支持 PlaceholderAPI 占位符（如果安装）

### 使用示例

```json
{
  "type": "tell",
  "commands": [
    "§a你好，{player}！",
    "§b当前时间：{server_time}",
    "§c你的等级：{player_level}"
  ]
}
```

## 🎨 文本格式化

支持 Minecraft 颜色代码：

```json
{
  "text": "§a绿色文本 §c红色文本 §b蓝色文本",
  "commands": [
    "§l粗体文本",
    "§o斜体文本",
    "§n下划线文本"
  ]
}
```

### 常用颜色代码

- `§0` - 黑色
- `§1` - 深蓝色
- `§2` - 深绿色
- `§3` - 深青色
- `§4` - 深红色
- `§5` - 深紫色
- `§6` - 金色
- `§7` - 灰色
- `§8` - 深灰色
- `§9` - 蓝色
- `§a` - 绿色
- `§b` - 青色
- `§c` - 红色
- `§d` - 粉色
- `§e` - 黄色
- `§f` - 白色

### 格式代码

- `§l` - 粗体
- `§o` - 斜体
- `§n` - 下划线
- `§m` - 删除线
- `§k` - 混乱
- `§r` - 重置

## 📝 完整示例

### 示例1：商店对话

```json
{
  "title": "商店对话",
  "description": "NPC商店系统",
  "dialogue": {
    "speaker": "商店老板",
    "text": "欢迎来到我的商店！你想要什么？",
    "options": [
      {
        "text": "购买钻石 (10金币)",
        "action": {
          "type": "console",
          "commands": [
            "eco take {player} 10",
            "give {player} minecraft:diamond 1",
            "tell {player} §a购买成功！"
          ]
        }
      },
      {
        "text": "查看余额",
        "action": {
          "type": "tell",
          "commands": [
            "§b你的余额：{vault_eco_balance} 金币"
          ]
        }
      },
      {
        "text": "离开商店",
        "action": "close"
      }
    ]
  }
}
```

### 示例2：任务对话

```json
{
  "title": "任务对话",
  "description": "多步骤任务系统",
  "pages": [
    {
      "id": "main",
      "speaker": "任务NPC",
      "text": "我有一个重要的任务需要你帮忙！",
      "inputs": [
        {
          "text": "接受任务",
          "next": "accept_quest"
        },
        {
          "text": "了解更多",
          "next": "quest_info"
        },
        {
          "text": "拒绝任务",
          "action": "close"
        }
      ]
    },
    {
      "id": "quest_info",
      "speaker": "任务NPC",
      "text": "这个任务需要你收集10个钻石。完成后你将获得丰厚的奖励！",
      "inputs": [
        {
          "text": "接受任务",
          "next": "accept_quest"
        },
        {
          "text": "返回",
          "next": "main"
        }
      ]
    },
    {
      "id": "accept_quest",
      "speaker": "任务NPC",
      "text": "太好了！去收集10个钻石吧，完成后回来找我！",
      "inputs": [
        {
          "text": "开始任务",
          "action": {
            "type": "console",
            "commands": [
              "tell {player} §a任务已接受：收集10个钻石",
              "give {player} minecraft:compass 1"
            ]
          }
        }
      ]
    }
  ]
}
```

## 🚀 使用方法

1. **创建配置文件**：在 `src/main/resources/dialog/` 目录下创建 `.json` 文件
2. **编写配置**：按照上述格式编写对话配置
3. **重启插件**：重启服务器或重载插件
4. **测试对话**：使用命令 `/cdn open <文件名>` 测试对话

### 命令使用

```bash
# 打开对话配置文件
/cdn open welcome

# 为指定玩家打开对话
/cdn open welcome PlayerName

# 查看帮助
/cdn help
```

## ⚠️ 注意事项

1. **JSON格式**：确保JSON格式正确，注意逗号和括号
2. **文件编码**：使用UTF-8编码保存文件
3. **权限检查**：某些命令可能需要特定权限
4. **占位符**：确保使用的占位符插件已安装
5. **测试**：创建后及时测试功能是否正常

---

*本教程涵盖了CobbleDialogNPC插件的所有对话配置功能，按照此教程可以创建丰富多样的NPC对话系统。*