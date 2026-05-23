package com.example.embabel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Embabel 企业知识库问答系统 - Spring Boot 启动类
 * 
 * <p>本应用基于 Embabel 框架构建，提供企业知识库智能问答功能，
 * 支持文档检索、政策查询和智能问答。</p>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>文档相似性搜索 - 使用 Spring AI VectorStore 进行向量检索</li>
 *   <li>公司政策查询 - 查询假期、加班、出差、培训等政策</li>
 *   <li>智能问答 - 基于检索到的信息生成回答</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class EmbabelApplication {

    /**
     * 应用程序入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EmbabelApplication.class, args);
    }
}