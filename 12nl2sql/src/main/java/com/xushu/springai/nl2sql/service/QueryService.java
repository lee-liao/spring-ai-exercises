package com.xushu.springai.nl2sql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Natural Language to SQL Service
 * Uses Spring AI Alibaba NL2SQL capabilities
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {

    private final JdbcTemplate jdbcTemplate;
    private final ChatModel chatModel;

    public Map<String, Object> query(String naturalQuery) {
        log.info("Natural language query: {}", naturalQuery);
        try {
            // Get database schema for context
            Map<String, Object> schemaInfo = getSchema();

            // Build prompt with schema context
            String systemPrompt = buildSystemPrompt(schemaInfo);

            // Use ChatModel to generate SQL from natural language
            Prompt prompt = new Prompt(
                List.of(
                    new SystemPromptTemplate(systemPrompt).createMessage(),
                    new UserMessage(naturalQuery)
                )
            );

            String response = chatModel.call(prompt).getResult().getOutput().getText();
            log.info("Generated SQL: {}", response);

            // Try to extract and execute the SQL
            String sql = extractSql(response);
            if (sql != null && !sql.isEmpty()) {
                return executeSql(sql);
            } else {
                return Map.of(
                    "query", naturalQuery,
                    "response", response,
                    "message", "Could not extract valid SQL from AI response",
                    "success", false
                );
            }
        } catch (Exception e) {
            log.error("Error executing query", e);
            return Map.of("query", naturalQuery, "error", e.getMessage(), "success", false);
        }
    }

    private String buildSystemPrompt(Map<String, Object> schemaInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a SQL expert. Convert natural language queries to SQL based on the following database schema:\n\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tables = (List<Map<String, Object>>) schemaInfo.get("tables");

        if (tables != null) {
            for (Map<String, Object> table : tables) {
                String tableName = (String) table.get("table_name");
                sb.append("- Table: ").append(tableName).append("\n");

                // Get column information for each table
                Map<String, Object> tableStructure = getTableStructure(tableName);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> columns = (List<Map<String, Object>>) tableStructure.get("columns");

                if (columns != null) {
                    for (Map<String, Object> column : columns) {
                        sb.append("  - ").append(column.get("column_name"))
                          .append(" (").append(column.get("data_type")).append(")\n");
                    }
                }
                sb.append("\n");
            }
        }

        sb.append("\nRules:\n");
        sb.append("1. Respond ONLY with the SQL query, no explanations\n");
        sb.append("2. Use PostgreSQL syntax\n");
        sb.append("3. Always use lowercase table and column names\n");
        sb.append("4. Add LIMIT clause to prevent excessive results\n");

        return sb.toString();
    }

    private String extractSql(String response) {
        // Extract SQL from the AI response
        // Remove common markdown code blocks
        String cleaned = response.replaceAll("```sql", "")
                                  .replaceAll("```", "")
                                  .trim();

        // Basic validation - check if it starts with SELECT
        if (cleaned.toUpperCase().startsWith("SELECT")) {
            return cleaned;
        }
        return null;
    }

    public Map<String, Object> executeSql(String sql) {
        log.info("Executing SQL: {}", sql);
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            log.info("Query returned {} rows", results.size());
            return Map.of("sql", sql, "results", results, "rowCount", results.size(), "success", true);
        } catch (Exception e) {
            log.error("Error executing SQL", e);
            return Map.of("sql", sql, "error", e.getMessage(), "success", false);
        }
    }

    public Map<String, Object> getSchema() {
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT table_name, table_type FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name"
            );
            log.info("Found {} tables in schema", tables.size());
            return Map.of("tables", tables, "database", "kb", "schema", "public", "tableCount", tables.size());
        } catch (Exception e) {
            log.error("Error fetching schema", e);
            return Map.of("error", e.getMessage());
        }
    }

    public Map<String, Object> getTableStructure(String tableName) {
        try {
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name, data_type, is_nullable, column_default FROM information_schema.columns WHERE table_name = ? ORDER BY ordinal_position",
                tableName
            );
            log.info("Table {} has {} columns", tableName, columns.size());
            return Map.of("table", tableName, "columns", columns, "columnCount", columns.size());
        } catch (Exception e) {
            log.error("Error fetching table structure for {}", tableName, e);
            return Map.of("error", e.getMessage());
        }
    }
}
