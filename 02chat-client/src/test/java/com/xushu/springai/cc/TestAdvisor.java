package com.xushu.springai.cc;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootTest
public class TestAdvisor {

    // 日志拦截件： logging.level.org.springframework.ai.chat.client.advisor=  DEBUG
    @Test
    public void testLoggerAdvisor(@Autowired
                                ChatClient.Builder chatClientBuilder) {
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        String content = chatClient.prompt()
                .user("你好")
                .call()
                .content();
        System.out.println(content);
    }


    // 敏感词拦截件：
    @Test
    public void testAdvisor(@Autowired
                            ChatClient.Builder chatClientBuilder,
                            @Autowired ChatMemory chatMemory) {
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        new SafeGuardAdvisor(List.of("徐庶")))
                .build();

        String content = chatClient.prompt()
                .user("徐庶帅不帅")
                .call()
                .content();
        System.out.println(content);
    }

    // 敏感词拦截件：
    @Test
    public void testReReadingAdvisor(@Autowired
                            ChatClient.Builder chatClientBuilder) {
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        new ReReadingAdvisor())
                .build();

        String content = chatClient.prompt()
                .user("徐庶帅不帅")
                .call()
                .content();
        System.out.println(content);
    }




}
