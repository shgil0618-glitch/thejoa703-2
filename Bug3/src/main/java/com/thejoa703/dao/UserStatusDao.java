package com.thejoa703.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.thejoa703.dto.AdminUserStatusDto;
import com.thejoa703.dto.UserStatusDto;

@Mapper
public interface UserStatusDao {
    
    // 1. 페이징 및 검색 (관리자 페이지용)
    List<AdminUserStatusDto> findAllUserStatus(
        @Param("keyword") String keyword, 
        @Param("offset") int offset, 
        @Param("limit") int limit
    );

    // 2. 전체 데이터 개수 (페이징 계산용)
    int countUsers(@Param("keyword") String keyword);

    // 3. 단일 사용자 상태 조회 (로그인/상태확인용)
    UserStatusDto findByAppUserId(Integer appUserId);

    // 4. 최초 생성
    int insert(UserStatusDto dto);

    // 5. 상태 변경
    int update(UserStatusDto dto);

    // 6. 관리자용 상세 정보 조회
    AdminUserStatusDto findAdminUserByAppUserId(Integer appUserId);

    /** * 🔥 에러 해결의 핵심! 
     * CustomUserDetailsService에서 호출하는 메서드를 추가합니다.
     */
    int recoverExpiredSuspension(Integer appUserId);
}