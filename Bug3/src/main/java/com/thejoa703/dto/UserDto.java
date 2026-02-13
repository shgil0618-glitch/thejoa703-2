package com.thejoa703.dto;

import java.util.Date;
import lombok.Data;

@Data // Lombok을 사용 중이라면 @Data 하나면 충분합니다.
public class UserDto {
    // BUG3 테이블 컬럼
    private int appUserId;
    private String email;
    private String nickname;
    private String password;
    private String provider;
    private String providerId;
    private String mobile;
    private String role;           // USER, ADMIN
    private String ufile;          // 프로필 이미지 경로
    private Date createdAt;
    private String postcode;
    private String address;
    private String detailAddress;

    // BUG3_MANAGE 테이블 연동 필드 (관리자 페이지용)
    // 회원의 현재 상태 (예: NORMAL, SUSPENDED)
    private String status;         
    private String suspendReason;  // 정지 사유
    private Date suspendUntil;     // 정지 기한
}