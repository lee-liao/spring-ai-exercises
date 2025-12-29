package com.xs.agent.evaluator_optimizer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationResponse;

import java.util.ArrayList;
import java.util.List;

public class SimpleEvaluatorOptimizer {  
      
    private final ChatClient chatClient;
      
    // 中文生成器提示词  
    private static final String GENERATOR_PROMPT = """
        你是一个Java代码生成助手。请根据任务要求生成高质量的Java代码。
        重要提醒：
        - 第一次生成时，创建一个基础但完整的实现  
        - 如果收到反馈，请仔细分析每一条建议并逐一改进  
        - 每次迭代都要在前一版本基础上显著提升代码质量  
        - 不要一次性实现所有功能，而是逐步完善  
          
        必须以JSON格式回复：  
        {"thoughts":"详细说明本轮的改进思路","response":"改进后的Java代码"}  
            """;
      
    // 中文评估器提示词    
    private static final String EVALUATOR_PROMPT = """  
        你是一个非常严格的面试官。请从以下维度严格评估代码：
            1. 代码是否高效：从底层分析每一个类型以满足最佳性能！
            2. 满足不重复扩容影响的性能
            评估标准：
            - 只有当代码满足要求达到优秀水平时才返回PASS
            - 如果任何一个维度有改进空间，必须返回NEEDS_IMPROVEMENT 
            - 提供具体、详细的改进建议  
              
            必须以JSON格式回复：  
            {"evaluation":"PASS或NEEDS_IMPROVEMENT或FAIL","feedback":"详细的分维度反馈"}  
              
            记住：宁可严格也不要放松标准！ 
        """;

    public SimpleEvaluatorOptimizer(ChatClient chatClient) {  
        this.chatClient = chatClient;  
    }

    int iteration = 0;
    String context = "";
    public RefinedResponse loop(String task) {
            System.out.println("=== 第" + (iteration + 1) + "轮迭代 ===");  
              
            // 生成代码  
            Generation generation = generate(task,context);
              
            // 评估代码  
            EvaluationResponse evaluation = evaluate(generation.response(), task);
            System.out.println("生成结果: " + generation.response());
            System.out.println("评估结果: " + evaluation.evaluation());
            System.out.println("反馈: " + evaluation.feedback());  
              
            if (evaluation.evaluation() == EvaluationResponse.Evaluation.PASS) {  
                System.out.println("代码通过评估！");
                return new RefinedResponse(generation.response());
            }
            else{
                // 准备下一轮的上下文
                context = String.format("之前的尝试:\n%s\n\n评估反馈:\n%s\n\n请根据反馈改进代码。",
                        generation.response(), evaluation.feedback());
                iteration++;
                return loop(task);
            }
    }  
      
    private Generation generate(String task, String context) {
        return chatClient.prompt()  
            .user(u -> u.text("{prompt}\n{context}\n任务: {task}")
                .param("prompt", GENERATOR_PROMPT)
                .param("context", context)
                .param("task", task))  
            .call()  
            .entity(Generation.class);  
    }  
      
    private EvaluationResponse evaluate(String content, String task) {  
        return chatClient.prompt()  
            .user(u -> u.text("{prompt}\n\n任务: {task}\n\n代码:\n{content}")  
                .param("prompt", EVALUATOR_PROMPT)  
                .param("task", task)  
                .param("content", content))  
            .call()  
            .entity(EvaluationResponse.class);  
    }  
      
    // 使用原始的记录类  
    public static record Generation(String thoughts, String response) {}  
      
    public static record EvaluationResponse(Evaluation evaluation, String feedback) {  
        public enum Evaluation { PASS, NEEDS_IMPROVEMENT, FAIL }  
    }  
      
    public static record RefinedResponse(String solution) {}
}