package com.xs.agent.chain_workflow;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    private final ChatClient chatClient;

    public WorkflowController(DashScopeChatModel dashScopeChatModel) {
        this.chatClient = ChatClient.create(dashScopeChatModel);
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeWorkflow(@RequestBody WorkflowRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Create workflow instance
            PracticalChainWorkflow workflow = new PracticalChainWorkflow(chatClient);

            // Execute workflow (this prints to console, we'll capture the flow)
            result.put("status", "success");
            result.put("message", "Workflow execution started");
            result.put("requirements", request.requirements());

            // Execute the workflow (outputs to console/logs)
            workflow.process(request.requirements());

            result.put("result", "Workflow completed. Check console/logs for detailed output.");

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Chain Workflow Service");
        status.put("endpoint", "/api/workflow/execute");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/sample")
    public ResponseEntity<WorkflowRequest> getSampleRequest() {
        String sampleRequirements = """
            电商平台需要升级订单处理系统，要求：
             1. 处理能力提升到每秒1000单
             2. 支持多种支付方式和优惠券
             3. 实时库存管理和预警
             4. 订单状态实时跟踪
             5. 数据分析和报表功能
            现有系统：Spring Boot + MySQL，日订单量10万
            """;
        return ResponseEntity.ok(new WorkflowRequest(sampleRequirements));
    }

    public record WorkflowRequest(String requirements) {}
}
