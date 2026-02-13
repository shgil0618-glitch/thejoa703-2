package com.thejoa703.dao;

import com.thejoa703.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminDao {
    
    // 이 부분이 반드시 List<Map<String, Object>> 여야 합니다!
    List<Map<String, Object>> findAllUsers();
    
    // 회원 상태 저장/수정
    void upsertUserStatus(Map<String, Object> params);
    
    // 권한 변경
    int updateUserRole(@Param("appUserId") int appUserId, @Param("role") String role);

    // 단일 유저 조회
    UserDto findByEmail(String email);
}