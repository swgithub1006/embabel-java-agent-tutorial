package com.example.embabel.agent;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.LlmInvocation;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.ModelSelectionCriteria;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * WeatherAgent 单元测试类
 *
 * <p>使用 Mockito 和 FakeOperationContext 对 WeatherAgent 的核心功能进行单元测试。
 * 单元测试的特点是独立于外部依赖，通过 mock 来模拟外部组件的行为。</p>
 *
 * <p>主要测试目标：
 * <ul>
 *   <li>extractCity 方法的功能正确性：验证城市名称提取逻辑是否正常工作</li>
 *   <li>LLM 调用参数正确性：验证温度参数、提示词内容等是否按预期设置</li>
 *   <li>交互行为验证：验证是否正确调用了 LLM 的 createObject 方法</li>
 * </ul>
 * </p>
 */
class WeatherAgentUnitTest {


    /**
     * 测试使用 Mockito 模拟依赖的城市名称提取功能
     *
     * <p>测试场景：
     * <ul>
     *   <li>模拟 OperationContext、Ai、PromptRunner 等核心组件</li>
     *   <li>设置当调用 createObject 时返回预设的 City 对象</li>
     *   <li>验证 extractCity 方法返回的结果与预期一致</li>
     *   <li>验证 Ai 的 withLlm 方法被正确调用，且参数正确</li>
     *   <li>验证 PromptRunner 的 createObject 方法被正确调用</li>
     * </ul>
     *
     * <p>关键测试点：
     * <ul>
     *   <li>提示词（prompt）是否按预期格式构建</li>
     *   <li>温度参数是否设置为 2.0（用于城市提取这种结构化任务，稍高的温度可以增加创造性）</li>
     *   <li>返回的 City 对象是否与预期一致</li>
     * </ul>
     */
    @Test
    void testExtractCityWithMock() {
        // 步骤1：创建所有需要的模拟对象
        // OperationContext 是代理执行的上下文环境
        OperationContext mockOperationContext = Mockito.mock(OperationContext.class);
        // Ai 是 LLM 调用的核心接口
        Ai mockAi = Mockito.mock(Ai.class);
        // PromptRunner 用于执行具体的提示词任务
        PromptRunner mockPromptRunner = Mockito.mock(PromptRunner.class);
        // RestTemplate 用于调用天气 API，这里也需要模拟
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);

        // 创建预期的城市对象（北京）
        var city = new WeatherAgent.City("北京");
        // 创建用户输入对象
        UserInput userInput = new UserInput("北京天气怎么样");

        // 构建预期的提示词格式
        String prompt = """
            Extract the city name from this user input.
            - city name: the name of the city

            User input: %s""".formatted(userInput.getContent());

        // 步骤2：设置模拟对象的行为
        // 当调用 mockOperationContext.ai() 时，返回我们的 mockAi
        when(mockOperationContext.ai()).thenReturn(mockAi);
        // 构建 LLM 选项：使用自动选择模型，设置温度为 2.0
        LlmOptions llmOptions = LlmOptions.fromCriteria(ModelSelectionCriteria.getAuto()).withTemperature(2.0);
        // 当调用 withLlm(llmOptions) 时，返回 mockPromptRunner
        when(mockAi.withLlm(llmOptions)).thenReturn(mockPromptRunner);
        // 当调用 createObject 时，返回我们预设的 city 对象
        when(mockPromptRunner.createObject(prompt, WeatherAgent.City.class)).thenReturn(city);

        // 步骤3：创建待测试的 WeatherAgent 实例
        var agent = new WeatherAgent(mockRestTemplate);
        // 执行待测试的方法
        WeatherAgent.City extractedCity = agent.extractCity(userInput, mockOperationContext);

        // 步骤4：验证结果是否正确
        // 验证提取到的城市对象是否与预期一致
        assertEquals(city, extractedCity);

        // 步骤5：验证模拟对象的交互行为是否符合预期
        // 验证 Ai.withLlm 方法被调用，且参数是我们预期的 llmOptions
        Mockito.verify(mockAi).withLlm(llmOptions);
        // 验证 PromptRunner.createObject 方法被调用，且参数是预期的 prompt 和类型
        Mockito.verify(mockPromptRunner).createObject(prompt, WeatherAgent.City.class);
    }

    /**
     * 测试使用 FakeOperationContext 的城市名称提取功能
     *
     * <p>FakeOperationContext 是 Embabel 提供的一个测试工具，相比于纯 Mockito 模拟，
     * 它提供了更方便的 API 来记录和验证 LLM 的调用。</p>
     *
     * <p>测试场景：
     * <ul>
     *   <li>使用 FakeOperationContext 而不是纯 Mockito 模拟</li>
     *   <li>设置 FakeOperationContext 的预期响应</li>
     *   <li>调用 extractCity 方法</li>
     *   <li>不仅验证返回结果，还验证 LLM 调用的详细信息</li>
     * </ul>
     *
     * <p>关键测试点：
     * <ul>
     *   <li>LLM 调用次数是否为 1 次</li>
     *   <li>提示词内容是否与预期完全一致</li>
     *   <li>温度参数是否为 2.0</li>
     *   <li>工具组（tool groups）是否为空（提取城市不需要工具）</li>
     *   <li>工具列表是否为空</li>
     * </ul>
     */
    @Test
    void testExtractCityWithFake() {
        // 步骤1：创建 FakeOperationContext 实例
        // FakeOperationContext 是 Embabel 提供的测试专用上下文对象
        var context = FakeOperationContext.create();

        // 步骤2：模拟 RestTemplate
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
        // 创建预期的城市对象
        var city = new WeatherAgent.City("北京");

        // 创建用户输入
        UserInput userInput = new UserInput("北京天气怎么样");

        // 构建预期的提示词
        String prompt = """
            Extract the city name from this user input.
            - city name: the name of the city

            User input: %s""".formatted(userInput.getContent());

        // 步骤3：设置 FakeOperationContext 的预期响应
        // 当请求 City 类型的对象时，返回我们的 city 对象
        context.expectResponse(city);

        // 步骤4：创建代理实例并执行方法
        var agent = new WeatherAgent(mockRestTemplate);
        WeatherAgent.City extractedCity = agent.extractCity(userInput, context);

        // 步骤5：验证返回结果
        assertEquals(city, extractedCity);

        // 步骤6：获取并验证 LLM 调用记录
        // FakeOperationContext 会记录所有的 LLM 调用，我们可以查询并验证
        List<LlmInvocation> llmInvocations = context.getLlmInvocations();
        // 验证 LLM 调用次数为 1
        assertEquals(1, llmInvocations.size());
        // 获取第一次（也是唯一一次）LLM 调用记录
        LlmInvocation invocation = llmInvocations.get(0);
        // 验证提示词内容与预期一致
        assertEquals(prompt, invocation.getPrompt());
        // 验证温度参数为 2.0
        assertEquals(2.0, invocation.getInteraction().getLlm().getTemperature());
        // 验证工具组为空（这个任务不需要任何工具）
        assertTrue(invocation.getInteraction().getToolGroups().isEmpty());
        // 验证工具列表为空
        assertTrue(invocation.getInteraction().getTools().isEmpty());
    }
}
