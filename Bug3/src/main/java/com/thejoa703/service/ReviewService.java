package com.thejoa703.service;

import java.util.List;

import com.thejoa703.dto.CommentDto;
import com.thejoa703.dto.ReviewDto;

public interface ReviewService {

    // 리뷰 목록 조회
       List<ReviewDto> getReviewsByRecipeId(int recipeId, int page);
       
       int getReviewCount(int recipeId);

    // 리뷰 등록
    int writeReview(ReviewDto dto);

    int updateReview(ReviewDto dto);          // 리뷰 수정
    int deleteReview(int reviewId);          // 리뷰 삭제
    ReviewDto getReviewById(int reviewId);    // 수정을 위한 리뷰 단건 조회
    double getAverageRating(int recipeId);

//	List<ReviewDto> getReviewList(int recipeId);

	void updateComment(CommentDto dto);
	CommentDto getCommentById(int commentId);

	// ReviewService.java (인터페이스인 경우)
	List<ReviewDto> getAllReviews();
	
	

}

