package org.springframework.ai.mcp.sample.client;

import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

/**
 * SSE Client Test for MCP Weather Server
 * Make sure the server is running on http://localhost:8088/mcp
 */
public class SSEClientTest {

    public static void main(String[] args) throws Exception {
        // SSE transport connects to the running server
        var transport = new HttpClientSseClientTransport("http://localhost:8088/mcp");
        var client = McpClient.sync(transport).build();

        try {
            // Initialize the connection
            client.initialize();

            // List available tools
            ListToolsResult toolsList = client.listTools();
            System.out.println("=== 可用工具 / Available Tools ===");
            System.out.println(toolsList);
            System.out.println();

            // Test weather forecast tool
            System.out.println("=== 测试天气预报工具 / Testing Weather Forecast ===");
            CallToolResult weatherResult = client.callTool(new CallToolRequest(
                    "getWeatherForecastByLocation",
                    Map.of("latitude", 39.9042, "longitude", 116.4074)));

            System.out.println("北京天气预报 / Beijing Weather Forecast:");
            System.out.println(weatherResult);
            System.out.println();

            // Test air quality tool
            System.out.println("=== 测试空气质量工具 / Testing Air Quality ===");
            CallToolResult airQualityResult = client.callTool(new CallToolRequest(
                    "getAirQuality",
                    Map.of("latitude", 39.9042, "longitude", 116.4074)));

            System.out.println("北京空气质量 / Beijing Air Quality:");
            System.out.println(airQualityResult);
            System.out.println();

        } finally {
            // Close the connection
            client.closeGracefully();
        }
    }
}
