package com.xushu.tools;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToolsController {

    ChatClient chatClient;

    public ToolsController(ChatClient.Builder ChatClientBuilder,
                           ToolService toolService) {
        this.chatClient = ChatClientBuilder
                .defaultSystem("""
                        # 角色
                        你是智能航空客服助手
                        ## 要求
                        严禁随意补全或猜测工具调用参数。 参数如缺失或语义不准，请不要补充或随意传递，请直接放弃本次工具调用。”
                        """)
                .defaultTools(toolService)  // 底层就会告诉大模型提供了什么工具， 需要什么参数
                // 动态tool设置
                //.defaultToolCallbacks(toolService.getToolCallList(toolService))
                .build();
    }

    @RequestMapping("/tool")
    public String tool(@RequestParam(value = "message",defaultValue = "讲个笑话")
                           String message) {
        return chatClient.prompt()
                .user(message)
                .call().content();
    }
}
