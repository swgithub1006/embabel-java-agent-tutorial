package com.example.embabel.agent;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WeatherAgent 端到端（E2E）测试类
 *
 * <p>使用 @SpringBootTest 注解启动完整的 Spring Boot 应用环境，
 * 测试真实的完整流程，包括真实的 LLM 调用和天气 API 调用。</p>
 *
 * <p>与单元测试、集成测试的区别：
 * <ul>
 *   <li>单元测试：完全 mock，测试单个类/方法</li>
 *   <li>集成测试：部分 mock，测试组件协作</li>
 *   <li>E2E测试：完全真实环境，测试完整流程</li>
 * </ul>
 *
 * <p>主要测试目标：
 * <ul>
 *   <li>验证完整的端到端流程是否正常工作</li>
 *   <li>验证 AgentPlatform 是否正确初始化和配置</li>
 *   <li>验证 WeatherAgent 是否能被正确注入和调用</li>
 *   <li>验证真实的 LLM 调用和天气 API 调用是否正常</li>
 * </ul>
 *
 * <p>注意事项：
 * <ul>
 *   <li>E2E测试通常较慢，因为需要启动完整的应用</li>
 *   <li>E2E测试可能产生真实的 API 调用费用</li>
 *   <li>E2E测试可能依赖外部服务的可用性</li>
 * </ul>
 */
@SpringBootTest
public class WeatherAgentE2ETest {

    /**
     * 自动注入 AgentPlatform
     *
     * <p>AgentPlatform 是 Embabel 的核心平台组件，负责：
     * <ul>
     *   <li>代理的注册和管理</li>
     *   <li>LLM 调用的路由和管理</li>
     *   <li>工具的注册和执行</li>
     *   <li>代理工作流的编排</li>
     * </ul>
     */
    @Autowired
    private AgentPlatform agentPlatform;

    /**
     * 自动注入 WeatherAgent
     *
     * <p>这是我们要测试的目标代理类，通过 Spring 的依赖注入机制获取。</p>
     */
    @Autowired
    private WeatherAgent weatherAgent;

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
     * 测试完整的端到端执行流程
     *
     * <p>这是一个真正的端到端测试，会：
     * <ol>
     *   <li>启动完整的 Spring Boot 应用</li>
     *   <li>初始化 Embabel 的 AgentPlatform</li>
     *   <li>注册 WeatherAgent</li>
     *   <li>通过 AgentInvocation 调用代理</li>
     *   <li>触发真实的 LLM 调用（城市提取、回答生成）</li>
     *   <li>触发真实的天气 API 调用</li>
     *   <li>验证最终结果</li>
     * </ol>
     *
     * <p>测试特点：
     * <ul>
     *   <li>使用 @SpringBootTest 注解启动完整 Spring 上下文</li>
     *   <li>使用 @Autowired 注入真实的 Bean</li>
     *   <li>使用 AgentInvocation 调用代理，模拟真实用户场景</li>
     *   <li>打印结果到控制台，方便调试</li>
     *   <li>验证结果不为 null 且包含关键信息</li>
     * </ul>
     *
     * <p>关键验证点：
     * <ul>
     *   <li>结果不为 null：证明代理执行成功</li>
     *   <li>结果包含 "北京"：证明城市提取成功</li>
     *   <li>结果包含 "天气"：证明生成了相关的天气回答</li>
     * </ul>
     *
     * <p>注意：
     * <ul>
     *   <li>此测试需要配置好 LLM API 和天气 API 的密钥</li>
     *   <li>此测试会产生真实的 API 调用费用</li>
     *   <li>如果外部服务不可用，此测试可能失败</li>
     * </ul>
     */
    @Test
    void shouldExecuteE2E() {

        // 步骤1：创建用户输入
        // 模拟真实用户的提问
        UserInput userInput = new UserInput("北京天气怎么样");
        
        // 步骤2：创建代理调用实例
        // AgentInvocation.create() 方法需要传入：
        // - agentPlatform：代理平台实例
        // - 返回类型：String.class，表示代理返回字符串
        var invocation =
            AgentInvocation.create(agentPlatform, String.class);

        // 步骤3：执行代理调用
        // 这会触发完整的端到端流程：
        // 1. 解析用户输入
        // 2. 识别要调用的代理
        // 3. 执行代理的工作流（Action 方法）
        // 4. 调用 LLM 和外部工具
        // 5. 返回最终结果
        String result = invocation.invoke(userInput);
        
        // 步骤4：打印结果，方便调试
        // 在实际测试中，可能不需要打印，但在开发调试时很有用
        System.out.println(result);
        
        // 步骤5：验证结果
        // 验证结果不为 null
        assertNotNull(result);
        // 验证结果包含 "北京"，说明城市信息被正确处理
        assertTrue(result.contains("北京"));
        // 验证结果包含 "天气"，说明生成了相关的天气回答
        assertTrue(result.contains("天气"));
    }
}
