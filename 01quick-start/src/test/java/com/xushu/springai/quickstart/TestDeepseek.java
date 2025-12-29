package com.xushu.springai.quickstart;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestDeepseek {
    @Test
    public void testDeepseek(@Autowired DeepSeekChatModel deepSeekChatModel) {
        String content = deepSeekChatModel.call("你好你是谁");
        System.out.println(content);
    }


    @Test
    public void testDeepseekStream(@Autowired DeepSeekChatModel deepSeekChatModel) {
        Flux<String> stream = deepSeekChatModel.stream("你好你是谁");
        stream.toIterable().forEach(System.out::println);
    }


    @Test
    public void testDeepseekReasoning(@Autowired DeepSeekChatModel deepSeekChatModel) {

        Prompt prompt = new Prompt("你好你是谁");
        ChatResponse response = deepSeekChatModel.call(prompt);

        DeepSeekAssistantMessage assistantMessage=  (DeepSeekAssistantMessage)response.getResult().getOutput();

        System.out.println(assistantMessage.getReasoningContent());
        System.out.println("-----------------------------------------");
        System.out.println(assistantMessage.getText());
    }


    @Test
    public void testDeepseekStreamReasoning(@Autowired DeepSeekChatModel deepSeekChatModel) {
        Flux<ChatResponse> stream = deepSeekChatModel.stream(new Prompt("你好你是谁"));
        stream.toIterable().forEach(chatResponse -> {
            DeepSeekAssistantMessage assistantMessage=  (DeepSeekAssistantMessage)chatResponse.getResult().getOutput();

            System.out.println(assistantMessage.getReasoningContent());
        });

        System.out.println("-----------------------------------------");
        stream.toIterable().forEach(chatResponse -> {
            DeepSeekAssistantMessage assistantMessage=  (DeepSeekAssistantMessage)chatResponse.getResult().getOutput();
            System.out.println(assistantMessage.getText());
        });
    }

    @Test
    public void testChatOptions(@Autowired
                                DeepSeekChatModel chatModel) {
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model("deepseek-chat")
                //.maxTokens(5)   // 字数
                .stop(Arrays.asList("，"))
                .temperature(2.0).build();
        Prompt prompt = new Prompt("请写一句诗描述清晨。", options);
        ChatResponse res = chatModel.call(prompt);
        System.out.println(res.getResult().getOutput().getText());
    }

}
