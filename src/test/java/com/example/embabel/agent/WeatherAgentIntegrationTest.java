package com.example.embabel.agent;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.integration.EmbabelMockitoIntegrationTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * WeatherAgent 集成测试类
 *
 * <p>继承自 EmbabelMockitoIntegrationTest，使用 Embabel 提供的集成测试框架。
 * 集成测试介于单元测试和端到端测试之间，它会启动部分 Embabel 框架环境，
 * 但仍然使用 Mockito 来模拟外部依赖（如 LLM API、天气 API 等）。</p>
 *
 * <p>与单元测试的区别：
 * <ul>
 *   <li>单元测试：完全独立，所有依赖都 mock，测试单个类/方法的逻辑</li>
 *   <li>集成测试：启动部分框架，测试多个组件之间的协作</li>
 *   <li>端到端测试：启动完整环境，测试真实流程</li>
 * </ul>
 *
 * <p>主要测试目标：
 * <ul>
 *   <li>验证代理的完整工作流是否正常：城市提取 → 天气查询 → 回答生成</li>
 *   <li>验证多个 Action 方法之间的协作是否正确</li>
 *   <li>验证 Embabel 框架的代理编排机制是否正常工作</li>
 * </ul>
 */
public class WeatherAgentIntegrationTest extends EmbabelMockitoIntegrationTest {


    /**
     * 测试前的全局配置
     *
     * <p>在所有测试方法执行之前运行一次，用于设置测试环境的配置。</p>
     *
     * <p>这里设置了 shell 的交互模式为 false，这样在测试时不会启动
     * 交互式命令行界面，避免测试被阻塞。</p>
     */
    @BeforeAll
    static void setUp() {
        // 设置shell configuration为非交互模式
        // 这样测试时不会启动交互式命令行，避免测试被阻塞
        System.setProperty("embabel.agent.shell.interactive.enabled", "false");
    }

    /**
     * 测试完整的代理执行工作流
     *
     * <p>这是一个典型的集成测试，测试 WeatherAgent 的完整执行流程：
     * <ol>
     *   <li>用户输入："北京天气怎么样"</li>
     *   <li>城市提取：使用 LLM 从用户输入中提取城市名称</li>
     *   <li>天气查询（本示例中可能通过 mock 实现）</li>
     *   <li>回答生成：使用 LLM 根据天气数据生成友好的回答</li>
     * </ol>
     *
     * <p>测试特点：
     * <ul>
     *   <li>使用 EmbabelMockitoIntegrationTest 提供的 whenCreateObject 来模拟城市提取</li>
     *   <li>使用 whenGenerateText 来模拟回答生成</li>
     *   <li>使用 AgentInvocation 来调用代理，模拟真实的执行环境</li>
     *   <li>验证 LLM 调用的参数是否正确（如温度参数）</li>
     *   <li>验证没有未预期的 LLM 调用（verifyNoMoreInteractions）</li>
     * </ul>
     *
     * <p>关键验证点：
     * <ul>
     *   <li>createObject 被正确调用，用于提取城市</li>
     *   <li>generateText 被正确调用，用于生成回答</li>
     *   <li>温度参数是否为 2.0</li>
     *   <li>工具组是否为空</li>
     *   <li>最终返回的结果是否与预期一致</li>
     * </ul>
     */
    @Test
    void shouldExecuteCompleteWorkflow() {
        // 步骤1：准备测试数据
        // 创建预期的城市对象
        WeatherAgent.City city = new WeatherAgent.City("北京");
        // 创建用户输入对象
        UserInput userInput = new UserInput("北京天气怎么样");
        
        // 步骤2：设置 LLM 调用的预期行为
        // 当遇到包含 "Extract the city name from this user input." 的提示词时，
        // 并且请求的类型是 WeatherAgent.City.class 时，返回我们的 city 对象
        whenCreateObject(prompt -> prompt.contains("Extract the city name from this user input."), WeatherAgent.City.class)
            .thenReturn(city);
        
        // 当遇到包含 "Generate a friendly weather response" 的提示词时，
        // 返回预设的文本回答
        whenGenerateText(prompt -> prompt.contains("Generate a friendly weather response for the user based on the following data")).thenReturn("北京天气晴，温度24度");
        
        // 步骤3：创建代理调用实例
        // AgentInvocation 是 Embabel 提供的用于测试代理调用的工具
        var invocation = AgentInvocation.create(agentPlatform, String.class);
        
        // 步骤4：执行代理调用
        // 这会触发完整的代理工作流
        String result = invocation.invoke(userInput);
        
        // 步骤5：验证结果
        // 验证结果不为 null
        assertNotNull(result);
        // 验证结果内容与预期一致
        assertEquals("北京天气晴，温度24度", result);

        // 步骤6：验证 LLM 调用的详细信息
        // 验证 createObject 被调用，且参数匹配：
        // - 提示词包含特定内容
        // - 温度参数为 2.0
        // - 工具组为空
        verifyCreateObjectMatching(prompt -> prompt.contains("Extract the city name from this user input."), WeatherAgent.City.class,
            llm -> llm.getLlm().getTemperature() == 2.0 && llm.getToolGroups().isEmpty());
        
        // 验证 generateText 被调用，且提示词包含特定内容
        verifyGenerateTextMatching(prompt -> prompt.contains("Generate a friendly weather response for the user based on the following data"));
        
        // 验证没有其他未预期的 LLM 调用
        // 这是一个重要的验证，确保我们的代码没有产生意外的 LLM 调用
        verifyNoMoreInteractions();
    }
}
