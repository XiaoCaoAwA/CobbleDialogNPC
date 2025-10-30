package com.example.cobbledialognpc.util.dialog;

import com.bedrockk.molang.Expression;
import com.cobblemon.mod.common.api.dialogue.*;
import com.cobblemon.mod.common.api.dialogue.input.DialogueInput;
import com.cobblemon.mod.common.api.dialogue.input.DialogueNoInput;
import com.cobblemon.mod.common.api.dialogue.input.DialogueOption;
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput;
import com.example.cobbledialognpc.util.DebugLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;

/**
 * 以链式调用方式快速构建 Cobblemon 的对话对象，方便在附属模组中直接通过代码定义 NPC 对话。
 */
public class TrainerDialogue {
    private final List<DialoguePage> pages;
    private final ResourceLocation background;
    private final DialogueAction escapeAction;
    private final Map<String, DialogueSpeaker> speakers;
    private final DialogueAction initializationAction;

    private TrainerDialogue(List<DialoguePage> pages, ResourceLocation background, 
                           DialogueAction escapeAction, Map<String, DialogueSpeaker> speakers,
                           DialogueAction initializationAction) {
        this.pages = pages;
        this.background = background;
        this.escapeAction = escapeAction;
        this.speakers = speakers;
        this.initializationAction = initializationAction;
    }

    public Dialogue toDialogue() {
        return new Dialogue(pages, background, escapeAction, speakers, initializationAction);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TrainerDialogue create(Consumer<Builder> configure) {
        Builder builder = builder();
        configure.accept(builder);
        return builder.build();
    }

    public static TrainerDialogue fromJson(JsonObject json, ConfigContext context) {
        Builder builder = builder();
        builder.loadFromJson(json, context);
        return builder.build();
    }

    // 解析对话文本的静态方法
    public static DialogueText parseDialogueText(JsonElement element, ConfigContext context) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        
        if (element.isJsonPrimitive()) {
            return PlaceholderProcessor.wrapLiteral(element.getAsString());
        }
        
        if (element.isJsonObject()) {
            return parseDialogueTextObject(element.getAsJsonObject(), context);
        }
        
        return null;
    }

    private static DialogueText parseDialogueTextObject(JsonObject obj, ConfigContext context) {
        // 处理引用
        if (obj.has("ref")) {
            String id = obj.get("ref").getAsString();
            DialogueText text = context.resolveText(id);
            if (text != null) return text;
            
            MutableComponent component = context.resolveComponent(id);
            if (component != null) {
                return PlaceholderProcessor.wrapComponent(component);
            }
        }

        // 处理函数
        if (obj.has("function")) {
            String id = obj.get("function").getAsString();
            return context.resolveText(id);
        }

        // 处理组件
        if (obj.has("component")) {
            return buildComponentTemplate(obj.get("component"), context).toDialogueText();
        }

        // 处理文本组件
        if (obj.has("text") || obj.has("translate") || obj.has("with")) {
            return buildComponentTemplate(obj, context).toDialogueText();
        }

        return null;
    }

    private static ComponentTemplate buildComponentTemplate(JsonElement element, ConfigContext context) {
        if (element.isJsonPrimitive()) {
            return new ComponentTemplate.Static(Component.literal(element.getAsString()));
        }
        
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            
            // 处理引用
            if (obj.has("ref")) {
                String id = obj.get("ref").getAsString();
                DialogueText text = context.resolveText(id);
                if (text != null) {
                    return new ComponentTemplate.TextReference(text);
                }
            }

            // 处理静态文本
            if (obj.has("text")) {
                String text = obj.get("text").getAsString();
                return new ComponentTemplate.Static(Component.literal(text));
            }

            // 处理翻译文本
            if (obj.has("translate")) {
                String key = obj.get("translate").getAsString();
                if (obj.has("with")) {
                    JsonArray args = obj.getAsJsonArray("with");
                    Object[] argArray = new Object[args.size()];
                    for (int i = 0; i < args.size(); i++) {
                        argArray[i] = args.get(i).getAsString();
                    }
                    return new ComponentTemplate.Static(Component.translatable(key, argArray));
                } else {
                    return new ComponentTemplate.Static(Component.translatable(key));
                }
            }
        }
        
        return new ComponentTemplate.Static(Component.literal(""));
    }

    /**
     * 解析输入选项数组
     */
    private static DialogueInput parseInputsArray(JsonArray inputsArray, ConfigContext context) {
        if (inputsArray == null || inputsArray.size() == 0) {
            return new DialogueNoInput();
        }
        
        if (inputsArray.size() == 1) {
            return parseInputElement(inputsArray.get(0), context);
        }
        
        // 多个选项，创建选项输入
        List<DialogueOption> options = new ArrayList<>();
        for (JsonElement inputElement : inputsArray) {
            if (inputElement.isJsonObject()) {
                JsonObject inputObj = inputElement.getAsJsonObject();
                if ("option".equals(inputObj.get("type").getAsString())) {
                    String text = inputObj.has("text") ? inputObj.get("text").getAsString() : "选项";
                    DialogueText optionText = PlaceholderProcessor.wrapLiteral(text);
                    
                    DebugLogger.debug("处理选项按钮: %s", text);
                    DebugLogger.debug("inputObj 内容: %s", inputObj.toString());
                    DebugLogger.debug("是否有 action: %s", inputObj.has("action"));
                    DebugLogger.debug("是否有 next: %s", inputObj.has("next"));
                    
                    DialogueAction action = null;
                    if (inputObj.has("action") && inputObj.has("next")) {
                        // 同时存在action和next时，创建一个组合动作
                        String nextPageId = inputObj.get("next").getAsString();
                        DebugLogger.debug("在parseInputsArray中创建组合动作：action + next，下一页: %s", nextPageId);
                        
                        action = new FunctionDialogueAction((dialogue, optionValue) -> {
                            // 从对话对象中获取玩家信息
                            String playerName = dialogue.getPlayerEntity().getGameProfile().getName();
                            DebugLogger.debug("=== parseInputsArray组合动作开始执行 ===");
                            DebugLogger.debug("组合动作被执行，玩家: %s, 选项值: %s", playerName, optionValue);
                            DebugLogger.debug("action 内容: %s", inputObj.get("action").toString());
                            
                            // 先执行命令动作（但不关闭对话框）
                            DebugLogger.debug("=== 开始执行命令动作 ===");
                            context.executeActionWithoutClosing(inputObj.get("action"), dialogue, playerName);
                            DebugLogger.debug("=== 命令动作执行完毕 ===");
                            
                            // 然后跳转到下一页
                            DebugLogger.debug("=== 开始跳转到下一页 ===");
                            DebugLogger.debug("跳转到下一页: %s", nextPageId);
                            Integer pageIndex = context.getPageIndex(nextPageId);
                            if (pageIndex != null) {
                                DebugLogger.debug("跳转到页面: %s (索引: %d)", nextPageId, pageIndex);
                                dialogue.setPage(pageIndex);
                            } else {
                                DebugLogger.debugWarn("找不到页面: %s，关闭对话", nextPageId);
                                dialogue.close();
                            }
                            return null;
                        });
                    } else if (inputObj.has("action")) {
                        DebugLogger.debug("在parseInputsArray中创建纯命令动作");
                        action = context.resolveAction(inputObj.get("action"));
                    } else if (inputObj.has("next")) {
                        // 对于页面跳转，我们需要跳转到指定的页面ID
                        String nextPageId = inputObj.get("next").getAsString();
                        DebugLogger.debug("在parseInputsArray中创建纯跳转动作，下一页: %s", nextPageId);
                        action = new FunctionDialogueAction((dialogue, optionValue) -> {
                            DebugLogger.debug("纯跳转动作被执行，跳转到: %s", nextPageId);
                            // 从上下文中获取页面索引
                            Integer pageIndex = context.getPageIndex(nextPageId);
                            if (pageIndex != null) {
                                DebugLogger.debug("跳转到页面: %s (索引: %d)", nextPageId, pageIndex);
                                dialogue.setPage(pageIndex);
                            } else {
                                DebugLogger.debugWarn("找不到页面: %s，关闭对话", nextPageId);
                                // 如果找不到页面，则关闭对话
                                dialogue.close();
                            }
                            return null;
                        });
                    }
                    
                    if (action != null) {
                        String value = inputObj.has("value") ? inputObj.get("value").getAsString() : String.valueOf(options.size());
                        // 创建默认的可见性和可选择性谓词（总是返回true）
                        DialoguePredicate alwaysTrue = dialogue -> true;
                        options.add(new DialogueOption(optionText, value, action, alwaysTrue, alwaysTrue));
                    }
                }
            }
        }
        
        if (options.isEmpty()) {
            return new DialogueNoInput();
        }
        
        return new DialogueOptionSetInput(options, null, false);
    }

    /**
     * 解析单个输入元素
     */
    private static DialogueInput parseInputElement(JsonElement inputElement, ConfigContext context) {
        DebugLogger.debug("parseInputElement 被调用");
        
        if (inputElement == null || inputElement.isJsonNull()) {
            DebugLogger.debug("输入元素为空，返回 DialogueNoInput");
            return new DialogueNoInput();
        }
        
        if (inputElement.isJsonPrimitive()) {
            DebugLogger.debug("输入元素是原始类型，解析为: %s", inputElement.getAsString());
            return context.resolveInput(inputElement);
        }
        
        if (inputElement.isJsonObject()) {
            JsonObject inputObj = inputElement.getAsJsonObject();
            String type = inputObj.has("type") ? inputObj.get("type").getAsString() : "none";
            DebugLogger.debug("解析输入对象，类型: %s", type);
            
            switch (type) {
                case "option":
                    String text = inputObj.has("text") ? inputObj.get("text").getAsString() : "继续";
                    DebugLogger.debug("创建选项按钮，文本: %s", text);
                    DialogueText optionText = PlaceholderProcessor.wrapLiteral(text);
                    
                    DialogueAction action = null;
                    if (inputObj.has("action") && inputObj.has("next")) {
                        // 同时存在action和next时，创建一个组合动作
                        String nextPageId = inputObj.get("next").getAsString();
                        DebugLogger.debug("创建组合动作：action + next，下一页: %s", nextPageId);
                        
                        action = new FunctionDialogueAction((dialogue, optionValue) -> {
                            // 从对话对象中获取玩家信息
                            String playerName = dialogue.getPlayerEntity().getGameProfile().getName();
                            DebugLogger.debug("=== 组合动作开始执行 ===");
                            DebugLogger.debug("组合动作被执行，玩家: %s, 选项值: %s", playerName, optionValue);
                            DebugLogger.debug("action 内容: %s", inputObj.get("action").toString());
                            
                            // 先执行命令动作（但不关闭对话框）
                            DebugLogger.debug("=== 开始执行命令动作 ===");
                            context.executeActionWithoutClosing(inputObj.get("action"), dialogue, playerName);
                            DebugLogger.debug("=== 命令动作执行完毕 ===");
                            
                            // 然后跳转到下一页
                            DebugLogger.debug("=== 开始跳转到下一页 ===");
                            DebugLogger.debug("跳转到下一页: %s", nextPageId);
                            Integer pageIndex = context.getPageIndex(nextPageId);
                            if (pageIndex != null) {
                                DebugLogger.debug("跳转到页面: %s (索引: %d)", nextPageId, pageIndex);
                                dialogue.setPage(pageIndex);
                            } else {
                                DebugLogger.debugWarn("找不到页面: %s，关闭对话", nextPageId);
                                dialogue.close();
                            }
                            return null;
                        });
                    } else if (inputObj.has("action")) {
                        DebugLogger.debug("创建纯命令动作");
                        action = context.resolveAction(inputObj.get("action"));
                    } else if (inputObj.has("next")) {
                        // 对于页面跳转，我们需要跳转到指定的页面ID
                        String nextPageId = inputObj.get("next").getAsString();
                        DebugLogger.debug("创建纯跳转动作，下一页: %s", nextPageId);
                        action = new FunctionDialogueAction((dialogue, optionValue) -> {
                            DebugLogger.debug("纯跳转动作被执行，跳转到: %s", nextPageId);
                            // 从上下文中获取页面索引
                            Integer pageIndex = context.getPageIndex(nextPageId);
                            if (pageIndex != null) {
                                DebugLogger.debug("跳转到页面: %s (索引: %d)", nextPageId, pageIndex);
                                dialogue.setPage(pageIndex);
                            } else {
                                DebugLogger.debugWarn("找不到页面: %s，关闭对话", nextPageId);
                                // 如果找不到页面，则关闭对话
                                dialogue.close();
                            }
                            return null;
                        });
                    }
                    
                    if (action != null) {
                        List<DialogueOption> options = new ArrayList<>();
                        String value = inputObj.has("value") ? inputObj.get("value").getAsString() : "0";
                        DebugLogger.debug("创建对话选项，值: %s", value);
                        // 创建默认的可见性和可选择性谓词（总是返回true）
                        DialoguePredicate alwaysTrue = dialogue -> true;
                        options.add(new DialogueOption(optionText, value, action, alwaysTrue, alwaysTrue));
                        DebugLogger.debug("返回 DialogueOptionSetInput，选项数量: %d", options.size());
                        return new DialogueOptionSetInput(options, null, false);
                    }
                    break;
                    
                case "none":
                default:
                    DebugLogger.debug("类型为 none 或未知，返回 DialogueNoInput");
                    return new DialogueNoInput();
            }
        }
        
        DebugLogger.debug("无法解析输入元素，返回 DialogueNoInput");
        return new DialogueNoInput();
    }

    /**
     * 构建器类
     */
    public static class Builder {
        private final List<DialoguePage> pages = new ArrayList<>();
        private final Map<String, DialogueSpeaker> speakers = new HashMap<>();
        private ResourceLocation background = ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/dialogue/default.png");
        private DialogueAction escapeAction = new FunctionDialogueAction((dialogue, optionValue) -> {
            dialogue.close();
            return null;
        });
        private DialogueAction initializationAction = new FunctionDialogueAction((dialogue, optionValue) -> {
            return null;
        });

        public Builder background(ResourceLocation background) {
            this.background = background;
            return this;
        }

        public Builder escapeAction(DialogueAction action) {
            this.escapeAction = action;
            return this;
        }

        public Builder initializationAction(DialogueAction action) {
            this.initializationAction = action;
            return this;
        }

        public Builder speaker(String id, DialogueSpeaker speaker) {
            this.speakers.put(id, speaker);
            return this;
        }

        public PageBuilder page(String id) {
            return new PageBuilder(id);
        }

        public void loadFromJson(JsonObject json, ConfigContext context) {
            // 加载背景
            if (json.has("background")) {
                String bg = json.get("background").getAsString();
                try {
                    this.background = ResourceLocation.parse(bg);
                } catch (Exception e) {
                    // 使用默认背景
                }
            }

            // 加载说话者
            if (json.has("speakers")) {
                JsonObject speakersObj = json.getAsJsonObject("speakers");
                for (Map.Entry<String, JsonElement> entry : speakersObj.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        DialogueSpeaker speaker = context.buildSpeaker(entry.getKey(), entry.getValue().getAsJsonObject());
                        this.speakers.put(entry.getKey(), speaker);
                    }
                }
            }

            // 加载页面
            if (json.has("pages")) {
                JsonArray pagesArray = json.getAsJsonArray("pages");
                int pageIndex = 0;
                for (JsonElement pageElement : pagesArray) {
                    if (pageElement.isJsonObject()) {
                        JsonObject pageObj = pageElement.getAsJsonObject();
                        String pageId = pageObj.has("id") ? pageObj.get("id").getAsString() : UUID.randomUUID().toString();
                        
                        // 注册页面索引
                        context.registerPageIndex(pageId, pageIndex);
                        pageIndex++;
                        
                        PageBuilder pageBuilder = page(pageId);
                        
                        // 加载页面内容
                        if (pageObj.has("speaker")) {
                            pageBuilder.speaker(pageObj.get("speaker").getAsString());
                        }
                        
                        if (pageObj.has("lines")) {
                            JsonArray lines = pageObj.getAsJsonArray("lines");
                            for (JsonElement line : lines) {
                                DialogueText text = parseDialogueText(line, context);
                                if (text != null) {
                                    pageBuilder.line(text);
                                }
                            }
                        }
                        
                        // 处理输入选项
                        if (pageObj.has("inputs")) {
                            JsonArray inputsArray = pageObj.getAsJsonArray("inputs");
                            DialogueInput dialogueInput = parseInputsArray(inputsArray, context);
                            if (dialogueInput != null) {
                                pageBuilder.input(dialogueInput);
                            }
                        }
                        
                        // 处理页面级别的action（当页面关闭时执行）
                        if (pageObj.has("action")) {
                            DialogueAction pageAction = context.resolveAction(pageObj.get("action"));
                            if (pageAction != null) {
                                pageBuilder.escapeAction(pageAction);
                            }
                        }
                        
                        pageBuilder.build();
                    }
                }
            }
        }

        public TrainerDialogue build() {
            return new TrainerDialogue(
                new ArrayList<>(pages),
                background,
                escapeAction,
                new HashMap<>(speakers),
                initializationAction
            );
        }

        /**
         * 页面构建器
         */
        public class PageBuilder {
            private String id;
            private String speaker;
            private DialogueInput input = new DialogueNoInput();
            private ResourceLocation background;
            private DialogueAction escapeAction;
            private final List<DialogueText> lines = new ArrayList<>();
            private final List<Expression> clientActions = new ArrayList<>();

            public PageBuilder(String id) {
                this.id = id;
            }

            public PageBuilder speaker(String speaker) {
                this.speaker = speaker;
                return this;
            }

            public PageBuilder input(DialogueInput input) {
                this.input = input;
                return this;
            }

            public PageBuilder background(ResourceLocation background) {
                this.background = background;
                return this;
            }

            public PageBuilder escapeAction(DialogueAction action) {
                this.escapeAction = action;
                return this;
            }

            public PageBuilder line(String text) {
                this.lines.add(PlaceholderProcessor.wrapLiteral(text));
                return this;
            }

            public PageBuilder line(DialogueText text) {
                this.lines.add(text);
                return this;
            }

            public PageBuilder clientAction(Expression expression) {
                this.clientActions.add(expression);
                return this;
            }

            public DialoguePage build() {
                DialoguePage page = new DialoguePage(
                    id,
                    speaker,
                    lines,
                    input,
                    background,
                    clientActions,
                    escapeAction
                );
                Builder.this.pages.add(page);
                return page;
            }
        }
    }

    /**
     * 配置上下文类
     */
    public static class ConfigContext {
        private final Map<String, DialogueAction> actions = new HashMap<>();
        private final Map<String, DialogueInput> inputs = new HashMap<>();
        private final Map<String, Expression> expressions = new HashMap<>();
        private final Map<String, DialogueSpeaker> speakers = new HashMap<>();
        private final Map<String, MutableComponent> components = new HashMap<>();
        private final Map<String, DialogueText> texts = new HashMap<>();
        private final Map<String, SpeakerFactory> speakerFactories = new HashMap<>();
        private final Map<String, Integer> pageIndexMap = new HashMap<>();

        public ConfigContext() {
            // 注册默认动作
            registerAction("close", new FunctionDialogueAction((dialogue, optionValue) -> {
                dialogue.close();
                return null;
            }));
            registerAction("next_page", new FunctionDialogueAction((dialogue, optionValue) -> {
                dialogue.incrementPage();
                return null;
            }));
            registerAction("noop", new FunctionDialogueAction((dialogue, optionValue) -> {
                return null;
            }));
        }

        public void registerAction(String id, DialogueAction action) {
            actions.put(id, action);
        }

        public void registerInput(String id, DialogueInput input) {
            inputs.put(id, input);
        }

        public void registerExpression(String id, Expression expression) {
            expressions.put(id, expression);
        }

        public void registerSpeaker(String id, DialogueSpeaker speaker) {
            speakers.put(id, speaker);
        }

        public void registerComponent(String id, MutableComponent component) {
            components.put(id, component);
        }

        public void registerText(String id, DialogueText text) {
            texts.put(id, text);
        }

        public void registerSpeakerFactory(String type, SpeakerFactory factory) {
            speakerFactories.put(type, factory);
        }

        public void registerPageIndex(String pageId, int index) {
            pageIndexMap.put(pageId, index);
        }

        public Integer getPageIndex(String pageId) {
            return pageIndexMap.get(pageId);
        }

        public void executeActionWithoutClosing(JsonElement element, ActiveDialogue dialogue, String playerName) {
            DebugLogger.debug("=== executeActionWithoutClosing 开始执行 ===");
            DebugLogger.debug("executeActionWithoutClosing 被调用，玩家: %s", playerName);
            
            if (element == null || element.isJsonNull()) {
                DebugLogger.debugWarn("action 元素为空或null");
                return;
            }
            
            DebugLogger.debug("action 元素内容: %s", element.toString());
            
            if (element.isJsonObject()) {
                JsonObject actionObj = element.getAsJsonObject();
                DebugLogger.debug("action 是 JsonObject，检查字段...");
                DebugLogger.debug("有 type 字段: %s", actionObj.has("type"));
                DebugLogger.debug("有 commands 字段: %s", actionObj.has("commands"));
                
                if (actionObj.has("type") && actionObj.has("commands")) {
                    String type = actionObj.get("type").getAsString();
                    JsonArray commandsArray = actionObj.getAsJsonArray("commands");
                    
                    List<String> commands = new ArrayList<>();
                    for (JsonElement cmdElement : commandsArray) {
                        commands.add(cmdElement.getAsString());
                    }
                    
                    DebugLogger.debug("执行命令（不关闭对话框），类型: %s, 命令数量: %d", type, commands.size());
                    
                    if (playerName != null && !playerName.isEmpty()) {
                        DebugLogger.debug("=== 调用 CommandExecutor.executeCommands ===");
                        CommandExecutor.executeCommands(type, commands, playerName);
                        DebugLogger.debug("=== CommandExecutor.executeCommands 调用完成 ===");
                    } else {
                        DebugLogger.debugWarn("玩家名称为空，跳过命令执行");
                    }
                } else {
                    DebugLogger.debugWarn("action 对象缺少 type 或 commands 字段");
                    if (!actionObj.has("type")) {
                        DebugLogger.debugWarn("缺少 type 字段");
                    }
                    if (!actionObj.has("commands")) {
                        DebugLogger.debugWarn("缺少 commands 字段");
                    }
                }
            } else {
                DebugLogger.debugWarn("action 不是 JsonObject 类型，实际类型: %s", element.getClass().getSimpleName());
            }
            DebugLogger.debug("=== executeActionWithoutClosing 执行完毕 ===");
        }

        public DialogueAction resolveAction(JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return null;
            }
            
            if (element.isJsonPrimitive()) {
                return actions.get(element.getAsString());
            }
            
            if (element.isJsonObject()) {
                JsonObject actionObj = element.getAsJsonObject();
                if (actionObj.has("type") && actionObj.has("commands")) {
                    String type = actionObj.get("type").getAsString();
                    JsonArray commandsArray = actionObj.getAsJsonArray("commands");
                    
                    List<String> commands = new ArrayList<>();
                    for (JsonElement cmdElement : commandsArray) {
                        commands.add(cmdElement.getAsString());
                    }
                    
                    DebugLogger.debug("创建 FunctionDialogueAction，类型: %s, 命令数量: %d", type, commands.size());
                    
                    return new FunctionDialogueAction((dialogue, optionValue) -> {
                        DebugLogger.debug("FunctionDialogueAction 被执行，选项值: %s", optionValue);
                        
                        // 从对话对象中获取玩家信息
                        String playerName = dialogue.getPlayerEntity().getGameProfile().getName();
                        DebugLogger.debug("获取到玩家名称: %s", playerName);
                        
                        if (playerName != null && !playerName.isEmpty()) {
                            DebugLogger.debug("调用 CommandExecutor.executeCommands");
                            CommandExecutor.executeCommands(type, commands, playerName);
                        } else {
                            DebugLogger.debugWarn("玩家名称为空，跳过命令执行");
                        }
                        DebugLogger.debug("关闭对话框");
                        dialogue.close();
                        return null;
                    });
                }
            }
            
            return null;
        }

        public DialogueInput resolveInput(JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return null;
            }
            
            if (element.isJsonPrimitive()) {
                return inputs.get(element.getAsString());
            }
            
            return null;
        }

        public Expression resolveExpression(String id) {
            return expressions.get(id);
        }

        public DialogueText resolveText(String id) {
            return texts.get(id);
        }

        public MutableComponent resolveComponent(String id) {
            return components.get(id);
        }

        public DialogueSpeaker buildSpeaker(String id, JsonObject json) {
            String type = json.has("type") ? json.get("type").getAsString() : "default";
            SpeakerFactory factory = speakerFactories.get(type);
            
            if (factory != null) {
                return factory.create(id, json, this);
            }
            
            // 默认说话者创建逻辑
            String name = json.has("name") ? json.get("name").getAsString() : id;
            return new DialogueSpeaker(PlaceholderProcessor.wrapLiteral(name), null);
        }

        @FunctionalInterface
        public interface SpeakerFactory {
            DialogueSpeaker create(String id, JsonObject json, ConfigContext context);
        }
    }
}