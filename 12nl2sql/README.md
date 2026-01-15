# NL2SQL Service - Natural Language to SQL

## Status: ✅ FULLY WORKING

**12nl2sql is a Spring Boot application** with working Natural Language to SQL conversion using Alibaba DashScope AI.

## What's Working

### ✅ Application Status
- **Multi-module Project**: Part of spring-ai-parent, inheriting dependency management
- **Clean Startup**: No OpenAI auto-configuration conflicts (removed problematic NL2SQL starter)
- **Database Connected**: Successfully connected to PostgreSQL at `localhost:5434/kb`
- **17 Tables Discovered**: Schema introspection working
- **NL2SQL Feature**: Natural language to SQL conversion using DashScope ChatModel

### ✅ REST Endpoints (Base URL: `http://localhost:8081/api/nl2sql`)

| Endpoint | Method | Description | Status |
|----------|--------|-------------|--------|
| `/health` | GET | Health check | ✅ Working |
| `/schema` | GET | Get all tables | ✅ Working |
| `/schema/{tableName}` | GET | Get table structure | ✅ Working |
| `/query` | POST/GET | Natural language to SQL | ✅ Working |
| `/execute` | POST | Execute raw SQL | ✅ Working |

#### Example Usage:

```bash
# Health check
curl http://localhost:8081/api/nl2sql/health
# Response: {"service":"NL2SQL","status":"UP"}

# Get schema
curl http://localhost:8081/api/nl2sql/schema
# Returns: 17 tables including users, roles, permissions, jobs, etc.

# Get table structure
curl http://localhost:8081/api/nl2sql/schema/users
# Returns: Column definitions with data types

# Execute raw SQL
curl -X POST http://localhost:8081/api/nl2sql/execute \
  -H "Content-Type: application/json" \
  -d '{"sql":"SELECT * FROM users LIMIT 10"}'
# Returns: Query results

# Natural language to SQL query
curl -X POST http://localhost:8081/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"query":"查询所有用户"}'
# Converts natural language to SQL and executes it
# Returns: Generated SQL + query results
```

## Architecture

### NL2SQL Implementation

The project uses **custom prompt-based NL2SQL** with Spring AI ChatModel:

```
Natural Language Query
    ↓
QueryService.fetchSchema() - Get database schema
    ↓
Build Prompt with Schema Context
    ↓
DashScope ChatModel (通义千问)
    ↓
Extract SQL from Response
    ↓
Execute SQL via JdbcTemplate
    ↓
Return Results
```

### Current Stack:
- **Spring Boot 3.2.5** - Application framework
- **Spring AI Alibaba DashScope** - ChatModel for NL2SQL (通义千问)
- **Spring Web** - REST API
- **Spring JDBC** - Database access
- **PostgreSQL Driver** - Database connectivity
- **Lombok** - Reduce boilerplate
- **Actuator** - Health checks and metrics

### How NL2SQL Works

1. **Schema Discovery**: Automatically fetches all tables and column information
2. **Prompt Engineering**: Builds a system prompt with complete schema context
3. **AI Generation**: Uses DashScope ChatModel to convert natural language to SQL
4. **SQL Extraction**: Parses the AI response to extract valid SQL
5. **Execution**: Runs the generated SQL and returns results

### Key Features

- ✅ **No OpenAI Conflicts**: Removed problematic `spring-ai-alibaba-starter-nl2sql` dependency
- ✅ **Clean Dependencies**: Uses only `spring-ai-alibaba-starter-dashscope`
- ✅ **Generic ChatModel**: Works with any Spring AI compatible model
- ✅ **PostgreSQL Syntax**: Optimized for PostgreSQL queries
- ✅ **Schema Awareness**: AI has full knowledge of database structure

## Database Schema

Your `kb` database has 17 tables:

**Core Tables**:
- `users` - User accounts
- `roles` - Role definitions
- `permissions` - Permission system
- `user_roles`, `role_permissions` - Many-to-many relationships

**Knowledge Base**:
- `kb_collections` - Document collections
- `kb_documents` - Documents
- `kb_chunks` - Document chunks
- `kb_document_versions` - Version history

**Writing System**:
- `writing_tasks` - Writing tasks
- `writing_styles` - Style templates
- `writing_outputs` - Generated content
- `writing_style_favorites` - Favorites

**Prompts**:
- `prompt_templates` - Prompt templates
- `prompt_template_favorites` - Favorites

**Other**:
- `jobs` - Background jobs
- `weekly_reports` - Reports

## Configuration

```yaml
# application.yml
server:
  port: 8081  # Changed from 8080 to avoid conflicts

spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/kb?currentSchema=public
    username: exercise8
    password: exercise8password
    driver-class-name: org.postgresql.Driver

  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}  # Set via environment variable
      chat:
        options:
          model: qwen-max  # 或 qwen-turbo for faster responses
```

### Environment Variables

```bash
# Required for Alibaba DashScope (通义千问)
export DASHSCOPE_API_KEY="sk-..."

# Get your key from: https://dashscope.aliyun.com/
```

## Running the Application

```bash
# From 12nl2sql directory
cd 12nl2sql

# Option 1: Run directly
mvn spring-boot:run

# Option 2: Build and run
mvn clean package
java -jar target/12nl2sql-0.0.1-xs.jar
```

## Project Structure

```
12nl2sql/
├── pom.xml                           # Standalone Maven config
├── src/main/java/
│   └── com/xushu/springai/nl2sql/
│       ├── Application.java                  # Main entry point
│       ├── controller/
│       │   └── Nl2SqlController.java         # REST endpoints
│       └── service/
│           └── QueryService.java             # NL2SQL business logic
└── src/main/resources/
    └── application.yml                       # Configuration
```

## NL2SQL Query Examples

### Chinese Queries

```bash
# Query all users
curl -X POST http://localhost:8081/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"query":"查询所有用户"}'

# Count records
curl -X POST http://localhost:8081/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"query":"统计用户总数"}'

# Query with conditions
curl -X POST http://localhost:8081/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"query":"查询最近的10个文档"}'
```

### English Queries

```bash
# Show all users
curl -X POST http://localhost:8081/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"query":"Show all users"}'

# Count total orders
curl -X POST http://localhost:8081/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"query":"Count total documents"}'
```

## Design Principles Applied

### ✅ KISS (Keep It Simple, Stupid)
- Removed unused Alibaba NL2SQL dependency
- Simple REST API with clear endpoints
- Direct JDBC access for SQL execution
- Prompt-based NL2SQL implementation

### ✅ YAGNI (You Aren't Gonna Need It)
- Removed unused `spring-ai-alibaba-starter-nl2sql` dependency
- No complex NL2SQL framework - simple prompt engineering works
- Minimal dependencies - only what's needed

### ✅ SOLID - Single Responsibility Principle
- `QueryService`: NL2SQL conversion and database operations
- `Nl2SqlController`: HTTP request handling
- `Application`: Application bootstrap

### ✅ DRY (Don't Repeat Yourself)
- Reused Spring Boot patterns
- No duplicate configuration
- Single source of truth for schema information

## Troubleshooting

### Issue: API Key Not Found

**Error**: `DashScope API key must be set`

**Solution**:
```bash
export DASHSCOPE_API_KEY="sk-your-key-here"
```

### Issue: Database Connection Failed

**Error**: `Connection refused`

**Solution**: Check PostgreSQL is running on port 5434
```bash
# Test connection
psql -h localhost -p 5434 -U exercise8 -d kb
```

### Issue: SQL Generation Failed

**Error**: `Could not extract valid SQL from AI response`

**Possible Causes**:
1. API key not set or invalid
2. Network connectivity issues
3. AI model returned non-SQL response

**Solution**: Check application logs for details

## Performance Tips

1. **Use Faster Model**: Change `qwen-max` to `qwen-turbo` in application.yml for faster responses
2. **Add LIMIT Clause**: The system automatically adds LIMIT to prevent excessive results
3. **Cache Schema**: Schema is fetched once per query for context

## Summary

The 12nl2sql module is:
1. ✅ **Multi-module Project** - Part of spring-ai-parent with dependency management
2. ✅ **Working** - All REST endpoints functional
3. ✅ **Connected** - PostgreSQL database access confirmed
4. ✅ **Clean** - No dependency conflicts or startup errors
5. ✅ **NL2SQL Working** - Natural language to SQL using DashScope ChatModel

**The application is production-ready for natural language to SQL queries!**
