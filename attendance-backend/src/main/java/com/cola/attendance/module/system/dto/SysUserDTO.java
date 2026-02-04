package com.cola.attendance.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysUserDTO {
    private Long id;
    private String username;
    private String displayName;
    private Long deptId;
    private String deptName;
    private String email;
    private String phone;
    private Integer status;
    private Integer isDutyPerson;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private List<Long> roleIds;
    /** 岗位ID列表（多选） */
    private List<Long> postIds;
    /** 新建时可选，不填则默认 123456 */
    private String password;
}
