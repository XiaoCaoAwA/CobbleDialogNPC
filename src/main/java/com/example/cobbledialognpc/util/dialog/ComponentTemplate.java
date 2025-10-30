package com.example.cobbledialognpc.util.dialog;

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue;
import com.cobblemon.mod.common.api.dialogue.DialogueText;
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueText;
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;

/**
 * 组件模板接口，用于创建对话文本
 */
public interface ComponentTemplate {
    DialogueText toDialogueText();

    /**
     * 静态组件模板
     */
    class Static implements ComponentTemplate {
        private final MutableComponent component;

        public Static(MutableComponent component) {
            this.component = component;
        }

        @Override
        public DialogueText toDialogueText() {
            return new WrappedDialogueText(component);
        }
    }

    /**
     * 动态组件模板
     */
    class Dynamic implements ComponentTemplate {
        private final Function<ActiveDialogue, MutableComponent> builder;

        public Dynamic(Function<ActiveDialogue, MutableComponent> builder) {
            this.builder = builder;
        }

        @Override
        public DialogueText toDialogueText() {
            return new FunctionDialogueText(dialogue -> builder.apply(dialogue));
        }
    }

    /**
     * 文本引用模板
     */
    class TextReference implements ComponentTemplate {
        private final DialogueText text;

        public TextReference(DialogueText text) {
            this.text = text;
        }

        @Override
        public DialogueText toDialogueText() {
            return text;
        }
    }
}