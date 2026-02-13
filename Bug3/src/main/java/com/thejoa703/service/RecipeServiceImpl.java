package com.thejoa703.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.thejoa703.dao.Recipes3Dao;
import com.thejoa703.dto.PagingDto;
import com.thejoa703.dto.Recipes3Dto;
import com.thejoa703.dto.RecipesIngre3;
import com.thejoa703.dto.RecipesStep3;
import com.thejoa703.dto.ReviewDto;
import com.thejoa703.dto.SearchDto;
import com.thejoa703.external.ApiModeration;
import com.thejoa703.external.OpenAIEmbeddingService;
import com.thejoa703.util.UtilPaging;
import com.thejoa703.util.UtilUpload;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RecipeServiceImpl implements RecipeService {
	
	@Autowired
    private ReviewService reviewService;
	
	@Autowired
	private Recipes3Dao recipeDao;

	@Autowired
	private UtilUpload utilUpload; // 파일 업로드 유틸

	// 공통 파일 업로드 처리
	private String uploadFile(MultipartFile file, String existingFile) {
		if (file != null && !file.isEmpty()) {
			try {
				return utilUpload.fileUpload(file);
			} catch (IOException e) {
				throw new RuntimeException("파일 업로드 실패", e);
			}
		}
		return existingFile; // 새 파일 없으면 기존 값 유지
	}

	// 📌 레시피 등록 (대표 이미지 + 재료 + 단계 + 단계 이미지)
	@Transactional
	@Override
	public int createRecipe(MultipartFile imageFile, Recipes3Dto dto, List<MultipartFile> stepImages) {

		// 🔎 비속어 검출 
				if (recipeHasBadWords(dto))
				{ dto.setStatus("PRIVATE"); // 비속어 걸리면 비공개 처리 
				} else { dto.setStatus("PUBLIC"); // 기본 공개 
				}
		
	    // 1️⃣ 대표 이미지 업로드
	    dto.setImage(uploadFile(imageFile, dto.getImage()));

	    // 2️⃣ 레시피 기본 정보 저장 (selectKey → recipeId 자동 세팅)
	    int result = recipeDao.insertRecipe(dto);

	    int recipeId = dto.getRecipeId();
	    if (recipeId <= 0) {
	        throw new IllegalStateException("recipeId 생성 실패");
	    }

	    // 3️⃣ 재료 저장
	    if (dto.getIngredients() != null) {
	        for (RecipesIngre3 ingre : dto.getIngredients()) {
	            ingre.setRecipeId(recipeId);
	            result += recipeDao.insertIngre(ingre);
	        }
	    }

	    // 4️⃣ 단계 저장 + 단계 이미지
	    if (dto.getSteps() != null) {
	        for (int i = 0; i < dto.getSteps().size(); i++) {
	            RecipesStep3 step = dto.getSteps().get(i);
	            step.setRecipeId(recipeId);

	            MultipartFile stepFile =
	                (stepImages != null && stepImages.size() > i) ? stepImages.get(i) : null;

	            step.setStepImage(uploadFile(stepFile, null));

	            result += recipeDao.insertStep(step);
	        }
	    }

	    return result;
	}


	@Transactional
	@Override
	public int updateRecipe(MultipartFile imageFile, Recipes3Dto dto, List<MultipartFile> stepImages) {
		int result = 0;
		int recipeId = dto.getRecipeId();
		
		// 🔎 비속어 검출 
		if (recipeHasBadWords(dto))
		{ dto.setStatus("PRIVATE"); // 비속어 걸리면 비공개 처리 
		} else { dto.setStatus("PUBLIC"); // 기본 공개 
		}
		
		
		// 1) 대표 이미지 교체
		dto.setImage(uploadFile(imageFile, dto.getImage()));
		result += recipeDao.updateRecipe(dto);

		// 2) 기존 재료 삭제 후 재삽입
		recipeDao.deleteIngreByRecipeId(recipeId);
		List<RecipesIngre3> ingredients = dto.getIngredients();
		if (ingredients != null && !ingredients.isEmpty()) {
			for (RecipesIngre3 ingre : ingredients) {
				ingre.setRecipeId(recipeId);
				result += recipeDao.insertIngre(ingre);
			}
		}

		// 3) 기존 단계 삭제 후 재삽입
		recipeDao.deleteStepByRecipeId(recipeId);
		List<RecipesStep3> steps = dto.getSteps();
		if (steps != null && !steps.isEmpty()) {
			for (int i = 0; i < steps.size(); i++) {
				RecipesStep3 step = steps.get(i);
				step.setRecipeId(recipeId);

				MultipartFile stepFile = (stepImages != null && stepImages.size() > i) ? stepImages.get(i) : null;
				step.setStepImage(uploadFile(stepFile, step.getStepImage()));

				result += recipeDao.insertStep(step);
			}
		}

		return result;
	}

	@Transactional
	@Override
	public int deleteRecipe(int recipeId) {
		int result = 0;

		// 1) 재료 삭제
		recipeDao.deleteIngreByRecipeId(recipeId);

		// 2) 단계 삭제
		recipeDao.deleteStepByRecipeId(recipeId);

		// 3) 레시피 삭제
		result += recipeDao.deleteRecipe(recipeId);

		return result;
	}

	
	@Override
	@Transactional
	public Recipes3Dto getRecipeById(int recipeId, Integer appUserId) {
	    recipeDao.incrementViews(recipeId);

	    Map<String,Object> params = new HashMap<>();
	    params.put("recipeId", recipeId);
	    params.put("appUserId", appUserId != null ? appUserId : -1);

	    Recipes3Dto recipe = recipeDao.selectRecipeById(params);
	    if (recipe == null) {
	        return null;
	    }

	    recipe.setIngredients(recipeDao.selectIngreByRecipeId(recipeId));
	    recipe.setSteps(recipeDao.selectStepByRecipeId(recipeId));
	    return recipe;
	}






	@Override
	public List<Recipes3Dto> selectRecipeAllPaged(Map<String, Object> params) {
	    // 로그인 사용자 ID가 없으면 -1 같은 더미 값 넣기
	    if (!params.containsKey("appUserId") || params.get("appUserId") == null) {
	        params.put("appUserId", -1);
	    }
	    return recipeDao.selectRecipeAllPaged(params);
	}


	@Override
	public int countAll(Integer category) {
	    Map<String, Object> params = new HashMap<>();
	    params.put("category", category);
	    params.put("searchField", "ALL");
	    // keyword는 아예 넣지 않거나 빈 문자열로 처리
	    return recipeDao.countSearchRecipes(params);

	}

	@Override
	public int countSearchRecipes(Map<String, Object> params) {
		return recipeDao.countSearchRecipes(params);
	}

	@Override
	public List<Recipes3Dto> searchRecipesPaged(Map<String, Object> params) {
		return recipeDao.searchRecipesPaged(params);
	}
	

	   @Autowired OpenAIEmbeddingService openAIEmbeddingService;

	   @Override
	   public Map<String, Object> searchRecipes(SearchDto condition) {

	       // 🔐 appUserId null 방어 (가장 중요)
	       int appUserId = condition.getAppUserId() != null
	               ? condition.getAppUserId()
	               : 0;

	       // =========================
	       // 1️⃣ 공통 파라미터 세팅
	       // =========================
	       Map<String, Object> params = new HashMap<>();
	       params.put("keyword", condition.getKeyword());
	       params.put("searchField", condition.getSearchField());
	       params.put("sort", condition.getSort());
	       params.put("category", condition.getCategory());
	       params.put("fields", condition.getFields());
	       params.put("appUserId", appUserId);

	       // =========================
	       // 2️⃣ 검색 기록 저장
	       // =========================
	       saveSearchHistory(appUserId, condition.getKeyword());

	       // =========================
	       // 3️⃣ 1차 카운트
	       // =========================
	       int totalCount = recipeDao.countSearchRecipes(params);
	       String suggestion = null;

	       // =========================
	       // 4️⃣ 추천어 처리
	       // =========================
	       if (totalCount == 0
	               && condition.getKeyword() != null
	               && !condition.getKeyword().isBlank()) {

	           List<Map<String, Object>> topKeywords = recipeDao.topKeywords(10);

	           List<String> candidates = topKeywords.stream()
	                   .map(m -> (String) m.get("KEYWORD"))
	                   .filter(Objects::nonNull)
	                   .map(String::trim)
	                   .filter(s -> !s.isEmpty())
	                   .filter(k -> !k.equalsIgnoreCase(condition.getKeyword()))
	                   .distinct()
	                   .collect(Collectors.toList());

	           try {
	               suggestion = openAIEmbeddingService
	                       .recommendKeyword(condition.getKeyword(), candidates);
	           } catch (Exception e) {
	               log.warn("추천어 API 호출 실패: {}", e.getMessage());
	           }

	           if (suggestion != null
	                   && !suggestion.equalsIgnoreCase(condition.getKeyword())) {

	               log.info("검색어 '{}' → 추천어 '{}' 로 교체",
	                       condition.getKeyword(), suggestion);

	               condition.setKeyword(suggestion);

	               // 🔁 params 동기화 (중요)
	               params.put("keyword", suggestion);

	               totalCount = recipeDao.countSearchRecipes(params);
	           }
	       }

	       // =========================
	       // 5️⃣ 페이징 계산
	       // =========================
	       UtilPaging paging = new UtilPaging(totalCount, condition.getCurrentPage());
	       condition.setRStart(paging.getPstartno());
	       condition.setREnd(paging.getPstartno() + paging.getOnepagelist() - 1);

	       params.put("rStart", condition.getRStart());
	       params.put("rEnd", condition.getREnd());

	       // =========================
	       // 6️⃣ 목록 조회 (❤️ likedByMe 여기서 결정됨)
	       // =========================
	       List<Recipes3Dto> list = recipeDao.searchRecipesPaged(params);

	       // =========================
	       // 7️⃣ 재료 / 단계 조립
	       // =========================
	    // RecipeServiceImpl.java 내 searchRecipes 메서드 수정
	       for (Recipes3Dto r : list) {
	           r.setIngredients(recipeDao.selectIngreByRecipeId(r.getRecipeId()));
	           r.setSteps(recipeDao.selectStepByRecipeId(r.getRecipeId()));
	           
	           // ⭐ 개수 제한 없이 해당 페이지의 리뷰를 다 가져옵니다.
	           try {
	               List<ReviewDto> reviews = reviewService.getReviewsByRecipeId(r.getRecipeId(), 1);
	               r.setReviews(reviews); // 여기서 subList로 자르지 마세요!
	           } catch (Exception e) {
	               log.error("리뷰 로드 실패: " + e.getMessage());
	           }
	       }
	       // =========================
	       // 8️⃣ 결과 리턴
	       // =========================
	       Map<String, Object> result = new HashMap<>();
	       result.put("list", list);
	       result.put("paging", paging);
	       result.put("totalCount", totalCount);
	       result.put("suggestion", suggestion);

	       return result;
	   }






	@Transactional
	@Override
	public int incrementViews(int recipeId) {
		return recipeDao.incrementViews(recipeId);
	}

	@Override
	public List<RecipesIngre3> getIngredients(int recipeId) {
		return recipeDao.selectIngreByRecipeId(recipeId);
	}

	@Override
	public List<RecipesStep3> getSteps(int recipeId) {
		return recipeDao.selectStepByRecipeId(recipeId);
	}

	// ---------------------------
	// 좋아요 기능
	// ---------------------------
	@Transactional
	@Override
	public void likeRecipe(int appUserId, int recipeId) {
		Map<String, Object> params = Map.of("appUserId", appUserId, "recipeId", recipeId);
		// 중복 좋아요 방지
		if (recipeDao.existsLike(params) == 0) {
			recipeDao.insertLike(params);
		}
	}

	@Transactional
	@Override
	public void unlikeRecipe(int appUserId, int recipeId) {
		Map<String, Object> params = Map.of("appUserId", appUserId, "recipeId", recipeId);
		recipeDao.deleteLike(params);
	}

	@Override
	public int countLikesByRecipe(int recipeId) {
		return recipeDao.countLikesByRecipe(recipeId);
	}

	// ---------------------------
	// 검색 기록
	// ---------------------------
	@Override
	public boolean saveSearchHistory(Integer appUserId, String keyword) {
	    if (keyword == null || keyword.isBlank()) {
	        return false;
	    }

	    // ✅ 로그인 안 한 경우 게스트 계정 ID 사용
	    int userId = (appUserId != null ? appUserId : 0);

	    Map<String, Object> params = new HashMap<>();
	    params.put("appUserId", userId);
	    params.put("keyword", keyword);

	    int rows = recipeDao.insertSearchHistory(params);
	    return rows > 0;
	}


	@Override
	public List<Map<String, Object>> topKeywords(int limit) {
		return recipeDao.topKeywords(limit);
	}

	// ---------------------------
	// 비속어 관리
	// ---------------------------
	@Override
	public List<Map<String, Object>> getAllBadWords() {
		return recipeDao.selectAllBadWords();
	}
	
	@Override
	public Map<String,Object> getBadWordsPaged(int currentPage) {
	    int totalCount = recipeDao.countBadWords();
	    PagingDto paging = new PagingDto(totalCount, currentPage);

	    Map<String,Object> params = new HashMap<>();
	    params.put("rStart", paging.getRStart());
	    params.put("rEnd", paging.getREnd());

	    List<Map<String,Object>> list = recipeDao.selectBadWordsPaged(params);

	    Map<String,Object> result = new HashMap<>();
	    result.put("list", list);
	    result.put("paging", paging);
	    return result;
	}



	@Transactional
	@Override
	public void addBadWord(String word) {
	    if (word == null || word.isBlank()) return;
	    if (recipeDao.existsBadWord(word) == 0) {
	        Map<String, Object> params = new HashMap<>();
	        params.put("word", word);
	        recipeDao.insertBadWord(params);
	    }
	}


	@Transactional
	@Override
	public void deleteBadWordById(int wordId) {
		recipeDao.deleteBadWordById(wordId);
	}
	
	// RecipeServiceImpl 내부

	// RecipeServiceImpl 내부

	public boolean containsBadWord(String text) {
	    if (text == null || text.isBlank()) return false;

	    List<Map<String, Object>> badWords = recipeDao.selectAllBadWords();
	    String lower = text.toLowerCase();

	    for (Map<String, Object> bw : badWords) {
	        Object w = bw.get("WORD");
	        if (w == null) w = bw.get("word");
	        if (w == null) continue;

	        String bad = String.valueOf(w).toLowerCase();
	        if (!bad.isBlank() && lower.contains(bad)) {
	            return true;
	        }
	    }
	    return false;
	}

	public boolean recipeHasBadWords(Recipes3Dto dto) {
	    if (containsBadWord(dto.getTitle())) return true;
	    if (containsBadWord(dto.getDescription())) return true;
	    if (dto.getSteps() != null) {
	        for (RecipesStep3 step : dto.getSteps()) {
	            if (containsBadWord(step.getDescription())) return true;
	        }
	    }
	    return false;
	}


	@Autowired ApiModeration apiModeration;
	@Transactional
	@Override
	public void filterBadWordsAndUpdateStatus() {
	    List<Recipes3Dto> allRecipes = recipeDao.selectRecipeAllPaged(Map.of("appUserId", -1)); // 전체 조회
	    
	    for (Recipes3Dto recipe : allRecipes) {
	        boolean flagged = false;

	        // 1️⃣ DB 비속어 체크
	        List<Map<String, Object>> badWords = recipeDao.selectAllBadWords();
	        for (Map<String, Object> bw : badWords) {
	            if (recipe.getTitle().contains((String) bw.get("word"))) {
	                flagged = true;
	                break;
	            }
	        }

	        // 2️⃣ AI Moderation 체크 (DB에서 안 걸렸을 때만)
	        if (!flagged) {
	            if (apiModeration.detectBadWords(recipe.getTitle())) {
	                flagged = true;
	            }
	        }

	        // 3️⃣ 필터링에 걸리면 status 변경
	        if (flagged) {
	            recipeDao.updateRecipeStatus(Map.of(
	                "recipeId", recipe.getRecipeId(),
	                "status", "PRIVATE"
	            ));
	        }
	    }
	}


	// ---------------------------
	// AI 사용 기록 관리
	// ---------------------------
	@Override
	public List<Map<String, Object>> getAllAiUsage() {
		return recipeDao.selectAllAiUsage();
	}

	@Transactional
	@Override
	public void deleteAiUsageById(int aiHistId) {
		recipeDao.deleteAiUsageById(aiHistId);
	}

	// ---------------------------
	// 카테고리
	// ---------------------------
	@Override
	public List<Map<String, Object>> getAllCategories() {
		return recipeDao.selectAllCategories();
	}

	@Override
	public String getCategoryName(int category) {
		return recipeDao.selectCategoryName(category);
	}

	// ---------------------------
	// 내 레시피 / 좋아요 레시피 조회
	// ---------------------------
	@Override
	public List<Recipes3Dto> selectMyRecipes(int appUserId) {
		return recipeDao.selectMyRecipes(appUserId);
	}

	@Override
	public List<Recipes3Dto> selectLikedRecipes(int appUserId) {
		return recipeDao.selectLikedRecipes(appUserId);
	}
	
	
	///////////////////////// 관리자용
	
	@Override
	public List<Recipes3Dto> selectAdminRecipePaged(Map<String,Object> params){
	    return recipeDao.selectAdminRecipePaged(params);
	}

	@Override
	public int countAdminRecipes(Map<String,Object> params){
	    return recipeDao.countAdminRecipes(params);
	}

	@Override
	@Transactional
	public int deleteAdminRecipe(int recipeId){
	    recipeDao.deleteAdminRecipeSteps(recipeId);
	    recipeDao.deleteAdminRecipeIngredients(recipeId);
	    recipeDao.deleteAdminRecipeLikes(recipeId);
	    return recipeDao.deleteAdminRecipe(recipeId);
	}
	
	@Override 
	public Map<String,Object> listSearchRecipes(Map<String,Object> params) { 
		// 총 개수 
		int totalCount = recipeDao.countListSearchRecipes(params); // 페이징 계산 
		int currentPage = (int) params.getOrDefault("currentPage", 1); 
		PagingDto paging = new PagingDto(totalCount, currentPage);
		params.put("rStart", paging.getRStart()); 
		params.put("rEnd", paging.getREnd()); 
		// 목록 조회 
		List<Recipes3Dto> list = recipeDao.listSearchRecipesPaged(params);
		Map<String,Object> result = new HashMap<>(); 
		result.put("list", list);
		result.put("paging", paging);
		return result; 
		}

	 @Transactional
	    @Override
	    public void changeRecipeStatus(int recipeId, String status) {
	        Map<String, Object> param = new HashMap<>();
	        param.put("recipeId", recipeId);
	        param.put("status", status);

	        recipeDao.updateRecipeStatus(param);
	    }
	
	
	// RecipeServiceImpl.java 에 추가
	 @Override
	 public Recipes3Dto getRandomRecipe(Integer category, String keyword, String highQuality) {
	     Map<String, Object> params = new HashMap<>();
	     params.put("category", category);
	     params.put("keyword", keyword);
	     params.put("highQuality", highQuality);
	     return recipeDao.selectRandomRecipe(params);
	 }
	

}