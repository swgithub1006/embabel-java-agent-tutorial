package com.example.embabel.service;

import com.example.embabel.common.Constants;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公司政策服务
 * 
 * <p>提供公司政策的查询功能，支持假期、加班、出差、培训等政策的查询。</p>
 */
@Service
public class CompanyPolicyService {

    /** 政策信息存储，使用线程安全的 ConcurrentHashMap */
    private final Map<String, PolicyInfo> policies = new ConcurrentHashMap<>();

    /**
     * 构造函数，初始化政策数据
     */
    public CompanyPolicyService() {
        initPolicies();
    }

    /**
     * 初始化政策数据
     * 
     * <p>将预设的公司政策加载到内存中，包括假期、加班、出差、培训等政策。</p>
     */
    private void initPolicies() {
        policies.put("vacation", new PolicyInfo("vacation", 
            "员工年假政策：正式员工每年享有15天带薪年假，入职满一年后开始享受。年假需提前两周申请，由部门主管审批。"));
        
        policies.put("overtime", new PolicyInfo("overtime", 
            "加班政策：工作日加班按1.5倍工资计算，周末加班按2倍工资计算，法定节假日加班按3倍工资计算。"));
        
        policies.put("travel", new PolicyInfo("travel", 
            "出差政策：员工出差需提前填写出差申请单，经审批后方可出行。差旅费用标准：一线城市住宿每天不超过500元，二线城市不超过300元。"));
        
        policies.put("training", new PolicyInfo("training", 
            "培训政策：公司每年为员工提供一定额度的培训经费，员工可申请参加与工作相关的培训课程。"));
    }

    /**
     * 获取政策信息
     * 
     * @param policyType 政策类型（vacation/overtime/travel/training）
     * @return 政策信息，如果未找到则返回默认消息
     */
    public PolicyInfo getPolicy(String policyType) {
        return policies.getOrDefault(policyType.toLowerCase(), 
            new PolicyInfo(policyType, Constants.NO_POLICY_FOUND));
    }

    /**
     * 政策信息记录
     * 
     * @param type 政策类型
     * @param content 政策内容
     */
    public record PolicyInfo(String type, String content) {}
}