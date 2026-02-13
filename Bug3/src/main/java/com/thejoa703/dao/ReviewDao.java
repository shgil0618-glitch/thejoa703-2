package com.thejoa703.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.thejoa703.dto.ReviewDto;

@Mapper
public interface ReviewDao {
//    List<ReviewDto> findByRecipeId(int recipeId);
    int insertReview(ReviewDto dto);
    
    // ⭐ 추가: 리뷰 수정
    int updateReview(ReviewDto dto);
    
    // ⭐ 추가: 리뷰 삭제
    int deleteReview(int reviewId);

    // ⭐ 추가: 수정을 위해 한 개의 리뷰 정보만 가져오기
    ReviewDto findByReviewId(int reviewId);
    
	double getAverageRating(int recipeId);
	List<String> findTextsByProductId(Integer productId);
	
	List<ReviewDto> findByRecipeId(
			@Param("recipeId") int recipeId, 
            @Param("startRow") int startRow, 
            @Param("endRow") int endRow);
	
	// 총 개수 세기
    int countByRecipeId(int recipeId);

	List<ReviewDto> getAllReviews();
}