# 09rag - Spring AI RAG Application

Retrieval-Augmented Generation (RAG) application using Spring AI with support for multiple AI models.

## Features

- **Multiple AI Providers**:
  - Alibaba DashScope (百炼) - Cloud-based LLM and embedding
  - Ollama - Local LLM and embedding models
- **Document Processing**:
  - Markdown document reader
  - PDF document reader
- **Vector Store**: Support for vector databases and embeddings
- **RAG Pipeline**: Document ingestion, embedding generation, and retrieval

## Prerequisites

### Option 1: Using Alibaba DashScope (Recommended)

1. Get API key from [Alibaba Cloud DashScope](https://dashscope.aliyun.com/)
2. Set environment variable:
   ```bash
   # Windows PowerShell
   $env:ALI_AI_KEY="your-dashscope-api-key"

   # Linux/Mac
   export ALI_AI_KEY="your-dashscope-api-key"
   ```

### Option 2: Using Ollama (Local)

1. Install [Ollama](https://ollama.ai/)
2. Pull required models:
   ```bash
   ollama pull qwen3:4b
   ollama pull nomic-embed-text
   ```
3. Start Ollama service:
   ```bash
   ollama serve
   ```
4. Verify it's running:
   ```bash
   curl http://localhost:11434/api/tags
   ```

## Quick Start

### 1. Build the Project

```bash
cd 09rag
./mvnw clean install
```

Or on Windows:
```bash
cd 09rag
mvnw.cmd clean install
```

### 2. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# For Alibaba DashScope (cloud)
spring.ai.dashscope.api-key=${ALI_AI_KEY}
spring.ai.dashscope.embedding.options.model=text-embedding-v4

# For Ollama (local)
spring.ai.ollama.chat.model=qwen3:4b
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.model=nomic-embed-text

# Logging
logging.level.org.springframework.ai=debug
```

### 3. Run the Application

```bash
# Using Maven
./mvnw spring-boot:run

# Or run JAR directly
java -jar target/09rag-0.0.1-xs.jar
```

### 4. Verify Application is Running

The application will start on default port `8080`. Check logs for:
```
Started Application in X.XXX seconds
```

## Project Structure

```
09rag/
├── src/main/java/com/xushu/springai/rag/
│   └── Application.java          # Spring Boot main class
├── src/main/resources/
│   └── application.properties     # Configuration
└── pom.xml                        # Maven dependencies
```

## Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-ai-alibaba-starter-dashscope` | Alibaba DashScope AI integration |
| `spring-ai-starter-model-ollama` | Ollama local model integration |
| `spring-ai-advisors-vector-store` | Vector store and RAG support |
| `spring-ai-markdown-document-reader` | Markdown document parsing |
| `spring-ai-pdf-document-reader` | PDF document parsing |

## Usage Examples

### RAG with Document Upload

Once running, you can:
1. Upload documents (Markdown/PDF)
2. Documents are automatically chunked and embedded
3. Query the documents using natural language
4. Retrieve relevant context and generate answers

### Example API Calls

```bash
# Health check
curl http://localhost:8080/actuator/health

# Upload and query documents (endpoints depend on implementation)
```

## Running Tests

### Prerequisites for Tests

Make sure Ollama models are available:
```bash
# Pull required models
ollama pull nomic-embed-text
ollama pull qwen3:8b

# Or use qwen3:4b if you prefer smaller models
```

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
# Test embedding functionality
./mvnw test -Dtest=EmbaddingTest

# Test vector store
./mvnw test -Dtest=VectorStoreTest

# Test RAG with chat client
./mvnw test -Dtest=ChatClientRagTest
```

### Test Configuration

Tests use `TestConfig.java` which provides:
- `VectorStore` bean with in-memory storage
- `EmbeddingModel` bean (Ollama or DashScope based on configuration)

## Configuration Options

### Alibaba DashScope

```properties
spring.ai.dashscope.api-key=your-api-key
spring.ai.dashscope.chat.options.model=qwen-max
spring.ai.dashscope.embedding.options.model=text-embedding-v4
```

### Ollama

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=qwen3:4b
spring.ai.ollama.embedding.model=nomic-embed-text
```

## Troubleshooting

### Issue: Ollama Connection Refused

**Solution**: Make sure Ollama is running:
```bash
ollama serve
```

### Issue: API Key Not Found

**Solution**: Set environment variable before running:
```bash
export ALI_AI_KEY="your-key"
```

### Issue: Model Not Found

**Solution**: Pull the required Ollama model:
```bash
ollama pull qwen3:4b
ollama pull nomic-embed-text
```

## Learning Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Alibaba DashScope](https://dashscope.aliyun.com/)
- [Ollama Documentation](https://github.com/ollama/ollama)

## Support

For questions or issues, contact: 程序员徐庶 (WeChat)
