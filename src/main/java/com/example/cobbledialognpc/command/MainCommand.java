package com.example.cobbledialognpc.command;

import org.jetbrains.annotations.Nullable;

/**
 * 命令管理器，用于注册和管理所有子命令
 */
public class MainCommand extends AbstractMainCommand {

    @Override
    protected @Nullable String getUnknownSubCommandMessage() {
        return "§c未知的子命令，请使用 /cdn help 查看帮助";
    }

}