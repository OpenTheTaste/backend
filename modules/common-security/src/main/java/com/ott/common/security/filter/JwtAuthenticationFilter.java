package com.ott.common.security.filter;

import com.ott.common.security.handler.JwtAuthenticationEntryPoint;
import com.ott.common.security.jwt.JwtTokenProvider;
import com.ott.common.web.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 들어오는 요청을 가로채서 토큰을 꺼내서 provider에게 검증을 요청
 * provider에서 정상임을 반환하면 Authentication을 securityContext에 넣음
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 토큰 꺼내옴
        String token = resolveToken(request);

        // 토큰을 검증하여 인증 없음, 만료됨, 유효x일 경우 에러코드를 저장
        if(token != null) {
            ErrorCode errorCode = jwtTokenProvider.validateAndGetErrorCode(token);

            if(errorCode == null) {
                Long memberId = jwtTokenProvider.getMemberId(token);

                // auth: ["ROLE_USER"]
                List<String> authorities = jwtTokenProvider.getAuthorities(token);

                // Authentication을 만듬
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        memberId, // 추후 UserDetails?로 변경 예정
                        null,
                        authorities.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                );
                // Authenication을 SecurityContext에 넣음
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 실패할 경우 해당 에러코드를 reqeust에 넣음
                request.setAttribute(JwtAuthenticationEntryPoint.ERROR_CODE, errorCode);
            }
        }

        filterChain.doFilter(request, response);
    }

    // request에서 토큰 빼오기
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }


}
