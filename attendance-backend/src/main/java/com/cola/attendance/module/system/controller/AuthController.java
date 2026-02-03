package com.cola.attendance.module.system.controller;

import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.system.dto.LoginDTO;
import com.cola.attendance.module.system.dto.LoginResultDTO;
import com.cola.attendance.module.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证")
@RestController
@RequestMapping("/system/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@RequestBody LoginDTO dto) {
        LoginResultDTO data = authService.login(dto);
        return Result.ok(data);
    }
}
