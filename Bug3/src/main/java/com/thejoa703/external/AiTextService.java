package com.thejoa703.external;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AiTextService {
	
    @Value("${openai.api.key}")
    private String apiKey;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String generateDescription(String title, List<String> ingredients, List<String> steps) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);

        String prompt = "레시피 제목: " + title + "\n"
                      + "재료: " + String.join(", ", ingredients) + "\n"
                      + "조리 단계: " + String.join(" / ", steps) + "\n"
                      + "위 내용을 바탕으로 요리 설명을 자연스럽게 작성해줘.";

        Map<String,Object> body = new HashMap<>();
        body.put("model", "gpt-4.1-mini"); // ✅ mini 모델만 사용
        body.put("messages", List.of(Map.of("role","user","content",
            prompt + "\n짧고 간단하게 요리 설명을 작성해줘.")));

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch(Exception e) {
            throw new RuntimeException("AI 응답 파싱 오류", e);
        }
    }
    
    public String generateSteps(String title, String shortDesc, List<String> ingredients) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);

        String prompt = "레시피 제목: " + title + "\n"
                      + "간단 설명: " + shortDesc + "\n"
                      + "재료: " + String.join(", ", ingredients) + "\n"
                      + "각 단계는 짧고 명확하게 설명해줘.";

        Map<String,Object> body = new HashMap<>();
        body.put("model", "gpt-4.1-mini"); // ✅ mini 모델만 사용
        body.put("messages", List.of(Map.of("role","user","content", prompt)));

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch(Exception e) {
            throw new RuntimeException("AI 응답 파싱 오류", e);
        }
    }


}

