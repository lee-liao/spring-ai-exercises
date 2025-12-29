package com.xs.agent.orchestrator_workers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

public class SimpleOrchestratorWorkers {  
      
    private final ChatClient chatClient;
      
    // 中文编排器提示词  
    private static final String ORCHESTRATOR_PROMPT = """  
                你是一个项目管理专家，需要将复杂任务分解为可并行执行的专业子任务。
                    任务: {task}
                    请分析任务的复杂性和专业领域需求，将其分解为2-4个需要不同专业技能的子任务。
                    每个子任务应该：
                    1. 有明确的专业领域（如：前端开发、后端API、数据库设计、测试等）
                    2. 可以独立执行
                    3. 有具体的交付物
                    
                    请以JSON格式回复：
                    {
                        "analysis": "任务复杂度分析和分解策略",
                        "tasks": [
                            {
                                "type": "后端API开发",
                                "description": "设计并实现RESTful API接口，包括数据验证和错误处理"
                            },
                            {
                                "type": "前端界面开发",
                                "description": "创建响应式用户界面，实现与后端API的交互"
                            },
                            {
                                "type": "数据库设计",
                                "description": "设计数据表结构，编写SQL脚本和索引优化"
                            }
                        ]
                    }
            """;  
      
    // 中文工作者提示词  
    private static final String WORKER_PROMPT = """  
            你是一个{task_type}领域的资深专家，请完成以下专业任务：
              项目背景: {original_task}
              专业领域: {task_type}
              具体任务: {task_description}
              
              请按照行业最佳实践完成任务，包括：
              1. 技术选型和架构考虑
              2. 具体实现方案
              3. 潜在风险和解决方案
              4. 质量保证措施
              
              请提供专业、详细的解决方案。
            """;  
      
    public SimpleOrchestratorWorkers(ChatClient chatClient) {  
        this.chatClient = chatClient;  
    }  
      
    public void process(String taskDescription) {
        System.out.println("=== 开始处理任务 ===");  
          
        // 步骤1: 编排器分析任务  
        OrchestratorResponse orchestratorResponse = chatClient.prompt()
            .system(p -> p.param("task", taskDescription))
            .user(ORCHESTRATOR_PROMPT)
            .call()
            .entity(OrchestratorResponse.class);  
          
        System.out.println("编排器分析: " + orchestratorResponse.analysis());  
        System.out.println("子任务列表: " + orchestratorResponse.tasks());  
          
        // 步骤2: 工作者处理各个子任务  
        orchestratorResponse.tasks().stream()
            .map(task -> {  
                System.out.println("-----------------------------------处理子任务: " + task.type()+"--------------------------------");
                String content = chatClient.prompt()
                        .user(u -> u.text(WORKER_PROMPT)
                                .param("original_task", taskDescription)
                                .param("task_type", task.type())
                                .param("task_description", task.description()))
                        .call()
                        .content();
                System.out.println(content);
                return task;
            }).toList();
          
        System.out.println("=== 所有工作者完成任务 ===");  
   }
      
    // 数据记录类  
    public record Task(String type, String description) {}  
    public record OrchestratorResponse(String analysis, List<Task> tasks) {}  
    public record FinalResponse(String analysis, List<String> workerResponses) {}  
}