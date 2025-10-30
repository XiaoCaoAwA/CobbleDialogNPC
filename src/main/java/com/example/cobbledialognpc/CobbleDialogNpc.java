package com.example.cobbledialognpc;

import com.example.cobbledialognpc.command.HelpCommand;
import com.example.cobbledialognpc.command.MainCommand;
import com.example.cobbledialognpc.command.OpenDialogCommand;
import com.example.cobbledialognpc.config.MainConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class CobbleDialogNpc extends JavaPlugin {

    @Getter
    public static CobbleDialogNpc instance;
    
    private MainCommand mainCommand;

    @Override
    public void onEnable() {
        instance = this;

        initializeMainConfig();

        initializeDialogDirectory();
        
        initializeCommands();

        printStartupMessage();

        getLogger().info("CobbleDialogNPC 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("CobbleDialogNPC 插件已禁用！");
    }
    
    /**
     * 初始化主配置文件
     */
    private void initializeMainConfig() {
        try {
            MainConfig.initialize();
        } catch (Exception e) {
            getLogger().severe("初始化主配置文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化对话配置目录和默认配置文件
     */
    private void initializeDialogDirectory() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File dialogDir = new File(getDataFolder(), "dialog");
        if (!dialogDir.exists()) {
            dialogDir.mkdirs();
        }

        try {
            java.net.URL resourceUrl = getClass().getClassLoader().getResource("dialog");
            if (resourceUrl != null) {
                java.net.URI resourceUri = resourceUrl.toURI();
                java.nio.file.Path resourcePath;

                java.nio.file.FileSystem fileSystem = java.nio.file.FileSystems.newFileSystem(resourceUri, java.util.Collections.emptyMap());
                resourcePath = fileSystem.getPath("/dialog");

                java.nio.file.Files.walk(resourcePath)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        File targetFile = new File(dialogDir, fileName);
                        
                        if (!targetFile.exists()) {
                            try (InputStream inputStream = getResource("dialog/" + fileName)) {
                                if (inputStream != null) {
                                    Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    getLogger().info("已复制默认配置文件: " + fileName);
                                } else {
                                    getLogger().warning("无法找到资源文件: dialog/" + fileName);
                                }
                            } catch (IOException e) {
                                getLogger().severe("复制配置文件失败: " + fileName + " - " + e.getMessage());
                            }
                        }
                    });
                    
            } else {
                getLogger().warning("无法找到 resources/dialog 目录");
            }
        } catch (Exception e) {
            getLogger().severe("初始化对话配置目录时出错: " + e.getMessage());
        }
    }
    
    /**
     * 初始化并注册所有命令
     */
    private void initializeCommands() {
        mainCommand = new MainCommand();

        mainCommand.registerSubCommand(new HelpCommand());
        mainCommand.registerSubCommand(new OpenDialogCommand(this));

        getCommand("cdn").setExecutor(mainCommand);
        getCommand("cdn").setTabCompleter(mainCommand);
    }


    private static void printStartupMessage() {
        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┏┳━━━━━━━━━━━━┓");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┃┃████████████┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┣┫████┏━━━━┓██┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┃┃████┃ 白 ┃██┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┣┫████┃ 嫖 ┃██┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┃┃████┃ 宝 ┃██┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┃┃████┃ 典 ┃██┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┃┃████┗━━━━┛██┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┣┫████████████┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┃┃████████████┃");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b┗┻━━━━━━━━━━━━┛");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b欢迎使用CobbleDialogNpc 随机体型 插件");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b作者:XiaoCaoAwA 感谢您的支持！");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b本插件为免费插件,如付费购买就是被骗了！");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b加入官方插件交流群：966720491 快速反馈问题和需求");
        consoleSender.sendMessage("§7[§aCobbleDialogNpc§7] §b官方插件交流群有许多有趣的小插件等您来获取哟！~");
    }
}

