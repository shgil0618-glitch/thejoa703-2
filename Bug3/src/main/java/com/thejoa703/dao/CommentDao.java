package com.thejoa703.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.thejoa703.dto.CommentDto;

@Mapper
public interface CommentDao {
    List<CommentDto> findByReviewId(int reviewId); // 특정 리뷰의 댓글들 가져오기
    int insertComment(CommentDto dto);            // 댓글 쓰기
    int deleteComment(int commentId);             // 댓글 삭제  
	int updateComment(CommentDto dto);
	CommentDto findById(int commentId);
	
}