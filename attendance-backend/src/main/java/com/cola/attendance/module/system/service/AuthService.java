package com.cola.attendance.module.system.service;

import com.cola.attendance.module.system.dto.LoginDTO;
import com.cola.attendance.module.system.dto.LoginResultDTO;

public interface AuthService {
    LoginResultDTO login(LoginDTO dto);
}
