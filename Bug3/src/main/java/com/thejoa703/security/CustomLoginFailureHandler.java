package com.thejoa703.security;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        HttpSession session = request.getSession();
        String errorMessage = "아이디 또는 비밀번호를 확인하세요."; // 기본 메시지

        // CustomUserDetailsService에서 던진 DisabledException 처리
        if (exception.getCause() instanceof DisabledException) {
            errorMessage = exception.getCause().getMessage();
        } else if (exception instanceof DisabledException) {
            errorMessage = exception.getMessage();
        }

        session.setAttribute("errorMessage", errorMessage);
        response.sendRedirect("/users/login?error=true");
    }
}