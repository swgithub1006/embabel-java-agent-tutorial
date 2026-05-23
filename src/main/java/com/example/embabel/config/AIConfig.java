package com.example.embabel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 相关配置类
 * 
 * <p>配置 Spring AI 的核心组件，包括嵌入模型、向量存储和文本分割器。</p>
 * 
 * <p>核心 Bean 定义：
 * <ul>
 *   <li>embeddingModel - 嵌入模型，用于将文本转换为向量</li>
 *   <li>vectorStore - 向量存储，用于存储和检索文档向量</li>
 *   <li>textSplitter - 文本分割器，用于将长文档分割为小块</li>
 * </ul>
 * </p>
 */
@Configuration
public class AIConfig {

    private final Logger logger = LoggerFactory.getLogger(AIConfig.class);

    /**
     * 配置嵌入模型
     * 
     * <p>使用简单的随机嵌入实现，适用于开发和测试环境。
     * 在生产环境中，建议使用真实的嵌入服务（如 OpenAI、DeepSeek 等）。</p>
     * 
     * @return EmbeddingModel 实例
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new SimpleEmbeddingModel();
    }

    /**
     * 配置向量存储
     * 
     * <p>使用 Spring AI 的 SimpleVectorStore，这是一个内存中的向量存储实现。
     * 它依赖于嵌入模型来生成文档向量。</p>
     * 
     * @param embeddingModel 嵌入模型
     * @return VectorStore 实例
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 配置文本分割器
     * 
     * <p>使用 TokenTextSplitter，基于 token 数量分割文档，
     * 确保每个文档片段适合嵌入模型的输入限制。</p>
     * 
     * @return TextSplitter 实例
     */
    @Bean
    public TextSplitter textSplitter() {
        return new TokenTextSplitter();
    }

    /**
     * 简单的嵌入模型实现
     * 
     * <p>生成随机的 384 维向量，用于开发和测试。
     * 注意：在生产环境中应替换为真实的嵌入服务。</p>
     */
    private static class SimpleEmbeddingModel implements EmbeddingModel {

        /**
         * 为多个文本生成嵌入向量
         * 
         * @param texts 文本列表
         * @return 嵌入向量列表
         */
        @Override
        public List<float[]> embed(List<String> texts) {
            List<float[]> embeddings = new ArrayList<>();
            for (String text : texts) {
                embeddings.add(embed(text));
            }
            return embeddings;
        }

        /**
         * 为单个文本生成嵌入向量
         * 
         * @param text 文本内容
         * @return 384 维嵌入向量
         */
        @Override
        public float[] embed(String text) {
            float[] embedding = new float[384];
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] = (float) Math.random() * 2 - 1;
            }
            return embedding;
        }

        /**
         * 为 Document 对象生成嵌入向量
         * 
         * @param document 文档对象
         * @return 嵌入向量
         */
        @Override
        public float[] embed(Document document) {
            return embed(document.getText());
        }

        /**
         * 获取嵌入向量的维度
         * 
         * @return 向量维度（384）
         */
        @Override
        public int dimensions() {
            return 384;
        }

        /**
         * 处理嵌入请求
         * 
         * @param request 嵌入请求
         * @return 嵌入响应
         */
        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<Embedding> embeddings = new ArrayList<>();
            int index = 0;
            for (String text : request.getInstructions()) {
                float[] embedding = embed(text);
                embeddings.add(new Embedding(embedding, index++, null));
            }
            return new EmbeddingResponse(embeddings);
        }
    }
}