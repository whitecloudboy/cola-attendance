package com.cola.attendance.module.system.dto;

import lombok.Data;

@Data
public class LoginResultDTO {
    private String token;
    private String username;
    private String displayName;
    private Long userId;
}
