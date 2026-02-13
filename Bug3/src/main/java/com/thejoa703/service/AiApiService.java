package com.thejoa703.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AiApiService {

    @Value("${openai.api.ym.key}")
    private String openAiKey;
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId; 

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    private final String OPEN_AI_URL = "https://api.openai.com/v1/chat/completions";

    public String summarizeReviews(List<String> reviews) {
    	System.out.println("보유한 API 키 확인: [" + openAiKey + "]");
        if (reviews == null || reviews.isEmpty()) return "요약할 리뷰가 없습니다.";

        // 1. 리뷰 리스트를 하나의 텍스트로 합치기
        String combinedReviews = String.join("\n", reviews);
        
        // 2. OpenAI에 보낼 프롬프트 구성
        String prompt = "다음은 상품 리뷰들입니다. 이 리뷰들의 핵심 내용을 3줄로 요약해줘:\n\n" + combinedReviews;

        // 3. API 요청 설정
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo"); // 또는  gpt-4o-mini gpt-3.5-turbo 
        body.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPEN_AI_URL, entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 요약 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
    
    
 // 2. 네이버 언어 감지 기능
 // 1. 언어 감지 (Smart Translation용)
    public String detectLanguage(String text) {
        String apiURL = "https://naveropenapi.apigw.ntruss.com/langs/v1/dect"; // NCP 전용 주소
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        
        // 헤더 키 이름이 바뀐 것이 핵심입니다!
        headers.set("X-NCP-APIGW-API-KEY-ID", naverClientId); 
        headers.set("X-NCP-APIGW-API-KEY-SECRET", naverClientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("query", text);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiURL, entity, Map.class);
            return (String) response.getBody().get("langCode");
        } catch (Exception e) {
            return "ko"; 
        }
    }
 // AiApiService.java 의 기존 translateSmart를 아래 내용으로 교체하세요.

    public String translateSmart(String text) {
        if (text == null || text.trim().isEmpty()) return "번역할 내용이 없습니다.";

        // 1. 먼저 GPT를 통해 언어를 감지하고 번역을 수행합니다.
        // 한국어면 영어로, 그 외 언어는 한국어로 번역하도록 프롬프트를 짭니다.
        String prompt = "Translate the following text. If it is Korean, translate to English. " +
                        "If it is not Korean, translate to Korean. " +
                        "Return only the translated text without any explanation:\n\n" + text;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPEN_AI_URL, entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return "번역 중 오류 발생: " + e.getMessage();
        }
    }

	
}