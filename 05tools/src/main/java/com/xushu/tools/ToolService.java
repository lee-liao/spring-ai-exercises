package com.xushu.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToolService  {

    @Autowired
    private TicketService ticketService;

    //@Autowired
    //private ToolService toolService;

    record cancelParam(String ticketNumber, String name){}

    @Tool(description = "退票")
    @PreAuthorize("hasRole('ADMIN')")
    public String cancel(
            // @ToolParam告诉大模型参数的描述
      @ToolParam(description = "预定号，可以是纯数字") String ticketNumber,
      @ToolParam(description = "真实人名（必填，必须为人的真实姓名，严禁用其他信息代替；如缺失请传null）") String name
           ) {
        // 当前登录用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 先查询 --->先校验
        ticketService.cancel(ticketNumber, name);
        return username+"退票成功！";
    }




    //  获取北京的天气
        @Tool(description = "获取指定位置天气,根据位置自动推算经纬度")
        public String getAirQuality(@ToolParam(description = "纬度") double latitude,
                                    @ToolParam(description = "经度") double longitude) {
            return "天晴";
        }


    /**
     * 模拟从数据库中动态根据当前用户角色读取tools
     * @param toolService 不能自己new，自己new不能解析依赖注入
     * @return
     */
    public List<ToolCallback> getToolCallList(ToolService toolService) {

        // todo.. 从数据库中读取的代码省略


        // 拿1个tool为例

        // 1.获取tools处理的方法
        Method method = ReflectionUtils.findMethod(ToolService.class, "cancel",String.class,String.class);
        // 2.构建Tool定义信息  动态配置的方式 @Tool @ToolParam都无效
        ToolDefinition toolDefinition = ToolDefinition.builder()
                .name("cancel")
                .description("退票")  // 对应@Tool的description
                // 对应@ToolParam
                .inputSchema("""
                        {
                          "type": "object",
                          "properties": {
                            "ticketNumber": {
                              "type": "string",
                              "description": "预定号，可以是纯数字"
                            },
                            "name": {
                              "type": "string",
                              "description": "真实人名"
                            }
                          },
                          "required": ["ticketNumber", "name"]
                        }
                        """)
                .build();
        // 一个ToolCallback对应一个tool
        ToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMethod(method)
                .toolObject(toolService)        // 不能自己new，自己new不能解析依赖注入
                .build();

        return List.of(toolCallback);
    }
}
