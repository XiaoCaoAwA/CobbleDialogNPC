package com.example.cobbledialognpc.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 对话配置文件加载器
 */
public class DialogConfigLoader {
    
    private static final Logger LOGGER = Logger.getLogger(DialogConfigLoader.class.getName());
    private static final Gson GSON = new Gson();
    private final JavaPlugin plugin;
    
    public DialogConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 获取所有可用的对话配置文件名称
     * 
     * @return 配置文件名称列表（不包含.json扩展名）
     */
    public List<String> getAvailableDialogFiles() {
        List<String> dialogFiles = new ArrayList<>();
        
        try {
            File dialogDir = new File(plugin.getDataFolder(), "dialog");
            
            if (!dialogDir.exists()) {
                LOGGER.warning("对话配置目录不存在: " + dialogDir.getAbsolutePath());
                return dialogFiles;
            }
            
            File[] files = dialogDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    dialogFiles.add(fileName.substring(0, fileName.lastIndexOf('.')));
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "获取对话配置文件列表时出错", e);
        }
        
        return dialogFiles;
    }
    
    /**
     * 加载指定的对话配置文件
     * 
     * @param fileName 配置文件名（不包含.json扩展名）
     * @return 解析后的JsonObject，如果加载失败则返回null
     */
    public JsonObject loadDialogConfig(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            LOGGER.warning("配置文件名不能为空");
            return null;
        }

        File configFile = new File(plugin.getDataFolder(), "dialog/" + fileName + ".json");
        
        try {
            if (!configFile.exists()) {
                LOGGER.warning("找不到配置文件: " + configFile.getAbsolutePath());
                return null;
            }
            
            try (FileReader reader = new FileReader(configFile)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "读取配置文件时出错: " + configFile.getAbsolutePath(), e);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "解析配置文件时出错: " + configFile.getAbsolutePath(), e);
            return null;
        }
    }
    
    /**
     * 验证配置文件是否存在
     * 
     * @param fileName 配置文件名（不包含.json扩展名）
     * @return 如果文件存在则返回true
     */
    public boolean isDialogConfigExists(String fileName) {
        return getAvailableDialogFiles().contains(fileName);
    }
}