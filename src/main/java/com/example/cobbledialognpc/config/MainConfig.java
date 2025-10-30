package com.example.cobbledialognpc.config;

import com.example.cobbledialognpc.CobbleDialogNpc;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author XiaoCaoAwA
 * @version 1.0
 */
@Getter
public class MainConfig extends BaseConfig {

    public static MainConfig INSTANCE;

    public MainConfig(JavaPlugin plugin, String fileName) {
        super(plugin, fileName);
    }

    /**
     * 初始化配置实例
     */
    public static void initialize() {
        if (INSTANCE == null) {
            INSTANCE = new MainConfig(CobbleDialogNpc.instance, "config.yml");
            INSTANCE.createDirectory();
            INSTANCE.saveDefault();
            INSTANCE.load();
        }
    }

    @Override
    public void load() {
        super.load();
    }

    /**
     * 获取调试模式配置
     * @return 是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return getBoolean("debug", false);
    }

}
