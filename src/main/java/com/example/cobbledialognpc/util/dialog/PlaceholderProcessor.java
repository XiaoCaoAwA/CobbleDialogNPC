package com.example.cobbledialognpc.util.dialog;

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue;
import com.cobblemon.mod.common.api.dialogue.DialogueText;
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueText;
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 占位符处理器，用于处理文本中的动态占位符
 */
public class PlaceholderProcessor {
    private static final Pattern PATTERN = Pattern.compile("<([a-zA-Z0-9_:-]+)>");
    private static final Map<String, Function<ActiveDialogue, String>> providers = new HashMap<>();

    static {
        // 注册默认占位符提供者
        providers.put("player", dialogue -> dialogue.getPlayerEntity().getGameProfile().getName());
        providers.put("player_name", dialogue -> dialogue.getPlayerEntity().getGameProfile().getName());
        providers.put("player_display", dialogue -> {
            Component displayName = dialogue.getPlayerEntity().getDisplayName();
            return displayName != null ? displayName.getString() : dialogue.getPlayerEntity().getScoreboardName();
        });
        providers.put("player_uuid", dialogue -> dialogue.getPlayerEntity().getGameProfile().getId().toString());
    }

    /**
     * 注册占位符提供者
     */
    public static void register(String id, Function<ActiveDialogue, String> provider) {
        providers.put(id, provider);
    }

    /**
     * 处理文本中的占位符
     */
    public static String process(String text, ActiveDialogue dialogue) {
        if (text == null || !text.contains("<")) {
            return text;
        }

        Matcher matcher = PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Function<ActiveDialogue, String> provider = providers.get(placeholder);
            String replacement = provider != null ? provider.apply(dialogue) : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 包装字面量文本为对话文本
     */
    public static DialogueText wrapLiteral(String text) {
        if (text == null || !text.contains("<")) {
            return new WrappedDialogueText(Component.literal(text));
        }
        return new FunctionDialogueText(dialogue -> Component.literal(process(text, dialogue)));
    }

    /**
     * 包装组件为对话文本
     */
    public static DialogueText wrapComponent(MutableComponent component) {
        String text = component.getString();
        if (text == null || !text.contains("<")) {
            return new WrappedDialogueText(component);
        }
        return new FunctionDialogueText(dialogue -> Component.literal(process(text, dialogue)).setStyle(component.getStyle()));
    }
}