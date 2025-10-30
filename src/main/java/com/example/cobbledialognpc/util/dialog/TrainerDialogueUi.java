package com.example.cobbledialognpc.util.dialog;

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue;
import com.cobblemon.mod.common.api.dialogue.DialogueManager;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * 打开 {@link TrainerDialogue} 对话的便捷工具。
 */
public class TrainerDialogueUi {
    
    /**
     * 启动玩家与训练师的对话
     */
    public static ActiveDialogue open(ServerPlayer player, TrainerDialogue dialogue) {
        return DialogueManager.INSTANCE.startDialogue(player, dialogue.toDialogue());
    }

    /**
     * 启动玩家与指定 NPC 的训练师对话
     */
    public static ActiveDialogue open(ServerPlayer player, NPCEntity npc, TrainerDialogue dialogue) {
        return DialogueManager.INSTANCE.startDialogue(player, npc, dialogue.toDialogue());
    }

    /**
     * 从 JSON 配置创建并启动对话
     */
    public static ActiveDialogue open(ServerPlayer player, JsonObject json) {
        return open(player, json, new TrainerDialogue.ConfigContext());
    }

    /**
     * 从 JSON 配置创建并启动对话，使用自定义上下文
     */
    public static ActiveDialogue open(ServerPlayer player, JsonObject json, TrainerDialogue.ConfigContext context) {
        return open(player, TrainerDialogue.fromJson(json, context));
    }

    /**
     * 从 JSON 配置创建 NPC 对话
     */
    public static ActiveDialogue open(ServerPlayer player, NPCEntity npc, JsonObject json) {
        return open(player, npc, json, new TrainerDialogue.ConfigContext());
    }

    /**
     * 从 JSON 配置创建 NPC 对话，使用自定义上下文
     */
    public static ActiveDialogue open(ServerPlayer player, NPCEntity npc, JsonObject json, TrainerDialogue.ConfigContext context) {
        return open(player, npc, TrainerDialogue.fromJson(json, context));
    }

    /**
     * 使用构建器创建对话
     */
    public static ActiveDialogue open(ServerPlayer player, Consumer<TrainerDialogue.Builder> configure) {
        return open(player, TrainerDialogue.create(configure));
    }

    /**
     * 使用构建器创建 NPC 对话
     */
    public static ActiveDialogue open(ServerPlayer player, NPCEntity npc, Consumer<TrainerDialogue.Builder> configure) {
        return open(player, npc, TrainerDialogue.create(configure));
    }
}