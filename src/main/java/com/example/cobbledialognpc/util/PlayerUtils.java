package com.example.cobbledialognpc.util;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 玩家工具类，用于处理 Bukkit Player 和 Minecraft ServerPlayer 之间的转换
 */
public class PlayerUtils {
    
    private static final Logger LOGGER = Logger.getLogger(PlayerUtils.class.getName());
    
    /**
     * 将 Bukkit Player 对象安全地转换为 Minecraft ServerPlayer 对象
     * 
     * @param player Bukkit Player 对象
     * @return ServerPlayer 对象，如果转换失败则返回 null
     */
    public static ServerPlayer getServerPlayer(Player player) {
        if (player == null) {
            LOGGER.warning("Player 对象为 null，无法进行转换");
            return null;
        }

        if (Bukkit.getServer() == null || !Bukkit.getServer().getClass().getName().contains("CraftServer")) {
            LOGGER.warning("当前不在 CraftServer 环境中，无法进行 Player 转换");
            return null;
        }
        
        try {

            Method getHandleMethod = player.getClass().getMethod("getHandle");
            Object handle = getHandleMethod.invoke(player);

            if (handle instanceof ServerPlayer) {
                return (ServerPlayer) handle;
            } else {
                LOGGER.warning("getHandle() 返回的对象不是 ServerPlayer 类型: " + 
                             (handle != null ? handle.getClass().getName() : "null"));
                return null;
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "反射获取 ServerPlayer 对象时发生异常", e);
            return null;
        }
    }
    
    /**
     * 将 Bukkit Player 对象安全地转换为 Minecraft ServerPlayer 对象，并向玩家发送错误消息
     * 
     * @param player Bukkit Player 对象
     * @return ServerPlayer 对象，如果转换失败则返回 null 并向玩家发送错误消息
     */
    public static ServerPlayer getServerPlayerWithMessage(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        
        if (serverPlayer == null && player != null) {
            player.sendMessage("§c无法获取服务器玩家对象，请确保在正确的服务器环境中运行");
        }
        
        return serverPlayer;
    }
    
    /**
     * 检查当前环境是否支持 Player 到 ServerPlayer 的转换
     * 
     * @return 如果支持转换则返回 true，否则返回 false
     */
    public static boolean isConversionSupported() {
        return Bukkit.getServer() != null && 
               Bukkit.getServer().getClass().getName().contains("CraftServer");
    }
}