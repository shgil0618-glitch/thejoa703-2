package com.thejoa703.external;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OpenAIEmbeddingService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/embeddings";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(OpenAIEmbeddingService.class);

    /**
     * 입력 키워드와 후보 키워드들의 임베딩을 비교하여 가장 유사한 키워드를 추천
     */
    public String recommendKeyword(String input, List<String> candidates) {
        double[] inputVec = getEmbedding(input);

        String bestMatch = null;
        double bestScore = -1.0;

        for (String candidate : candidates) {
            double[] candidateVec = getEmbedding(candidate);
            double score = cosineSimilarity(inputVec, candidateVec);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }

    /**
     * OpenAI Embedding API 호출
     */
    private double[] getEmbedding(String text) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "text-embedding-3-small"); // 또는 text-embedding-3-large
        body.put("input", text);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(API_URL, requestEntity, String.class);

            // ✅ 상태 코드 확인
            log.info("OpenAI API status: {}", responseEntity.getStatusCode());

            JsonNode root = objectMapper.readTree(responseEntity.getBody());

            // ✅ 모델명과 usage 정보만 로그 출력
            String modelName = root.get("model").asText();
            JsonNode usageNode = root.get("usage");
            int promptTokens = usageNode.get("prompt_tokens").asInt();
            int totalTokens = usageNode.get("total_tokens").asInt();

            log.info("Model: {}", modelName);
            log.info("Usage - prompt_tokens: {}, total_tokens: {}", promptTokens, totalTokens);

            // ✅ 실제 embedding 벡터 추출
            JsonNode vectorNode = root.get("data").get(0).get("embedding");
            double[] vector = new double[vectorNode.size()];
            for (int i = 0; i < vectorNode.size(); i++) {
                vector[i] = vectorNode.get(i).asDouble();
            }
            return vector;

        } catch (Exception e) {
            if (responseEntity != null) {
                log.error("OpenAI API 호출 실패 - status: {}, body: {}",
                          responseEntity.getStatusCode(), responseEntity.getBody());
            }
            throw new RuntimeException("Embedding parsing failed", e);
        }
    }

    /**
     * 두 벡터 간 코사인 유사도 계산
     */
    private double cosineSimilarity(double[] vec1, double[] vec2) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            normA += Math.pow(vec1[i], 2);
            normB += Math.pow(vec2[i], 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
