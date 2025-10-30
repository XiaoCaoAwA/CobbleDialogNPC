package com.example.cobbledialognpc.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author XiaoCaoAwA
 * @version 1.0
 */
public abstract class AbstractMainCommand implements TabExecutor {

    private final Map<String, SubCommand> commandMap = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            onCommand(sender);
            return false;
        }
        SubCommand subCommand = commandMap.get(args[0]);
        if (subCommand == null) {
            String message = getUnknownSubCommandMessage();
            if (message != null) {
                sender.sendMessage(message);
            }
            return false;
        }
        boolean permission = subCommand.hasPermission(sender);
        if (!permission) {
            String message = this.getNoPermissionMessage();
            if (message != null) {
                sender.sendMessage(message);
            }
            return false;
        }
        String[] array = Arrays.copyOfRange(args, 1, args.length);
        subCommand.onCommand(sender, array);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return new ArrayList<>(commandMap.keySet());
        SubCommand subCommand = commandMap.get(args[0]);
        if (subCommand == null) return Collections.emptyList();
        String[] array = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.onTabComplete(sender, array);
    }

    @Nullable
    protected String getNoPermissionMessage() {
        return null;
    }

    @Nullable
    protected String getUnknownSubCommandMessage() {
        return null;
    }

    public void onCommand(CommandSender sender) {
    }

    public void registerSubCommand(SubCommand subCommand) {
        subCommand.setParentCommand(this);
        commandMap.put(subCommand.getName(), subCommand);
    }

    /**
     * 获取所有子命令
     *
     * @return 子命令列表
     */
    public List<SubCommand> getCommands() {
        return new ArrayList<>(commandMap.values());
    }

    /**
     * 获取指定名称的子命令
     *
     * @param name 子命令名称
     * @return 子命令实例，如果不存在则返回null
     */
    public SubCommand getSubCommand(String name) {
        return commandMap.get(name.toLowerCase());
    }

}
