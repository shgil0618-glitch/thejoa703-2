package com.thejoa703.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.thejoa703.oauth.Oauth2IUserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final Oauth2IUserService oauth2IUserService;
    private final CustomLoginFailureHandler customLoginFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeRequests()
                .antMatchers(
                    "/users/join",
                    "/users/login",
                    "/users/iddouble",
                    "/images/**",
                    "/reviews/api/**",  //이렇게 해서 요청을 한다거나...
                    "/api/**"
                ).permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers(
                    "/users/mypage",
                    "/users/update",
                    "/users/delete"
                ).authenticated()
                .anyRequest().permitAll()
            .and()

            .formLogin()
                .loginPage("/users/login")
                .loginProcessingUrl("/users/loginProc")
                .defaultSuccessUrl("/users/mypage", true)
                .failureHandler(customLoginFailureHandler) // ✅ 여기만 사용
                .permitAll()
            .and()

            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/users/logout"))
                .logoutSuccessUrl("/users/login")
                .invalidateHttpSession(true)
                .permitAll()
            .and()

            .oauth2Login()
                .loginPage("/users/login")
                .defaultSuccessUrl("/users/mypage", true)
                .userInfoEndpoint()
                    .userService(oauth2IUserService)
            .and()
            .and()

            .csrf()
                .ignoringAntMatchers(
                    "/users/join",
                    "/users/update",
                    "/users/delete",
                    "/admin/**",
                    "/reviews/api/**"  // csrf 설정안했다면.... 
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
