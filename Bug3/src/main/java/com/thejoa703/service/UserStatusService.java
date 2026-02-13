package com.thejoa703.service;

import java.util.Map;

public interface UserStatusService {

    /** 페이징 및 검색이 적용된 사용자 목록 조회 */
    Map<String, Object> getUserList(String keyword, int page);

    /** 정지 해제 */
    void activateUser(int appUserId);

    /** 관리자 정지 및 이메일 발송 */
    void suspendUser(int appUserId, String reason, String untilDate);

    /** 회원 가입 시 기본 상태 생성 */
    void createDefaultStatus(int appUserId);
}