package com.thejoa703.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private int reviewId;
    private int recipeId;
    private int rating;
    private String commentText;
    private int appUserId;    // 필드명 변경 완료
    private LocalDateTime createdAt;
    private List<CommentDto> comments; // 리뷰에 달린 댓글들을 담을 리스트 추가
    
    private String nickname;
    

}