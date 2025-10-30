package com.example.cobbledialognpc.util;

import com.example.cobbledialognpc.config.MainConfig;
import org.bukkit.Bukkit;

/**
 * 调试日志工具类
 * 只有在config.yml中debug设置为true时才会输出调试日志
 * 
 * @author XiaoCaoAwA
 * @version 1.0
 */
public class DebugLogger {
    
    private static final String PREFIX = "[CobbleDialogNPC-DEBUG] ";
    
    /**
     * 检查是否启用调试模式
     * @return 是否启用调试模式
     */
    private static boolean isDebugEnabled() {
        return MainConfig.INSTANCE != null && MainConfig.INSTANCE.isDebugEnabled();
    }
    
    /**
     * 输出调试信息
     * @param message 调试消息
     */
    public static void debug(String message) {
        if (isDebugEnabled()) {
            Bukkit.getLogger().info(PREFIX + message);
        }
    }
    
    /**
     * 输出调试信息（带格式化）
     * @param format 格式化字符串
     * @param args 参数
     */
    public static void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            Bukkit.getLogger().info(PREFIX + String.format(format, args));
        }
    }
    
    /**
     * 输出调试警告
     * @param message 警告消息
     */
    public static void debugWarn(String message) {
        if (isDebugEnabled()) {
            Bukkit.getLogger().warning(PREFIX + message);
        }
    }
    
    /**
     * 输出调试警告（带格式化）
     * @param format 格式化字符串
     * @param args 参数
     */
    public static void debugWarn(String format, Object... args) {
        if (isDebugEnabled()) {
            Bukkit.getLogger().warning(PREFIX + String.format(format, args));
        }
    }
    
    /**
     * 输出调试错误
     * @param message 错误消息
     */
    public static void debugError(String message) {
        if (isDebugEnabled()) {
            Bukkit.getLogger().severe(PREFIX + message);
        }
    }
    
    /**
     * 输出调试错误（带异常）
     * @param message 错误消息
     * @param throwable 异常对象
     */
    public static void debugError(String message, Throwable throwable) {
        if (isDebugEnabled()) {
            Bukkit.getLogger().severe(PREFIX + message);
            throwable.printStackTrace();
        }
    }
}