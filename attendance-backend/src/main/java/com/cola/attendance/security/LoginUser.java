package com.cola.attendance.security;

import lombok.Data;

import java.util.List;

@Data
public class LoginUser {
    private Long id;
    private String username;
    private String displayName;
    private Long deptId;
    private List<Long> roleIds;
}
