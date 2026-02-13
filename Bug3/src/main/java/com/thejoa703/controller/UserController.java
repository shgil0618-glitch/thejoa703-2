package com.thejoa703.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thejoa703.dto.AppUserDto;
import com.thejoa703.security.CustomUserDetails;
import com.thejoa703.service.AppUserService;

@Controller
@RequestMapping("/users")
public class UserController {

	@Autowired
	private AppUserService userService;

	/* ================= 회원가입 ================= */

	@PreAuthorize("permitAll()")
	@RequestMapping("/iddouble")
	@ResponseBody
	public Map<String, Object> iddouble(@RequestParam String email, @RequestParam String provider) {

		Map<String, Object> result = new HashMap<>();
		result.put("cnt", userService.iddouble(email, provider));
		return result;
	}

	@GetMapping("/join")
	public String joinForm() {
		return "users/join";
	}

	@PostMapping("/join")
	public String join(@RequestParam(value = "file", required = false) MultipartFile file, 
			           @RequestParam("g-recaptcha-response") String recaptchaResponse, // ✅ reCAPTCHA 파라미터 추가
			           AppUserDto dto,
			           RedirectAttributes rttr) {

		// 1. reCAPTCHA 검증 (서비스에 구현된 verifyRecaptcha 호출)
		if (!userService.verifyRecaptcha(recaptchaResponse)) {
			rttr.addFlashAttribute("errorMessage", "로봇 방지 인증에 실패했습니다. 다시 시도해주세요.");
			return "redirect:/users/join";
		}

		try {
			int result = userService.insert(file, dto);
			rttr.addFlashAttribute("successMessage", result > 0 ? "회원가입 성공!" : "회원가입 실패");
			return "redirect:/users/login";
		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "회원가입 실패: " + e.getMessage());
			return "redirect:/users/join";
		}
	}

	/* ================= 로그인 ================= */

	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error,
	                    HttpServletRequest request,
	                    Model model) {
	    
	    if (error != null) {
	        HttpSession session = request.getSession(false);
	        if (session != null) {
	            String msg = (String) session.getAttribute("errorMessage");
	            model.addAttribute("errorMessage", msg);
	            session.removeAttribute("errorMessage"); // 노출 후 세션에서 제거
	        }
	    }
	    return "users/login";
	}

	/* ================= 마이페이지 ================= */

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/mypage")
	public String mypage(Authentication authentication, Model model) {

		AppUserDto dto = resolveLoginUser(authentication);
		if (dto != null) {
			dto = userService.selectEmail(dto.getEmail(), dto.getProvider());
		}

		model.addAttribute("dto", dto);
		return "users/mypage";
	}

	/* ================= 회원정보 수정 ================= */

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/update")
	public String updateForm(Authentication authentication, Model model) {

		// ✅ 로그인 정보 기준
		AppUserDto loginUser = resolveLoginUser(authentication);

		if (loginUser == null) {
			return "redirect:/login";
		}

		// ✅ email + provider 정확히 조회
		AppUserDto dto = userService.selectEmail(loginUser.getEmail(), loginUser.getProvider());

		model.addAttribute("dto", dto);
		return "users/update";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/update")
	public String update(@RequestParam(value = "file", required = false) MultipartFile file, AppUserDto dto,
			Authentication authentication, RedirectAttributes rttr) {

		// 🔥🔥🔥 핵심: 폼 값 무시하고 로그인 정보로 덮어쓰기
		AppUserDto loginUser = resolveLoginUser(authentication);

		dto.setEmail(loginUser.getEmail());
		dto.setProvider(loginUser.getProvider());

		int result = userService.update(file, dto);

		rttr.addFlashAttribute("successMessage", result > 0 ? "회원정보 수정 성공" : "회원정보 수정 실패");

		return "redirect:/users/mypage";
	}

	/* ================= 회원탈퇴 ================= */

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete")
	public String deleteForm(Authentication authentication, Model model) {

		AppUserDto dto = resolveLoginUser(authentication);
		if (dto != null) {
			dto = userService.selectEmail(dto.getEmail(), dto.getProvider());
		}

		model.addAttribute("dto", dto);
		return "users/delete";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/delete")
	public String delete(AppUserDto dto, RedirectAttributes rttr, Authentication authentication,
			HttpServletRequest request, HttpServletResponse response) {

		AppUserDto loginUser = resolveLoginUser(authentication);
		if (loginUser == null) {
			return "redirect:/users/login";
		}

		dto.setEmail(loginUser.getEmail());
		dto.setProvider(loginUser.getProvider());

		boolean requirePasswordCheck = "local".equalsIgnoreCase(loginUser.getProvider());

		if (requirePasswordCheck) {
			if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
				return "redirect:/users/delete";
			}

			if (!userService.matchesPassword(dto.getEmail(), dto.getProvider(), dto.getPassword())) {

				rttr.addFlashAttribute("errorMessage", "회원탈퇴 실패: 비밀번호가 일치하지 않습니다.");
				return "redirect:/users/delete";
			}
		}

		if (userService.delete(dto, requirePasswordCheck) > 0) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				new SecurityContextLogoutHandler().logout(request, response, auth);
			}
			rttr.addFlashAttribute("successMessage", "회원탈퇴가 완료되었습니다.");
		}

		return "redirect:/users/login";
	}

	/* ================= 공통 로그인 사용자 추출 ================= */

	private AppUserDto resolveLoginUser(Authentication authentication) {

		Object principal = authentication.getPrincipal();

		// local
		if (principal instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) principal;
			return userDetails.getUser();
		}

		// social
		if (principal instanceof OAuth2User && authentication instanceof OAuth2AuthenticationToken) {

			OAuth2User oAuth2User = (OAuth2User) principal;
			String email = (String) oAuth2User.getAttributes().get("email");

			String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

			AppUserDto dto = new AppUserDto();
			dto.setEmail(email);
			dto.setProvider(provider);
			return dto;
		}

		return null;
	}

	/* ================= 이메일 찾기 (AJAX) ================= */
	@PostMapping("/find-email")
	@ResponseBody 
	public Map<String, Object> findEmail(@RequestParam String phoneNumber) {
		Map<String, Object> result = new HashMap<>();
		String email = userService.findEmailByPhone(phoneNumber);
		
		if (email != null) {
			result.put("success", true);
			result.put("message", "찾으시는 이메일은 [" + email + "] 입니다.");
		} else {
			result.put("success", false);
			result.put("message", "해당 번호로 등록된 이메일이 없습니다.");
		}
		return result;
	}

	/* ================= 비밀번호 재설정 (AJAX) ================= */
	@PostMapping("/reset-password")
	@ResponseBody
	public Map<String, Object> resetPassword(@RequestParam String email, @RequestParam String phoneNumber,
			@RequestParam String newPassword) {
		Map<String, Object> result = new HashMap<>();
		boolean success = userService.resetPassword(email, phoneNumber, newPassword);
		
		if (success) {
			result.put("success", true);
			result.put("message", "비밀번호가 성공적으로 변경되었습니다.");
		} else {
			result.put("success", false);
			result.put("message", "정보가 일치하지 않아 변경에 실패했습니다.");
		}
		return result;
	}

}