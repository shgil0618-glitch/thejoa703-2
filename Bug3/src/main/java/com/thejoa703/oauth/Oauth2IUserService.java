package com.thejoa703.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thejoa703.dao.AppUserDao;
import com.thejoa703.dao.UserStatusDao;
import com.thejoa703.dto.AppUserAuthDto;
import com.thejoa703.dto.AppUserDto;
import com.thejoa703.dto.AuthDto;
import com.thejoa703.dto.UserStatusDto;
import com.thejoa703.security.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Oauth2IUserService extends DefaultOAuth2UserService {

    @Autowired private AppUserDao dao;
    @Autowired private UserStatusDao userStatusDao;

    // 순환참조 방지를 위해 직접 생성
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        UserInfoOAuth2 info;
        if ("google".equals(provider)) {
            info = new UserInfoGoogle(oAuth2User.getAttributes());
        } else if ("kakao".equals(provider)) {
            info = new UserInfoKakao(oAuth2User.getAttributes());
        } else if ("naver".equals(provider)) {
            info = new UesrInfoNaver(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 provider: " + provider);
        }

        String email = info.getEmail();
        
        // [중요 수정] provider를 따지지 않고 일단 이메일로만 사용자가 있는지 찾습니다.
        // 이메일이 UNIQUE 제약조건이므로, 어떤 소셜이든 이메일이 같으면 같은 사람으로 봅니다.
        AppUserDto searchParam = new AppUserDto();
        searchParam.setEmail(email);
        searchParam.setProvider(provider);
        
        // 기존에 만든 findByEmail이 provider까지 같이 체크한다면 
        // Mapper XML에서 provider 체크를 빼거나, 이메일로만 찾는 별도의 메서드를 써야 합니다.
        AppUserDto user = dao.findByEmail(searchParam); 

        if (user == null) {
            // 1. 아예 생판 처음 온 유저일 때만 INSERT
            user = new AppUserDto();
            user.setEmail(email);
            user.setNickname(info.getNickname() != null ? info.getNickname() : "소셜사용자");
            user.setProvider(provider);
            user.setProviderId(info.getProviderId());
            user.setRole("ROLE_MEMBER");
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            dao.insertAppUser(user);

            AuthDto auth = new AuthDto();
            auth.setEmail(email);
            auth.setAuth("ROLE_MEMBER");
            dao.insertAuth(auth);
            
            log.info("신규 소셜 가입성공 ({}): {}", provider, email);
        } else {
            // 2. 이미 해당 이메일로 가입된 유저가 있다면 INSERT 하지 않고 통과
            log.info("이미 가입된 이메일입니다. 기존 계정으로 로그인합니다: {}", email);
            
            // 만약 기존 유저의 소셜 정보(provider)를 업데이트하고 싶다면 여기서 update
            if (!provider.equals(user.getProvider())) {
                log.info("제공자 정보 업데이트: {} -> {}", user.getProvider(), provider);
            }
        }

        // 권한 및 유저 상세 정보 구성
        // readAuthByEmail 호출 시에도 DB에 있는 기존 유저 정보를 정확히 가져오도록 설정
        AppUserAuthDto authDto = dao.readAuthByEmail(user);
        
        CustomUserDetails customUserDetails = new CustomUserDetails(user, authDto);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("provider", provider);
        customUserDetails.setAttributes(attributes);

        return customUserDetails;
    }
}