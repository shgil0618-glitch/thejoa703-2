package com.thejoa703.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.thejoa703.dao.CommentDao;
import com.thejoa703.dto.CommentDto;

@Service // ⭐ 이 어노테이션이 있어야 컨트롤러에서 주입받을 수 있어!
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;

    // 생성자 주입
    public CommentServiceImpl(CommentDao commentDao) {
        this.commentDao = commentDao;
    }

    @Override
    public List<CommentDto> getComments(int reviewId) {
        // 특정 리뷰에 달린 댓글 리스트 가져오기
        return commentDao.findByReviewId(reviewId);
    }

    @Override
    public void addComment(CommentDto dto) {
        // 댓글 저장하기
        commentDao.insertComment(dto);
    }

    @Override
    public void deleteComment(int commentId) {
        commentDao.deleteComment(commentId); 
    }
    @Override
    public void modifyComment(CommentDto dto) {
        commentDao.updateComment(dto);
    }

    @Override
    public CommentDto getCommentById(int commentId) {
        return commentDao.findById(commentId); // 이 결과가 null이면 에러가 납니다!
    }

	@Override
	public void updateComment(CommentDto dto) {
		// TODO Auto-generated method stub
		
	}
}