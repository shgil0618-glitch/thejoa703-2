package com.thejoa703.security;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.thejoa703.dao.AppUserDao;
import com.thejoa703.dao.UserStatusDao;
import com.thejoa703.dto.AppUserAuthDto;
import com.thejoa703.dto.AppUserDto;
import com.thejoa703.dto.UserStatusDto;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserDao userDao;

    @Autowired
    private UserStatusDao userStatusDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. 아이디 및 로그인 방식 분리
        String[] parts = username.split(":");
        String email = parts[0];
        String provider = parts.length > 1 ? parts[1] : "local";

        AppUserDto param = new AppUserDto();
        param.setEmail(email);
        param.setProvider(provider);

        // 2. 인증 및 사용자 정보 조회
        AppUserAuthDto authDto = userDao.readAuthByEmail(param);
        if (authDto == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        AppUserDto user = userDao.findByEmail(param);
        if (user == null) {
            throw new UsernameNotFoundException("사용자 기본정보를 찾을 수 없습니다: " + username);
        }

        // 3. [영민님 코드 반영] 콘솔 디버깅 로그 출력
        System.out.println("----------------------------------------");
        System.out.println("로그인 시도 이메일: " + email);
        System.out.println("조회된 유저 ID: " + user.getAppUserId());
        System.out.println("----------------------------------------");

        // 4. [팀원 코드 반영] 정지 상태 확인 및 자동 복구 로직
        Integer appUserId = user.getAppUserId();
        UserStatusDto statusDto = userStatusDao.findByAppUserId(appUserId);
     // ⭐ statusDto가 null인지 먼저 확인하고, null이 아닐 때만 getStatus()를 호출합니다.
        if (statusDto != null && statusDto.getStatus() != null) {
            if ("SUSPEND".equalsIgnoreCase(statusDto.getStatus())) {
                String reason = (statusDto.getSuspendReason() != null) ? statusDto.getSuspendReason() : "사유 미정";
                String untilDate = (statusDto.getSuspendUntil() != null) ? statusDto.getSuspendUntil().toString() : "미정";
                
                String errorMsg = "활동이 정지된 계정입니다. (사유: " + reason + " / 기한: " + untilDate + ")";
                System.out.println("!!! 차단 시스템 작동: " + email + " 접속 거부 (" + errorMsg + ")");
                
                throw new DisabledException(errorMsg);
            }
        } else {
            // 데이터가 없거나(null) 상태값이 없으면 정상 유저로 간주하고 로그만 찍고 넘어감
            System.out.println(">>> [알림] " + email + " 계정의 상태 정보가 없어 정상 계정으로 처리합니다.");
        }

        // 5. 로그인 성공
        return new CustomUserDetails(user, authDto);
    }
}