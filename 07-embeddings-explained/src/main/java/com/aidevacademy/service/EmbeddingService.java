package com.aidevacademy.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    // Simple in-memory product catalog for demo
    private final Map<String, float[]> productIndex = new HashMap<>();
    private final List<String> productNames = new ArrayList<>();

    private static final List<String> SAMPLE_PRODUCTS = List.of(
        "Nike Air Max running shoes for jogging and athletics",
        "Leather Oxford dress shoes for formal occasions",
        "Hiking boots with waterproof membrane for trails",
        "Flip flops for beach and casual summer wear",
        "Basketball high-top sneakers with ankle support",
        "Office ergonomic chair with lumbar support",
        "Standing desk converter for home office",
        "Wireless mechanical keyboard for programmers"
    );

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        indexProducts();
    }

    private void indexProducts() {
        for (String product : SAMPLE_PRODUCTS) {
            float[] vector = embeddingModel.embed(product);
            productIndex.put(product, vector);
            productNames.add(product);
        }
    }

    public List<Map<String, Object>> search(String query, int topK) {
        float[] queryVector = embeddingModel.embed(query);
        List<Map<String, Object>> results = new ArrayList<>();

        for (String product : productNames) {
            float[] productVector = productIndex.get(product);
            double similarity = cosineSimilarity(queryVector, productVector);
            results.add(Map.of("product", product, "score", similarity));
        }

        results.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return results.subList(0, Math.min(topK, results.size()));
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
