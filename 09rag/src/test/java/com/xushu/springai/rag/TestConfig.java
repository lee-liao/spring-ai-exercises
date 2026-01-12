package com.xushu.springai.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@TestConfiguration
public class TestConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) throws IOException {
        // Create a temporary file-based vector store for testing
        Path tempFile = Files.createTempFile("vector-store-test", ".json");
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        return vectorStore;
    }
}
