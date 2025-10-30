package com.example.cobbledialognpc.util.dialog;

import com.example.cobbledialognpc.util.DebugLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

/**
 * 命令执行器类
 * 支持不同类型的命令执行：command、op、console、broadcast、tell
 */
public class CommandExecutor {
    
    private final JavaPlugin plugin;
    
    public CommandExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 静态方法：根据类型执行命令列表
     * @param type 命令类型
     * @param commands 命令列表
     * @param playerName 目标玩家名称
     */
    public static void executeCommands(String type, List<String> commands, String playerName) {
        DebugLogger.debug("CommandExecutor.executeCommands 被调用");
        DebugLogger.debug("类型: %s, 命令数量: %d, 玩家: %s", type, commands.size(), playerName);
        
        if (commands == null || commands.isEmpty() || playerName == null) {
            DebugLogger.debugWarn("参数检查失败，退出执行");
            return;
        }
        
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            DebugLogger.debugWarn("找不到玩家: %s", playerName);
            return;
        }
        
        DebugLogger.debug("找到玩家，开始执行 %d 个命令", commands.size());
        for (String command : commands) {
            DebugLogger.debug("执行命令: %s", command);
            executeCommandByType(type, command, player);
        }
        DebugLogger.debug("所有命令执行完毕");
    }
    
    /**
     * 静态方法：根据类型执行单个命令
     * @param type 命令类型
     * @param command 命令字符串
     * @param player 目标玩家
     */
    public static void executeCommandByType(String type, String command, Player player) {
        DebugLogger.debug("executeCommandByType 被调用，类型: %s, 命令: %s", type, command);
        
        if (command == null || command.trim().isEmpty()) {
            DebugLogger.debugWarn("命令为空，跳过执行");
            return;
        }
        
        // 替换变量
        String originalCommand = command;
        command = command.replace("{player}", player.getName());
        command = command.replace("{p}", player.getName()); // 兼容旧格式
        DebugLogger.debug("变量替换: %s -> %s", originalCommand, command);
        
        try {
            DebugLogger.debug("执行类型: %s", type.toLowerCase());
            switch (type.toLowerCase()) {
                case "command":
                    DebugLogger.debug("执行玩家命令");
                    executePlayerCommandStatic(command, player);
                    break;
                case "op":
                    DebugLogger.debug("执行OP命令");
                    executeOpCommandStatic(command, player);
                    break;
                case "console":
                    DebugLogger.debug("执行控制台命令");
                    executeConsoleCommandStatic(command, player);
                    break;
                case "broadcast":
                    DebugLogger.debug("执行广播");
                    executeBroadcastStatic(command, player);
                    break;
                case "tell":
                    DebugLogger.debug("执行私聊");
                    executeTellStatic(command, player);
                    break;
                default:
                    DebugLogger.debug("未知类型，默认执行玩家命令");
                    // 默认作为玩家命令执行
                    executePlayerCommandStatic(command, player);
                    break;
            }
            DebugLogger.debug("命令执行完成");
        } catch (Exception e) {
            DebugLogger.debugError("命令执行异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行单个命令
     * @param player 目标玩家
     * @param command 命令字符串
     */
    public void executeCommand(Player player, String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }
        
        // 替换变量
        command = replaceVariables(command, player);
        
        try {
            // 解析命令类型和内容
            if (command.startsWith("[command]")) {
                executePlayerCommand(player, command.substring(9).trim());
            } else if (command.startsWith("[op]")) {
                executeOpCommand(player, command.substring(4).trim());
            } else if (command.startsWith("[console]")) {
                executeConsoleCommand(command.substring(9).trim());
            } else if (command.startsWith("[broadcast]")) {
                executeBroadcast(command.substring(11).trim());
            } else if (command.startsWith("[tell]")) {
                executeTell(player, command.substring(6).trim());
            } else {
                // 默认作为玩家命令执行
                executePlayerCommand(player, command);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "执行命令时出现错误: " + command, e);
        }
    }
    
    /**
     * 替换命令中的变量
     * @param command 原始命令
     * @param player 玩家对象
     * @return 替换后的命令
     */
    private String replaceVariables(String command, Player player) {
        return command.replace("{p}", player.getName());
    }
    
    /**
     * 执行玩家命令
     * @param player 玩家
     * @param command 命令
     */
    private void executePlayerCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        final String finalCommand = command;
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.performCommand(finalCommand);
        });
    }
    
    /**
     * 执行OP命令
     * @param player 玩家
     * @param command 命令
     */
    private void executeOpCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        final String finalCommand = command;
        boolean wasOp = player.isOp();
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                // 临时给予OP权限
                player.setOp(true);
                player.performCommand(finalCommand);
            } finally {
                // 恢复原始OP状态
                player.setOp(wasOp);
            }
        });
    }
    
    /**
     * 执行控制台命令
     * @param command 命令
     */
    private void executeConsoleCommand(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        final String finalCommand = command;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        });
    }
    
    /**
     * 执行广播
     * @param message 广播消息
     */
    private void executeBroadcast(String message) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.broadcastMessage(message);
        });
    }
    
    /**
     * 发送私人消息
     * @param player 接收者
     * @param message 消息内容
     */
    private void executeTell(Player player, String message) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(message);
        });
    }
    
    // 静态方法版本，用于在没有CommandExecutor实例时调用
    
    /**
     * 静态方法：执行玩家命令
     * @param player 玩家
     * @param command 命令
     */
    public static void executePlayerCommandStatic(String command, Player player) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        final String finalCommand = command;
        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(CommandExecutor.class), () -> {
            player.performCommand(finalCommand);
        });
    }
    
    /**
     * 静态方法：执行OP命令
     * @param player 玩家
     * @param command 命令
     */
    public static void executeOpCommandStatic(String command, Player player) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        final String finalCommand = command;
        boolean wasOp = player.isOp();
        
        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(CommandExecutor.class), () -> {
            try {
                // 临时给予OP权限
                player.setOp(true);
                player.performCommand(finalCommand);
            } finally {
                // 恢复原始OP状态
                player.setOp(wasOp);
            }
        });
    }
    
    /**
     * 静态方法：执行控制台命令
     * @param command 命令
     */
    public static void executeConsoleCommandStatic(String command, Player player) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        final String finalCommand = command;
        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(CommandExecutor.class), () -> {
            String processedCommand = finalCommand.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        });
    }
    
    /**
     * 静态方法：执行广播
     * @param message 广播消息
     * @param player 触发广播的玩家（用于变量替换）
     */
    public static void executeBroadcastStatic(String message, Player player) {
        String processedMessage = message.replace("{player}", player.getName());
        Bukkit.broadcastMessage(processedMessage);
    }
    
    /**
     * 静态方法：发送私人消息
     * @param message 消息内容
     * @param player 接收者
     */
    public static void executeTellStatic(String message, Player player) {
        String processedMessage = message.replace("{player}", player.getName());
        player.sendMessage(processedMessage);
    }
}