# Spring AI Agent - Design Patterns Demo

This module demonstrates **4 classical AI Agent design patterns** implemented with Spring AI 1.0 and Alibaba DashScope/DeepSeek models.

## 📋 Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Pattern 1: Chain Workflow](#pattern-1-chain-workflow)
- [Pattern 2: Orchestrator-Workers](#pattern-2-orchestrator-workers)
- [Pattern 3: Evaluator-Optimizer](#pattern-3-evaluator-optimizer)
- [Pattern 4: Parallelization Workflow](#pattern-4-parallelization-workflow)
- [Architecture](#architecture)
- [Configuration](#configuration)

---

## Overview

This project showcases **4 fundamental AI Agent patterns**:

| Pattern | Description | Use Case |
|---------|-------------|----------|
| **Chain Workflow** | Sequential processing with gate logic | Multi-step business workflows |
| **Orchestrator-Workers** | Task decomposition & parallel execution | Complex project planning |
| **Evaluator-Optimizer** | Iterative refinement with feedback | Code generation & optimization |
| **Parallelization Workflow** | Parallel processing with aggregation | Multi-department risk analysis |

All patterns use:
- **Spring AI 1.0** with ChatClient API
- **Alibaba DashScope** (通义千问) models
- **DeepSeek** models (optional)
- **Chinese-optimized prompts** for better results

---

## Prerequisites

### 1. Java 17+

```bash
java -version
```

### 2. Maven 3.8+

```bash
mvn -version
```

### 3. API Keys

**Alibaba DashScope API Key** (Required):

```bash
# Get your key from: https://dashscope.aliyun.com/
export DASHSCOPE_API_KEY="your-dashscope-api-key"
```

**DeepSeek API Key** (Optional):

```bash
# Get your key from: https://platform.deepseek.com/
export DEEP_SEEK_KEY="your-deepseek-api-key"
```

---

## Quick Start

### 1. Build the Project

```bash
cd 11spring-ai-agent
mvn clean install
```

### 2. Run Specific Pattern

**Option A: Maven Exec Plugin (Recommended - Cross-Platform)**

```bash
# Pattern 1: Chain Workflow
mvn exec:java -Dexec.mainClass="com.xs.agent.chain_workflow.Application"

# Pattern 2: Orchestrator-Workers
mvn exec:java -Dexec.mainClass="com.xs.agent.orchestrator_workers.Application"

# Pattern 3: Evaluator-Optimizer
mvn exec:java -Dexec.mainClass="com.xs.agent.evaluator_optimizer.Application"

# Pattern 4: Parallelization Workflow
mvn exec:java -Dexec.mainClass="com.xs.agent.parallelization_worflow.Application"
```

**Option B: Spring Boot Maven Plugin**

```bash
# Pattern 1: Chain Workflow
mvn spring-boot:run -Dspring-boot.run.main-class=com.xs.agent.chain_workflow.Application

# Pattern 2: Orchestrator-Workers
mvn spring-boot:run -Dspring-boot.run.main-class=com.xs.agent.orchestrator_workers.Application

# Pattern 3: Evaluator-Optimizer
mvn spring-boot:run -Dspring-boot.run.main-class=com.xs.agent.evaluator_optimizer.Application

# Pattern 4: Parallelization Workflow
mvn spring-boot:run -Dspring-boot.run.main-class=com.xs.agent.parallelization_worflow.Application
```

**Option C: Direct Java Execution**

```bash
# Build first
mvn clean package

# Pattern 1: Chain Workflow
java -jar target/spring-ai-agent-0.0.1-SNAPSHOT.jar --spring.main.main-class=com.xs.agent.chain_workflow.Application
```

### 3. Verify Output

Each pattern prints detailed execution logs to console.

---

## Pattern 1: Chain Workflow

### 📌 Concept

Sequential processing through multiple stages, with **gate logic** to terminate early if criteria aren't met.

### 🏗️ Architecture

```
Input → Step 1 → Gate → Step 2 → Step 3 → Step 4 → Output
                     ↓ (FAIL)
                   Stop
```

### 📂 Implementation

- **File**: `src/main/java/com/xs/agent/chain_workflow/PracticalChainWorkflow.java`
- **Main Class**: `com.xs.agent.chain_workflow.Application`

### 🔍 Use Case

**E-commerce Platform Upgrade Project**:

The workflow processes an order system upgrade request through 4 stages:

1. **Requirement Analysis** - Analyze business goals, features, risks
2. **Gate Check** - If unfeasible, stop immediately (returns "FAIL")
3. **Architecture Design** - System architecture, tech stack, database design
4. **Implementation Plan** - Development phases, team allocation, timeline
5. **Delivery Checklist** - Acceptance criteria, deployment checklist

### 🚀 Test It

```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.xs.agent.chain_workflow.Application
```

**Expected Output**:

```
=== 开始项目全流程处理 ===
步骤1: 业务需求分析
需求分析完成: [Detailed requirement analysis...]
步骤2: 系统架构设计
架构设计完成: [Architecture design...]
步骤3: 项目实施规划
实施计划完成: [Implementation plan...]
步骤4: 交付清单制定
交付清单完成: [Delivery checklist...]
=== 项目全流程处理完成 ===
```

### 💡 Key Features

- **Gate Logic**: Stops early if requirement is unfeasible
- **Context Passing**: Each step receives previous step's output
- **Practical Prompts**: Real-world business scenario prompts

---

## Pattern 2: Orchestrator-Workers

### 📌 Concept

**Orchestrator** decomposes complex tasks → **Workers** execute specialized sub-tasks in parallel.

### 🏗️ Architecture

```
User Task
    ↓
Orchestrator (Task Decomposition)
    ↓
┌───────────┬───────────┬───────────┐
│ Worker 1  │ Worker 2  │ Worker 3  │  (Parallel)
│ (Backend) │ (Frontend)│ (Database)│
└───────────┴───────────┴───────────┘
    ↓
Aggregate Results
```

### 📂 Implementation

- **File**: `src/main/java/com/xs/agent/orchestrator_workers/SimpleOrchestratorWorkers.java`
- **Main Class**: `com.xs.agent.orchestrator_workers.Application`

### 🔍 Use Case

**Enterprise Attendance System Design**:

The Orchestrator decomposes the task into specialized sub-tasks:

1. **Backend API Development** - RESTful APIs, validation, error handling
2. **Frontend UI Development** - Responsive interface, API integration
3. **Database Design** - Table structure, SQL scripts, indexing

Each worker is a domain expert providing detailed solutions.

### 🚀 Test It

```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.xs.agent.orchestrator_workers.Application
```

**Expected Output**:

```
=== 开始处理任务 ===
编排器分析: [Task complexity analysis...]
子任务列表: [Task list...]
-----------------------------------处理子任务: 后端API开发--------------------------------
[Detailed backend solution...]
-----------------------------------处理子任务: 前端界面开发--------------------------------
[Detailed frontend solution...]
-----------------------------------处理子任务: 数据库设计--------------------------------
[Detailed database solution...]
=== 所有工作者完成任务 ===
```

### 💡 Key Features

- **JSON-based Task Decomposition**: Structured task breakdown
- **Domain Expert Workers**: Each worker specializes in one area
- **Parallel Execution**: Workers process independently
- **Real-world Scenario**: Enterprise system design

---

## Pattern 3: Evaluator-Optimizer

### 📌 Concept

**Generator** produces output → **Evaluator** assesses quality → Loop until PASS.

### 🏗️ Architecture

```
Task
  ↓
Generator (Create Solution)
  ↓
Evaluator (Assess Quality)
  ↓
┌─────────────────┐
│ PASS? → Output  │
│ NO → Feedback   │
└─────────────────┘
      ↓ (Feedback)
Generator (Improve)
  ↓
  Loop...
```

### 📂 Implementation

- **File**: `src/main/java/com/xs/agent/evaluator_optimizer/SimpleEvaluatorOptimizer.java`
- **Main Class**: `com.xs.agent.evaluator_optimizer.Application`

### 🔍 Use Case

**Java Code Generation - List to Map Conversion**:

**Task**: "Efficiently convert 10,000 `List<User>` to `Map<id, User>` without Stream API"

**Generator**: Creates initial Java implementation
**Evaluator**: Strictly assesses:
- Code efficiency (low-level performance)
- No repeated resizing/reallocation
- Returns PASS/NEEDS_IMPROVEMENT/FAIL with detailed feedback

**Loop**: Continues until code meets strict standards.

### 🚀 Test It

```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.xs.agent.evaluator_optimizer.Application
```

**Expected Output**:

```
=== 第1轮迭代 ===
生成结果: [Initial Java code...]
评估结果: NEEDS_IMPROVEMENT
反馈: [Detailed feedback on efficiency...]

=== 第2轮迭代 ===
生成结果: [Improved Java code...]
评估结果: NEEDS_IMPROVEMENT
反馈: [More specific feedback...]

=== 第3轮迭代 ===
生成结果: [Optimized Java code...]
评估结果: PASS
代码通过评估！
```

### 💡 Key Features

- **Recursive Loop**: Continues until evaluation passes
- **Context Preservation**: Feedback accumulates across iterations
- **Strict Evaluation**: High quality standards enforced
- **JSON-based Entities**: Structured generation and evaluation

---

## Pattern 4: Parallelization Workflow

### 📌 Concept

Process multiple inputs in parallel → **Aggregator** synthesizes results.

### 🏗️ Architecture

```
Input List
    ↓
┌─────────┬─────────┬─────────┐
│ Task 1  │ Task 2  │ Task 3  │  (Parallel)
│ (IT)    │ (Sales) │ (HR)    │
└─────────┴─────────┴─────────┘
    ↓
Aggregator (Synthesize)
    ↓
Final Report
```

### 📂 Implementation

- **File**: `src/main/java/com/xs/agent/parallelization_worflow/ParallelizationWorkflowWithAggregator.java`
- **Main Class**: `com.xs.agent.parallelization_worflow.Application`

### 🔍 Use Case

**Multi-Department Digital Transformation Risk Assessment**:

Four departments are analyzed in parallel:

1. **IT Department** - Technology risks, budget constraints, skill gaps
2. **Sales Department** - CRM adoption, customer relationship concerns, resistance to change
3. **Finance Department** - Data security, cloud storage concerns, complex processes
4. **HR Department** - Digital recruitment, lack of technical staff, time constraints

**Aggregator** produces:
- Comprehensive summary
- Common trends and patterns
- Key differences comparison
- Overall conclusions and recommendations

### 🚀 Test It

```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.xs.agent.parallelization_worflow.Application
```

**Expected Output**:

```
=== 并行分析 + 聚合处理 ===

=== 各部门独立分析结果 ===
部门1:
[IT department risk analysis...]

部门2:
[Sales department risk analysis...]

部门3:
[Finance department risk analysis...]

部门4:
[HR department risk analysis...]

=== 聚合器综合报告 ===
[Aggregated comprehensive report with trends, comparisons, recommendations...]
```

### 💡 Key Features

- **CompletableFuture**: True parallel processing with thread pool
- **Aggregator Pattern**: Synthesizes multiple LLM outputs
- **Chinese Prompts**: Optimized for Chinese business context
- **Structured Output**: Individual + aggregated results

---

## Architecture

### Project Structure

```
11spring-ai-agent/
├── src/main/java/com/xs/agent/
│   ├── config/
│   │   └── RestClientConfig.java              # HTTP client config
│   ├── chain_workflow/
│   │   ├── Application.java                   # Pattern 1 main
│   │   └── PracticalChainWorkflow.java        # Chain workflow impl
│   ├── orchestrator_workers/
│   │   ├── Application.java                   # Pattern 2 main
│   │   └── SimpleOrchestratorWorkers.java     # Orchestrator-Workers impl
│   ├── evaluator_optimizer/
│   │   ├── Application.java                   # Pattern 3 main
│   │   └── SimpleEvaluatorOptimizer.java      # Evaluator-Optimizer impl
│   └── parallelization_worflow/
│       ├── Application.java                   # Pattern 4 main
│       └── ParallelizationWorkflowWithAggregator.java  # Parallel + Aggregator
├── src/main/resources/
│   └── application.yml                        # API keys configuration
├── pom.xml                                    # Maven dependencies
└── README.md                                  # This file
```

### Technology Stack

- **Spring Boot 3.x**
- **Spring AI 1.0** (ChatClient API)
- **Alibaba DashScope** (通义千问 models)
- **DeepSeek** (optional, via `spring-ai-starter-model-deepseek`)
- **Maven** for dependency management
- **Java 17+**

---

## Configuration

### application.yml

```yaml
spring:
  ai:
    # DeepSeek configuration (optional)
    deepseek:
      api-key: ${DEEP_SEEK_KEY}
      chat:
        options:
          model: deepseek-chat

    # Alibaba DashScope (main model used)
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}  # Set via environment variable
      chat:
        options:
          model: qwen-max
```

### Environment Variables

```bash
# Required for Alibaba DashScope
export DASHSCOPE_API_KEY="sk-..."

# Optional for DeepSeek
export DEEP_SEEK_KEY="sk-..."
```

---

## Testing All Patterns

### Quick Test Script (Bash)

```bash
#!/bin/bash
# test-all-patterns.sh

echo "=== Testing Pattern 1: Chain Workflow ==="
mvn exec:java -Dexec.mainClass="com.xs.agent.chain_workflow.Application"

echo -e "\n=== Testing Pattern 2: Orchestrator-Workers ==="
mvn exec:java -Dexec.mainClass="com.xs.agent.orchestrator_workers.Application"

echo -e "\n=== Testing Pattern 3: Evaluator-Optimizer ==="
mvn exec:java -Dexec.mainClass="com.xs.agent.evaluator_optimizer.Application"

echo -e "\n=== Testing Pattern 4: Parallelization Workflow ==="
mvn exec:java -Dexec.mainClass="com.xs.agent.parallelization_worflow.Application"
```

### PowerShell Test Script

```powershell
# test-all-patterns.ps1

Write-Host "=== Testing Pattern 1: Chain Workflow ===" -ForegroundColor Green
mvn exec:java -Dexec.mainClass="com.xs.agent.chain_workflow.Application"

Write-Host "`n=== Testing Pattern 2: Orchestrator-Workers ===" -ForegroundColor Green
mvn exec:java -Dexec.mainClass="com.xs.agent.orchestrator_workers.Application"

Write-Host "`n=== Testing Pattern 3: Evaluator-Optimizer ===" -ForegroundColor Green
mvn exec:java -Dexec.mainClass="com.xs.agent.evaluator_optimizer.Application"

Write-Host "`n=== Testing Pattern 4: Parallelization Workflow ===" -ForegroundColor Green
mvn exec:java -Dexec.mainClass="com.xs.agent.parallelization_worflow.Application"
```

### Interactive Testing

Run each pattern individually and observe:

1. **Console Output**: All patterns print detailed logs
2. **LLM Responses**: Raw AI model responses displayed
3. **Pattern Behavior**: Observe how each pattern handles the task

---

## Design Principles

### SOLID Principles Applied

1. **Single Responsibility Principle (SRP)**
   - Each pattern class handles one workflow type
   - Separate `Application.java` for each pattern

2. **Open/Closed Principle (OCP)**
   - Patterns are extensible without modification
   - Prompts are externalized in constants

3. **Dependency Inversion Principle (DIP)**
   - Depends on `ChatClient` abstraction, not concrete implementations
   - Easy to swap DashScope/DeepSeek models

### KISS (Keep It Simple, Stupid)

- Clean, straightforward implementations
- No unnecessary abstractions
- Console output for easy debugging

### DRY (Don't Repeat Yourself)

- Shared `RestClientConfig` across patterns
- Consistent prompt structure
- Reusable record classes for data transfer

---

## Common Issues & Troubleshooting

### Issue 1: API Key Not Found

**Error**: `DashScope API key must be set`

**Solution**:
```bash
export DASHSCOPE_API_KEY="your-key-here"
```

### Issue 2: Wrong Main Class

**Error**: `Could not find or load main class`

**Solution**: Use the fully qualified main class name:
```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.xs.agent.chain_workflow.Application
```

### Issue 3: Port Already in Use

**Error**: `Port 8080 was already in use`

**Solution**: Change port in `application.yml` or stop the conflicting process.

### Issue 4: Slow Responses

**Cause**: LLM API calls can take 5-30 seconds per request.

**Solutions**:
- Use faster models (e.g., `qwen-turbo` instead of `qwen-max`)
- Check network connectivity to DashScope API
- Reduce prompt complexity

---

## Advanced Usage

### Customizing Prompts

Edit the prompt constants in each pattern's Java file:

```java
private static final String CUSTOM_PROMPT = """
    Your custom prompt here...
    """;
```

### Switching Models

Change the model in `application.yml` or programmatically:

```java
// Use DashScope model
var chatClient = ChatClient.create(dashScopeChatModel);

// Use DeepSeek model
var chatClient = ChatClient.create(deepSeekChatModel);
```

### Adding New Patterns

1. Create new package under `com.xs.agent`
2. Implement pattern class extending or using `ChatClient`
3. Create `Application.java` with `@SpringBootApplication`
4. Test with `mvn spring-boot:run`

---

## References

- [Spring AI Official Documentation](https://docs.spring.io/spring-ai/reference/)
- [Alibaba DashScope Documentation](https://dashscope.aliyun.com/doc)
- [DeepSeek Platform](https://platform.deepseek.com/)
- [LangChain Agent Patterns](https://python.langchain.com/docs/modules/agents/)

---

## Summary

This module provides **production-ready examples** of 4 classical AI Agent patterns:

1. ✅ **Chain Workflow** - Sequential processing with gates
2. ✅ **Orchestrator-Workers** - Task decomposition & parallel execution
3. ✅ **Evaluator-Optimizer** - Iterative refinement loop
4. ✅ **Parallelization Workflow** - Parallel processing with aggregation

Each pattern is:
- ✅ **Fully functional** - Run with single Maven command
- ✅ **Real-world scenario** - Practical business use cases
- ✅ **Chinese-optimized** - Prompts tuned for Chinese language
- ✅ **Extensible** - Easy to customize and extend

**Start exploring AI Agent patterns today!**
