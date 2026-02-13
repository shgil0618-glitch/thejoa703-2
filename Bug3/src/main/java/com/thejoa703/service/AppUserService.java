package com.thejoa703.service;

import org.springframework.web.multipart.MultipartFile;
import com.thejoa703.dto.AppUserAuthDto;
import com.thejoa703.dto.AppUserDto;

public interface AppUserService {

    /** 회원가입 (유저정보삽입 + 권한 + 비밀번호암호화) **/
    int insert(MultipartFile file, AppUserDto dto);
    
    /** 회원정보 수정- local **/
    int update(MultipartFile file, AppUserDto dto);
    
    /** 회원탈퇴 - local true / 소셜계정 false **/
    int delete(AppUserDto dto, boolean requirePasswordCheck);
    
    /** 권한조회 - readAuth (로그인) **/
    AppUserAuthDto readAuth(String email, String provider);
    
    /** 사용자조회 (마이페이지) **/
    AppUserDto selectEmail(String email, String provider);
    
    /** 중복체크 **/
    int iddouble(String email, String provider);
    
    /** 비밀번호검증 **/
    boolean matchesPassword(String email, String provider, String rawPassword);

    /** 이메일 찾기 (핸드폰번호 기반) **/
    String findEmailByPhone(String phoneNumber);

    /** 비밀번호 재설정 (이메일+핸드폰 일치 시) **/
    boolean resetPassword(String email, String phoneNumber, String newPassword);
    
    /** 봇 방지 **/
	boolean verifyRecaptcha(String recaptchaResponse);
}
