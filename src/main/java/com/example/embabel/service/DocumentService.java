package com.example.embabel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档服务
 * 
 * <p>负责文档的初始化、存储和相似性检索。
 * 使用 Spring AI 的 VectorStore 进行向量存储和检索。</p>
 */
@Service
public class DocumentService {

    private final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    
    /** 向量存储，用于存储和检索文档向量 */
    private final VectorStore vectorStore;
    
    /** 文本分割器，用于将长文档分割为小块 */
    private final TextSplitter textSplitter;

    /**
     * 构造函数，注入依赖
     * 
     * @param vectorStore 向量存储
     * @param textSplitter 文本分割器
     */
    public DocumentService(VectorStore vectorStore, TextSplitter textSplitter) {
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
    }

    /**
     * 初始化文档数据
     * 
     * <p>在应用启动时自动执行，将预设的企业知识库文档加载到向量存储中。
     * 文档会先经过文本分割处理，然后添加到 VectorStore。</p>
     */
    @PostConstruct
    public void initDocuments() {
        // 创建示例文档列表
        List<Document> documents = new ArrayList<>();
        
        documents.add(new Document("公司简介：Embabel是一家专注于AI Agent技术的创新企业，致力于为企业提供智能化解决方案。"));
        documents.add(new Document("企业文化：我们的核心价值观是创新、协作、卓越、诚信。"));
        documents.add(new Document("组织架构：公司分为产品研发部、市场营销部、客户服务部、人力资源部等部门。"));
        documents.add(new Document("技术栈：主要使用Java、Python、Go等编程语言，采用微服务架构。"));
        documents.add(new Document("办公时间：周一至周五，上午9:00至下午6:00，中间有1小时午休时间。"));
        documents.add(new Document("福利待遇：除基本工资外，公司提供五险一金、年终奖金、定期体检等福利。"));
        documents.add(new Document("晋升机制：公司每年进行两次绩效评估，表现优秀者可获得晋升机会。"));
        documents.add(new Document("员工活动：公司定期组织团建活动，包括户外拓展、年会等。"));

        // 使用文本分割器分割长文档
        List<Document> splitDocuments = new ArrayList<>();
        for (Document doc : documents) {
            splitDocuments.addAll(textSplitter.split(doc));
        }
        
        // 将分割后的文档添加到向量存储
        vectorStore.add(splitDocuments);
        logger.info("Loaded {} documents into VectorStore", splitDocuments.size());
    }

    /**
     * 搜索相似文档
     * 
     * <p>根据用户查询，使用向量相似性搜索找到相关文档。</p>
     * 
     * @param query 用户查询文本
     * @return 相似文档结果列表
     */
    public List<DocResult> searchSimilarDocuments(String query) {
        // 构建搜索请求，设置查询和返回数量
        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(query)
                .topK(5)  // 返回前5个最相似的文档
                .build();
        
        // 执行相似性搜索
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        
        // 将结果转换为 DocResult 列表
        return results.stream()
                .map(doc -> new DocResult(doc.getId(), doc.getText()))
                .collect(Collectors.toList());
    }

    /**
     * 文档检索结果记录
     * 
     * @param id 文档ID
     * @param content 文档内容
     */
    public record DocResult(String id, String content) {}
}