package com.example.cobbledialognpc.config;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

@Getter
public class BaseConfig extends YamlConfiguration {

    protected final JavaPlugin plugin;
    protected final File file;
    protected final File absoluteFile;

    public BaseConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName.endsWith(".yml") ? fileName : fileName + ".yml");
        this.absoluteFile = file.getAbsoluteFile();
    }

    public BaseConfig(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        this.absoluteFile = file.getAbsoluteFile();
    }

    public void load() {
        if (absoluteFile.exists()) {
            try {
                this.load(absoluteFile);
            } catch (Exception e) {
                plugin.getLogger().severe("无法加载配置文件: " + absoluteFile.getPath());
            }
        }
    }

    public void asyncSave() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> this.save());
    }

    public void save() {
        if (!absoluteFile.exists()) {
            this.createDirectory();
        }
        try {
            this.save(absoluteFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存配置文件: " + absoluteFile.getPath());
        }
    }

    public void saveDefault() {
        saveDefault(false);
    }

    public void saveDefault(boolean replace) {
        saveDefault("", replace);
    }

    public void saveDefault(String resourcePath, boolean replace) {
        if (!absoluteFile.exists() || replace) {
            plugin.saveResource(resourcePath + file.getName(), replace);
        }
    }

    public void createDirectory() {
        File directory = this.absoluteFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            plugin.getLogger().severe("无法创建目录: " + directory.getPath());
        }
    }

    @NotNull
    public ItemStack getItemStack(@NotNull String path) {
        Material material = getMaterial(path + ".material");
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getString(path + ".name"));
            meta.setLore(getStringList(path + ".lore"));
        }
        return stack;
    }

    public Material getMaterial(String path) {
        String string = getString(path);
        if (string == null) return null;
        return Material.matchMaterial(string);
    }

    public Location getLocation(String path) {
        World world = getWorld(path + ".world");
        if (world == null) {
            return null;
        }
        double x = getDouble(path + ".x");
        double y = getDouble(path + ".y");
        double z = getDouble(path + ".z");
        float yaw = getFloat(path + ".yaw");
        float pitch = getFloat(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Nullable
    public World getWorld(String path) {
        String world = getString(path, "world");
        if (world == null) return null;
        return Bukkit.getWorld(world);
    }

    public float getFloat(@NotNull String path) {
        Object def = getDefault(path);
        return getFloat(path, (def instanceof Number) ? toFloat(def) : 0);
    }

    public float getFloat(@NotNull String path, float def) {
        Object val = get(path, def);
        return (val instanceof Number) ? toFloat(val) : def;
    }

    public static float toFloat(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        }

        try {
            if (object != null) {
                return Float.parseFloat(object.toString());
            }
        } catch (Exception ignored) {
        }

        return 0;
    }


}