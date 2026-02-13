package com.thejoa703.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thejoa703.dao.CommentDao;
import com.thejoa703.dao.ReviewDao;
import com.thejoa703.dto.CommentDto;
import com.thejoa703.dto.ReviewDto;

@Service
public class ReviewServiceImpl implements ReviewService {
	

    @Autowired
    private ReviewDao reviewDao;

    @Override
    public List<ReviewDto> getReviewsByRecipeId(int recipeId, int page) {
        int pageSize = 5; // 한 페이지에 보여줄 리뷰 개수
        
        // [산수 공식]
        // 1페이지: (1-1)*5 + 1 = 1번부터, 1*5 = 5번까지
        // 2페이지: (2-1)*5 + 1 = 6번부터, 2*5 = 10번까지
        int startRow = (page - 1) * pageSize + 1;
        int endRow = page * pageSize;

        return reviewDao.findByRecipeId(recipeId, startRow, endRow);
    }
    
    @Override
    public int getReviewCount(int recipeId) {
        return reviewDao.countByRecipeId(recipeId);
    }

    @Override
    public int writeReview(ReviewDto dto) {
        return reviewDao.insertReview(dto);
    }
    
    
    @Override
    public int updateReview(ReviewDto dto) { return reviewDao.updateReview(dto); }

    @Override
    public int deleteReview(int reviewId) { return reviewDao.deleteReview(reviewId); }

    @Override
    public ReviewDto getReviewById(int reviewId) { return reviewDao.findByReviewId(reviewId); }
    
    @Override
    public double getAverageRating(int recipeId) {    
        return reviewDao.getAverageRating(recipeId);
    }
//    @Override
//    public List<ReviewDto> getReviewList(int recipeId) {
//        // null 대신 실제 DAO를 호출하도록 수정
//        return reviewDao.findByRecipeId(recipeId);
//    }
    
    @Autowired
    private CommentDao commentDao;

    // ... 다른 메서드들 (getComments, addComment 등)

    @Override
    public void updateComment(CommentDto dto) {
        // 여기서 로그를 찍어보면 쿼리 실행 직전인지 알 수 있습니다.
        System.out.println("DAO 호출 직전 ID: " + dto.getCommentId());
        commentDao.updateComment(dto);
    }
    
    @Override
    public CommentDto getCommentById(int commentId) {
        return commentDao.findById(commentId);
    }
    
    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewDao.getAllReviews(); // DAO의 메서드를 호출합니다.
    }
    
}
