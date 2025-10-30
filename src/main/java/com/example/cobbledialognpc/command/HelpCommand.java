package com.example.cobbledialognpc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 帮助命令
 * 用法: /cdn help
 */
public class HelpCommand extends SubCommand {

    @Override
    @NotNull
    public String getName() {
        return "help";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "显示帮助信息";
    }

    @Override
    public String getUsage() {
        return "/cdn help";
    }

    @Override
    @Nullable
    public String getPermission() {
        return "cobbledialognpc.command.use";
    }

    @Override
    public void onCommand(Player player, String[] args) {
        player.sendMessage("§7CobbleDialogNPC:");
        player.sendMessage("§f/cdn help - §a显示此帮助信息");
        player.sendMessage("§f/cdn open <配置文件> [玩家ID] - §a打开指定的对话配置文件");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Arrays.asList();
    }
}