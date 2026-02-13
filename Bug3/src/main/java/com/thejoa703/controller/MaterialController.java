package com.thejoa703.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thejoa703.dto.MaterialDto;
import com.thejoa703.dto.PagingDto;
import com.thejoa703.service.MaterialService;

@RequestMapping("/material")
@Controller
public class MaterialController {

    //@Autowired RecipeService recipeService;
    //@Autowired AppUserSecurityService userService;
    @Autowired MaterialService service;

    /////////////////////// User - Detail
    // /materialdetail?materialid=2
    @GetMapping("/materialdetail")
    public String materialDetail(
        @RequestParam(value="materialid", required=false) Integer materialid, 
        @RequestParam(value="title", required=false) String title, 
        Model model) {

        MaterialDto dto = null;

        // 1. materialid(숫자)가 넘어온 경우
        if (materialid != null) {
            dto = service.selectMaterial(materialid);
        } 
        // 2. title(이름)이 넘어온 경우 (현재 사용자님 상황)
        else if (title != null && !title.isEmpty()) {
            dto = service.selectTitle(title);
        }

        // 만약 둘 다 없거나 데이터를 못 찾으면 리스트로 튕기기 (400 에러 방지)
        if (dto == null) return "redirect:/material/materiallist";

        model.addAttribute("dto", dto);
        return "material/materialdetail"; 
    }
	/* @GetMapping("/materialdetail") public String
	 * detail(@RequestParam("materialid") int materialid, Model model) { MaterialDto
	 * dto = service.selectMaterial(materialid);
	 * 
	 * model.addAttribute("dto", dto); return "material/materialdetail"; }
	 */
	
	/*
	 * @GetMapping("/materialdetail") public String
	 * materialDetail(@RequestParam("materialid") int materialid, Model model) { //
	 * service.selectDetail -> service.selectMaterial로 변경 MaterialDto dto =
	 * service.selectMaterial(materialid);
	 * 
	 * model.addAttribute("dto", dto);
	 * 
	 * return "material/materialdetail"; }
	 */

   

    /////////////////////// User - Detail(Ajax)
    @GetMapping("/materialdetailAjax")
    public String materialdetailAjax(@RequestParam("materialid") int materialid, Model model) {
        MaterialDto dto = service.selectMaterial(materialid);
        model.addAttribute("dto", dto);
        return "material/materialdetailAjax";
    }

    /////////////////////// Admin - List (Paging)
    // /admin/materiallist?pstartno=1
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/materiallist")
    public String adminMaterialList(
            Model model,
            @RequestParam(value = "pstartno", defaultValue = "1") int pstartno,
            @RequestParam(value = "keyword", required = false) String keyword) {

        // 1. 서비스에서 검색어(keyword)를 포함해 10개씩 가져옴
        model.addAttribute("list", service.select10(pstartno, keyword));

        // 2. 검색어에 맞는 전체 개수로 페이징 처리
        int totalCnt = service.selectTotalCnt(keyword);
        model.addAttribute("paging", new PagingDto(totalCnt, pstartno));
        
        // 3. 검색창에 검색어가 남도록 다시 넘겨줌
        model.addAttribute("keyword", keyword);

        return "material/materiallist_admin"; // 2번 이미지의 관리자 목록 페이지
    }
	/*
	 * @PreAuthorize("hasRole('ROLE_ADMIN')")
	 * 
	 * @GetMapping("/materiallist") public String adminMaterialList( Model model,
	 * 
	 * @RequestParam(value = "pstartno", defaultValue = "1") int pstartno,
	 * 
	 * @RequestParam(value = "keyword", required = false) String keyword) {
	 * 
	 * // 1. 서비스에 검색어와 페이지 번호를 전달하여 리스트 조회 // (service.select10 내부에서 keyword 유무에 따라
	 * 쿼리를 분기하도록 수정 필요) model.addAttribute("list", service.select10(pstartno,
	 * keyword));
	 * 
	 * // 2. 전체 개수 조회 시에도 검색어가 있으면 검색된 결과의 개수만 가져와야 페이징이 정확합니다. int totalCnt =
	 * service.selectTotalCnt(keyword); model.addAttribute("paging", new
	 * PagingDto(totalCnt, pstartno));
	 * 
	 * // 3. 뷰에서 검색어를 유지하기 위해 다시 넘겨줌 model.addAttribute("keyword", keyword);
	 * 
	 * return "material/materiallist_admin"; }
	 */
	

    /////////////////////// Admin - Insert Form
    // /admin/materialinsert
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/materialinsert")
    public String materialinsertForm(HttpSession session) {
        return "material/materialinsert";
    }

    /////////////////////// Admin - Insert
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/materialinsert", headers = ("content-type=multipart/*"))
    public String materialinsert(
            @RequestParam(value = "file", required = false) MultipartFile file, // 필수 해제
            MaterialDto dto,
            RedirectAttributes rttr,
            HttpSession session) {

        String result = "추가 실패";
        if (service.insert2Material(file, dto) > 0) {
            result = "추가 성공";
        }
        rttr.addFlashAttribute("success", result);
        return "redirect:/material/materiallist";
    }

    /////////////////////// Admin - Edit Form
    // /admin/materialedit?materialid=2
    // 일반 유저가 주소치고 들어오는거 방지
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/materialedit")
    public String materialeditForm(@RequestParam("materialid") int materialid, Model model, HttpSession session) {
        MaterialDto dto = service.selectMaterial(materialid);
        model.addAttribute("dto", dto);
        return "material/materialedit";
    }

    /////////////////////// Admin - Edit
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/materialedit", headers = ("content-type=multipart/*"))
    public String materialedit(
    		@RequestParam(value="file", required=false) MultipartFile file,
            MaterialDto dto,
            HttpSession session,
            RedirectAttributes rttr) {

        String result = "수정 실패";
        if (service.update2Material(file, dto) > 0) {
            result = "수정 성공";
        }
        rttr.addFlashAttribute("success", result);
        return "redirect:/material/materiallist";
    }

    /////////////////////// Admin - Delete
    // /admin/materialdelete?materialid=2
   
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/materialdelete")
    public String materialdelete(@RequestParam("materialid") int materialid, RedirectAttributes rttr) {
        String result = "삭제 실패";
        if (service.deleteMaterial(materialid) > 0) {
            result = "삭제 성공";
        }
        rttr.addFlashAttribute("success", result);
        return "redirect:/material/materiallist";
    }

    /////////////////////// User - Search by Title (alias/연결용)
    // /materialtitle?title=양파
    @GetMapping("/materialtitle")
    public String selecttitle(@RequestParam("title") String title, Model model) {
        MaterialDto dto = service.selectTitle(title);
        model.addAttribute("dto", dto);
        return "material/materialdetail";
    }

    /////////////////////// Recipe -> Material Search (JSON)
//    @GetMapping("/materialsearch")
//    @ResponseBody
//    public Map<String, Object> materialsearch(@RequestParam("recipeId") int recipeId) {
//        Map<String, Object> result = new HashMap<>();
//        result.put("result", recipeService.selectRecipeDetail(recipeId));
//        return result;
//    }

    /////////////////////// Material Title Check (AJAX)
    @GetMapping("/material/check")
    @ResponseBody
    public boolean checkMaterial(@RequestParam("title") String title) {
        MaterialDto dto = service.selectTitle(title);
        return dto != null;
    }
    
    @GetMapping("/allergy")
    @ResponseBody
    public String getAllergyInfo(@RequestParam String keyword) {
        try {
            // 1. 기본 주소
            String baseUrl = "https://apis.data.go.kr/B553748/CertImgListServiceV3/getCertImgListServiceV3";
            
            // 2. 인증키 (Encoding된 키를 그대로 복사해서 넣으세요)
            String serviceKey = "d1dbaa48990a65bd6404577b3a7b2de5afbde095cdee258bfeba049b8945d2c7"; 

            // 3. 한글 키워드만 미리 인코딩 (초코파이 -> %EC%B4%88...)
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");

            // 4. 전체 주소를 문자열로 직접 연결 (가장 확실함)
            // 주의: serviceKey 앞뒤에 공백이 없는지 확인하세요!
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("?serviceKey=").append(serviceKey);
            urlBuilder.append("&prdlstNm=").append(encodedKeyword);
            urlBuilder.append("&returnType=json");
            urlBuilder.append("&pageNo=1");
            urlBuilder.append("&numOfRows=1");

            String fullUrl = urlBuilder.toString();
            System.out.println("최종 요청 주소: " + fullUrl); // 콘솔에서 주소 클릭해보기 위함

            // 5. 호출 (인코딩을 아예 하지 않도록 설정된 RestTemplate 사용)
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            
            // 중요: 주소에 이미 %가 다 들어있으므로 RestTemplate이 다시 인코딩하지 못하게 함
            return restTemplate.getForObject(new java.net.URI(fullUrl), String.class);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"최종 호출 에러: " + e.getMessage() + "\"}";
        }
    }
}
