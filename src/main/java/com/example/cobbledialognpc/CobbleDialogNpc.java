package com.example.cobbledialognpc;

import com.example.cobbledialognpc.command.HelpCommand;
import com.example.cobbledialognpc.command.MainCommand;
import com.example.cobbledialognpc.command.OpenDialogCommand;
import com.example.cobbledialognpc.config.MainConfig;
import lombok.Getter;
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

        // 初始化主配置文件
        initializeMainConfig();

        // 初始化配置目录和文件
        initializeDialogDirectory();
        
        initializeCommands();
        
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
            // 初始化MainConfig实例
            MainConfig.initialize();
            
            getLogger().info("主配置文件已初始化");
            getLogger().info("调试模式: " + (MainConfig.INSTANCE.isDebugEnabled() ? "启用" : "禁用"));
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
            getLogger().info("已创建对话配置目录: " + dialogDir.getAbsolutePath());
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

}

