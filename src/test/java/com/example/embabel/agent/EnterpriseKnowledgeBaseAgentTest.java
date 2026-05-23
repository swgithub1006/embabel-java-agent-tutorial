package com.example.embabel.agent;

import com.example.embabel.service.CompanyPolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 企业知识库代理测试类
 * 
 * <p>测试公司政策服务和搜索结果记录的功能。</p>
 */
class EnterpriseKnowledgeBaseAgentTest {

    /** 公司政策服务实例 */
    private CompanyPolicyService policyService;

    /**
     * 测试前的初始化操作
     */
    @BeforeEach
    void setUp() {
        policyService = new CompanyPolicyService();
    }

    /**
     * 测试查询假期政策
     */
    @Test
    void testQueryCompanyPolicy_Vacation() {
        var policy = policyService.getPolicy("vacation");
        
        assertNotNull(policy);
        assertEquals("vacation", policy.type());
        assertTrue(policy.content().contains("年假"));
    }

    /**
     * 测试查询未知政策
     */
    @Test
    void testQueryCompanyPolicy_Unknown() {
        var policy = policyService.getPolicy("unknown");
        
        assertNotNull(policy);
        assertEquals("unknown", policy.type());
        assertEquals("未找到相关政策信息", policy.content());
    }

    /**
     * 测试搜索结果记录
     */
    @Test
    void testSearchResultRecord() {
        var searchResult = new EnterpriseKnowledgeBaseAgent.SearchResult("test", List.of());
        
        assertNotNull(searchResult);
        assertEquals("test", searchResult.query());
        assertNotNull(searchResult.results());
    }

    /**
     * 测试公司政策服务初始化
     */
    @Test
    void testCompanyPolicyServiceInit() {
        assertNotNull(policyService.getPolicy("vacation"));
        assertNotNull(policyService.getPolicy("overtime"));
        assertNotNull(policyService.getPolicy("travel"));
        assertNotNull(policyService.getPolicy("training"));
    }
}