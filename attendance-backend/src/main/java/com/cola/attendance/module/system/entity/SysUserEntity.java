package com.cola.attendance.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUserEntity {
    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;
    private Long deptId;
    private String email;
    private String phone;
    private Integer status;
    private Integer isDutyPerson;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
