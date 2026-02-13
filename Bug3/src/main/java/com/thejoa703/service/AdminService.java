package com.thejoa703.service;

import java.util.List;
import java.util.Map;

public interface AdminService {
    // 회원 목록 가져오기
    List<Map<String, Object>> findAllUsers();

    // 회원 상태 업데이트 (컨트롤러가 부르는 메서드명과 일치해야 함)
    void updateUserStatus(Map<String, Object> params);
}