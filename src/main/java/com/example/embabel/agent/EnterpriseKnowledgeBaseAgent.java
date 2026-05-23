package com.example.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.example.embabel.common.Constants;
import com.example.embabel.service.CompanyPolicyService;
import com.example.embabel.service.DocumentService;

import java.util.List;

/**
 * 企业知识库智能问答代理
 * 
 * <p>基于 Embabel 框架构建的企业知识库问答系统，支持：
 * <ul>
 *   <li>文档相似性搜索 - 使用 VectorStore 检索相关文档</li>
 *   <li>公司政策查询 - 查询假期、加班、出差、培训等政策</li>
 *   <li>智能问答 - 基于检索到的信息生成回答</li>
 * </ul>
 * </p>
 */
@Agent(description = "企业知识库智能问答系统")
public class EnterpriseKnowledgeBaseAgent {

    /** 文档服务，用于文档检索 */
    private final DocumentService documentService;
    
    /** 公司政策服务，用于政策查询 */
    private final CompanyPolicyService policyService;

    /**
     * 构造函数，注入依赖服务
     * 
     * @param documentService 文档服务
     * @param policyService 公司政策服务
     */
    public EnterpriseKnowledgeBaseAgent(DocumentService documentService, CompanyPolicyService policyService) {
        this.documentService = documentService;
        this.policyService = policyService;
    }

    /**
     * 搜索相似文档
     * 
     * <p>根据用户输入在向量存储中搜索相似文档。</p>
     * 
     * @param userInput 用户输入
     * @return 搜索结果，包含查询和匹配的文档列表
     */
    @Action
    public SearchResult searchSimilarDocuments(UserInput userInput) {
        List<DocumentService.DocResult> similarDocs = documentService.searchSimilarDocuments(userInput.getContent());
        return new SearchResult(userInput.getContent(), similarDocs);
    }

    /**
     * 查询公司政策
     * 
     * <p>使用 AI 从用户输入中提取政策类型，然后查询相应的政策信息。</p>
     * 
     * @param userInput 用户输入
     * @param context 操作上下文，包含 AI 工具
     * @return 政策信息
     */
    @Action
    public CompanyPolicyService.PolicyInfo queryCompanyPolicy(UserInput userInput, OperationContext context) {
        // 使用 AI 从用户输入中提取政策类型
        String policyType = context.ai().withAutoLlm().generateText(
            """
            从用户输入中提取政策类型，只返回政策类型名称，不要解释。
            可能的政策类型包括：vacation（假期）、overtime（加班）、travel（出差）、training（培训）
            
            用户输入：%s
            """.formatted(userInput.getContent())
        );
        
        // 处理空结果
        if (policyType == null || policyType.isEmpty()) {
            policyType = "unknown";
        }
        
        // 查询政策信息
        return policyService.getPolicy(policyType);
    }

    /**
     * 回答用户问题
     * 
     * <p>基于检索到的文档和政策信息，使用 AI 生成最终回答。
     * 这是代理的核心目标方法。</p>
     * 
     * @param userInput 用户输入
     * @param searchResult 文档搜索结果
     * @param policyInfo 政策信息
     * @param ai AI 工具
     * @return 最终回答
     */
    @AchievesGoal(description = "基于知识库回答用户问题")
    @Action
    public String answerQuestion(UserInput userInput, SearchResult searchResult, 
                                 CompanyPolicyService.PolicyInfo policyInfo, Ai ai) {
        // 构建上下文信息
        StringBuilder contextBuilder = new StringBuilder();
        
        // 添加文档检索结果
        if (searchResult != null && searchResult.results() != null) {
            for (DocumentService.DocResult doc : searchResult.results()) {
                contextBuilder.append(doc.content()).append("\n");
            }
        }
        
        // 添加政策信息
        if (policyInfo != null && !Constants.NO_POLICY_FOUND.equals(policyInfo.content())) {
            contextBuilder.append("相关政策：").append(policyInfo.content()).append("\n");
        }
        
        // 构建提示词
        String prompt = String.format(
            "基于以下上下文回答用户问题：\n\n" +
            "上下文：\n%s\n\n" +
            "用户问题：%s",
            contextBuilder.toString(),
            userInput.getContent()
        );
        
        // 使用 AI 生成回答
        return ai.withAutoLlm().generateText(prompt);
    }

    /**
     * 搜索结果记录
     * 
     * @param query 查询文本
     * @param results 匹配的文档列表
     */
    public record SearchResult(String query, List<DocumentService.DocResult> results) {}
}