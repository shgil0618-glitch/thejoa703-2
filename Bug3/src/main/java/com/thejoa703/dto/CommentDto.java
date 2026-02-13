package com.thejoa703.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private int commentId;
    private int reviewId;
    private int appUserId;
    private String content;    // DB의 CONTENT 컬럼
    private Date createdAt;
    private Date updatedAt;
    private Integer parentId;  // NULL이 가능해야 하므로 int 대신 Integer 사용!
}