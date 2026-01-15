package com.xushu.springai.nl2sql.controller;

import com.xushu.springai.nl2sql.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NL2SQL REST Controller
 * Provides natural language to SQL query endpoints
 */
@RestController
@RequestMapping("/api/nl2sql")
@RequiredArgsConstructor
public class Nl2SqlController {

    private final QueryService queryService;

    /**
     * Execute natural language query
     *
     * Example:
     * POST /api/nl2sql/query
     * {
     *   "query": "查询所有用户"
     * }
     */
    @PostMapping("/query")
    public Map<String, Object> query(@RequestBody Map<String, String> request) {
        String naturalQuery = request.get("query");
        if (naturalQuery == null || naturalQuery.trim().isEmpty()) {
            return Map.of(
                "success", false,
                "error", "Query cannot be empty"
            );
        }
        return queryService.query(naturalQuery);
    }

    /**
     * GET method for simple queries
     *
     * Example: GET /api/nl2sql/query?query=查询所有用户
     */
    @GetMapping("/query")
    public Map<String, Object> queryGet(@RequestParam String query) {
        return queryService.query(query);
    }

    /**
     * Get database schema
     */
    @GetMapping("/schema")
    public Map<String, Object> getSchema() {
        return queryService.getSchema();
    }

    /**
     * Get table structure
     */
    @GetMapping("/schema/{tableName}")
    public Map<String, Object> getTableStructure(@PathVariable String tableName) {
        return queryService.getTableStructure(tableName);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "NL2SQL");
    }

    /**
     * Execute raw SQL (for testing)
     *
     * Example: POST /api/nl2sql/execute
     * {
     *   "sql": "SELECT * FROM users LIMIT 10"
     * }
     */
    @PostMapping("/execute")
    public Map<String, Object> executeSql(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return Map.of("success", false, "error", "SQL cannot be empty");
        }
        return queryService.executeSql(sql);
    }
}
