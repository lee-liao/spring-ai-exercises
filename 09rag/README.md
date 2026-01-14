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

#### Core RAG Tests
```bash
# Test embedding functionality (Ollama & DashScope)
./mvnw test -Dtest=EmbaddingTest

# Test vector store similarity search
./mvnw test -Dtest=VectorStoreTest

# Test RAG with chat client
./mvnw test -Dtest=ChatClientRagTest
```

#### ELT Pipeline Tests
```bash
# Test document readers (Text, Markdown, PDF)
./mvnw test -Dtest=ReaderTest

# Test text splitters and metadata enrichment
./mvnw test -Dtest=SplitterTest
```

#### Advanced RAG Tests
```bash
# Test reranking for improved retrieval
./mvnw test -Dtest=RerankTest

# Test RAG evaluation metrics
./mvnw test -Dtest=RagEvalTest

# Test fact-checking evaluator
./mvnw test -Dtest=FactCheckingTest
```

### Test Configuration

Tests use `TestConfig.java` which provides:
- `VectorStore` bean with in-memory storage
- `EmbeddingModel` bean (Ollama or DashScope based on configuration)

---

## Test Cases Explained

### 1. **EmbaddingTest** - Embedding Functionality

Tests embedding generation for text vectorization.

**Test Methods:**
- `testEmbadding()` - Tests Ollama embedding model (nomic-embed-text)
- `testAliEmbadding()` - Tests Alibaba DashScope embedding model

**What it demonstrates:**
- Converting text ("我叫徐庶") to vector embeddings
- Embedding dimensions and array structure
- Multi-provider embedding support

---

### 2. **VectorStoreTest** - Vector Storage & Similarity Search

Tests in-memory vector store for document storage and retrieval.

**Test Methods:**
- `testVectorStore()` - Stores documents and performs similarity search

**What it demonstrates:**
- Adding documents to vector store
- Automatic vectorization of document text
- Similarity search with threshold filtering
- Retrieval scoring (e.g., 0.5363 relevance score)

**Example:**
```java
SearchRequest searchRequest = SearchRequest.builder()
    .query("退票")
    .topK(2)
    .similarityThreshold(0.5)
    .build();
```

---

### 3. **ReaderTest** - Document Extraction (ELT - Extract)

Tests various document readers for different file formats.

**Test Methods:**
- `testReaderText()` - Reads plain text files
- `testReaderMD()` - Reads Markdown with custom configuration
- `testReaderPdf()` - Reads PDF page by page
- `testReaderParagraphPdf()` - Reads PDF with paragraph detection using TOC

**What it demonstrates:**
- Reading multiple document formats
- Configuring document parsers
- Adding metadata to documents
- Handling PDF structure (pages vs paragraphs)

**Markdown Configuration Example:**
```java
MarkdownDocumentReaderConfig.builder()
    .withHorizontalRuleCreateDocument(false)
    .withIncludeCodeBlock(false)
    .withIncludeBlockquote(false)
    .withAdditionalMetadata("filename", resource.getFilename())
    .build();
```

---

### 4. **SplitterTest** - Text Splitting & Enrichment (ELT - Transform)

Tests document chunking strategies and metadata enhancement.

**Test Methods:**
- `testTokenTextSplitter()` - Token-based text splitting
- `testChineseTokenTextSplitter()` - Chinese-specific tokenization
- `testKeywordMetadataEnricher()` - AI-powered keyword extraction
- `testSummaryMetadataEnricher()` - AI-powered summarization

**What it demonstrates:**
- Splitting long documents into chunks
- Chinese language tokenization
- Using LLM to extract keywords (5 keywords per document)
- Generating summaries (previous, current, next context)
- Enriching documents with metadata for better retrieval

**Example:**
```java
ChineseTokenTextSplitter splitter = new ChineseTokenTextSplitter(130, 10, 5, 10000, true);
KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 5);
```

---

### 5. **ChatClientRagTest** - RAG with Chat Client

Tests complete RAG pipeline integrated with ChatClient.

**Test Methods:**
- `testRag()` - Basic RAG with QuestionAnswerAdvisor
- `testRag2()` - RAG with similarity threshold tuning
- `testRag3()` - Advanced RAG with multiple enhancements

**What it demonstrates:**
- Building RAG-powered chat applications
- QuestionAnswerAdvisor for context injection
- Vector similarity search with ChatClient
- Advanced RAG features:
  - **Query transformation** (RewriteQueryTransformer)
  - **Translation** (TranslationQueryTransformer - translate to English)
  - **Context handling** (allowEmptyContext)
  - **Document post-processing**

**testRag3 Example:**
```java
RetrievalAugmentationAdvisor.builder()
    .documentRetriever(VectorStoreDocumentRetriever.builder()...)
    .queryAugmenter(ContextualQueryAugmenter.builder()
        .allowEmptyContext(false)
        .emptyContextPromptTemplate(...)...)
    .queryTransformers(
        RewriteQueryTransformer.builder()
            .targetSearchSystem("航空票务助手")...,
        TranslationQueryTransformer.builder()
            .targetLanguage("english")...)
    .build();
```

---

### 6. **RerankTest** - Retrieval Reranking

Tests reranking of retrieved documents for improved relevance.

**Test Methods:**
- `testRerank()` - Reranks top 200 documents using DashScope rerank model

**What it demonstrates:**
- Improving retrieval quality with reranking
- Using `RetrievalRerankAdvisor` with DashScope rerank model
- Retrieving more documents (topK=200) then reranking
- Better relevance for user queries

**Example:**
```java
RetrievalRerankAdvisor advisor = new RetrievalRerankAdvisor(
    vectorStore,
    dashScopeRerankModel,
    SearchRequest.builder().topK(200).build()
);
```

---

### 7. **RagEvalTest** - RAG Quality Evaluation

Tests evaluation metrics for RAG system quality.

**Test Methods:**
- `testRag()` - Evaluates relevancy of RAG responses

**What it demonstrates:**
- Using `RelevancyEvaluator` to assess answer quality
- EvaluationRequest with:
  - Original user query
  - Retrieved context from vector store
  - AI-generated response
- Measuring RAG system performance

**Example:**
```java
EvaluationRequest request = new EvaluationRequest(
    query,                                    // User question
    chatResponse.getMetadata().get(...),     // Retrieved context
    chatResponse.getResult().getOutput()     // AI response
);
RelevancyEvaluator evaluator = new RelevancyEvaluator(...);
EvaluationResponse response = evaluator.evaluate(request);
```

---

### 8. **FactCheckingTest** - Fact Verification

Tests fact-checking and relevancy evaluation of AI responses.

**Test Methods:**
- `testFactChecking()` - Verifies AI responses against source documents
- `testRelevancyEvaluator()` - Tests relevancy scoring

**What it demonstrates:**
- `FactCheckingEvaluator` - Validates responses against facts
- `RelevancyEvaluator` - Scores response relevance to context
- Detecting hallucinations or incorrect information
- Ensuring AI stays grounded in retrieved context

**Example:**
```java
Document doc = Document.builder().text("取消预订: 取消费用：经济舱 75 美元...").build();
String response = "经济舱取消费用75 美元";
EvaluationRequest request = new EvaluationRequest(List.of(doc), response);
FactCheckingEvaluator evaluator = new FactCheckingEvaluator(...);
EvaluationResponse response = evaluator.evaluate(request);
```

---

## Test Organization

```
src/test/java/com/xushu/springai/rag/
├── EmbaddingTest.java              # Embedding generation tests
├── VectorStoreTest.java            # Vector storage and search tests
├── ChatClientRagTest.java          # RAG with ChatClient integration
├── RerankTest.java                 # Reranking tests
├── ELT/                            # Extract-Load-Transform pipeline
│   ├── ReaderTest.java            # Document readers (Text, MD, PDF)
│   ├── SplitterTest.java          # Text splitters & enrichment
│   └── ChineseTokenTextSplitter.java
├── eval/                           # Evaluation & testing
│   ├── RagEvalTest.java           # RAG quality metrics
│   └── FactCheckingTest.java      # Fact verification
└── TestConfig.java                 # Test configuration
```

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
