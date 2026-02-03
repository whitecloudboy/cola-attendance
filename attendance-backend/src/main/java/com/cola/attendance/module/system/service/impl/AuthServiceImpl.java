package com.cola.attendance.module.system.service.impl;

import com.cola.attendance.module.system.dao.SysUserDao;
import com.cola.attendance.module.system.dao.SysUserRoleDao;
import com.cola.attendance.module.system.dto.LoginDTO;
import com.cola.attendance.module.system.dto.LoginResultDTO;
import com.cola.attendance.module.system.entity.SysUserEntity;
import com.cola.attendance.module.system.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserDao sysUserDao;
    private final SysUserRoleDao sysUserRoleDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    public AuthServiceImpl(SysUserDao sysUserDao, SysUserRoleDao sysUserRoleDao, PasswordEncoder passwordEncoder) {
        this.sysUserDao = sysUserDao;
        this.sysUserRoleDao = sysUserRoleDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResultDTO login(LoginDTO dto) {
        SysUserEntity user = sysUserDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已禁用");
        }
        String token = buildToken(user);
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUsername(user.getUsername());
        result.setDisplayName(user.getDisplayName());
        result.setUserId(user.getId());
        return result;
    }

    private String buildToken(SysUserEntity user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", String.valueOf(user.getId()))
                .claim("displayName", user.getDisplayName())
                .claim("deptId", user.getDeptId() != null ? String.valueOf(user.getDeptId()) : null)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }
}
