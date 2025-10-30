package com.example.cobbledialognpc.command;

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue;
import com.example.cobbledialognpc.util.DialogConfigLoader;
import com.example.cobbledialognpc.util.PlayerUtils;
import com.example.cobbledialognpc.util.dialog.TrainerDialogueUi;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 打开对话配置文件命令
 * 用法: /cdn open <配置文件>
 */
public class OpenDialogCommand extends SubCommand {
    
    private final DialogConfigLoader configLoader;
    
    public OpenDialogCommand(JavaPlugin plugin) {
        this.configLoader = new DialogConfigLoader(plugin);
    }

    @Override
    @NotNull
    public String getName() {
        return "open";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "为指定玩家打开对话配置文件";
    }

    @Override
    public String getUsage() {
        return "/cdn open <配置文件> [玩家名称]";
    }

    @Override
    @Nullable
    public String getPermission() {
        return "cobbledialognpc.command.open";
    }

    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§c请指定要打开的配置文件！");
            player.sendMessage("§e用法: /cdn open <配置文件> [玩家名称]");
            
            List<String> availableFiles = configLoader.getAvailableDialogFiles();
            if (!availableFiles.isEmpty()) {
                player.sendMessage("§a可用的配置文件: " + String.join(", ", availableFiles));
            }
            return;
        }
        
        String fileName = args[0];
        Player targetPlayer = player;

        if (args.length >= 2) {
            String targetPlayerName = args[1];
            targetPlayer = player.getServer().getPlayer(targetPlayerName);
            
            if (targetPlayer == null) {
                player.sendMessage("§c玩家 '" + targetPlayerName + "' 不在线或不存在！");
                return;
            }

            if (!targetPlayer.equals(player) && !player.hasPermission("cobbledialognpc.command.open.others")) {
                player.sendMessage("§c你没有权限为其他玩家打开对话！");
                return;
            }
        }

        if (!configLoader.isDialogConfigExists(fileName)) {
            player.sendMessage("§c配置文件 '" + fileName + "' 不存在！");
            
            List<String> availableFiles = configLoader.getAvailableDialogFiles();
            if (!availableFiles.isEmpty()) {
                player.sendMessage("§a可用的配置文件: " + String.join(", ", availableFiles));
            }
            return;
        }
        
        try {

            ServerPlayer serverPlayer = PlayerUtils.getServerPlayerWithMessage(targetPlayer);
            if (serverPlayer == null) {
                player.sendMessage("§c无法获取玩家 '" + targetPlayer.getName() + "' 的服务器对象！");
                return;
            }

            JsonObject config = configLoader.loadDialogConfig(fileName);
            if (config == null) {
                player.sendMessage("§c加载配置文件失败！");
                return;
            }

            JsonObject convertedConfig = convertToTrainerDialogueFormat(config);
            ActiveDialogue activeDialogue = TrainerDialogueUi.open(serverPlayer, convertedConfig);
            if (activeDialogue == null) {
                player.sendMessage("§c打开对话失败！");
                return;
            }

            if (targetPlayer.equals(player)) {
                player.sendMessage("§a已打开对话配置: " + fileName);
            } else {
                player.sendMessage("§a已为玩家 " + targetPlayer.getName() + " 打开对话配置: " + fileName);
                targetPlayer.sendMessage("§a管理员为你打开了对话: " + fileName);
            }
            
        } catch (Exception e) {
            player.sendMessage("§c打开对话时出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {

            List<String> availableFiles = configLoader.getAvailableDialogFiles();
            String input = args[0].toLowerCase();
            
            for (String file : availableFiles) {
                if (file.toLowerCase().startsWith(input)) {
                    completions.add(file);
                }
            }
        } else if (args.length == 2) {

            String input = args[1].toLowerCase();

            if (sender.hasPermission("cobbledialognpc.command.open")) {
                sender.getServer().getOnlinePlayers().forEach(player -> {
                    String playerName = player.getName();
                    if (playerName.toLowerCase().startsWith(input)) {
                        completions.add(playerName);
                    }
                });
            }
        }
        
        return completions;
    }
    
    /**
     * 将简化的对话配置格式转换为 TrainerDialogue 期望的格式
     */
    private JsonObject convertToTrainerDialogueFormat(JsonObject config) {
        JsonObject result = new JsonObject();
        
        // 添加默认背景
        result.addProperty("background", "cobblemon:textures/gui/dialogue/default.png");
        
        // 创建说话者
        JsonObject speakers = new JsonObject();
        
        // 检查是否是新的多页格式
        if (config.has("pages")) {
            // 新的多页格式 - 从第一个页面获取说话者信息
            JsonArray configPages = config.getAsJsonArray("pages");
            if (configPages.size() > 0) {
                JsonObject firstPage = configPages.get(0).getAsJsonObject();
                if (firstPage.has("speaker")) {
                    String speakerName = firstPage.get("speaker").getAsString();
                    JsonObject speaker = new JsonObject();
                    speaker.addProperty("name", speakerName);
                    speaker.addProperty("type", "npc");
                    speakers.add("main_speaker", speaker);
                }
            }
            
            // 直接转换页面格式
            JsonArray pages = new JsonArray();
            for (JsonElement pageElement : configPages) {
                if (pageElement.isJsonObject()) {
                    JsonObject configPage = pageElement.getAsJsonObject();
                    JsonObject convertedPage = new JsonObject();
                    
                    // 复制页面ID
                    if (configPage.has("id")) {
                        convertedPage.addProperty("id", configPage.get("id").getAsString());
                    }
                    
                    // 设置说话者
                    convertedPage.addProperty("speaker", "main_speaker");
                    
                    // 转换文本为lines数组
                    JsonArray lines = new JsonArray();
                    if (configPage.has("text")) {
                        lines.add(configPage.get("text").getAsString());
                    }
                    convertedPage.add("lines", lines);
                    
                    // 转换inputs
                    if (configPage.has("inputs")) {
                        JsonArray configInputs = configPage.getAsJsonArray("inputs");
                        JsonArray convertedInputs = new JsonArray();
                        
                        for (JsonElement inputElement : configInputs) {
                            if (inputElement.isJsonObject()) {
                                JsonObject configInput = inputElement.getAsJsonObject();
                                JsonObject convertedInput = new JsonObject();
                                convertedInput.addProperty("type", "option");
                                
                                // 复制文本
                                if (configInput.has("text")) {
                                    convertedInput.addProperty("text", configInput.get("text").getAsString());
                                }
                                
                                // 复制next和action（两者可以同时存在）
                                if (configInput.has("next")) {
                                    convertedInput.addProperty("next", configInput.get("next").getAsString());
                                }
                                if (configInput.has("action")) {
                                    // 支持复杂的action对象
                                    JsonElement actionElement = configInput.get("action");
                                    if (actionElement.isJsonPrimitive()) {
                                        convertedInput.addProperty("action", actionElement.getAsString());
                                    } else if (actionElement.isJsonObject()) {
                                        convertedInput.add("action", actionElement);
                                    }
                                }
                                
                                convertedInputs.add(convertedInput);
                            }
                        }
                        
                        if (convertedInputs.size() > 0) {
                            convertedPage.add("inputs", convertedInputs);
                        }
                    }
                    
                    // 处理页面级别的action（当页面关闭时执行）
                    if (configPage.has("action")) {
                        JsonElement actionElement = configPage.get("action");
                        if (actionElement.isJsonPrimitive()) {
                            convertedPage.addProperty("action", actionElement.getAsString());
                        } else if (actionElement.isJsonObject()) {
                            convertedPage.add("action", actionElement);
                        }
                    }
                    
                    pages.add(convertedPage);
                }
            }
            
            result.add("pages", pages);
            
        } else if (config.has("dialogue")) {
            // 旧的单页格式 - 保持原有逻辑
            JsonObject dialogue = config.getAsJsonObject("dialogue");
            if (dialogue.has("speaker")) {
                String speakerName = dialogue.get("speaker").getAsString();
                JsonObject speaker = new JsonObject();
                speaker.addProperty("name", speakerName);
                speaker.addProperty("type", "npc");
                speakers.add("main_speaker", speaker);
            }
            
            // 创建页面数组
            JsonArray pages = new JsonArray();
            JsonArray responsePages = new JsonArray(); // 临时存储响应页面
            
            // 创建主页面
            JsonObject mainPage = new JsonObject();
            mainPage.addProperty("id", "main");
            mainPage.addProperty("speaker", "main_speaker");
            
            // 添加对话文本
            JsonArray lines = new JsonArray();
            if (dialogue.has("text")) {
                lines.add(dialogue.get("text").getAsString());
            }
            mainPage.add("lines", lines);
            
            // 处理选项
            if (dialogue.has("options")) {
                JsonArray options = dialogue.getAsJsonArray("options");
                JsonArray inputs = new JsonArray();
                
                for (int i = 0; i < options.size(); i++) {
                    JsonElement optionElement = options.get(i);
                    if (optionElement.isJsonObject()) {
                        JsonObject option = optionElement.getAsJsonObject();
                        JsonObject input = new JsonObject();
                        input.addProperty("type", "option");
                        
                        if (option.has("text")) {
                            input.addProperty("text", option.get("text").getAsString());
                        }
                        
                        // 处理响应或动作
                        if (option.has("response")) {
                            // 创建响应页面
                            String responsePageId = "response_" + i;
                            JsonObject responsePage = new JsonObject();
                            responsePage.addProperty("id", responsePageId);
                            responsePage.addProperty("speaker", "main_speaker");
                            
                            JsonArray responseLines = new JsonArray();
                            responseLines.add(option.get("response").getAsString());
                            responsePage.add("lines", responseLines);
                            
                            // 添加"继续"按钮返回主页面或关闭对话
                            JsonArray responseInputs = new JsonArray();
                            JsonObject continueInput = new JsonObject();
                            continueInput.addProperty("type", "option");
                            continueInput.addProperty("text", "继续");
                            continueInput.addProperty("action", "close");
                            responseInputs.add(continueInput);
                            responsePage.add("inputs", responseInputs);
                            
                            responsePages.add(responsePage);
                            input.addProperty("next", responsePageId);
                        } else if (option.has("action")) {
                            JsonElement actionElement = option.get("action");
                            if (actionElement.isJsonPrimitive()) {
                                String action = actionElement.getAsString();
                                if ("close".equals(action)) {
                                    input.addProperty("action", "close");
                                }
                            } else if (actionElement.isJsonObject()) {
                                // 处理复杂的action对象
                                input.add("action", actionElement);
                            }
                        }
                        
                        inputs.add(input);
                    }
                }
                
                if (inputs.size() > 0) {
                    mainPage.add("inputs", inputs);
                }
            }
            
            // 首先添加主页面，然后添加响应页面
            pages.add(mainPage);
            for (JsonElement responsePage : responsePages) {
                pages.add(responsePage);
            }
            
            result.add("pages", pages);
        }
        
        result.add("speakers", speakers);
        return result;
    }
}