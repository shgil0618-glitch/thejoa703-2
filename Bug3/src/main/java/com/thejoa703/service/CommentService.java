package com.thejoa703.service;

import java.util.List;

import com.thejoa703.dto.CommentDto;

public interface CommentService {
	List<CommentDto> getComments(int reviewId);
    void addComment(CommentDto dto);
	void deleteComment(int commentId);
	void modifyComment(CommentDto dto);
	CommentDto getCommentById(int commentId);
	void updateComment(CommentDto dto);
	
}


