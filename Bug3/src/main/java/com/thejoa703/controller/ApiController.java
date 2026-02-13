package com.thejoa703.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.thejoa703.external.ApiEmailNaver;
import com.thejoa703.service.MaterialService;

@Controller
@RequestMapping("/api")
public class ApiController {

	///////////////////////// Email
	@Autowired
	ApiEmailNaver apiEmailNaver;

	@GetMapping("/mail")
	public String mail_get() {
		return "external/mail";
	}

	@PostMapping(value = "/mail")
	public String mail(String subject, String content, String email) {
		apiEmailNaver.sendMail(subject, content, email);
		return "external/mail_result";
	}

	///////////////////////// Chatbot
	@GetMapping("/chatbot")
	public String chatbot() {
		return "external/chatbot";
	}
	
	@Autowired MaterialService materialService;
	
	@GetMapping("/trend")
	   @ResponseBody
	   public String getTrend(@RequestParam int materialId, @RequestParam String keyword) {

	       String clientId = "FigimYBBeWqPMrh2uQ0W";
	       String clientSecret = "g4voC20v0G";
	       String apiUrl = "https://openapi.naver.com/v1/datalab/search";

	       RestTemplate restTemplate = new RestTemplate();
	       HttpHeaders headers = new HttpHeaders();
	       headers.setContentType(MediaType.APPLICATION_JSON);
	       headers.set("X-Naver-Client-Id", clientId);
	       headers.set("X-Naver-Client-Secret", clientSecret);

	       String jsonBody = String.format(
	           "{\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-29\",\"timeUnit\":\"month\"," +
	           "\"keywordGroups\":[{\"groupName\":\"%s\",\"keywords\":[\"%s\"]}]}", 
	           keyword, keyword
	       );

	       HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

	       try {
	           ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
	           String resultJson = response.getBody();

	           // [수정 전]: MaterialService.saveTrendData (X) -> 클래스명으로 호출하면 static 에러 발생
	           // [수정 후]: 주입받은 변수명을 사용해야 합니다.
	           materialService.saveTrendData(materialId, keyword, resultJson); 

	           return resultJson;
	       } catch (Exception e) {
	           e.printStackTrace();
	           return "{\"error\":\"API 호출 실패\"}";
	       }
	   }
	
	@GetMapping("/allergy")
	@ResponseBody
	public String getAllergyInfo(@RequestParam String keyword) {
	    // 1. 공공데이터포털 인증키 (본인의 디코딩된 서비스키 입력)
	    String serviceKey = "d1dbaa48990a65bd6404577b3a7b2de5afbde095cdee258bfeba049b8945d2c7";
	    String apiUrl = "https://apis.data.go.kr/B553748/CertImgListServiceV3";

	    RestTemplate restTemplate = new RestTemplate();
	    
	    // 2. 파라미터 설정 (prdlstNm: 제품명으로 검색)
	    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
	            .queryParam("serviceKey", serviceKey)
	            .queryParam("prdlstNm", keyword)
	            .queryParam("returnType", "json")
	            .queryParam("pageNo", "1")
	            .queryParam("numOfRows", "1");

	    try {
	        // 3. API 호출
	        ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
	        return response.getBody();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "{\"error\":\"API 호출 실패\"}";
	    }
	}

}
