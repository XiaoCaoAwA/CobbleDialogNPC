package com.example.cobbledialognpc.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 子命令抽象类，所有子命令都需要继承此类
 */
@Getter
public abstract class SubCommand {

    private AbstractMainCommand parentCommand;

    public boolean canRegister() {
        return true;
    }

    /**
     * 获取命令名称
     * @return 命令名称
     */
    @NotNull
    public String getName() {
        return getClass().getSimpleName().toLowerCase();
    }

    /**
     * 获取命令描述
     * @return 命令描述
     */
    @NotNull
    public String getDescription() {
        return "";
    }

    /**
     * 获取命令用法
     * @return 命令用法
     */
    public String getUsage() {
        return "/" + getName();
    }

    @Nullable
    public String getPermission() {
        return null;
    }

    public boolean hasPermission(CommandSender sender) {
        String permission = getPermission();
        if (permission == null) return true;
        return sender.hasPermission(permission);
    }

    public void setParentCommand(AbstractMainCommand parentCommand) {
        if (this.parentCommand != null) return;
        this.parentCommand = parentCommand;
    }

    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            onCommand((Player) sender, args);
        } else if (sender instanceof ConsoleCommandSender) {
            onCommand((ConsoleCommandSender) sender, args);
        }
    }

    /**
     * 执行命令
     * @param player 执行命令的玩家
     * @param args 命令参数
     */
    public void onCommand(Player player, String[] args) {}

    /**
     * 执行命令
     * @param console 执行命令的控制台
     * @param args 命令参数
     */
    public void onCommand(ConsoleCommandSender console, String[] args) {}

    /**
     * 获取Tab补全列表
     * @param sender 命令执行者
     * @param args 命令参数
     * @return Tab补全列表
     */
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}