package com.ott.api_user.config;

import com.ott.api_user.auth.oauth2.CustomOAuth2UserService;
import com.ott.api_user.auth.oauth2.handler.OAuth2FailureHandler;
import com.ott.api_user.auth.oauth2.handler.OAuth2SuccessHandler;
import com.ott.common.security.filter.JwtAuthenticationFilter;
import com.ott.common.security.handler.JwtAccessDeniedHandler;
import com.ott.common.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // api-user 전용 OAuth2
    private final CustomOAuth2UserService CustomOAuth2UserService; // 카카오에서 받은 사용자 프로필 조회 후 DB에 적재
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontedUrl;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화, Authorization 헤더로 보냄
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .cors(AbstractHttpConfigurer::disable)

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 기반 인증

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler)) // 403

                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요
                        .requestMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/auth/reissue",
                                "/auth/logout",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                                ).permitAll()

                        // 나머지 url에 대해서는 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 카카오 로그인
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(CustomOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                // UsernamePasswordAuthenticationFilter 보다 먼저 실행
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // allowedOrigins -> 허용할 Origin 내역
        // allowCredentials -> 브라우저가 요청에 인증정보를 포함하는 것을 허용하겠냐
        // credentials가 true일 경우 Allow-origin의 경우 구체적인 경로를 명시해야됨

        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
//        config.setAllowedOrigins(List.of(frontedUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
