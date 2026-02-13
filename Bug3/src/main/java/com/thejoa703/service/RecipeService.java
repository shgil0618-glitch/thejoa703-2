package com.thejoa703.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.thejoa703.dto.Recipes3Dto;
import com.thejoa703.dto.RecipesIngre3;
import com.thejoa703.dto.RecipesStep3;
import com.thejoa703.dto.SearchDto;

public interface RecipeService {

    // 레시피 CRUD
    public int createRecipe(MultipartFile imageFile, Recipes3Dto dto, List<MultipartFile> stepImages);
    public int updateRecipe(MultipartFile imageFile, Recipes3Dto dto, List<MultipartFile> stepImages);
    public int deleteRecipe(int recipeId);
    public Recipes3Dto getRecipeById(int recipeId, Integer appUserId);


    // 목록 / 검색 / 페이징
    public List<Recipes3Dto> selectRecipeAllPaged(Map<String, Object> params);
    public int countAll(Integer category);
    public int countSearchRecipes(Map<String, Object> params);
    public List<Recipes3Dto> searchRecipesPaged(Map<String, Object> params);
    public Map<String, Object> searchRecipes(SearchDto condition);
    
    public Map<String,Object> listSearchRecipes(Map<String,Object> params);



    // 조회수
    public int incrementViews(int recipeId);

    // 재료 / 단계
    public List<RecipesIngre3> getIngredients(int recipeId);
    public List<RecipesStep3> getSteps(int recipeId);

    // 좋아요
    public void likeRecipe(int appUserId, int recipeId);
    public void unlikeRecipe(int appUserId, int recipeId);
    public int countLikesByRecipe(int recipeId);

    // 검색 기록
    public boolean saveSearchHistory(Integer appUserId, String keyword);
    public List<Map<String, Object>> topKeywords(int limit);
    
    // ✅ 비속어 검출 관련 메서드 선언 추가
    public boolean containsBadWord(String text);
    public boolean recipeHasBadWords(Recipes3Dto dto);

    // 비속어 관리
    public List<Map<String, Object>> getAllBadWords();
    public void addBadWord(String word);
    public void deleteBadWordById(int wordId);
    public void filterBadWordsAndUpdateStatus();
    public Map<String,Object> getBadWordsPaged(int currentPage);
    public void changeRecipeStatus(int recipeId, String status);

    // AI 사용 기록 관리
    public List<Map<String, Object>> getAllAiUsage();
    public void deleteAiUsageById(int aiHistId);

    // 카테고리
    public List<Map<String, Object>> getAllCategories();
    public String getCategoryName(int category);

    // 내가 작성한 레시피 목록
    public List<Recipes3Dto> selectMyRecipes(int appUserId);

    // 내가 좋아요 표시한 레시피 목록
    public List<Recipes3Dto> selectLikedRecipes(int appUserId);
    
    //관리자용
 // 관리자용
    public List<Recipes3Dto> selectAdminRecipePaged(Map<String,Object> params);
    public int countAdminRecipes(Map<String,Object> params);
    public int deleteAdminRecipe(int recipeId); // 내부에서 재료/단계/좋아요까지 처리
    
    // 랜덤 추천
    public Recipes3Dto getRandomRecipe(Integer category, String keyword, String highQuality);

}