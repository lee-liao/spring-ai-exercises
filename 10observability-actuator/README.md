# Observability with Actuator

This module demonstrates observability features in Spring AI using Spring Boot Actuator, providing insights into AI chat client operations, metrics, and health status.

## Overview

This project showcases how to integrate Spring Boot Actuator with Spring AI to monitor:
- Chat client invocation metrics
- Request/response logging
- Application health checks
- Performance monitoring

## Prerequisites

- Java 17+
- Maven 3.6+
- One or more AI provider API keys:
  - Ali DashScope (default)
  - Ollama (local)
  - DeepSeek

## Configuration

### 1. API Keys Setup

Set environment variables for your preferred AI provider:

```bash
# Ali DashScope (primary)
export DASHSCOPE_API_KEY=your_dashscope_api_key

# DeepSeek (optional)
export DEEPSEEK_API_KEY=your_deepseek_api_key
```

### 2. Application Properties

The `application.properties` file includes:

```properties
spring.application.name=chat-client

# Ali DashScope Configuration
spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY}
spring.ai.dashscope.embedding.options.model=text-embedding-v4

# Ollama Configuration (local)
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=qwen3:8b
spring.ai.ollama.embedding.model=nomic-embed-text

# DeepSeek Configuration
spring.ai.deepseek.api-key=${DEEPSEEK_API_KEY}
spring.ai.deepseek.chat.options.model=deepseek-reasoner

# Logging Configuration
logging.level.org.springframework.ai=debug
logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=debug
```

### 3. Actuator Endpoints

Spring Boot Actuator provides the following endpoints out of the box:

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Metrics endpoint
- `/actuator/prometheus` - Prometheus metrics (if configured)
- `/actuator/httptrace` - HTTP tracing

## Running the Application

### Start the Application

Set your API key and run:

```bash
# Set API key
$env:DASHSCOPE_API_KEY="your-api-key"

# Start application
mvn spring-boot:run
```

Or run the main application class directly from your IDE.

### Verify Actuator Endpoints

Once running, test the actuator endpoints:

```bash
# Health check
curl http://localhost:8080/actuator/health

# List all available endpoints
curl http://localhost:8080/actuator

# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Multi-Project Setup

This module uses tagged metrics for multi-project observability:

```properties
management.metrics.tags.application=${spring.application.name}
management.metrics.tags.environment=development
management.metrics.tags.project=spring-ai-parent
management.metrics.tags.module=10observability-actuator
```

See `docs/MULTI-PROJECT-GUIDE.md` for details on:
- Using a shared observability stack across multiple projects
- Project labeling best practices
- Grafana dashboard organization
- Configuring external observability tools (Prometheus, Grafana, Jaeger)

## Observability Features

### 1. Chat Client Metrics

Spring AI automatically collects metrics for:
- Chat client invocations
- Token usage (prompt/tokens/completion)
- Request duration
- Success/failure rates

### 2. Request Logging

The configured logger levels provide detailed logging:

```properties
logging.level.org.springframework.ai=debug
logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=debug
```

This enables you to see:
- Request prompts
- AI responses
- Advisor chain execution
- Token counts

### 3. Actuator Metrics Examples

Query specific metrics:

```bash
# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Custom Spring AI metrics (if configured)
curl http://localhost:8080/actuator/metrics/spring.ai.chat.client
```

## Advanced Configuration

### Enable Prometheus Metrics

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Then access Prometheus metrics at:
```bash
curl http://localhost:8080/actuator/prometheus
```

### Custom Actuator Endpoints

You can expose additional endpoints in `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,httptrace
```

### Configure Micrometer Observations

For advanced observability, configure Micrometer:

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0
```

## Testing Observability

### 1. Make a Chat Request

Create a simple test request to generate metrics:

```bash
curl "http://localhost:8080/chat?message=Hello%20AI"
```

### 2. View Metrics

Check metrics after making requests:

```bash
curl http://localhost:8080/actuator/metrics/spring.ai.chat.client
```

### 3. Monitor Logs

Watch the application logs for detailed advisor chain execution:

```bash
# The debug logging will show:
# - Request prompts
# - Advisor chain processing
# - AI responses
# - Token usage
```

## Monitoring Integration

### Prometheus + Grafana

1. Scrape metrics from `/actuator/prometheus`
2. Create dashboards for:
   - Request latency
   - Token usage trends
   - Error rates
   - Model-specific metrics

### Other Observability Tools

- **Zipkin/Jaeger**: For distributed tracing
- **ELK Stack**: For log aggregation
- **Wavefront/ Datadog**: For cloud monitoring

## Troubleshooting

### Actuator Endpoints Not Accessible

Check if actuator is included in dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Metrics Not Showing

Verify management endpoints configuration:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

### Logs Too Verbose

Adjust logging levels:

```properties
logging.level.org.springframework.ai=info
logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=info
```

## Learning Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Spring AI Observability](https://docs.spring.io/spring-ai/reference/)
- [Micrometer Documentation](https://micrometer.io/docs)

## Next Steps

1. Integrate with your preferred monitoring system
2. Create custom metrics for business logic
3. Set up alerting based on metrics thresholds
4. Configure distributed tracing for microservices
