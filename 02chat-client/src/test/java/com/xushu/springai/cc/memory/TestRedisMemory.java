package com.xushu.springai.cc.memory;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import com.xushu.springai.cc.ReReadingAdvisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest
public class TestRedisMemory {

    ChatClient chatClient;
    @BeforeEach
    public  void init(@Autowired DashScopeChatModel chatModel,
                      @Autowired ChatMemory chatMemory) {
        chatClient = ChatClient
                .builder(chatModel)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }
    @Test
    public void testChatOptions() {
        String content = chatClient.prompt()
                .user("你好，我叫徐庶！")
                .advisors(new ReReadingAdvisor())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"1"))
                .call()
                .content();
        System.out.println(content);
        System.out.println("--------------------------------------------------------------------------");

        // MQ 异步处理  ----> 存储向量数据库  ---> 相似性检索  

        content = chatClient.prompt()
                .user("我叫什么 ？")
                .advisors(new ReReadingAdvisor())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"1"))
                .call()
                .content();
        System.out.println(content);
    }

    @TestConfiguration
    static class Config {

        @Value("${spring.ai.memory.redis.host}")
        private String redisHost;
        @Value("${spring.ai.memory.redis.port}")
        private int redisPort;
        @Value("${spring.ai.memory.redis.password}")
        private String redisPassword;
        @Value("${spring.ai.memory.redis.timeout}")
        private int redisTimeout;

        @Bean
        public RedisChatMemoryRepository redisChatMemoryRepository() {
            return RedisChatMemoryRepository.builder()
                    .host(redisHost)
                    .port(redisPort)
                    // 若没有设置密码则注释该项
                    // .password(redisPassword)
                    .timeout(redisTimeout)
                    .build();
        }


        @Bean
        ChatMemory chatMemory(RedisChatMemoryRepository chatMemoryRepository) {
            return MessageWindowChatMemory
                    .builder()
                    .maxMessages(10)
                    .chatMemoryRepository(chatMemoryRepository).build();
        }
    }
}
