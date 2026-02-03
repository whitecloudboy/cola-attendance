package com.cola.attendance.security;

import com.cola.attendance.common.constant.Constant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(Constant.TOKEN_HEADER);
        if (token != null && !token.isBlank()) {
            LoginUser user = null;
            try {
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                user = new LoginUser();
                user.setId(Long.valueOf(claims.get("userId", String.class)));
                user.setUsername(claims.getSubject());
                user.setDisplayName(claims.get("displayName", String.class));
                if (claims.get("deptId") != null) {
                    user.setDeptId(Long.valueOf(claims.get("deptId", String.class)));
                }
            } catch (Exception ignored) {
            }
            if (user != null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        user, null,
                        user.getRoleIds() != null
                                ? user.getRoleIds().stream()
                                .map(id -> new SimpleGrantedAuthority("ROLE_" + id))
                                .collect(Collectors.toList())
                                : Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
