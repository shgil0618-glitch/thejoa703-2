package com.thejoa703.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thejoa703.dao.UserStatusDao;
import com.thejoa703.dto.AdminUserStatusDto;
import com.thejoa703.dto.UserStatusDto;
import com.thejoa703.external.ApiEmailNaver;

@Service
public class UserStatusServiceImpl implements UserStatusService {

    @Autowired
    private UserStatusDao userStatusDao;

    @Autowired
    private ApiEmailNaver apiEmailNaver;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserList(String keyword, int page) {
        int limit = 10;
        int offset = (page - 1) * limit;

        List<AdminUserStatusDto> users = userStatusDao.findAllUserStatus(keyword, offset, limit);
        int totalCount = userStatusDao.countUsers(keyword);
        int totalPages = (int) Math.ceil((double) totalCount / limit);

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("keyword", keyword);
        
        return result;
    }

    @Override
    @Transactional
    public void suspendUser(int appUserId, String reason, String untilDate) {
        // 1. 상태 데이터 존재 여부 확인 (소셜 가입자 대응)
        UserStatusDto existingStatus = userStatusDao.findByAppUserId(appUserId);
        
        UserStatusDto dto = new UserStatusDto();
        dto.setAppUserId(appUserId);
        dto.setStatus("SUSPEND");
        dto.setSuspendReason(reason);
        dto.setSuspendUntil(LocalDate.parse(untilDate));

        if (existingStatus == null) {
            // 데이터가 없으면 신규 삽입 후 업데이트
            userStatusDao.insert(dto);
        }
        userStatusDao.update(dto);

        // 2. 이메일 발송
        AdminUserStatusDto user = userStatusDao.findAdminUserByAppUserId(appUserId);
        if (user != null && user.getEmail() != null) {
            String subject = "[오늘 뭐먹지?] 계정 이용 제한 안내";
            String content = "회원님의 계정이 정지되었습니다.<br>사유: " + reason + "<br>기한: " + untilDate;
            apiEmailNaver.sendMail(subject, content, user.getEmail());
        }
    }

    @Override
    @Transactional
    public void activateUser(int appUserId) {
        UserStatusDto dto = new UserStatusDto();
        dto.setAppUserId(appUserId);
        dto.setStatus("ACTIVE");
        dto.setSuspendReason(null);
        dto.setSuspendUntil(null);
        userStatusDao.update(dto);
    }

    @Override
    @Transactional
    public void createDefaultStatus(int appUserId) {
        UserStatusDto dto = new UserStatusDto();
        dto.setAppUserId(appUserId);
        dto.setStatus("ACTIVE");
        userStatusDao.insert(dto);
    }
}