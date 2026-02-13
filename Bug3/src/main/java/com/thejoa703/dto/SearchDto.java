package com.thejoa703.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {
    private String keyword;      // 검색어
    private String searchField;  // TITLE, AUTHOR, DESCRIPTION, ALL
    private List<String> fields; // 추가
    private String sort;         // LATEST, VIEWS, LIKES
    private Integer category;    // 카테고리 ID
    private int currentPage;     // 현재 페이지
    private Integer appUserId; // 로그인 유저 ID

    // 페이징 계산 결과
    private int rStart;
    private int rEnd;
    
    
}