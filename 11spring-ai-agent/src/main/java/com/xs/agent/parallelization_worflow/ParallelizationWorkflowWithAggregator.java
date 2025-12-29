package com.xs.agent.parallelization_worflow;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ParallelizationWorkflowWithAggregator {
      
    private final ChatClient chatClient;


    private static final String RISK_ASSESSMENT_PROMPT = """  
            你是一个风险评估专家，请分析以下部门在数字化转型过程中面临的主要风险：  
              
            请从以下角度分析：  
            1. 技术风险  
            2. 人员风险    
            3. 业务连续性风险  
            4. 预算风险  
            5. 应对建议  
            """;

    public ParallelizationWorkflowWithAggregator(ChatClient chatClient) {  
        this.chatClient = chatClient;  
    }  
      
    public AggregatedResult parallelWithAggregation(List<String> inputs) {
        // 步骤1: 并行处理  
        List<String> parallelResults = parallel(inputs);
          
        // 步骤2: 聚合结果  
        String aggregatedOutput = aggregateResults(parallelResults);
          
        return new AggregatedResult(parallelResults, aggregatedOutput);  
    }  
      
    private List<String> parallel(List<String> inputs ) {
        ExecutorService executor = Executors.newFixedThreadPool(inputs.size());
          
        try {  
            List<CompletableFuture<String>> futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> {  
                    return chatClient.prompt(RISK_ASSESSMENT_PROMPT + "\n输入内容: " + input)
                        .call()  
                        .content();  
                }, executor))  
                .collect(Collectors.toList());
              
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(  
                futures.toArray(CompletableFuture[]::new));  
            allFutures.join();  
              
            return futures.stream()  
                .map(CompletableFuture::join)  
                .collect(Collectors.toList());  
                  
        } finally {  
            executor.shutdown();  
        }  
    }  
      
    // 聚合器：将多个并行结果合并为统一输出  
    private String aggregateResults(List<String> results) {
        String aggregatorPrompt = """  
            你是一个数据聚合专家，请将以下多个分析结果合并为一份综合报告：  
              
            原始分析任务: {originalPrompt}  
              
            各部门/地区分析结果:  
            {results}  
              
            请提供：  
            1. 综合分析摘要  
            2. 共同趋势和模式  
            3. 关键差异对比  
            4. 整体结论和建议  
              
            请生成一份统一的综合报告。  
            """;  
          
        String combinedResults = String.join("\n\n---\n\n", results);  
          
        return chatClient.prompt()  
            .user(u -> u.text(aggregatorPrompt)
                .param("originalPrompt", RISK_ASSESSMENT_PROMPT)
                .param("results", combinedResults))  
            .call()  
            .content();  
    }  
      
    public record AggregatedResult(List<String> individualResults, String aggregatedOutput) {}  
}