package com.sesac.solbid.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.api.ApiResponse;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (token != null && !jwtUtil.isReactivationToken(token) && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (!userDetails.isEnabled()) {
                        throw new DisabledException("User is not active");
                    }

                    // 이메일 인증 확인 (특정 엔드포인트 + 메서드)
                    if (requiresEmailVerification(request.getRequestURI(), request.getMethod())) {
                        User user = userService.getByEmail(username);
                        // 소셜 로그인 사용자(password == null)는 이메일 인증 제외
                        if (user.getPassword() != null && !user.getEmailVerified()) {
                            sendErrorResponse(response, ErrorCode.EMAIL_NOT_VERIFIED);
                            return;
                        }
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
            // 토큰 오류 등은 인증 미설정으로 간주하고 다음 필터로 진행
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 특정 엔드포인트 + 메서드에만 이메일 인증을 강제
     */
    private boolean requiresEmailVerification(String requestURI, String method) {
        // 상품 등록만 인증 강제
        if (requestURI.startsWith("/api/products") && "POST".equals(method)) {
            return true;
        }
        if (requestURI.startsWith("/api/auctions")) return true;
        if (requestURI.startsWith("/api/points")) return true;
        if (requestURI.startsWith("/api/users/profile")) return true;
        return false;
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> apiResponse = ApiResponse.error(errorCode.name(), errorCode.getMessage());
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}
