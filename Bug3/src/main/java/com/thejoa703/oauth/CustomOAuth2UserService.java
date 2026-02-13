package com.thejoa703.oauth;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.thejoa703.dao.AdminDao;
import com.thejoa703.dto.UserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor // 생성자 주입을 위해 추가 (AdminDao 등을 쓰기 위함)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AdminDao adminDao; // DB 조회를 위해 주입

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 1. 소셜 로그인에서 제공하는 이메일 추출
        String email = oAuth2User.getAttribute("email");
        
        // 2. DB에서 해당 이메일로 유저 정보 조회
        // (가정: adminDao에 findByEmail 메서드가 있거나 새로 만들어야 함)
        UserDto userDto = adminDao.findByEmail(email); 
        
        String role = "USER"; // 기본값
        if (userDto != null) {
            role = userDto.getRole(); // DB에 저장된 ROLE (ADMIN 등) 가져오기
        }

        log.info("로그인 시도 유저 권한: ROLE_{}", role);

        // 3. 권한 부여 시 "ROLE_" 접두사 붙이기
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)), 
            oAuth2User.getAttributes(), 
            "email" 
        );
    }
}