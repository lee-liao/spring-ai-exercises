package com.xushu.springai.cc.memory;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest
public class TestMemory {

    @Test
    public void testMemory(@Autowired
                               DashScopeChatModel chatModel) {
        ChatClient chatClient = ChatClient
                .builder(chatModel)
                .build();

        String chatHis="我叫徐庶";
        String content = chatClient.prompt()
                .user(chatHis)
                .call()
                .content();
        System.out.println(content);
        System.out.println("--------------------------------------------------------------------------");
        chatHis+=content;
        chatHis+="我叫什么 ？";
        content = chatClient.prompt()
                .user(chatHis)
                .call()
                .content();
        System.out.println(content);
    }

    @Test
    public void testMemory2(@Autowired
                                DashScopeChatModel chatModel) {

        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        String conversationId = "xs001";   // 当前对话唯一标识

        // First interaction
        UserMessage userMessage1 = new UserMessage("我叫徐庶");
        chatMemory.add(conversationId, userMessage1);
        ChatResponse response1 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
        chatMemory.add(conversationId, response1.getResult().getOutput());

        // Second interaction
        UserMessage userMessage2 = new UserMessage("我叫什么?");
        chatMemory.add(conversationId, userMessage2);
        ChatResponse response2 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
        chatMemory.add(conversationId, response2.getResult().getOutput());
        System.out.println(response2.getResult().getOutput().getText());
    }



    ChatClient chatClient;

    @BeforeEach
    public void init(@Autowired ChatClient.Builder builder,
                     @Autowired ChatMemory chatMemory) {
        chatClient = builder
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }


    @Test
    public void testMemoryAdvisor(
                                  @Autowired ChatMemory chatMemory) {
        String content = chatClient.prompt()
                .user("我叫徐庶" )
                .call()
                .content();
        System.out.println(content);
        System.out.println("--------------------------------------------------------------------------");

        content = chatClient.prompt()
                .user("我叫什么 ？")
                .call()
                .content();
        System.out.println(content);
    }

    @TestConfiguration
    static class Config {

        @Bean
        ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
            return MessageWindowChatMemory
                    .builder()
                    .maxMessages(1)
                    .chatMemoryRepository(chatMemoryRepository).build();
        }

    }


    @Test
    public void testChatOptions() {
        String content = chatClient.prompt()
                .user("我叫徐庶 ？")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"1"))
                .call()
                .content();
        System.out.println(content);
        System.out.println("--------------------------------------------------------------------------");

        content = chatClient.prompt()
                .user("我叫什么 ？")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"1"))
                .call()
                .content();
        System.out.println(content);


        System.out.println("--------------------------------------------------------------------------");

        content = chatClient.prompt()
                .user("我叫什么 ？")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"2"))
                .call()
                .content();
        System.out.println(content);
    }

}
